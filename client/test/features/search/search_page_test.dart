import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/search/domain/search_repository.dart';
import 'package:client/features/search/presentation/search_page.dart';
import 'package:client/features/search/presentation/search_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingSearchRepository implements SearchRepository {
  @override
  Future<SearchPageData> search(String query, String filter) async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<void> borrow(String bookId) async {}
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C4 正常展示搜索结果', (tester) async {
    await pumpPage(tester, const SearchPage());

    expect(find.textContaining('乡土中国'), findsWidgets);
    expect(find.textContaining('找到'), findsWidgets);
  });

  testWidgets('C4 借阅成功后提示并给回 C1 的入口', (tester) async {
    await pumpPage(tester, const SearchPage());

    await tester.enterText(find.byType(TextField), '百年');
    await tester.pump(const Duration(milliseconds: 350));
    await tester.pump(const Duration(seconds: 1));
    await tester.pumpAndSettle();

    await tester.tap(find.text('借阅').first);
    await tester.pumpAndSettle();
    await tester.tap(find.text('确认借阅'));
    await tester.pumpAndSettle();

    expect(find.textContaining('已借出《百年孤独》'), findsOneWidget);
    expect(find.text('查看我的借阅'), findsOneWidget);
  });

  testWidgets('C4 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const SearchPage(),
      overrides: [
        searchRepositoryProvider.overrideWithValue(_FailingSearchRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
