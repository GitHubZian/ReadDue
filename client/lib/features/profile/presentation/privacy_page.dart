import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/widgets.dart';
import '../domain/privacy_repository.dart';
import 'profile_providers.dart';

/// C7 子页：隐私授权与提醒偏好（B-06 / B-10）。
class PrivacyPage extends ConsumerWidget {
  const PrivacyPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(privacyProvider);

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '隐私与提醒'),
            Expanded(
              child: AsyncBody<PrivacyData>(
                value: value,
                onRetry: () => ref.invalidate(privacyProvider),
                builder: (data) => _body(context, ref, data),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _body(BuildContext context, WidgetRef ref, PrivacyData data) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 96),
      children: [
        const _SectionLabel('隐私授权'),
        AppCard(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Column(
            children: [
              _SwitchRow(
                title: '借阅场景授权',
                subtitle: '用于宽限场景判断与到期提醒，关闭不影响借还',
                value: data.sceneConsent,
                onChanged: data.forgotten
                    ? null
                    : (value) => _setConsent(context, ref, scene: value),
              ),
              const Divider(height: 1, color: AppColors.line2),
              _SwitchRow(
                title: '本地缓存授权',
                subtitle: '缓存最近借阅与消息，便于离线查看',
                value: data.cacheConsent,
                onChanged: data.forgotten
                    ? null
                    : (value) => _setConsent(context, ref, cache: value),
              ),
            ],
          ),
        ),
        const SizedBox(height: 8),
        Text(
          '关闭授权后仍可检索、借阅与归还，仅不再使用对应数据。',
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
        const SizedBox(height: 20),
        const _SectionLabel('提醒偏好'),
        Text(
          'P0 为履约类必达通知，站内信不可关闭；可调整汇总频率。',
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
        const SizedBox(height: 10),
        AppCard(
          padding: EdgeInsets.zero,
          child: Column(
            children: [
              for (var i = 0; i < data.topics.length; i++) ...[
                if (i > 0) const Divider(height: 1, color: AppColors.line2),
                _TopicRow(
                  topic: data.topics[i],
                  onTap: data.topics[i].locked
                      ? null
                      : () => _pickFrequency(context, ref, data.topics[i]),
                ),
              ],
            ],
          ),
        ),
        const SizedBox(height: 20),
        const _SectionLabel('隐私与遗忘'),
        AppCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                data.forgotten ? '遗忘申请处理中' : '申请遗忘个人信息',
                style: AppText.body.copyWith(fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 6),
              Text(
                data.forgotten
                    ? '姓名、手机号与院系将在 30 天内清除，借阅事实保留。'
                    : '脱敏姓名、手机号与院系；借阅与归还记录依法保留，不影响毕业离校。',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                height: 44,
                child: OutlinedButton(
                  onPressed:
                      data.forgotten ? null : () => _forget(context, ref),
                  style: OutlinedButton.styleFrom(
                    foregroundColor: AppColors.red,
                    side: BorderSide(
                      color: AppColors.red.withOpacity(0.45),
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppRadius.button),
                    ),
                  ),
                  child: const Text('申请遗忘个人信息'),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),
        Center(
          child: Text(
            '阅有期 1.0.0 · 校园图书馆',
            style: AppText.caption.copyWith(color: AppColors.ink3),
          ),
        ),
      ],
    );
  }

  Future<void> _setConsent(
    BuildContext context,
    WidgetRef ref, {
    bool? scene,
    bool? cache,
  }) async {
    final repo = ref.read(privacyRepositoryProvider);
    try {
      if (scene != null) await repo.setSceneConsent(scene);
      if (cache != null) await repo.setCacheConsent(cache);
      ref.invalidate(privacyProvider);
    } catch (_) {
      if (context.mounted) showToast(context, '保存失败，请稍后重试');
    }
  }

  Future<void> _pickFrequency(
    BuildContext context,
    WidgetRef ref,
    NotifyTopic topic,
  ) async {
    final options = topic.priority == 'P2'
        ? const ['DAILY', 'WEEKLY']
        : const ['REALTIME', 'DAILY', 'WEEKLY'];
    final labels = {
      'REALTIME': '实时提醒',
      'DAILY': '每日汇总',
      'WEEKLY': '每周汇总',
    };
    final picked = await showModalBottomSheet<String>(
      context: context,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (sheetContext) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 10),
            Text(
              '${topic.name} · 提醒频率',
              style: AppText.body.copyWith(fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 8),
            for (final value in options)
              ListTile(
                title: Text(labels[value]!),
                trailing: value == topic.frequency
                    ? const Icon(Icons.check, color: AppColors.mintDeep)
                    : null,
                onTap: () => Navigator.of(sheetContext).pop(value),
              ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
    if (picked == null || picked == topic.frequency) return;
    try {
      await ref.read(privacyRepositoryProvider).setFrequency(topic, picked);
      ref.invalidate(privacyProvider);
      if (context.mounted) showToast(context, '已调整为${labels[picked]}');
    } catch (_) {
      if (context.mounted) showToast(context, '调整失败，请稍后重试');
    }
  }

  Future<void> _forget(BuildContext context, WidgetRef ref) async {
    final ok = await showConfirmDialog(
      context,
      title: '申请遗忘个人信息',
      message: '将清除姓名、手机号与院系，借阅事实保留。此操作不可撤销。',
      confirmLabel: '确认申请',
      danger: true,
    );
    if (!ok) return;
    try {
      await ref.read(privacyRepositoryProvider).forget();
      ref.invalidate(privacyProvider);
      if (context.mounted) showToast(context, '已提交遗忘申请，30 天内完成');
    } catch (_) {
      if (context.mounted) showToast(context, '提交失败，请稍后重试');
    }
  }
}

class _SectionLabel extends StatelessWidget {
  const _SectionLabel(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Text(text, style: AppText.h3.copyWith(fontSize: 17)),
    );
  }
}

class _SwitchRow extends StatelessWidget {
  const _SwitchRow({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool>? onChanged;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 10, 8, 10),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 2),
                Text(
                  subtitle,
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
              ],
            ),
          ),
          Switch(value: value, onChanged: onChanged),
        ],
      ),
    );
  }
}

class _TopicRow extends StatelessWidget {
  const _TopicRow({required this.topic, required this.onTap});

  final NotifyTopic topic;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final tone = switch (topic.priority) {
      'P0' => PillTone.mint,
      'P2' => PillTone.slate,
      _ => PillTone.amber,
    };
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 12, 12),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        topic.name,
                        style: AppText.bodySm.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(width: 8),
                      StatusPill(label: topic.priority, tone: tone),
                    ],
                  ),
                  const SizedBox(height: 3),
                  Text(
                    topic.locked
                        ? '${topic.frequencyLabel} · 必达通知'
                        : '${topic.frequencyLabel} · 点击调整',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                ],
              ),
            ),
            if (!topic.locked)
              const Icon(Icons.chevron_right, color: AppColors.ink3, size: 20),
          ],
        ),
      ),
    );
  }
}
