import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/endpoints.dart';
import '../domain/deletion_request_repository.dart';

/// 真后端注销申请：GET 查最新状态，POST 提交（审批由馆员完成）。
class DioDeletionRequestRepository implements DeletionRequestRepository {
  DioDeletionRequestRepository(this._client);

  final ApiClient _client;

  @override
  Future<DeletionRequest?> fetchLatest() async {
    try {
      return await _client.get<DeletionRequest?>(
        Endpoints.meDeletionRequest,
        decode: deletionRequestFromJson,
      );
    } on ApiException catch (error) {
      // 契约约定无记录返回 null；后端若用 404 表达同一含义也按无记录处理。
      if (error.isNotFound) return null;
      rethrow;
    }
  }

  @override
  Future<DeletionRequest> submit({String? reason}) {
    final trimmed = reason?.trim();
    return _client.post<DeletionRequest>(
      Endpoints.meDeletionRequest,
      body: {
        if (trimmed != null && trimmed.isNotEmpty) 'reason': trimmed,
      },
      // 后端成功时可能返回空 body，此时按已进入审批处理。
      decode: (data) =>
          deletionRequestFromJson(data) ??
          const DeletionRequest(status: 'PENDING'),
    );
  }
}
