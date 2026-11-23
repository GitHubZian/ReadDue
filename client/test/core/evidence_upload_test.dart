import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_client.dart';
import 'package:client/core/network/evidence_upload.dart';
import 'package:client/core/storage/kv_store.dart';
import 'package:client/features/appeal/data/dio_appeal_repository.dart';
import 'package:client/features/grace/data/dio_grace_repository.dart';
import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Directory tempDir;
  late String pdfPath;
  late SharedPreferences prefs;

  setUp(() async {
    SharedPreferences.setMockInitialValues({'auth.token': 'test-token'});
    prefs = await SharedPreferences.getInstance();
    tempDir = await Directory.systemTemp.createTemp('rdu_upload_');
    final file = File('${tempDir.path}/proof.pdf');
    await file.writeAsBytes([0x25, 0x50, 0x44, 0x46]);
    pdfPath = file.path;
  });

  tearDown(() => tempDir.deleteSync(recursive: true));

  ApiClient apiClient(
      List<RequestOptions> requests, Map<String, dynamic> data) {
    final dio = Dio(
      BaseOptions(
        baseUrl: 'http://127.0.0.1:8125/api',
        contentType: Headers.jsonContentType,
      ),
    );
    dio.httpClientAdapter = _FakeAdapter((options) async {
      requests.add(options);
      return ResponseBody.fromString(
        jsonEncode({'code': 'OK', 'message': 'ok', 'data': data}),
        200,
        headers: {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      );
    });
    return ApiClient(kv: KvStore(prefs), dio: dio);
  }

  test('uploadEvidence 以 multipart 上传并回填文件 id', () async {
    final requests = <RequestOptions>[];
    final api = apiClient(requests, {
      'id': '4900000000000000001',
      'name': 'proof.pdf',
    });

    final ids = await uploadEvidence(api, [(name: 'proof.pdf', path: pdfPath)]);

    expect(ids, ['4900000000000000001']);
    expect(requests.single.path, '/v1/files');
    final form = requests.single.data as FormData;
    expect(form.files.single.key, 'file');
    expect(form.files.single.value.filename, 'proof.pdf');
  });

  test('evidenceFileError 校验类型、大小与可读路径', () {
    expect(
      evidenceFileError(
        PlatformFile(name: 'a.txt', size: 1, path: pdfPath),
      ),
      '仅支持 PDF / JPG / PNG',
    );
    expect(
      evidenceFileError(
        PlatformFile(name: 'a.pdf', size: kMaxEvidenceBytes + 1, path: pdfPath),
      ),
      '单个文件不能超过 5MB',
    );
    expect(
      evidenceFileError(PlatformFile(name: 'a.PDF', size: 10, path: pdfPath)),
      isNull,
    );
    expect(
      evidenceFileError(PlatformFile(name: 'a.pdf', size: 10)),
      '无法读取该文件，请重新选择',
    );
  });

  test('宽限提交把已上传文件 id 作为 evidenceIds 发给后端', () async {
    final requests = <RequestOptions>[];
    final api = apiClient(requests, {'code': 'G-1', 'status': 'PENDING'});

    await DioGraceRepository(api).submit(
      const GraceSubmitRequest(
        orderId: '100',
        sceneCode: 'NORMAL',
        description: '实习申请延后归还',
        attachments: ['11', '12'],
      ),
    );

    final body = requests.single.data as Map<String, dynamic>;
    expect(body['evidenceIds'], [11, 12]);
    expect(body['evidenceText'], '实习申请延后归还');
  });

  test('申诉提交把已上传文件 id 作为 evidenceIds 发给后端', () async {
    final requests = <RequestOptions>[];
    final api = apiClient(requests, {'code': 'A-2042', 'status': 'OPEN'});

    await DioAppealRepository(api).submit(
      const AppealCreateRequest(
        orderId: '200',
        type: 'OVERDUE_TIER',
        description: '逾期判定与事实不符，请核对借阅记录',
        attachments: ['21'],
      ),
    );

    final body = requests.single.data as Map<String, dynamic>;
    expect(body['evidenceIds'], [21]);
    expect(body['reason'], '逾期判定与事实不符，请核对借阅记录');
  });
}

class _FakeAdapter implements HttpClientAdapter {
  _FakeAdapter(this._handler);

  final Future<ResponseBody> Function(RequestOptions options) _handler;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    // 消费请求体，确保 MultipartFile 打开的文件句柄被关闭。
    await requestStream?.drain<void>();
    return _handler(options);
  }

  @override
  void close({bool force = false}) {}
}
