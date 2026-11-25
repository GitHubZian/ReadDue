import '../models/models.dart';
import 'cache_box.dart';

/// 把模型 toJson 的结果压成纯 JSON 值。
///
/// freezed 的 toJson 默认不展开嵌套模型，这里递归调用各层 toJson 并处理
/// DateTime，保证写入 Hive 后能用同一个 fromJson 读回。
Object? _plain(Object? value) {
  if (value == null || value is String || value is num || value is bool) {
    return value;
  }
  if (value is DateTime) return value.toIso8601String();
  if (value is List) return value.map(_plain).toList();
  if (value is Map) {
    return value.map(
      (key, item) => MapEntry(key.toString(), _plain(item)),
    );
  }
  // ignore: avoid_dynamic_calls
  final dynamic model = value;
  return _plain(model.toJson() as Object?);
}

/// 借阅列表与消息列表的精简缓存。
///
/// 只缓存页面渲染所需字段（模型自身 JSON），读取时用同一模型解析；
/// 是否允许读写由调用方先确认 `cacheConsentProvider`，本类不判断授权。
class ReaderCache {
  ReaderCache(this._box);

  final CacheBox _box;

  static const borrowKey = 'borrow_dashboard';
  static const notificationsKey = 'notifications_page';

  BorrowDashboard? readBorrow() {
    final raw = _box.read(borrowKey);
    if (raw is! Map) return null;
    try {
      return BorrowDashboard.fromJson(Map<String, dynamic>.from(raw));
    } on Object {
      return null;
    }
  }

  Future<void> writeBorrow(BorrowDashboard data) =>
      _box.write(borrowKey, _plain(data.toJson()));

  NotificationsPageData? readNotifications() {
    final raw = _box.read(notificationsKey);
    if (raw is! Map) return null;
    try {
      return NotificationsPageData.fromJson(Map<String, dynamic>.from(raw));
    } on Object {
      return null;
    }
  }

  Future<void> writeNotifications(NotificationsPageData data) =>
      _box.write(notificationsKey, _plain(data.toJson()));

  Future<void> clear() => _box.clear();
}
