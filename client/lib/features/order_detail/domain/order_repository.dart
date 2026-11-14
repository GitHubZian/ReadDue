import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class OrderRepository {
  Future<OrderDetail> fetchOrder(String orderId);
}

class MockOrderRepository implements OrderRepository {
  MockOrderRepository({MockBackend? backend})
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<OrderDetail> fetchOrder(String orderId) =>
      _backend.fetchOrder(orderId);
}
