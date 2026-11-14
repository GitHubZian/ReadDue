import 'package:client/core/models/models.dart';
import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/borrow/domain/borrow_repository.dart';
import 'package:client/features/borrow/presentation/borrow_page.dart';
import 'package:client/features/borrow/presentation/borrow_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingBorrowRepository implements BorrowRepository {
  @override
  Future<BorrowDashboard> fetchDashboard() async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<DateTime> renew(String id) async => throw UnimplementedError();

  @override
  Future<void> returnBook(String id) async => throw UnimplementedError();
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C1 正常展示摘要、临期与到书', (tester) async {
    await pumpPage(tester, const BorrowPage());

    expect(find.textContaining('林知远'), findsWidgets);
    expect(find.textContaining('线性代数'), findsWidgets);
    expect(find.text('即将到期'), findsOneWidget);

    await tester.dragUntilVisible(
      find.textContaining('人类简史'),
      find.byType(Scrollable).first,
      const Offset(0, -300),
    );
    expect(find.textContaining('人类简史'), findsWidgets);
  });

  testWidgets('C1 摘要数字可打开在借清单', (tester) async {
    await pumpPage(tester, const BorrowPage());

    await tester.tap(find.text('在借中'));
    await tester.pumpAndSettle();

    expect(find.text('在借中 · 3 本'), findsOneWidget);
    expect(find.textContaining('算法导论'), findsWidgets);
  });

  testWidgets('C1 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const BorrowPage(),
      overrides: [
        borrowRepositoryProvider.overrideWithValue(_FailingBorrowRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
