import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../config/app_env.dart';
import '../network/endpoints.dart';
import '../network/network_providers.dart';

/// 服务端的「本地缓存授权」，供借阅/消息列表决定是否读写 reader_cache。
///
/// 读取失败视为未授权：宁可不缓存，也不在用户未同意时落盘。
/// mock 演示态没有真实授权接口，固定返回 true。
final cacheConsentProvider = FutureProvider<bool>((ref) async {
  if (ref.watch(appUseMockProvider)) return true;
  try {
    final data = await ref.watch(apiClientProvider).get<Map<String, dynamic>>(
          Endpoints.meConsents,
          decode: (raw) => Map<String, dynamic>.from(raw as Map),
        );
    return data['cacheConsent'] == true;
  } on Object {
    return false;
  }
});
