import 'dart:typed_data';

import '../../../core/models/grace_detail.dart';
import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

export '../../../core/models/grace_detail.dart'
    show GraceDetail, GraceEvidence, graceStatusLabel;

abstract class GraceRepository {
  Future<GracePageData> fetchPage(String orderId);

  Future<GraceSubmitResult> submit(GraceSubmitRequest request);

  /// 宽限详情：用于历史记录点开后回显申请天数/快照/证据。
  Future<GraceDetail> fetchDetail(String id);

  /// 下载证据原文件；失败（文件缺失/权限）由调用方提示「证据已失效」。
  Future<Uint8List> downloadEvidence(String evidenceId);
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

  @override
  Future<GraceDetail> fetchDetail(String id) => _backend.fetchGraceDetail(id);

  @override
  Future<Uint8List> downloadEvidence(String evidenceId) =>
      _backend.downloadGraceEvidence(evidenceId);
}
