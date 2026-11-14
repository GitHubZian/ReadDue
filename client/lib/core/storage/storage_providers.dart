import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'cache_box.dart';
import 'kv_store.dart';

/// 在 main 中覆盖为已初始化的实例，页面与 repo 只依赖接口。
final kvStoreProvider = Provider<KvStore>(
  (ref) => throw UnimplementedError('kvStoreProvider 需在 main 中覆盖'),
);

final cacheBoxProvider = Provider<CacheBox>(
  (ref) => throw UnimplementedError('cacheBoxProvider 需在 main 中覆盖'),
);
