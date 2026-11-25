import 'package:flutter/material.dart';

import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/widgets.dart';

/// C7 子页：静态隐私政策。文案需与后端实际处理一致：
/// 借阅事实保留、审计日志 90 天、管理员批准注销后立即打码。
class PrivacyPolicyPage extends StatelessWidget {
  const PrivacyPolicyPage({super.key});

  static const _sections = <_PolicySection>[
    _PolicySection(
      title: '我们收集什么',
      items: [
        '账号与身份：学号/工号、姓名、院系、专业、年级；你主动填写的手机号与邮箱。',
        '借阅事实：借出、归还、续借、逾期、预约与费用记录；不包含你阅读的具体内容。',
        '宽限材料：你主动提交的说明与证据文件。',
        '提醒偏好：通知主题、频率、渠道开关与免打扰时段。',
        '审计日志：登录、授权变更、遗忘与馆员操作留痕，仅用于安全追责。',
      ],
    ),
    _PolicySection(
      title: '我们不收集什么',
      items: [
        '位置信息、通讯录、设备指纹、第三方行为数据与生物特征。',
        '除你主动选择的文件外，不读取相册或本地文件的内容。',
      ],
    ),
    _PolicySection(
      title: '用途与依据',
      items: [
        '用于提供借还、到期提醒、宽限与申诉处理，以及必要的安全审计。',
        '以完成借阅服务和你主动开启的授权为依据，只处理必要信息；关闭借阅场景授权仍可检索、借阅与归还，也不影响提醒接收。',
      ],
    ),
    _PolicySection(
      title: '保存与删除',
      items: [
        '借阅事实（含逾期与费用）按图书馆规定保留，不随注销删除。',
        '你申请遗忘后，姓名、手机号、邮箱、院系/专业/年级立即打码，账号显示为“已注销读者-尾号”。',
        '管理员批准注销申请后，立即对姓名、手机号、邮箱打码，借阅事实保留。',
        '审计日志保留 90 天，到期清理。',
        '本地缓存可由你随时在设置的“清除本地缓存”中删除。',
      ],
    ),
    _PolicySection(
      title: '你的权利',
      items: [
        '在“隐私与提醒”中随时开关场景与缓存授权，调整提醒频率与免打扰时段。',
        '申请遗忘、对判定发起申诉、提交注销申请。',
        '对处理有异议时，可通过“帮助与反馈”联系馆员说明情况。',
      ],
    ),
    _PolicySection(
      title: '联系与更新日期',
      items: [
        '校园图书馆服务台 · 在线客服 9:00–21:00。',
        '本政策更新日期：2026-11-25。',
      ],
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '隐私政策'),
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
                children: [
                  Text(
                    '本政策说明阅有期客户端与校园图书馆服务如何处理你的信息。',
                    style: AppText.caption.copyWith(
                      color: AppColors.ink3,
                      height: 1.7,
                    ),
                  ),
                  const SizedBox(height: 16),
                  for (var i = 0; i < _sections.length; i++) ...[
                    if (i > 0) const SizedBox(height: 20),
                    _SectionCard(section: _sections[i]),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PolicySection {
  const _PolicySection({required this.title, required this.items});

  final String title;
  final List<String> items;
}

class _SectionCard extends StatelessWidget {
  const _SectionCard({required this.section});

  final _PolicySection section;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: Text(section.title, style: AppText.h3.copyWith(fontSize: 17)),
        ),
        AppCard(
          padding: const EdgeInsets.fromLTRB(14, 12, 14, 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              for (var i = 0; i < section.items.length; i++) ...[
                if (i > 0) const SizedBox(height: 8),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '· ',
                      style: AppText.bodySm.copyWith(
                        color: AppColors.mintDeep,
                        height: 1.7,
                      ),
                    ),
                    Expanded(
                      child: Text(
                        section.items[i],
                        style: AppText.bodySm.copyWith(
                          color: AppColors.ink2,
                          height: 1.7,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}
