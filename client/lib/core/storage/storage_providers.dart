import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'cache_box.dart';
import 'kv_store.dart';
import 'reader_cache.dart';

/// 在 main 中覆盖为已初始化的实例，页面与 repo 只依赖接口。
final kvStoreProvider = Provider<KvStore>(
  (ref) => throw UnimplementedError('kvStoreProvider 需在 main 中覆盖'),
);

/// 未覆盖时返回一个未初始化的盒子：[CacheBox.read] 返回 null、写入为 no-op，
/// 便于只关心业务、不关心缓存的 widget 测试直接跑。
final cacheBoxProvider = Provider<CacheBox>((ref) => CacheBox());

final readerCacheProvider = Provider<ReaderCache>(
  (ref) => ReaderCache(ref.watch(cacheBoxProvider)),
);
