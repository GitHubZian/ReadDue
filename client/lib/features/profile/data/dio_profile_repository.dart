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
    final me = await _client.get<Map<String, dynamic>>(
      Endpoints.me,
      decode: (data) => data as Map<String, dynamic>,
    );
    final borrows = await _client.get<Map<String, dynamic>>(
      Endpoints.myBorrows,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final reservations = await _client.get<Map<String, dynamic>>(
      Endpoints.myReservations,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );

    final borrowItems = (borrows['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .toList();
    final overdueCount = borrowItems
        .where(
          (json) => (int.tryParse('${json['overdueDays'] ?? 0}') ?? 0) > 0,
        )
        .length;
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
      creditLabel: overdueCount > 0 ? '有逾期' : '信用良好',
      quotaUsed: borrowItems.length,
      quotaTotal: 10,
      borrowingCount: borrowItems.length,
      totalBorrowed: borrowItems.length,
      reservationCount: activeReservations,
      returnedThisTerm: 0,
      overdueCount: overdueCount,
      unreadCount: 0,
    );
  }

  @override
  Future<void> logout() async {
    await _client.post<void>(Endpoints.logout, decode: (_) {});
  }
}
