import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class ReservationRepository {
  Future<ReservationsPage> fetchReservations();

  Future<void> cancel(String reservationId);

  /// 当前活动预约数（排队中 + 待取书），用于底栏角标。
  Future<int> activeCount();
}

class MockReservationRepository implements ReservationRepository {
  MockReservationRepository({MockBackend? backend})
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<ReservationsPage> fetchReservations() => _backend.fetchReservations();

  @override
  Future<void> cancel(String reservationId) =>
      _backend.cancelReservation(reservationId);

  @override
  Future<int> activeCount() => _backend.activeReservationCount();
}
