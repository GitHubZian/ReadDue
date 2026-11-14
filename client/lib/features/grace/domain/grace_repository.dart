import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class GraceRepository {
  Future<GracePageData> fetchPage(String orderId);

  Future<GraceSubmitResult> submit(GraceSubmitRequest request);
}

class MockGraceRepository implements GraceRepository {
  MockGraceRepository({MockBackend? backend})
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<GracePageData> fetchPage(String orderId) =>
      _backend.fetchGracePage(orderId);

  @override
  Future<GraceSubmitResult> submit(GraceSubmitRequest request) =>
      _backend.submitGrace(request);
}
