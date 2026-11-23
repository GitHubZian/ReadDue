import 'package:file_picker/file_picker.dart';

import 'api_client.dart';
import 'api_exception.dart';
import 'endpoints.dart';

/// 证据附件契约：最多 5 个、单个 ≤ 5MB，仅 PDF / JPG / PNG。
const kMaxEvidenceFiles = 5;
const kMaxEvidenceBytes = 5 * 1024 * 1024;
const kEvidenceExtensions = <String>['pdf', 'jpg', 'jpeg', 'png'];

typedef PickedEvidence = ({String name, String path});

/// 校验单个已选文件；合法返回 null，否则返回可展示的原因。
String? evidenceFileError(PlatformFile file) {
  final ext = file.extension?.toLowerCase() ??
      (file.name.contains('.') ? file.name.split('.').last.toLowerCase() : '');
  if (!kEvidenceExtensions.contains(ext)) return '仅支持 PDF / JPG / PNG';
  if (file.size > kMaxEvidenceBytes) return '单个文件不能超过 5MB';
  if (file.path == null || file.path!.isEmpty) {
    return '无法读取该文件，请重新选择';
  }
  return null;
}

/// 逐个上传到 /v1/files，返回后端文件 id（顺序与入参一致）。
Future<List<String>> uploadEvidence(
  ApiClient client,
  List<PickedEvidence> files,
) async {
  final ids = <String>[];
  for (final file in files) {
    final data = await client.postFile<Map<String, dynamic>>(
      Endpoints.fileUpload,
      filePath: file.path,
      fileName: file.name,
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    final id = (data['id'] ?? '').toString();
    if (id.isEmpty) {
      throw const ApiException(
        code: 'UPLOAD_FAILED',
        message: '文件上传失败，请重试',
      );
    }
    ids.add(id);
  }
  return ids;
}
