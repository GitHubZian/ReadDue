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
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import 'notifications_providers.dart';

/// C6 消息中心：分类筛选、未读、免打扰开关。
class NotificationsPage extends ConsumerStatefulWidget {
  const NotificationsPage({super.key});

  @override
  ConsumerState<NotificationsPage> createState() => _NotificationsPageState();
}

class _NotificationsPageState extends ConsumerState<NotificationsPage> {
  int _tab = 0;
  bool? _quietOverride;
  bool? _emailOverride;
  bool _markingAll = false;

  @override
  Widget build(BuildContext context) {
    final value = ref.watch(notificationsProvider);
    final data = value.asData?.value;

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            PageTitle(
              title: '消息中心',
              showBack: true,
              actions: [
                TextButton(
                  onPressed:
                      data == null || data.unreadCount == 0 || _markingAll
                          ? null
                          : () => _markAllRead(),
                  child: Text(
                    '全部已读',
                    style: AppText.bodySm.copyWith(
                      fontWeight: FontWeight.w600,
                      color: AppColors.ink2,
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
                    SegItem(label: '全部', count: data.items.length),
                    SegItem(
                      label: '借阅',
                      count: data.items
                          .where((item) => item.category == 'borrow')
                          .length,
                    ),
                    SegItem(
                      label: '系统',
                      count: data.items
                          .where((item) => item.category == 'system')
                          .length,
                    ),
                  ],
                  index: _tab,
                  onChanged: (index) => setState(() => _tab = index),
                ),
              ),
            Expanded(
              child: AsyncBody<NotificationsPageData>(
                value: value,
                onRetry: () => ref.invalidate(notificationsProvider),
                skeleton: const SkeletonList(count: 4, itemHeight: 82),
                builder: (data) => _body(data),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _body(NotificationsPageData data) {
    final items = switch (_tab) {
      1 => data.items.where((item) => item.category == 'borrow').toList(),
      2 => data.items.where((item) => item.category == 'system').toList(),
      _ => data.items,
    };
    final quiet = _quietOverride ?? data.quietHours.enabled;
    final quietHours = data.quietHours;

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 2, 20, 24),
      children: [
        if (items.isEmpty)
          const Padding(
            padding: EdgeInsets.only(top: 40),
            child: EmptyState(
              title: '暂无消息',
              message: '借阅、到书与申诉的结果都会在这里通知你。',
              icon: Icons.notifications_none,
            ),
          )
        else
          for (final item in items) ...[
            _NotificationCard(item: item, onTap: () => _open(item)),
            const SizedBox(height: 10),
          ],
        const SizedBox(height: 10),
        AppCard(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          onTap: () => _openSettings(quietHours, quiet, data.channels),
          child: Row(
            children: [
              const Icon(Icons.settings_outlined,
                  size: 20, color: AppColors.ink2),
              const SizedBox(width: 10),
              Text(
                '消息设置',
                style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
              ),
              const Spacer(),
              Text(
                '免打扰 ${quietHours.start}–${quietHours.end}',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const Icon(Icons.chevron_right, size: 18, color: AppColors.ink3),
            ],
          ),
        ),
        const SizedBox(height: 12),
        Text(
          '消息保留 90 天 · 可在设置中关闭非必要提醒',
          textAlign: TextAlign.center,
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
      ],
    );
  }

  Future<void> _setQuiet(bool enabled) async {
    setState(() => _quietOverride = enabled);
    try {
      await ref.read(notificationRepositoryProvider).setQuietHours(enabled);
      if (!mounted) return;
      ref.invalidate(notificationsProvider);
      showToast(context, enabled ? '已开启免打扰' : '已关闭免打扰');
    } catch (error) {
      if (!mounted) return;
      setState(() => _quietOverride = null);
      showToast(context, messageForError(error));
    }
  }

  Future<void> _setEmail(bool enabled) async {
    setState(() => _emailOverride = enabled);
    try {
      await ref
          .read(notificationRepositoryProvider)
          .setChannels(email: enabled);
      if (!mounted) return;
      ref.invalidate(notificationsProvider);
      showToast(context, enabled ? '已开启邮件提醒' : '已关闭邮件提醒');
    } catch (error) {
      if (!mounted) return;
      setState(() => _emailOverride = null);
      showToast(context, messageForError(error));
    }
  }

  Future<void> _openSettings(
    QuietHours quietHours,
    bool initial,
    NotificationChannels channels,
  ) async {
    await showModalBottomSheet<void>(
      context: context,
      backgroundColor: AppColors.card,
      showDragHandle: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (sheetContext) => StatefulBuilder(
        builder: (sheetContext, setSheet) {
          final enabled = _quietOverride ?? initial;
          final emailOn = _emailOverride ?? channels.email;
          return SafeArea(
            top: false,
            child: Padding(
              padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('消息设置', style: AppText.h3),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '免打扰 ${quietHours.start}–${quietHours.end}',
                              style: AppText.bodySm.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              '履约类通知仍会送达站内信',
                              style: AppText.caption
                                  .copyWith(color: AppColors.ink3),
                            ),
                          ],
                        ),
                      ),
                      Switch(
                        value: enabled,
                        onChanged: (value) {
                          setSheet(() {});
                          _setQuiet(value);
                        },
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Divider(height: 1, color: AppColors.line2),
                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '邮件提醒',
                              style: AppText.bodySm.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              '到期与逾期提醒同时发送到校园邮箱',
                              style: AppText.caption
                                  .copyWith(color: AppColors.ink3),
                            ),
                          ],
                        ),
                      ),
                      Switch(
                        value: emailOn,
                        onChanged: (value) {
                          setSheet(() {});
                          _setEmail(value);
                        },
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Future<void> _open(AppNotification item) async {
    if (!item.read) {
      try {
        await ref.read(notificationRepositoryProvider).markRead(item.id);
        if (mounted) ref.invalidate(notificationsProvider);
      } catch (_) {
        // 已读失败不阻断查看详情。
      }
    }
    if (!mounted) return;
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
              Text(item.title, style: AppText.h3.copyWith(fontSize: 17)),
              const SizedBox(height: 6),
              Text(
                '${AppDate.dateTime(item.createdAt)} · 站内信',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const SizedBox(height: 14),
              Text(
                item.body,
                style: AppText.bodySm.copyWith(
                  color: AppColors.ink2,
                  height: 1.7,
                ),
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

  Future<void> _markAllRead() async {
    final ok = await showConfirmDialog(
      context,
      title: '全部标为已读',
      message: '所有未读消息将标记为已读，此操作可在消息详情中复查。',
      confirmLabel: '全部已读',
    );
    if (!ok || !mounted) return;
    setState(() => _markingAll = true);
    try {
      await ref.read(notificationRepositoryProvider).markAllRead();
      if (!mounted) return;
      ref.invalidate(notificationsProvider);
      showToast(context, '已全部标为已读');
    } catch (error) {
      if (!mounted) return;
      showToast(context, messageForError(error));
    } finally {
      if (mounted) setState(() => _markingAll = false);
    }
  }
}

class _NotificationCard extends StatelessWidget {
  const _NotificationCard({required this.item, required this.onTap});

  final AppNotification item;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final (icon, color, background) = switch (item.type) {
      'overdue' => (Icons.error_outline, AppColors.amber, AppColors.amberSoft),
      'grace' => (
          Icons.check_circle_outline,
          AppColors.mintDeep,
          AppColors.mintSoft,
        ),
      'reservation' => (
          Icons.menu_book_outlined,
          AppColors.mintDeep,
          AppColors.mintSoft,
        ),
      _ => (Icons.notifications_none, AppColors.ink2, AppColors.sunken),
    };

    return AppCard(
      padding: const EdgeInsets.all(12),
      onTap: onTap,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 36,
            height: 36,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: background,
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, size: 18, color: color),
          ),
          const SizedBox(width: 11),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Text(
                        item.title,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: AppText.bodySm.copyWith(
                          fontSize: 14,
                          fontWeight:
                              item.read ? FontWeight.w500 : FontWeight.w700,
                          color: AppColors.ink,
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    AnimatedOpacity(
                      opacity: item.read ? 0 : 1,
                      duration: Motion.fast,
                      child: Container(
                        width: 8,
                        height: 8,
                        margin: const EdgeInsets.only(top: 5),
                        decoration: const BoxDecoration(
                          color: AppColors.mint,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  item.body,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: AppText.caption.copyWith(
                    color: AppColors.ink2,
                    height: 1.5,
                  ),
                ),
                const SizedBox(height: 5),
                Text(
                  '${AppDate.monthDay(item.createdAt)} ${AppDate.time(item.createdAt)}',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
