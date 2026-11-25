/// 注销申请的最新状态：PENDING / APPROVED / REJECTED。
class DeletionRequest {
  const DeletionRequest({required this.status, this.reason});

  final String status;

  /// 驳回原因，仅在 REJECTED 时由后端返回。
  final String? reason;
}

/// 解析注销申请 JSON；无有效 status 时返回 null。
DeletionRequest? deletionRequestFromJson(dynamic data) {
  if (data is! Map) return null;
  final status = (data['status'] ?? '').toString().trim();
  if (status.isEmpty) return null;
  // 后端驳回备注在 note；message 兼容简化响应。reason 是读者自己填的，不能当驳回原因。
  final rawReason = (data['note'] ?? data['message'])?.toString().trim() ?? '';
  return DeletionRequest(
    status: status,
    reason: rawReason.isEmpty ? null : rawReason,
  );
}

abstract class DeletionRequestRepository {
  /// 没有申请记录时返回 null。
  Future<DeletionRequest?> fetchLatest();

  Future<DeletionRequest> submit({String? reason});
}
