import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class ReservationRepository {
  Future<ReservationsPage> fetchReservations();

  Future<void> cancel(String reservationId);
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
}
