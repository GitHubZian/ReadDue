import 'dart:convert';
import 'dart:typed_data';

import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/network/api_client.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/core/storage/kv_store.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

class _RecordingAdapter implements HttpClientAdapter {
  final List<RequestOptions> requests = [];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    requests.add(options);
    return ResponseBody.fromString(
      jsonEncode({
        'code': 'OK',
        'message': 'ok',
        'data': {'ok': true},
      }),
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  test('拉黑用户续借返回 ACCOUNT_BLACKLISTED', () async {
    final backend = MockBackend.instance..blacklisted = true;

    await expectLater(
      backend.renew('1900000000000000001'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.code, 'code', 'ACCOUNT_BLACKLISTED'),
      ),
    );
  });

  test('访问不存在的单据按 404 且不泄露存在性', () async {
    final backend = MockBackend.instance;

    await expectLater(
      backend.fetchOrder('9999999999999999999'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.statusCode, 'statusCode', 404)
            .having((e) => e.message, 'message', '单据不存在或无权查看'),
      ),
    );
  });

  test('写请求携带 Idempotency-Key，读请求不带', () async {
    SharedPreferences.setMockInitialValues({'auth.token': 'test-token'});
    final prefs = await SharedPreferences.getInstance();
    final kv = KvStore(prefs);
    final dio = Dio(BaseOptions(baseUrl: 'http://localhost/api/v1'));
    final adapter = _RecordingAdapter();
    dio.httpClientAdapter = adapter;
    final client = ApiClient(kv: kv, dio: dio);

    await client.post<Map<String, dynamic>>(
      '/borrows',
      body: const {'copyId': '1'},
      decode: (data) => data as Map<String, dynamic>,
    );
    await client.get<Map<String, dynamic>>(
      '/borrows',
      decode: (data) => data as Map<String, dynamic>,
    );

    expect(adapter.requests, hasLength(2));
    final post = adapter.requests.first;
    final get = adapter.requests.last;
    expect(post.headers['Idempotency-Key'], isNotEmpty);
    expect(post.headers['X-Trace-Id'], isNotEmpty);
    expect(post.headers['Authorization'], 'Bearer test-token');
    expect(get.headers.containsKey('Idempotency-Key'), isFalse);
  });
}
