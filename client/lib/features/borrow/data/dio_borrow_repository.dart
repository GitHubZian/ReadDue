import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_api.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/borrow_repository.dart';

/// 真后端：C1 借阅概览（/v1/borrows/self + /v1/reservations/self）。
class DioBorrowRepository implements BorrowRepository {
  DioBorrowRepository(this._client);

  final ApiClient _client;

  Future<List<BorrowItem>> _borrows() async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myBorrows,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final items = (page['items'] as List<dynamic>? ?? const []);
    return items
        .whereType<Map<String, dynamic>>()
        .map((json) => borrowItemFromJson(json))
        .toList();
  }

  Future<List<ReadyBook>> _readyBooks() async {
    try {
      final page = await _client.get<Map<String, dynamic>>(
        Endpoints.myReservations,
        query: {'page': 1, 'size': 100},
        decode: (data) => data as Map<String, dynamic>,
      );
      final items = (page['items'] as List<dynamic>? ?? const []);
      return items.whereType<Map<String, dynamic>>().where((json) {
        return (json['status'] ?? '').toString() == 'READY';
      }).map((json) {
        final expires = parseTime(json['expiresAt']);
        final remaining = expires == null
            ? 0
            : expires.difference(DateTime.now()).inHours.clamp(0, 999);
        return ReadyBook(
          reservationId: (json['id'] ?? '').toString(),
          title: (json['bookTitle'] ?? '—').toString(),
          author: (json['bookAuthor'] ?? '').toString(),
          pickupPoint: '一楼服务台',
          pickupCode: '凭校园卡',
          expiresAt: expires ?? DateTime.now(),
          remainingHours: remaining,
          queueNo: int.tryParse((json['queuePosition'] ?? 1).toString()) ?? 1,
        );
      }).toList();
    } catch (_) {
      return const [];
    }
  }

  @override
  Future<BorrowDashboard> fetchDashboard() async {
    final results = await Future.wait<Object>([
      _borrows(),
      _readyBooks(),
      fetchSummary(_client),
      fetchUnread(_client),
    ]);
    final items = results[0] as List<BorrowItem>;
    final ready = results[1] as List<ReadyBook>;
    final summary = results[2] as Map<String, dynamic>;
    final unread = results[3] as int;
    final now = DateTime.now();
    final overdue = items.where((item) => item.overdueDays > 0).length;
    final dueSoon = items
        .where((item) =>
            item.overdueDays == 0 && item.daysLeft >= 0 && item.daysLeft <= 3)
        .length;
    final sorted = [...items]..sort((a, b) => a.dueAt.compareTo(b.dueAt));
    return BorrowDashboard(
      summary: BorrowSummary(
        borrowing: asInt(summary['borrowed'], items.length),
        dueSoon: asInt(summary['dueIn3Days'], dueSoon),
        overdue: asInt(summary['overdue'], overdue),
        returnedThisTerm: asInt(summary['returnedThisTerm'], 0),
        creditLabel: (summary['creditLabel'] ?? '信用良好').toString(),
        quotaUsed: asInt(summary['borrowed'], items.length),
        quotaTotal: asInt(summary['quota'], 10),
      ),
      dueSoon: sorted,
      readyBooks: ready,
      feeNote: '逾期费用按天计费 · 归还时一次结清',
      lastSyncAt: now,
      unreadCount: unread,
    );
  }

  @override
  Future<DateTime> renew(String orderId) async {
    final data = await _client.post<Map<String, dynamic>>(
      Endpoints.renewBorrow(orderId),
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return parseTime(data['dueAt']) ?? DateTime.now();
  }

  @override
  Future<void> returnBook(String orderId) async {
    throw const ApiException(
      code: 'RETURN_AT_DESK',
      message: '请到服务台或自助借还机归还，扫码后自动结算',
      statusCode: 422,
    );
  }
}
