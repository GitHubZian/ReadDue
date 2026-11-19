import 'api_client.dart';
import 'endpoints.dart';

/// 读者端跨页面复用的后端小查询：摘要、未读数、数值容错。
///
/// 失败时返回中性值，页面仍可用借阅/预约列表兜底，不阻塞首屏。
int asInt(Object? value, int fallback) =>
    value is num ? value.toInt() : int.tryParse('$value') ?? fallback;

Future<Map<String, dynamic>> fetchSummary(ApiClient client) async {
  try {
    return await client.get<Map<String, dynamic>>(
      Endpoints.meSummary,
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
  } catch (_) {
    return const {};
  }
}

Future<int> fetchUnread(ApiClient client) async {
  try {
    final data = await client.get<Map<String, dynamic>>(
      Endpoints.notificationsUnread,
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return asInt(data['unread'], 0);
  } catch (_) {
    return 0;
  }
}
