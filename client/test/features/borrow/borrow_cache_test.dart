import 'package:client/core/models/models.dart';
import 'package:client/core/storage/cache_box.dart';
import 'package:client/core/storage/cache_consent.dart';
import 'package:client/core/storage/reader_cache.dart';
import 'package:client/core/storage/storage_providers.dart';
import 'package:client/features/borrow/domain/borrow_repository.dart';
import 'package:client/features/borrow/presentation/borrow_page.dart';
import 'package:client/features/borrow/presentation/borrow_providers.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _MemoryCacheBox extends CacheBox {
  final Map<String, dynamic> store = {};

  @override
  dynamic read(String key) => store[key];

  @override
  Future<void> write(String key, Object? value) async {
    store[key] = value;
  }

  @override
  Future<void> clear() async {
    store.clear();
  }
}

class _StaticBorrowRepository implements BorrowRepository {
  _StaticBorrowRepository(this.dashboard);

  final BorrowDashboard dashboard;

  @override
  Future<BorrowDashboard> fetchDashboard() async => dashboard;

  @override
  Future<DateTime> renew(String orderId) async => DateTime(2026, 5, 1);

  @override
  Future<void> returnBook(String orderId) async {}
}

const _dashboard = BorrowDashboard(
  summary: BorrowSummary(
    borrowing: 1,
    quotaUsed: 1,
    quotaTotal: 5,
    returnedThisTerm: 2,
  ),
);

void main() {
  testWidgets('缓存授权开启时借阅列表写入缓存并可读回', (tester) async {
    final box = _MemoryCacheBox();
    await pumpPage(
      tester,
      const BorrowPage(),
      overrides: [
        cacheBoxProvider.overrideWithValue(box),
        cacheConsentProvider.overrideWith((ref) async => true),
        borrowRepositoryProvider
            .overrideWithValue(_StaticBorrowRepository(_dashboard)),
      ],
    );
    await tester.pumpAndSettle();

    expect(box.store.containsKey(ReaderCache.borrowKey), isTrue);
    final cached = ReaderCache(box).readBorrow();
    expect(cached, isNotNull);
    expect(cached!.summary.borrowing, 1);
    expect(cached.summary.quotaTotal, 5);
  });

  testWidgets('缓存授权关闭时借阅列表不写缓存', (tester) async {
    final box = _MemoryCacheBox();
    await pumpPage(
      tester,
      const BorrowPage(),
      overrides: [
        cacheBoxProvider.overrideWithValue(box),
        cacheConsentProvider.overrideWith((ref) async => false),
        borrowRepositoryProvider
            .overrideWithValue(_StaticBorrowRepository(_dashboard)),
      ],
    );
    await tester.pumpAndSettle();

    expect(box.store, isEmpty);
    expect(ReaderCache(box).readBorrow(), isNull);
  });
}
