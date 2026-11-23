import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/storage/storage_providers.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import '../../auth/presentation/auth_controller.dart';
import 'privacy_page.dart';
import 'profile_providers.dart';

/// C7 我的：身份、额度、统计、入口组与退出登录。
class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(profileProvider);
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            TabPageHeader(
              title: '我的',
              trailing: Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(13),
                  border: Border.all(color: AppColors.line),
                ),
                child: IconButton(
                  onPressed: () => _openSettings(context, ref),
                  icon: const Icon(
                    Icons.settings_outlined,
                    size: 22,
                    color: AppColors.ink,
                  ),
                  tooltip: '设置',
                ),
              ),
            ),
            Expanded(
              child: RefreshIndicator(
                color: AppColors.mintDeep,
                onRefresh: () async {
                  ref.invalidate(profileProvider);
                  await ref.read(profileProvider.future);
                },
                child: AsyncBody<ProfileData>(
                  value: value,
                  onRetry: () => ref.invalidate(profileProvider),
                  skeleton: const SkeletonList(count: 5, itemHeight: 72),
                  builder: (data) => _body(context, ref, data),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _body(BuildContext context, WidgetRef ref, ProfileData data) {
    final total = data.quotaTotal == 0 ? 1 : data.quotaTotal;
    final ratio = (data.quotaUsed / total).clamp(0.0, 1.0);
    final full = data.quotaUsed >= data.quotaTotal && data.quotaTotal > 0;

    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(20, 2, 20, 24),
      children: [
        if (data.blacklisted) ...[
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.redSoft,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(color: AppColors.red.withOpacity(0.35)),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Icon(
                  Icons.block,
                  size: 18,
                  color: AppColors.red,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    data.blacklistReason ?? '账号已暂停借阅权限，归还与申诉不受影响。',
                    style: AppText.caption.copyWith(
                      color: AppColors.red,
                      height: 1.6,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 12),
        ],
        AppCard(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              Row(
                children: [
                  Container(
                    width: 54,
                    height: 54,
                    alignment: Alignment.center,
                    decoration: const BoxDecoration(
                      color: AppColors.mintSoft,
                      shape: BoxShape.circle,
                    ),
                    child: Text(
                      data.name.isEmpty ? '读' : data.name.substring(0, 1),
                      style: AppText.h2.copyWith(
                        color: AppColors.mintDeep,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Text(
                              data.name,
                              style: AppText.h3.copyWith(fontSize: 18),
                            ),
                            const SizedBox(width: 8),
                            StatusPill(
                              label: data.creditLabel,
                              tone: data.blacklisted
                                  ? PillTone.red
                                  : PillTone.mint,
                            ),
                          ],
                        ),
                        const SizedBox(height: 5),
                        Text(
                          [data.studentNo, data.major, data.grade]
                              .where((part) => part.trim().isNotEmpty)
                              .join(' · '),
                          style: AppText.caption.copyWith(
                            color: AppColors.ink3,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              const Divider(height: 1, color: AppColors.line2),
              const SizedBox(height: 14),
              Row(
                children: [
                  Text(
                    '借阅额度',
                    style: AppText.bodySm.copyWith(color: AppColors.ink2),
                  ),
                  const Spacer(),
                  Text(
                    '${data.quotaUsed} / ${data.quotaTotal} 本',
                    style: AppText.bodySm.copyWith(
                      fontWeight: FontWeight.w700,
                      color: full ? AppColors.amber : AppColors.ink,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: LinearProgressIndicator(
                  value: ratio,
                  minHeight: 6,
                  backgroundColor: AppColors.sunken,
                  color: full ? AppColors.amber : AppColors.mintDeep,
                ),
              ),
              const SizedBox(height: 8),
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  '本学期已归还 ${data.returnedThisTerm} 本 · '
                  '${full ? '已达上限，归还后可继续借阅' : (data.overdueCount > 0 ? '${data.overdueCount} 本逾期' : '无欠费')}',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        AppCard(
          padding: const EdgeInsets.symmetric(vertical: 16),
          child: Row(
            children: [
              _Stat(value: '${data.borrowingCount}', label: '在借'),
              const _StatDivider(),
              _Stat(value: '${data.totalBorrowed}', label: '累计借阅'),
              const _StatDivider(),
              _Stat(value: '${data.reservationCount}', label: '预约'),
            ],
          ),
        ),
        const SizedBox(height: 12),
        AppCard(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Column(
            children: [
              _MenuRow(
                icon: Icons.menu_book_outlined,
                title: '我的借阅',
                onTap: () => context.go('/borrow'),
              ),
              _MenuRow(
                icon: Icons.calendar_today_outlined,
                title: '预约记录',
                onTap: () => context.go('/reservations'),
              ),
              _MenuRow(
                icon: Icons.chat_bubble_outline,
                title: '消息中心',
                badge: data.unreadCount,
                onTap: () => context.push('/notifications'),
              ),
              _MenuRow(
                icon: Icons.shield_outlined,
                title: '申诉工单',
                onTap: () => context.push('/appeals'),
              ),
              _MenuRow(
                icon: Icons.menu_book_outlined,
                title: '借阅规则',
                subtitle: '额度 · 借期 · 费用',
                onTap: () => _showRules(context, data),
              ),
              _MenuRow(
                icon: Icons.settings_outlined,
                title: '设置',
                onTap: () => _openSettings(context, ref),
              ),
              _MenuRow(
                icon: Icons.phone_iphone,
                title: '帮助与反馈',
                subtitle: '在线客服 · 常见问题',
                onTap: () => showToast(context, '在线客服 9:00–21:00'),
                last: true,
              ),
            ],
          ),
        ),
        const SizedBox(height: 18),
        Text(
          '阅有期 ${data.version} · 服务条款 · 隐私政策',
          textAlign: TextAlign.center,
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
      ],
    );
  }

  void _showRules(BuildContext context, ProfileData data) {
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
              Text('借阅规则', style: AppText.h3.copyWith(fontSize: 17)),
              const SizedBox(height: 12),
              Text(
                '· 借期 ${data.loanDays} 天，最多同时借 ${data.quotaTotal} 本，可续借 ${data.maxRenew} 次（新应还日以服务端为准）\n'
                '· 预约到书保留 48 小时，超时顺延下一位\n'
                '· 逾期费用以归还时服务端核算为准，客户端不计算\n'
                '· 对判定有异议可在每处判定旁发起申诉，馆员 2 小时内回应',
                style: AppText.bodySm.copyWith(
                  color: AppColors.ink2,
                  height: 1.9,
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

  Future<void> _openSettings(BuildContext context, WidgetRef ref) async {
    await showModalBottomSheet<void>(
      context: context,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (sheetContext) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 8),
            Container(
              width: 36,
              height: 4,
              decoration: BoxDecoration(
                color: AppColors.line,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 8),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 4, 20, 8),
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text('设置', style: AppText.h3.copyWith(fontSize: 17)),
              ),
            ),
            _MenuRow(
              icon: Icons.shield_outlined,
              title: '隐私与提醒',
              onTap: () async {
                Navigator.of(sheetContext).pop();
                await Navigator.of(context, rootNavigator: true).push(
                  MaterialPageRoute<void>(
                    builder: (_) => const PrivacyPage(),
                  ),
                );
              },
            ),
            _MenuRow(
              icon: Icons.cleaning_services_outlined,
              title: '清除本地缓存',
              onTap: () async {
                Navigator.of(sheetContext).pop();
                final ok = await showConfirmDialog(
                  context,
                  title: '清除本地缓存',
                  message: '将删除离线缓存的借阅与消息，不影响服务器数据。',
                  confirmLabel: '清除',
                );
                if (!ok) return;
                try {
                  await ref.read(cacheBoxProvider).clear();
                  if (context.mounted) showToast(context, '本地缓存已清理');
                } catch (_) {
                  if (context.mounted) showToast(context, '清理失败，请稍后重试');
                }
              },
            ),
            _MenuRow(
              icon: Icons.logout,
              title: '退出登录',
              danger: true,
              last: true,
              onTap: () {
                Navigator.of(sheetContext).pop();
                _logout(context, ref);
              },
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }

  Future<void> _logout(BuildContext context, WidgetRef ref) async {
    final ok = await showConfirmDialog(
      context,
      title: '退出登录',
      message: '退出后需要重新使用校园统一身份登录。',
      confirmLabel: '退出登录',
      danger: true,
    );
    if (!ok) return;
    try {
      await ref.read(authControllerProvider.notifier).logout();
    } catch (error) {
      if (context.mounted) showToast(context, messageForError(error));
    }
  }
}

class _Stat extends StatelessWidget {
  const _Stat({required this.value, required this.label});

  final String value;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        children: [
          Text(
            value,
            style: AppText.number.copyWith(
              fontSize: 24,
              color: AppColors.ink,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: AppText.caption.copyWith(color: AppColors.ink3),
          ),
        ],
      ),
    );
  }
}

class _StatDivider extends StatelessWidget {
  const _StatDivider();

  @override
  Widget build(BuildContext context) {
    return Container(width: 1, height: 28, color: AppColors.line2);
  }
}

class _MenuRow extends StatelessWidget {
  const _MenuRow({
    required this.icon,
    required this.title,
    this.subtitle,
    this.badge = 0,
    this.onTap,
    this.danger = false,
    this.last = false,
  });

  final IconData icon;
  final String title;
  final String? subtitle;
  final int badge;
  final VoidCallback? onTap;
  final bool danger;
  final bool last;

  @override
  Widget build(BuildContext context) {
    final color = danger ? AppColors.red : AppColors.ink;
    return InkWell(
      onTap: onTap,
      child: Container(
        constraints: const BoxConstraints(minHeight: 52),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          border: last
              ? null
              : const Border(bottom: BorderSide(color: AppColors.line2)),
        ),
        child: Row(
          children: [
            Icon(
              icon,
              size: 20,
              color: danger ? AppColors.red : AppColors.ink2,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                title,
                style: AppText.bodySm.copyWith(
                  fontSize: 14.5,
                  fontWeight: FontWeight.w500,
                  color: color,
                ),
              ),
            ),
            if (subtitle != null && badge == 0)
              Text(
                subtitle!,
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
            if (badge > 0) ...[
              Container(
                constraints: const BoxConstraints(minWidth: 22),
                height: 22,
                padding: const EdgeInsets.symmetric(horizontal: 6),
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  color: AppColors.redSoft,
                  borderRadius: BorderRadius.circular(11),
                ),
                child: Text(
                  '$badge',
                  style: AppText.caption.copyWith(
                    color: AppColors.red,
                    fontSize: 11,
                    fontWeight: FontWeight.w600,
                    height: 1,
                  ),
                ),
              ),
              const SizedBox(width: 8),
            ],
            if (!danger)
              const Icon(
                Icons.chevron_right,
                size: 18,
                color: AppColors.ink3,
              ),
          ],
        ),
      ),
    );
  }
}
