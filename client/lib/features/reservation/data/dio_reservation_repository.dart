import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/reservation_repository.dart';

/// 真后端预约：/v1/reservations/self。
class DioReservationRepository implements ReservationRepository {
  DioReservationRepository(this._client);

  final ApiClient _client;

  @override
  Future<ReservationsPage> fetchReservations() async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myReservations,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final items = (page['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .toList();
    final ready = <ReservationItem>[];
    final completed = <ReservationItem>[];
    final cancelled = <ReservationItem>[];
    for (final json in items) {
      final status = (json['status'] ?? 'WAITING').toString();
      final expires = parseTime(json['expiresAt']);
      final remaining = expires == null
          ? 0
          : expires.difference(DateTime.now()).inHours.clamp(0, 999);
      final item = ReservationItem(
        id: (json['id'] ?? '').toString(),
        bookTitle: (json['bookTitle'] ?? '—').toString(),
        author: (json['bookAuthor'] ?? '').toString(),
        coverLabel: coverLabelOf((json['bookTitle'] ?? '书').toString()),
        coverTone: status == 'READY' ? 'mint' : 'slate',
        pickupCode: '凭校园卡',
        readyAt: parseTime(json['readyAt']),
        expiresAt: expires,
        remainingHours: remaining,
        queueNo: int.tryParse((json['queuePosition'] ?? 0).toString()) ?? 0,
        status: status,
      );
      if (status == 'READY') {
        ready.add(item);
      } else if (status == 'FULFILLED') {
        completed.add(item);
      } else if (status == 'CANCELLED' || status == 'EXPIRED') {
        cancelled.add(item);
      }
    }
    return ReservationsPage(
      ready: ready,
      completed: completed,
      cancelled: cancelled,
    );
  }

  @override
  Future<void> cancel(String reservationId) async {
    await _client.delete<void>(
      Endpoints.reservation(reservationId),
      decode: (_) {},
    );
  }

  @override
  Future<int> activeCount() async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myReservations,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    return (page['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .where((json) {
      final status = (json['status'] ?? '').toString();
      return status == 'WAITING' || status == 'READY';
    }).length;
  }
}
