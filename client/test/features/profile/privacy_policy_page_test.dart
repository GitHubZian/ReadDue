import 'package:client/features/profile/presentation/privacy_policy_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

void main() {
  testWidgets('隐私政策页展示六节且与后端实现一致', (tester) async {
    tester.view.physicalSize = const Size(1200, 4200);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await pumpPage(tester, const PrivacyPolicyPage());

    for (final title in [
      '我们收集什么',
      '我们不收集什么',
      '用途与依据',
      '保存与删除',
      '你的权利',
      '联系与更新日期',
    ]) {
      expect(find.text(title), findsOneWidget);
    }

    expect(find.textContaining('借阅事实'), findsWidgets);
    expect(find.textContaining('审计日志保留 90 天'), findsOneWidget);
    expect(
      find.textContaining('管理员批准注销申请后，立即对姓名、手机号、邮箱打码'),
      findsOneWidget,
    );
    expect(find.textContaining('2026-11-25'), findsOneWidget);
    // 旧口径不应再出现。
    expect(find.textContaining('30 天内'), findsNothing);
  });
}
