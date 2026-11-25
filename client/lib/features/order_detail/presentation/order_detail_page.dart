import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/network/reader_mappers.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/utils/grace_due.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/timeline_item.dart';
import '../../../core/widgets/widgets.dart';
import 'order_providers.dart';

/// C2 借阅详情：书目 + 四个事实 + 办理进度时间线 + 申请宽限。
class OrderDetailPage extends ConsumerWidget {
  const OrderDetailPage({super.key, required this.orderId});

  final String orderId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detail = ref.watch(orderDetailProvider(orderId));
    final data = detail.asData?.value;

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            PageTitle(
              title: '借阅详情',
              actions: [
                TextButton.icon(
                  onPressed: () => showToast(
                    context,
                    '借期与续借次数以服务端规则为准；逾期可申请宽限或申诉。',
                  ),
                  icon: const Icon(
                    Icons.info_outline,
                    size: 16,
                    color: AppColors.ink3,
                  ),
                  label: Text(
                    '帮助',
                    style: AppText.bodySm.copyWith(color: AppColors.ink2),
                  ),
                  style: TextButton.styleFrom(
                    minimumSize: const Size(56, AppSizes.touch),
                  ),
                ),
              ],
            ),
            Expanded(
              child: AsyncBody<OrderDetail>(
                value: detail,
                onRetry: () => ref.invalidate(orderDetailProvider(orderId)),
                skeleton: const SkeletonList(count: 3, itemHeight: 120),
                builder: (data) => _Content(detail: data),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: data == null ? null : _BottomBar(detail: data),
    );
  }
}

class _Content extends StatelessWidget {
  const _Content({required this.detail});

  final OrderDetail detail;

  @override
  Widget build(BuildContext context) {
    final overdue = isOverdue(detail.status, detail.overdueDays);
    final meta = [detail.author, detail.publisher]
        .where((value) => value.isNotEmpty && value != '—')
        .join(' · ');
    final location = [
      if (detail.callNo.isNotEmpty && detail.callNo != '—')
        '索书号 ${detail.callNo}',
      if (detail.barcode.isNotEmpty && detail.barcode != '—')
        '条码 ${detail.barcode}',
    ].join(' · ');
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 0, 20, 24),
      children: [
        if (detail.isOffline) const OfflineBanner(),
        AppCard(
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              BookCover(
                label: detail.coverLabel,
                tone: _tone(detail.coverTone),
                width: 72,
                height: 96,
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      detail.title,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: AppText.h2.copyWith(height: 1.3),
                    ),
                    const SizedBox(height: 6),
                    if (meta.isNotEmpty) ...[
                      const SizedBox(height: 6),
                      Text(
                        meta,
                        style: AppText.caption.copyWith(color: AppColors.ink2),
                      ),
                    ],
                    if (location.isNotEmpty) ...[
                      const SizedBox(height: 4),
                      Text(
                        location,
                        style: AppText.caption.copyWith(color: AppColors.ink3),
                      ),
                    ],
                    const SizedBox(height: 10),
                    Wrap(
                      spacing: 6,
                      runSpacing: 6,
                      children: [
                        if (overdue)
                          StatusPill(
                            label: detail.overdueDays > 0
                                ? '逾期 ${detail.overdueDays} 天'
                                : '已逾期',
                            tone: PillTone.amber,
                          ),
                        if (detail.hasAppliedGrace)
                          StatusPill(
                            label: '宽限 +${detail.appliedGraceDays} 天已通过',
                            tone: PillTone.mint,
                          ),
                        if (detail.gracePending)
                          const StatusPill(
                            label: '宽限申请审核中',
                            tone: PillTone.mint,
                          ),
                        if (detail.status == 'RETURNED')
                          const StatusPill(
                            label: '已归还',
                            tone: PillTone.slate,
                          ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        AppCard(
          child: Column(
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: _Fact(
                      label: '借出日期',
                      value:
                          '${AppDate.monthDay(detail.borrowedAt)} ${AppDate.time(detail.borrowedAt)}',
                    ),
                  ),
                  Expanded(
                    child: _Fact(
                      label: '应还日期',
                      value: AppDate.monthDay(detail.settledDueAt),
                      emphasis: overdue,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: _Fact(
                      label: '宽限期',
                      value: detail.appliedGraceDays == 0
                          ? '0 天'
                          : '${detail.appliedGraceDays} 天 · 已通过',
                    ),
                  ),
                  Expanded(
                    child: _Fact(
                      label: '预计逾期费用（截至今日）',
                      value: detail.feeEstimate ?? detail.feePerDay,
                      emphasis: overdue,
                    ),
                  ),
                ],
              ),
              if (detail.hasAppliedGrace) ...[
                const SizedBox(height: 14),
                Container(
                  width: double.infinity,
                  padding:
                      const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                  decoration: BoxDecoration(
                    color: AppColors.mintSoft,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Icon(
                        Icons.event_available,
                        size: 16,
                        color: AppColors.mintDeep,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          '宽限审核已通过：${detail.graceDueLabel}，'
                          '原应还 ${AppDate.monthDay(detail.dueAt)}。',
                          style: AppText.caption
                              .copyWith(color: AppColors.mintDeep),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 12),
        AppCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Text('办理进度', style: AppText.h3),
                  const SizedBox(width: 8),
                  Text(
                    '自动核算',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                  const Spacer(),
                  InkWell(
                    onTap: () => _showRules(context),
                    borderRadius: BorderRadius.circular(6),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 2,
                        vertical: 6,
                      ),
                      child: Text(
                        '查看规则 ›',
                        style: AppText.caption.copyWith(
                          color: AppColors.mintDeep,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Text(
                '单据 ${detail.orderNo}',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const SizedBox(height: 16),
              for (var i = 0; i < detail.timeline.length; i++)
                TimelineItem(
                  state: _state(detail.timeline[i].state),
                  time: detail.timeline[i].time == null
                      ? '待定'
                      : '${AppDate.monthDay(detail.timeline[i].time!)} ${AppDate.time(detail.timeline[i].time!)}',
                  title: detail.timeline[i].title,
                  subtitle: detail.timeline[i].subtitle,
                  isLast: i == detail.timeline.length - 1,
                ),
              const SizedBox(height: 14),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: AppColors.wash,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Text(
                  detail.settleNote,
                  style: AppText.caption.copyWith(color: AppColors.ink2),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  void _showRules(BuildContext context) {
    showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        backgroundColor: AppColors.card,
        title: const Text('借阅规则'),
        content: Text(
          '· 借期与续借次数以图书馆配置为准\n'
          '· 逾期按自然日计费，归还时一次结清\n'
          '· 宽限申请馆员将在 2 小时内答复\n'
          '· 费用减免需馆员操作并留痕',
          style: AppText.bodySm.copyWith(height: 1.7),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(dialogContext).pop(),
            child: const Text('知道了'),
          ),
        ],
      ),
    );
  }

  TimelineState _state(String value) {
    return switch (value) {
      'current' => TimelineState.current,
      'future' => TimelineState.future,
      _ => TimelineState.done,
    };
  }

  CoverTone _tone(String value) {
    return switch (value) {
      'amber' => CoverTone.amber,
      'slate' => CoverTone.slate,
      _ => CoverTone.mint,
    };
  }
}

class _Fact extends StatelessWidget {
  const _Fact({
    required this.label,
    required this.value,
    this.emphasis = false,
  });

  final String label;
  final String value;
  final bool emphasis;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: AppText.caption.copyWith(color: AppColors.ink3)),
        const SizedBox(height: 4),
        Text(
          value,
          style: AppText.bodySm.copyWith(
            fontWeight: FontWeight.w600,
            color: emphasis ? AppColors.amber : AppColors.ink,
            fontFeatures: AppText.tabular.fontFeatures,
          ),
        ),
      ],
    );
  }
}

class _BottomBar extends ConsumerWidget {
  const _BottomBar({required this.detail});

  final OrderDetail detail;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final returned = detail.status == 'RETURNED';
    final canTap = detail.gracePending || detail.canGrace;
    final label = returned
        ? '查看回执'
        : detail.gracePending
            ? '查看申请（审核中）'
            : '申请宽限';

    return BottomActionBar(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (detail.blacklisted) ...[
            Text(
              '账号已暂停借阅权限，可归还与申诉',
              style: AppText.caption.copyWith(color: AppColors.red),
            ),
            const SizedBox(height: 8),
          ],
          _primary(context, label, canTap, returned, detail, ref),
          if (!returned) ...[
            const SizedBox(height: 8),
            Text(
              '已开通「无接触归还」：可先还书，后结算',
              style: AppText.caption.copyWith(color: AppColors.ink3),
            ),
          ],
        ],
      ),
    );
  }

  Widget _primary(
    BuildContext context,
    String label,
    bool canTap,
    bool returned,
    OrderDetail detail,
    WidgetRef ref,
  ) {
    return PrimaryButton(
      label: label,
      onPressed: !canTap
          ? null
          : () async {
              if (returned) {
                context.push('/notifications');
                return;
              }
              final updated = await context.push<bool>(
                '/orders/${detail.id}/grace',
              );
              if (updated == true) {
                ref.invalidate(orderDetailProvider(detail.id));
              }
            },
    );
  }
}
