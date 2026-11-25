import '../domain/deletion_request_repository.dart';

/// 离线演示：注销申请只在本进程内流转，不触碰后端。
class MockDeletionRequestRepository implements DeletionRequestRepository {
  DeletionRequest? _latest;

  @override
  Future<DeletionRequest?> fetchLatest() async => _latest;

  @override
  Future<DeletionRequest> submit({String? reason}) async {
    _latest = const DeletionRequest(status: 'PENDING');
    return _latest!;
  }
}
