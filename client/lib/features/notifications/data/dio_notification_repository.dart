import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/notification_copy.dart';
import '../domain/notification_repository.dart';

/// 真后端：C6 消息中心（/v1/notifications + 免打扰 + 渠道）。
class DioNotificationRepository implements NotificationRepository {
  DioNotificationRepository(this._client);

  final ApiClient _client;

  @override
  Future<NotificationsPageData> fetchNotifications() async {
    final results = await Future.wait<Object>([
      _page(),
      _unread(),
      _channels(),
      _quietHours(),
    ]);
    final items = results[0] as List<AppNotification>;
    final unread = results[1] as int;
    return NotificationsPageData(
      items: items,
      unreadCount: unread,
      quietHours: results[3] as QuietHours,
      channels: results[2] as NotificationChannels,
    );
  }

  Future<List<AppNotification>> _page() async {
    final results = await Future.wait<Object>([
      _client.get<Map<String, dynamic>>(
        Endpoints.notifications,
        query: {'page': 1, 'size': 100},
        decode: (data) => data as Map<String, dynamic>,
      ),
      _borrowSnapshots(),
    ]);
    final page = results[0] as Map<String, dynamic>;
    final snapshots = results[1] as Map<String, _BorrowSnapshot>;
    final items = (page['items'] as List<dynamic>? ?? const []);
    return items.whereType<Map<String, dynamic>>().map((json) {
      final rawTitle = (json['title'] ?? '').toString();
      final level = (json['level'] ?? '').toString();
      final orderId = (json['orderId'] ?? '').toString();
      final snapshot = snapshots[orderId];
      final copy = normalizeNotificationCopy(
        title: rawTitle,
        body: (json['body'] ?? '').toString(),
        overdueDays: snapshot?.overdueDays ?? 0,
      );
      final type = _typeOf(copy.title, level);
      return AppNotification(
        id: (json['id'] ?? '').toString(),
        type: type,
        title: copy.title,
        body: copy.body,
        createdAt: parseTime(json['sentAt']) ?? DateTime.now(),
        read: json['read'] == true,
        category: type == 'system' ? 'system' : 'borrow',
      );
    }).toList();
  }

  /// 借阅状态只用于纠正通知口径，失败不阻塞消息列表。
  Future<Map<String, _BorrowSnapshot>> _borrowSnapshots() async {
    try {
      final page = await _client.get<Map<String, dynamic>>(
        Endpoints.myBorrows,
        query: {'page': 1, 'size': 50},
        decode: (data) => data as Map<String, dynamic>,
      );
      final snapshots = <String, _BorrowSnapshot>{};
      for (final json
          in (page['items'] as List<dynamic>? ?? const [])
              .whereType<Map<String, dynamic>>()) {
        final id = (json['id'] ?? '').toString();
        if (id.isEmpty) continue;
        snapshots[id] = _BorrowSnapshot(
          overdueDays:
              int.tryParse((json['overdueDays'] ?? 0).toString()) ?? 0,
        );
      }
      return snapshots;
    } catch (_) {
      return const {};
    }
  }

  String _typeOf(String title, String level) {
    if (title.contains('逾期')) return 'overdue';
    if (title.contains('宽限')) return 'grace';
    if (title.contains('到馆')) return 'reservation';
    if (level.contains('P2')) return 'system';
    return 'system';
  }

  Future<int> _unread() async {
    try {
      final data = await _client.get<Map<String, dynamic>>(
        Endpoints.notificationsUnread,
        decode: (data) => (data as Map<String, dynamic>?) ?? const {},
      );
      final value = data['unread'];
      return value is num ? value.toInt() : int.tryParse('$value') ?? 0;
    } catch (_) {
      return 0;
    }
  }

  Future<NotificationChannels> _channels() async {
    final data = await _client.get<Map<String, dynamic>>(
      Endpoints.meChannels,
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return NotificationChannels(
      inbox: data['inbox'] != false,
      email: data['email'] != false,
    );
  }

  Future<QuietHours> _quietHours() async {
    final data = await _client.get<Map<String, dynamic>>(
      Endpoints.meQuietHours,
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return QuietHours(
      enabled: data['enabled'] != false,
      start: (data['start'] ?? '22:00').toString(),
      end: (data['end'] ?? '08:00').toString(),
    );
  }

  @override
  Future<void> markRead(String id) async {
    await _client.put<Map<String, dynamic>>(
      Endpoints.notificationsRead,
      body: {
        'ids': [id],
      },
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
  }

  @override
  Future<void> markAllRead() async {
    await _client.put<Map<String, dynamic>>(
      Endpoints.notificationsRead,
      body: {'all': true},
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
  }

  @override
  Future<QuietHours> setQuietHours(bool enabled) async {
    final current = await _quietHours();
    final data = await _client.put<Map<String, dynamic>>(
      Endpoints.meQuietHours,
      body: {
        'enabled': enabled,
        'start': current.start,
        'end': current.end,
      },
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return QuietHours(
      enabled: data['enabled'] != false,
      start: (data['start'] ?? current.start).toString(),
      end: (data['end'] ?? current.end).toString(),
    );
  }

  @override
  Future<NotificationChannels> setChannels({bool? email, bool? push}) async {
    final current = await _channels();
    final data = await _client.put<Map<String, dynamic>>(
      Endpoints.meChannels,
      body: {'email': email ?? current.email},
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    final channels = (data['channels'] as Map<String, dynamic>?) ??
        const <String, dynamic>{};
    return NotificationChannels(
      inbox: channels['inbox'] != false,
      email: channels['email'] != false,
    );
  }
}

class _BorrowSnapshot {
  const _BorrowSnapshot({required this.overdueDays});

  final int overdueDays;
}
