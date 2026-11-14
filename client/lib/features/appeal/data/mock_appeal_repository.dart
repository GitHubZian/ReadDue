import '../../../core/mock/mock_backend.dart';
import '../../../core/models/models.dart';
import '../domain/appeal_repository.dart';

class MockAppealRepository implements AppealRepository {
  MockAppealRepository([MockBackend? backend])
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<AppealsPageData> fetchAppeals() => _backend.fetchAppeals();

  @override
  Future<List<AppealType>> fetchTypes() => _backend.appealTypes();

  @override
  Future<List<BorrowItem>> fetchOrderOptions() async {
    final dashboard = await _backend.fetchDashboard();
    return dashboard.dueSoon;
  }

  @override
  Future<AppealCreateResult> submit(AppealCreateRequest request) =>
      _backend.submitAppeal(request);
}
