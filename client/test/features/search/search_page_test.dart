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

  @override
  Future<BookDetailData> fetchDetail(String id) async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<int> reserve(String bookId) async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }
}

class _RecordingSearchRepository implements SearchRepository {
  final _delegate = MockSearchRepository();
  int reserveCalls = 0;

  @override
  Future<SearchPageData> search(String query, String filter) =>
      _delegate.search(query, filter);

  @override
  Future<BookDetailData> fetchDetail(String bookId) =>
      _delegate.fetchDetail(bookId);

  @override
  Future<void> borrow(String bookId) => _delegate.borrow(bookId);

  @override
  Future<int> reserve(String bookId) async {
    reserveCalls++;
    return 3;
  }
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C4 正常展示搜索结果', (tester) async {
    await pumpPage(tester, const SearchPage());

    expect(find.textContaining('乡土中国'), findsWidgets);
    expect(find.textContaining('找到'), findsWidgets);
  });

  testWidgets('C4 点结果卡展示馆藏详情', (tester) async {
    await pumpPage(tester, const SearchPage());

    await tester.tap(find.text('乡土中国').first);
    await tester.pumpAndSettle();

    expect(find.textContaining('ISBN'), findsOneWidget);
    expect(find.text('馆藏'), findsOneWidget);
    expect(find.text('借阅'), findsWidgets);
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

  testWidgets('C4 预约走真实接口并回显排位', (tester) async {
    final repository = _RecordingSearchRepository();
    await pumpPage(
      tester,
      const SearchPage(),
      overrides: [
        searchRepositoryProvider.overrideWithValue(repository),
      ],
    );

    await tester.tap(find.text('预约').first);
    await tester.pumpAndSettle();
    expect(find.textContaining('预计排在第'), findsOneWidget);

    await tester.tap(find.text('确认预约'));
    await tester.pumpAndSettle();

    expect(repository.reserveCalls, 1);
    expect(find.text('已预约 · 排位 3'), findsOneWidget);
    expect(find.textContaining('已加入预约队列'), findsNothing);
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

  testWidgets('C12 筛选面板按可借状态过滤', (tester) async {
    await pumpPage(tester, const SearchPage());

    expect(find.textContaining('乡土中国·生育制度'), findsOneWidget);
    await tester.tap(find.text('筛选'));
    await tester.pumpAndSettle();
    expect(find.text('筛选结果'), findsOneWidget);
    expect(find.textContaining('可借＝有在架副本'), findsOneWidget);

    await tester.tap(find.text('可借').last);
    await tester.pumpAndSettle();
    await tester.pump(const Duration(seconds: 1));
    await tester.pumpAndSettle();

    expect(find.textContaining('乡土中国·生育制度'), findsNothing);
    expect(find.text('乡土中国'), findsOneWidget);
  });

  testWidgets('C12 排序面板更新标签', (tester) async {
    await pumpPage(tester, const SearchPage());

    await tester.tap(find.textContaining('按相关度'));
    await tester.pumpAndSettle();
    expect(find.text('排序方式'), findsOneWidget);

    await tester.tap(find.text('书名 A-Z'));
    await tester.pumpAndSettle();

    expect(find.textContaining('书名 A-Z'), findsWidgets);
    expect(find.textContaining('按相关度'), findsNothing);
  });

  testWidgets('C12 详情页固定高度且不可上拉扩展', (tester) async {
    await pumpPage(tester, const SearchPage());

    await tester.tap(find.text('乡土中国').first);
    await tester.pumpAndSettle();

    expect(find.byType(DraggableScrollableSheet), findsNothing);
    expect(find.textContaining('ISBN'), findsOneWidget);
    expect(find.text('馆藏位置'), findsOneWidget);
  });
}
