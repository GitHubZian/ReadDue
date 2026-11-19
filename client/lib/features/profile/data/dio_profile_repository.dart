import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../domain/profile_repository.dart';

/// 真后端个人资料：/v1/me + 借阅/预约计数。
///
/// 后端暂未提供院系、学历与累计借阅统计，缺失字段留空或按当前借阅数展示，
/// 不在客户端编造。
class DioProfileRepository implements ProfileRepository {
  DioProfileRepository(this._client);

  final ApiClient _client;

  @override
  Future<ProfileData> fetchProfile() async {
    final results = await Future.wait<Object>([
      _client.get<Map<String, dynamic>>(
        Endpoints.me,
        decode: (data) => data as Map<String, dynamic>,
      ),
      _client.get<Map<String, dynamic>>(
        Endpoints.myBorrows,
        query: {'page': 1, 'size': 100},
        decode: (data) => data as Map<String, dynamic>,
      ),
      _client.get<Map<String, dynamic>>(
        Endpoints.myReservations,
        query: {'page': 1, 'size': 100},
        decode: (data) => data as Map<String, dynamic>,
      ),
      _summary(),
      _unread(),
    ]);
    final me = results[0] as Map<String, dynamic>;
    final borrows = results[1] as Map<String, dynamic>;
    final reservations = results[2] as Map<String, dynamic>;
    final summary = results[3] as Map<String, dynamic>;
    final unread = results[4] as int;

    final borrowItems = (borrows['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .toList();
    final overdueCount = _int(summary['overdue'], 0);
    final reservationItems =
        (reservations['items'] as List<dynamic>? ?? const [])
            .whereType<Map<String, dynamic>>()
            .toList();
    final activeReservations = reservationItems
        .where(
          (json) => const ['WAITING', 'READY'].contains(
            (json['status'] ?? '').toString(),
          ),
        )
        .length;
    final role = (me['role'] ?? 'READER').toString();
    final roleLabel = switch (role) {
      'ADMIN' => '管理员',
      'LIBRARIAN' => '馆员',
      _ => '读者',
    };

    return ProfileData(
      name: (me['name'] ?? '读者').toString(),
      studentNo: (me['studentNo'] ?? '').toString(),
      department: '',
      degree: roleLabel,
      creditLabel:
          (summary['creditLabel'] ?? (overdueCount > 0 ? '有逾期' : '信用良好'))
              .toString(),
      quotaUsed: _int(summary['borrowed'], borrowItems.length),
      quotaTotal: _int(summary['quota'], 10),
      borrowingCount: _int(summary['borrowed'], borrowItems.length),
      totalBorrowed: _int(summary['borrowed'], borrowItems.length),
      reservationCount: activeReservations,
      returnedThisTerm: _int(summary['returnedThisTerm'], 0),
      overdueCount: overdueCount,
      unreadCount: unread,
    );
  }

  Future<int> _unread() async {
    try {
      final data = await _client.get<Map<String, dynamic>>(
        Endpoints.notificationsUnread,
        decode: (data) => (data as Map<String, dynamic>?) ?? const {},
      );
      return _int(data['unread'], 0);
    } catch (_) {
      return 0;
    }
  }

  Future<Map<String, dynamic>> _summary() async {
    try {
      return await _client.get<Map<String, dynamic>>(
        Endpoints.meSummary,
        decode: (data) => (data as Map<String, dynamic>?) ?? const {},
      );
    } catch (_) {
      return const {};
    }
  }

  int _int(Object? value, int fallback) =>
      value is num ? value.toInt() : int.tryParse('$value') ?? fallback;

  @override
  Future<void> logout() async {
    await _client.post<void>(Endpoints.logout, decode: (_) {});
  }
}
