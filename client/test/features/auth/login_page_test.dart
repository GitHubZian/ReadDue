import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/auth/domain/auth_repository.dart';
import 'package:client/features/auth/presentation/auth_controller.dart';
import 'package:client/features/auth/presentation/login_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingAuthRepository implements AuthRepository {
  @override
  Future<Session> login({
    required String studentNo,
    required String password,
    required bool remember,
  }) async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<Session?> restore() async => null;

  @override
  Future<void> logout() async {}
}

void main() {
  testWidgets('C9 正常展示登录表单', (tester) async {
    await pumpPage(tester, const LoginPage());

    expect(find.text('阅有期'), findsWidgets);
    expect(find.byType(TextField), findsNWidgets(2));
    expect(find.text('登录'), findsWidgets);
  });

  testWidgets('C9 登录失败展示错误', (tester) async {
    await pumpPage(
      tester,
      const LoginPage(),
      overrides: [
        authRepositoryProvider.overrideWithValue(_FailingAuthRepository()),
      ],
    );

    await tester.enterText(find.byType(TextField).last, 'password123');
    await tester.tap(find.text('登录').last);
    await tester.pumpAndSettle();

    expect(find.textContaining('服务暂时不可用'), findsWidgets);
  });
}
