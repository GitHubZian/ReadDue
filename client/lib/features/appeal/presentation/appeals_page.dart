import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import 'appeal_providers.dart';

/// C8 申诉：工单列表与进度、2 小时 SLA 说明。
class AppealsPage extends ConsumerStatefulWidget {
  const AppealsPage({super.key});

  @override
  ConsumerState<AppealsPage> createState() => _AppealsPageState();
}

class _AppealsPageState extends ConsumerState<AppealsPage> {
  int _tab = 0;

  @override
  Widget build(BuildContext context) {
    final value = ref.watch(appealsProvider);
    final data = value.asData?.value;

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            PageTitle(
              title: '申诉工单',
              showBack: true,
              actions: [
                TextButton(
                  onPressed: _newAppeal,
                  child: Text(
                    '+ 新建',
                    style: AppText.bodySm.copyWith(
                      fontWeight: FontWeight.w600,
                      color: AppColors.mintDeep,
                    ),
                  ),
                ),
              ],
            ),
            if (data != null)
              Padding(
                padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
                child: SegmentedTabs(
                  items: [
                    SegItem(label: '全部', count: data.tickets.length),
                    SegItem(label: '处理中', count: data.openCount),
                    SegItem(label: '已完成', count: data.doneCount),
                  ],
                  index: _tab,
                  onChanged: (index) => setState(() => _tab = index),
                ),
              ),
            Expanded(
              child: AsyncBody<AppealsPageData>(
                value: value,
                onRetry: () => ref.invalidate(appealsProvider),
                skeleton: const SkeletonList(count: 3, itemHeight: 108),
                builder: (data) => _body(data),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: SafeArea(
        top: false,
        child: Container(
          padding: const EdgeInsets.fromLTRB(20, 10, 20, 12),
          decoration: const BoxDecoration(
            color: AppColors.bg,
            border: Border(top: BorderSide(color: AppColors.line)),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              PrimaryButton(label: '新建申诉', onPressed: _newAppeal),
              const SizedBox(height: 6),
              Text(
                '需选择关联单据，并上传至少 1 份凭证',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _body(AppealsPageData data) {
    final items = switch (_tab) {
      1 => data.tickets.where(_isOpen).toList(),
      2 => data.tickets.where((ticket) => !_isOpen(ticket)).toList(),
      _ => data.tickets,
    };

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 2, 20, 24),
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 11),
          decoration: BoxDecoration(
            color: AppColors.wash,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AppColors.line),
          ),
          child: Text(
            '对逾期判定、费用计算或提醒送达有异议？馆员将在 '
            '${data.slaHours} 小时内答复，超时自动补 3 天。',
            style: AppText.caption.copyWith(
              color: AppColors.ink2,
              height: 1.6,
            ),
          ),
        ),
        const SizedBox(height: 12),
        if (items.isEmpty)
          const Padding(
            padding: EdgeInsets.only(top: 32),
            child: EmptyState(
              title: '暂无申诉工单',
              message: '在借阅详情或逾期提醒旁，都可以对判定发起申诉。',
              icon: Icons.support_agent,
            ),
          )
        else
          for (final ticket in items) ...[
            _AppealCard(ticket: ticket, onTap: () => _showProgress(ticket)),
            const SizedBox(height: 10),
          ],
        const SizedBox(height: 4),
        Text(
          '同一单据最多提交 2 次；被驳回后 7 天内可补充材料再提。',
          textAlign: TextAlign.center,
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
      ],
    );
  }

  bool _isOpen(AppealTicket ticket) =>
      ticket.status == 'OPEN' ||
      ticket.status == 'ACKED' ||
      ticket.status == 'APPEALING';

  Future<void> _newAppeal() async {
    final created = await context.push<bool>('/appeals/new');
    if (created == true) {
      ref.invalidate(appealsProvider);
      ref.invalidate(appealOrderOptionsProvider);
    }
  }

  Future<void> _showProgress(AppealTicket ticket) async {
    final (tone, label) = _style(ticket.status);
    await showDialog<void>(
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
              Row(
                children: [
                  Text(
                    ticket.ticketNo,
                    style: AppText.bodySm.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const Spacer(),
                  StatusPill(label: label, tone: tone),
                ],
              ),
              const SizedBox(height: 10),
              Text(
                '${ticket.typeLabel} · ${ticket.bookTitle}',
                style: AppText.bodySm.copyWith(color: AppColors.ink2),
              ),
              const SizedBox(height: 6),
              Text(
                AppDate.dateTime(ticket.createdAt),
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const SizedBox(height: 14),
              Text(
                _progressText(ticket),
                style: AppText.bodySm.copyWith(
                  color: AppColors.ink2,
                  height: 1.7,
                ),
              ),
              if (ticket.rejectReason != null &&
                  ticket.rejectReason!.isNotEmpty) ...[
                const SizedBox(height: 10),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.redSoft,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    '驳回原因：${ticket.rejectReason}',
                    style: AppText.caption.copyWith(
                      color: AppColors.red,
                      height: 1.6,
                    ),
                  ),
                ),
              ],
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

  String _progressText(AppealTicket ticket) {
    final sla = ticket.slaDueAt == null
        ? ''
        : '，最晚 ${AppDate.dateTime(ticket.slaDueAt!)} 前答复';
    return switch (ticket.status) {
      'OPEN' => '已提交，正在等待馆员受理$sla。',
      'ACKED' => '馆员已受理，正在核实材料$sla。',
      'RESOLVED' => '已处理完成，处理结果以工单描述为准。',
      'AUTO_COMPENSATED' => '馆员未在 SLA 内回应，系统已自动补偿 3 天，并计入服务考核。',
      'REJECTED' => '本次申诉未通过，可在 7 天内补充材料后再次提交。',
      _ => '工单已关闭。',
    };
  }

  (PillTone, String) _style(String status) => switch (status) {
        'OPEN' => (PillTone.amber, '待处理'),
        'ACKED' => (PillTone.mint, '已受理'),
        'RESOLVED' => (PillTone.mint, '已解决'),
        'AUTO_COMPENSATED' => (PillTone.mint, '已补偿'),
        'REJECTED' => (PillTone.slate, '已驳回'),
        _ => (PillTone.slate, '已关闭'),
      };
}

class _AppealCard extends StatelessWidget {
  const _AppealCard({required this.ticket, required this.onTap});

  final AppealTicket ticket;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final (tone, label) = switch (ticket.status) {
      'OPEN' => (PillTone.amber, '待处理'),
      'ACKED' => (PillTone.mint, '已受理'),
      'RESOLVED' => (PillTone.mint, '已解决'),
      'AUTO_COMPENSATED' => (PillTone.mint, '已补偿'),
      'REJECTED' => (PillTone.slate, '已驳回'),
      _ => (PillTone.slate, '已关闭'),
    };

    return AppCard(
      padding: const EdgeInsets.all(14),
      onTap: onTap,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                ticket.ticketNo,
                style: AppText.caption.copyWith(
                  color: AppColors.ink3,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              StatusPill(label: label, tone: tone),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            '${ticket.typeLabel} · ${ticket.bookTitle}',
            style: AppText.bodySm.copyWith(
              fontSize: 14.5,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 5),
          Text(
            ticket.description,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: AppText.caption.copyWith(
              color: AppColors.ink2,
              height: 1.5,
            ),
          ),
          if (ticket.rejectReason != null &&
              ticket.rejectReason!.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(
              '驳回原因：${ticket.rejectReason}',
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: AppText.caption.copyWith(color: AppColors.red),
            ),
          ],
          const Padding(
            padding: EdgeInsets.only(top: 10),
            child: Divider(height: 1, color: AppColors.line2),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Text(
                '${AppDate.monthDay(ticket.createdAt)} ${AppDate.time(ticket.createdAt)}',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const Spacer(),
              Text(
                '查看进度',
                style: AppText.caption.copyWith(
                  color: AppColors.mintDeep,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Icon(
                Icons.chevron_right,
                size: 16,
                color: AppColors.ink3,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
