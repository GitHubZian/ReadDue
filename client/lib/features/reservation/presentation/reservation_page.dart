import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/theme/motion.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/enter_fade.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import 'reservation_providers.dart';

/// C5 预约排队：待到书 / 已完成 / 已取消，取书码与取消预约。
class ReservationPage extends ConsumerStatefulWidget {
  const ReservationPage({super.key});

  @override
  ConsumerState<ReservationPage> createState() => _ReservationPageState();
}

class _ReservationPageState extends ConsumerState<ReservationPage> {
  int _tab = 0;
  String? _cancellingId;

  @override
  Widget build(BuildContext context) {
    final value = ref.watch(reservationsProvider);
    final data = value.asData?.value;

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const TabPageHeader(
              title: '预约',
              subtitle: '到书保留 48 小时，过期自动顺延下一位',
            ),
            if (data != null)
              Padding(
                padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
                child: SegmentedTabs(
                  items: [
                    SegItem(label: '待取书', count: data.ready.length),
                    SegItem(label: '已完成', count: data.completed.length),
                    SegItem(label: '已取消', count: data.cancelled.length),
                  ],
                  index: _tab,
                  onChanged: (index) => setState(() => _tab = index),
                ),
              ),
            Expanded(
              child: RefreshIndicator(
                color: AppColors.mintDeep,
                onRefresh: () async {
                  ref.invalidate(reservationsProvider);
                  await ref.read(reservationsProvider.future);
                },
                child: AsyncBody<ReservationsPage>(
                  value: value,
                  onRetry: () => ref.invalidate(reservationsProvider),
                  skeleton: const SkeletonList(count: 2, itemHeight: 150),
                  builder: (data) => _body(data),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _body(ReservationsPage data) {
    if (_tab == 0) {
      if (data.ready.isEmpty) {
        return ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          children: const [
            SizedBox(height: 40),
            EmptyState(
              title: '暂无待取书的预约',
              message: '借出中的书可以在检索页预约，到书后会第一时间通知。',
              icon: Icons.event_available_outlined,
            ),
          ],
        );
      }
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(20, 2, 20, 24),
        children: [
          for (var i = 0; i < data.ready.length; i++) ...[
            EnterFade(
              index: i,
              child: _ReadyReservationCard(
                item: data.ready[i],
                cancelling: _cancellingId == data.ready[i].id,
                onPickup: () => _showPickupCode(data.ready[i]),
                onCancel: () => _cancel(data.ready[i]),
              ),
            ),
            const SizedBox(height: 12),
          ],
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 11),
            decoration: BoxDecoration(
              color: AppColors.wash,
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: AppColors.line),
            ),
            child: Text(
              '${data.ruleNote}；累计 3 次未取将暂停预约 30 天。',
              style: AppText.caption.copyWith(color: AppColors.ink2),
            ),
          ),
        ],
      );
    }
    if (_tab == 1) {
      if (data.completed.isEmpty) {
        return ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          children: const [
            SizedBox(height: 40),
            EmptyState(
              title: '暂无已完成的预约',
              message: '取书完成后，历史记录会保留在这里。',
              icon: Icons.history,
            ),
          ],
        );
      }
      return ListView.separated(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(20, 2, 20, 24),
        itemCount: data.completed.length,
        separatorBuilder: (_, __) => const SizedBox(height: 10),
        itemBuilder: (_, index) =>
            _DoneReservationCard(item: data.completed[index]),
      );
    }
    if (data.cancelled.isEmpty) {
      return ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        children: const [
          SizedBox(height: 40),
          EmptyState(
            title: '暂无已取消的预约',
            message: '取消预约后会保留记录，方便复查。',
            icon: Icons.event_busy_outlined,
          ),
        ],
      );
    }
    return ListView.separated(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(20, 2, 20, 24),
      itemCount: data.cancelled.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (_, index) => _DoneReservationCard(
        item: data.cancelled[index],
        cancelled: true,
      ),
    );
  }

  void _showPickupCode(ReservationItem item) {
    showDialog<void>(
      context: context,
      builder: (dialogContext) => Dialog(
        backgroundColor: AppColors.card,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.card),
        ),
        child: Padding(
          padding: const EdgeInsets.all(AppSpacing.page),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '取书码',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const SizedBox(height: 8),
              SelectableText(
                item.pickupCode,
                style: AppText.mono.copyWith(
                  fontSize: 30,
                  letterSpacing: 4,
                  color: AppColors.ink,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                '请向${item.pickupPoint}出示此码，工作人员扫码后完成借出。',
                style: AppText.bodySm.copyWith(color: AppColors.ink2),
              ),
              const SizedBox(height: 18),
              PrimaryButton(
                label: '知道了',
                onPressed: () => Navigator.of(dialogContext).pop(),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _cancel(ReservationItem item) async {
    final ok = await showConfirmDialog(
      context,
      title: '取消预约',
      message: '取消后名额将顺延给下一位读者，重新预约需排在队尾。',
      confirmLabel: '取消预约',
      danger: true,
    );
    if (!ok || !mounted) return;
    setState(() => _cancellingId = item.id);
    try {
      await ref.read(reservationRepositoryProvider).cancel(item.id);
      if (!mounted) return;
      ref.invalidate(reservationsProvider);
      showToast(context, '已取消，名额已顺延');
    } catch (error) {
      if (!mounted) return;
      showToast(context, messageForError(error));
    } finally {
      if (mounted) setState(() => _cancellingId = null);
    }
  }
}

class _ReadyReservationCard extends StatelessWidget {
  const _ReadyReservationCard({
    required this.item,
    required this.cancelling,
    required this.onPickup,
    required this.onCancel,
  });

  final ReservationItem item;
  final bool cancelling;
  final VoidCallback onPickup;
  final VoidCallback onCancel;

  @override
  Widget build(BuildContext context) {
    final urgent = item.remainingHours < 12;
    final progress = (item.remainingHours / 48).clamp(0.0, 1.0);
    return AppCard(
      padding: const EdgeInsets.all(14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              BookCover(
                label: item.coverLabel,
                tone: switch (item.coverTone) {
                  'amber' => CoverTone.amber,
                  'slate' => CoverTone.slate,
                  _ => CoverTone.mint,
                },
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Text(
                            item.bookTitle,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: AppText.bodySm.copyWith(
                              fontSize: 18,
                              fontWeight: FontWeight.w700,
                              height: 1.3,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        const StatusPill(label: '已到馆', tone: PillTone.mint),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      [
                        item.author,
                        if (item.publisher.isNotEmpty) item.publisher,
                      ].join(' · '),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: AppText.caption.copyWith(color: AppColors.ink2),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      '${item.pickupPoint} · 凭校园卡取书',
                      style: AppText.caption.copyWith(color: AppColors.ink3),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const Padding(
            padding: EdgeInsets.symmetric(vertical: 12),
            child: Divider(height: 1, color: AppColors.line2),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '取书码',
                          style:
                              AppText.caption.copyWith(color: AppColors.ink3),
                        ),
                        const SizedBox(height: 2),
                        SelectableText(
                          item.pickupCode,
                          style: AppText.mono.copyWith(
                            fontSize: 40,
                            fontWeight: FontWeight.w700,
                            letterSpacing: 2,
                            color: AppColors.ink,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        '保留至',
                        style: AppText.caption.copyWith(color: AppColors.ink3),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        item.expiresAt == null
                            ? '以服务端为准'
                            : '${AppDate.monthDay(item.expiresAt!)} ${AppDate.time(item.expiresAt!)}',
                        style: AppText.bodySm.copyWith(
                          fontWeight: FontWeight.w700,
                          fontFeatures: AppText.tabular.fontFeatures,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '剩余 ${item.remainingHours} 小时',
                        style: AppText.caption.copyWith(
                          color: urgent ? AppColors.amber : AppColors.ink3,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 10),
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: TweenAnimationBuilder<double>(
                  tween: Tween(begin: 0, end: progress),
                  duration: Motion.reduced(context)
                      ? Duration.zero
                      : const Duration(milliseconds: 400),
                  curve: Curves.easeOutCubic,
                  builder: (context, value, _) => LinearProgressIndicator(
                    value: value,
                    minHeight: 4,
                    backgroundColor: AppColors.sunken,
                    color: urgent ? AppColors.amber : AppColors.mint,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Text(
                    '队列第 ${item.queueNo} 位 / 共 ${item.queueTotal} 位',
                    style: AppText.caption.copyWith(color: AppColors.ink2),
                  ),
                  const Spacer(),
                  Text(
                    '超时顺延',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                ],
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: PrimaryButton(
                  label: '去取书',
                  height: AppSizes.touch,
                  onPressed: onPickup,
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: SizedBox(
                  height: AppSizes.touch,
                  child: OutlinedButton(
                    onPressed: cancelling ? null : onCancel,
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.ink2,
                      side: const BorderSide(color: AppColors.line),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(AppRadius.button),
                      ),
                    ),
                    child: cancelling
                        ? const SizedBox(
                            width: 14,
                            height: 14,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : Text(
                            '取消预约',
                            style: AppText.bodySm.copyWith(
                              fontWeight: FontWeight.w600,
                              color: AppColors.ink2,
                            ),
                          ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _DoneReservationCard extends StatelessWidget {
  const _DoneReservationCard({required this.item, this.cancelled = false});

  final ReservationItem item;
  final bool cancelled;

  @override
  Widget build(BuildContext context) {
    final subtitle = cancelled
        ? '预约已取消'
        : '${item.fulfilledAt == null ? '' : '${AppDate.monthDay(item.fulfilledAt!)} 已借出'}'
            '${item.returnedAt == null ? '' : ' · ${AppDate.date(item.returnedAt!)} 已归还'}';
    return AppCard(
      padding: const EdgeInsets.all(12),
      child: Row(
        children: [
          BookCover(label: item.coverLabel, width: 46, height: 62),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.bookTitle,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: AppText.bodySm.copyWith(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
              ],
            ),
          ),
          StatusPill(
            label: cancelled ? '已取消' : '已完成',
            tone: PillTone.slate,
          ),
        ],
      ),
    );
  }
}
