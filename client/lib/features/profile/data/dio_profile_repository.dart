import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_api.dart';
import '../domain/profile_repository.dart';

/// 真后端个人资料：/v1/me（含 department/major/grade/totalBorrowed）+ 借阅/预约计数。
///
/// 资料字段缺失时按空串展示；累计借阅取 /me 的 totalBorrowed（含已归还）。
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
      fetchSummary(_client),
      fetchUnread(_client),
    ]);
    final me = results[0] as Map<String, dynamic>;
    final borrows = results[1] as Map<String, dynamic>;
    final reservations = results[2] as Map<String, dynamic>;
    final summary = results[3] as Map<String, dynamic>;
    final unread = results[4] as int;

    final borrowItems = (borrows['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .toList();
    final overdueCount = asInt(summary['overdue'], 0);
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

    return ProfileData(
      name: (me['name'] ?? '读者').toString(),
      studentNo: (me['studentNo'] ?? '').toString(),
      department: (me['department'] ?? '').toString(),
      major: (me['major'] ?? '').toString(),
      grade: (me['grade'] ?? '').toString(),
      creditLabel:
          (summary['creditLabel'] ?? (overdueCount > 0 ? '有逾期' : '信用良好'))
              .toString(),
      quotaUsed: asInt(summary['borrowed'], borrowItems.length),
      quotaTotal: asInt(summary['quota'], 10),
      borrowingCount: asInt(summary['borrowed'], borrowItems.length),
      totalBorrowed: asInt(me['totalBorrowed'], 0),
      reservationCount: activeReservations,
      returnedThisTerm: asInt(summary['returnedThisTerm'], 0),
      overdueCount: overdueCount,
      unreadCount: unread,
    );
  }

  @override
  Future<void> logout() async {
    await _client.post<void>(Endpoints.logout, decode: (_) {});
  }
}
