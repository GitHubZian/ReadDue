import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/reader_mappers.dart';
import '../../../core/theme/motion.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/utils/grace_due.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/enter_fade.dart';
import '../../../core/widgets/small_button.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/summary_block.dart';
import '../../../core/widgets/widgets.dart';
import '../../auth/presentation/auth_controller.dart';
import 'borrow_providers.dart';

/// C1 我的借阅：摘要 + 即将到期 + 预约到书。
class BorrowPage extends ConsumerStatefulWidget {
  const BorrowPage({super.key});

  @override
  ConsumerState<BorrowPage> createState() => _BorrowPageState();
}

class _BorrowPageState extends ConsumerState<BorrowPage> {
  String? _renewingId;

  @override
  Widget build(BuildContext context) {
    final dashboard = ref.watch(borrowDashboardProvider);
    final name = ref.watch(authControllerProvider).session?.name ?? '同学';

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: RefreshIndicator(
          color: AppColors.mintDeep,
          onRefresh: () async {
            ref.invalidate(borrowDashboardProvider);
            await ref.read(borrowDashboardProvider.future);
          },
          child: AsyncBody<BorrowDashboard>(
            value: dashboard,
            onRetry: () => ref.invalidate(borrowDashboardProvider),
            skeleton: const SkeletonList(count: 4, itemHeight: 104),
            isEmpty: (data) => data.dueSoon.isEmpty && data.readyBooks.isEmpty,
            empty: EmptyState(
              title: '暂无借阅',
              message: '去检索一本想读的书，借阅后在这里跟进。',
              actionLabel: '去检索',
              onAction: () => context.go('/search'),
            ),
            builder: (data) => _content(data, name),
          ),
        ),
      ),
    );
  }

  Widget _content(BorrowDashboard data, String name) {
    final summary = data.summary;
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    '${_greeting()}，$name',
                    style: AppText.h1.copyWith(fontSize: 30, height: 1.15),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '本学期已归还 ${summary.returnedThisTerm} 本 · ${summary.creditLabel}',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                ],
              ),
            ),
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: AppColors.card,
                borderRadius: BorderRadius.circular(13),
                border: Border.all(color: AppColors.line),
              ),
              child: Stack(
                clipBehavior: Clip.none,
                children: [
                  Center(
                    child: IconButton(
                      onPressed: () => context.push('/notifications'),
                      tooltip: '消息中心',
                      icon: const Icon(Icons.notifications_none, size: 22),
                      color: AppColors.ink,
                    ),
                  ),
                  if (data.unreadCount > 0)
                    Positioned(
                      right: 10,
                      top: 9,
                      child: Container(
                        width: 7,
                        height: 7,
                        decoration: const BoxDecoration(
                          color: AppColors.red,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        if (data.isOffline) const OfflineBanner(),
        SummaryBlock(
          stats: [
            SummaryStat(
              value: '${summary.borrowing}',
              label: '在借中',
              onTap: () => _showBorrows(
                '在借中 · ${summary.borrowing} 本',
                data.dueSoon,
              ),
            ),
            SummaryStat(
              value: '${summary.dueSoon}',
              label: '2 天内到期',
              accent: true,
              onTap: () => _showBorrows(
                '2 天内到期 · ${summary.dueSoon} 本',
                data.dueSoon
                    .where(
                      (b) =>
                          b.overdueDays == 0 &&
                          b.daysLeft >= 0 &&
                          b.daysLeft <= 2,
                    )
                    .toList(),
              ),
            ),
            SummaryStat(
              value: '${summary.overdue}',
              label: '逾期',
              onTap: () => _showBorrows(
                '逾期 · ${summary.overdue} 本',
                data.dueSoon
                    .where((b) => b.overdueDays > 0 || b.daysLeft < 0)
                    .toList(),
              ),
            ),
          ],
          note: '已开通无接触归还 · 可先还书后结算',
        ),
        const SizedBox(height: 24),
        SectionHeader(
          title: '即将到期',
          hint: '按应还日排序',
          actionLabel: '全部 ${data.dueSoon.length} 本',
          onAction: () => _showBorrows(
            '全部在借 · ${data.dueSoon.length} 本',
            data.dueSoon,
          ),
        ),
        const SizedBox(height: 6),
        if (data.dueSoon.isEmpty)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 20),
            child: Text(
              '近 3 天没有到期的书。',
              style: AppText.bodySm.copyWith(color: AppColors.ink3),
            ),
          )
        else
          for (var i = 0; i < data.dueSoon.length; i++) ...[
            EnterFade(
              index: i,
              child: _DueCard(
                item: data.dueSoon[i],
                renewing: _renewingId == data.dueSoon[i].id,
                onTap: () => context.push('/orders/${data.dueSoon[i].id}'),
                onRenew: () => _renew(data.dueSoon[i]),
                onReturn: () => _returnBook(data.dueSoon[i]),
              ),
            ),
            const SizedBox(height: 10),
          ],
        const SizedBox(height: 18),
        if (data.readyBooks.isNotEmpty) ...[
          SectionHeader(
            title: '预约到书',
            hint: '保留 48 小时',
            actionLabel: '全部 ${data.readyBooks.length} 本',
            onAction: () => context.go('/reservations'),
          ),
          const SizedBox(height: 6),
          for (var i = 0; i < data.readyBooks.length; i++) ...[
            EnterFade(
              index: i + data.dueSoon.length,
              child: _ReadyCard(
                book: data.readyBooks[i],
                onTap: () => context.push('/reservations'),
              ),
            ),
            const SizedBox(height: 10),
          ],
        ],
        const SizedBox(height: 10),
        CustomPaint(
          painter: _DashedBorderPainter(color: AppColors.line),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.only(top: 2),
                  child: Icon(
                    Icons.notifications_none,
                    size: 16,
                    color: AppColors.ink3,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    '${data.quietHoursText} 为免打扰时段，到期提醒顺延至次日 08:00 合并发送；可在「我的 · 设置」调整。',
                    style: AppText.caption.copyWith(
                      color: AppColors.ink3,
                      height: 1.5,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 18),
        Center(
          child: Text(
            data.feeNote,
            style: AppText.caption.copyWith(color: AppColors.ink3),
            textAlign: TextAlign.center,
          ),
        ),
      ],
    );
  }

  Future<void> _showBorrows(String title, List<BorrowItem> items) {
    return showModalBottomSheet<void>(
      context: context,
      useRootNavigator: true,
      backgroundColor: AppColors.card,
      isScrollControlled: true,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(AppRadius.card),
      ),
      builder: (sheetContext) => SafeArea(
        top: false,
        child: ConstrainedBox(
          constraints: BoxConstraints(
            maxHeight: MediaQuery.of(context).size.height * 0.72,
          ),
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 10, 20, 20),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 36,
                    height: 4,
                    decoration: BoxDecoration(
                      color: AppColors.line,
                      borderRadius: BorderRadius.circular(99),
                    ),
                  ),
                ),
                const SizedBox(height: 14),
                Text(title, style: AppText.h3),
                const SizedBox(height: 4),
                Text(
                  '点一本书查看详情与办理进度',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
                const SizedBox(height: 12),
                Flexible(
                  child: items.isEmpty
                      ? Padding(
                          padding: const EdgeInsets.symmetric(vertical: 24),
                          child: Text(
                            '暂无借阅',
                            style: AppText.bodySm.copyWith(
                              color: AppColors.ink3,
                            ),
                          ),
                        )
                      : ListView.separated(
                          shrinkWrap: true,
                          itemCount: items.length,
                          separatorBuilder: (_, __) =>
                              const SizedBox(height: 8),
                          itemBuilder: (_, index) {
                            final item = items[index];
                            return _BorrowSheetTile(
                              item: item,
                              onTap: () {
                                Navigator.of(sheetContext).pop();
                                context.push('/orders/${item.id}');
                              },
                            );
                          },
                        ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _renew(BorrowItem item) async {
    if (_renewingId != null) return;
    setState(() => _renewingId = item.id);
    try {
      final newDue = await ref.read(borrowRepositoryProvider).renew(item.id);
      if (!mounted) return;
      ref.invalidate(borrowDashboardProvider);
      showToast(context, '已续借，新应还日 ${AppDate.date(newDue)}');
    } catch (error) {
      if (!mounted) return;
      showToast(context, messageForError(error));
    } finally {
      if (mounted) setState(() => _renewingId = null);
    }
  }

  Future<void> _returnBook(BorrowItem item) async {
    final confirmed = await showConfirmDialog(
      context,
      title: '归还《${item.title}》',
      message: '确认已在服务台/自助机归还？归还后将自动核算，减免费用需馆员处理。',
      confirmLabel: '确认归还',
    );
    if (!confirmed || !mounted) return;
    try {
      await ref.read(borrowRepositoryProvider).returnBook(item.id);
      if (!mounted) return;
      ref.invalidate(borrowDashboardProvider);
      showToast(context, '已归还，正在结算');
    } catch (error) {
      if (!mounted) return;
      showToast(context, messageForError(error));
    }
  }

  String _greeting() {
    final hour = DateTime.now().hour;
    if (hour < 6) return '凌晨好';
    if (hour < 12) return '上午好';
    if (hour < 14) return '中午好';
    if (hour < 18) return '下午好';
    return '晚上好';
  }
}

class _BorrowSheetTile extends StatelessWidget {
  const _BorrowSheetTile({required this.item, required this.onTap});

  final BorrowItem item;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final overdue = isOverdue(item.status, item.overdueDays);
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(12),
      child: Row(
        children: [
          BookCover(
            label: item.coverLabel,
            tone: _tone(item.coverTone),
            width: 40,
            height: 54,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.title,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 4),
                Text(
                  item.dueSummary,
                  style: AppText.caption.copyWith(
                    color: overdue ? AppColors.amber : AppColors.ink3,
                  ),
                ),
                if (item.hasAppliedGrace) ...[
                  const SizedBox(height: 6),
                  const StatusPill(
                    label: '宽限已通过',
                    tone: PillTone.mint,
                  ),
                ],
              ],
            ),
          ),
          const Icon(Icons.chevron_right, size: 18, color: AppColors.ink3),
        ],
      ),
    );
  }
}

class _DueCard extends StatelessWidget {
  const _DueCard({
    required this.item,
    required this.renewing,
    required this.onTap,
    required this.onRenew,
    required this.onReturn,
  });

  final BorrowItem item;
  final bool renewing;
  final VoidCallback onTap;
  final VoidCallback onRenew;
  final VoidCallback onReturn;

  @override
  Widget build(BuildContext context) {
    final overdue = isOverdue(item.status, item.overdueDays);
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          BookCover(
            label: item.coverLabel,
            tone: _tone(item.coverTone),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.title,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: AppText.bodySm.copyWith(
                    fontSize: 16,
                    fontWeight: FontWeight.w700,
                    height: 1.35,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  item.hasAppliedGrace
                      ? '${item.graceDueLabel} · ${item.dueLabel}'
                      : overdue
                          ? item.overdueDays > 0
                              ? '应还 ${AppDate.monthDay(item.settledDueAt)} · 已逾期 ${item.overdueDays} 天'
                              : '应还 ${AppDate.monthDay(item.settledDueAt)} · 已逾期'
                          : item.shelfCode.isEmpty
                              ? '应还 ${AppDate.monthDay(item.settledDueAt)}'
                              : '应还 ${AppDate.monthDay(item.settledDueAt)} · 馆藏 ${item.shelfCode}',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
                if (item.hasAppliedGrace) ...[
                  const SizedBox(height: 6),
                  StatusPill(
                    label: '宽限 +${item.appliedGraceDays} 天已通过',
                    tone: PillTone.mint,
                  ),
                ],
                const SizedBox(height: 8),
                AnimatedSwitcher(
                  duration: Motion.base,
                  transitionBuilder: (child, animation) => FadeTransition(
                    opacity: animation,
                    child: SlideTransition(
                      position: Tween<Offset>(
                        begin: const Offset(0, .12),
                        end: Offset.zero,
                      ).animate(animation),
                      child: child,
                    ),
                  ),
                  child: StatusPill(
                    key: ValueKey('${item.id}-$overdue-${item.dueLabel}'),
                    label: overdue
                        ? '逾期 · 可申请宽限'
                        : (item.dueLabel.isEmpty ? '待归还' : item.dueLabel),
                    tone: overdue ? PillTone.amber : PillTone.mint,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          if (overdue)
            SmallButton(label: '去归还', filled: true, onPressed: onReturn)
          else if (item.canRenew)
            SmallButton(label: '续借', loading: renewing, onPressed: onRenew),
        ],
      ),
    );
  }
}

class _ReadyCard extends StatelessWidget {
  const _ReadyCard({required this.book, required this.onTap});

  final ReadyBook book;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.all(12),
      color: AppColors.mintSoft,
      borderColor: AppColors.mint.withOpacity(.35),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          BookCover(
            label: coverLabelOf(book.title),
            tone: CoverTone.mint,
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  book.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: AppText.bodySm.copyWith(
                    fontSize: 15,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '取书截止 ${AppDate.monthDay(book.expiresAt)} '
                  '${AppDate.time(book.expiresAt)} · ${book.pickupPoint}',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
                const SizedBox(height: 8),
                StatusPill(
                  label: '已到馆 · 第 ${book.queueNo} 位',
                  tone: PillTone.mint,
                ),
              ],
            ),
          ),
          const SizedBox(width: 8),
          SmallButton(label: '去取书', onPressed: onTap),
        ],
      ),
    );
  }
}

CoverTone _tone(String value) {
  return switch (value) {
    'amber' => CoverTone.amber,
    'slate' => CoverTone.slate,
    _ => CoverTone.mint,
  };
}

class _DashedBorderPainter extends CustomPainter {
  _DashedBorderPainter({required this.color});

  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;
    final path = Path()
      ..addRRect(
        RRect.fromRectAndRadius(
          Offset.zero & size,
          const Radius.circular(12),
        ),
      );
    for (final metric in path.computeMetrics()) {
      var distance = 0.0;
      while (distance < metric.length) {
        canvas.drawPath(
          metric.extractPath(distance, distance + 6),
          paint,
        );
        distance += 11;
      }
    }
  }

  @override
  bool shouldRepaint(covariant _DashedBorderPainter oldDelegate) =>
      oldDelegate.color != color;
}
