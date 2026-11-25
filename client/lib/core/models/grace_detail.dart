/// 宽限详情模型：纯 Dart，不参与 freezed 生成。
///
/// 状态文案与详情 Sheet 共用，避免各处中文不一致。
String graceStatusLabel(String status) => switch (status) {
      'APPROVED' => '已通过',
      'REJECTED' => '已驳回',
      _ => '审核中',
    };

class GraceEvidence {
  const GraceEvidence({
    required this.id,
    required this.name,
    required this.contentType,
    required this.size,
  });

  final String id;
  final String name;
  final String contentType;
  final int size;

  bool get isImage => contentType.startsWith('image/');

  String get sizeLabel {
    if (size >= 1024 * 1024) {
      return '${(size / (1024 * 1024)).toStringAsFixed(1)} MB';
    }
    if (size >= 1024) {
      return '${(size / 1024).toStringAsFixed(1)} KB';
    }
    return '$size B';
  }
}

/// 后端 `/v1/graces/{id}` 只有列表同款字段：
/// 申请天数、批准天数、创建时间、快照有效期与证据，没有驳回原因/审批备注。
class GraceDetail {
  const GraceDetail({
    required this.id,
    required this.applyNo,
    required this.borrowId,
    required this.sceneCode,
    required this.sceneLabel,
    required this.status,
    required this.requestedDays,
    required this.approvedDays,
    required this.createdAt,
    required this.snapshotExpiresAt,
    required this.evidence,
  });

  final String id;
  final String applyNo;
  final String borrowId;
  final String sceneCode;
  final String sceneLabel;
  final String status;
  final int requestedDays;
  final int? approvedDays;
  final DateTime createdAt;
  final DateTime? snapshotExpiresAt;
  final List<GraceEvidence> evidence;

  String get statusLabel => graceStatusLabel(status);

  bool get snapshotExpired {
    final at = snapshotExpiresAt;
    return at != null && DateTime.now().isAfter(at);
  }
}
