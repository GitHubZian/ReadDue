import 'package:client/core/storage/kv_store.dart';
import 'package:client/core/storage/storage_providers.dart';
import 'package:client/core/theme/app_theme.dart';
import 'package:client/features/auth/presentation/auth_controller.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 统一的页面测试外壳：真实主题 + 内存 KV + 已恢复的登录态。
Future<void> pumpPage(
  WidgetTester tester,
  Widget page, {
  List<Override> overrides = const [],
}) async {
  SharedPreferences.setMockInitialValues({
    'auth.token': 'test-token',
    'auth.userId': '1900000000000000000',
    'auth.name': '林知远',
    'auth.studentNo': '20230114',
  });
  final prefs = await SharedPreferences.getInstance();
  final kv = KvStore(prefs);

  final container = ProviderContainer(
    overrides: [
      kvStoreProvider.overrideWithValue(kv),
      ...overrides,
    ],
  );
  addTearDown(container.dispose);
  await container.read(authControllerProvider.notifier).restore();

  await tester.pumpWidget(
    UncontrolledProviderScope(
      container: container,
      child: MaterialApp(
        theme: AppTheme.light(),
        home: page,
      ),
    ),
  );
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 400));
  await tester.pump(const Duration(seconds: 1));
  await tester.pumpAndSettle();
}
