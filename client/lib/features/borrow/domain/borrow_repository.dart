import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class BorrowRepository {
  Future<BorrowDashboard> fetchDashboard();

  Future<DateTime> renew(String orderId);

  Future<void> returnBook(String orderId);
}

class MockBorrowRepository implements BorrowRepository {
  MockBorrowRepository({MockBackend? backend})
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<BorrowDashboard> fetchDashboard() => _backend.fetchDashboard();

  @override
  Future<DateTime> renew(String orderId) => _backend.renew(orderId);

  @override
  Future<void> returnBook(String orderId) => _backend.returnBook(orderId);
}
