import 'dart:convert';
import 'dart:typed_data';

import 'package:client/core/network/api_client.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/core/network/endpoints.dart';
import 'package:client/core/storage/kv_store.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late SharedPreferences prefs;
  late KvStore kv;

  setUp(() async {
    SharedPreferences.setMockInitialValues({
      'auth.token': 'old-token',
      'auth.userId': '1',
      'auth.name': '陈昊',
      'auth.studentNo': '2021001',
    });
    prefs = await SharedPreferences.getInstance();
    kv = KvStore(prefs);
  });

  ApiClient client(_Handler handler) {
    final dio = Dio(
      BaseOptions(
        baseUrl: 'http://127.0.0.1:8125/api',
        contentType: Headers.jsonContentType,
      ),
    );
    dio.httpClientAdapter = _FakeAdapter(handler);
    return ApiClient(kv: kv, dio: dio);
  }

  test('401 触发 refresh 后重放原请求', () async {
    var refreshCount = 0;
    var protectedCount = 0;
    final api = client((options) async {
      if (options.path == Endpoints.refresh) {
        refreshCount++;
        expect(options.headers['Authorization'], 'Bearer old-token');
        return _json(200, {
          'token': 'new-token',
          'expiresIn': '7200',
          'user': {'id': '1', 'name': '陈昊', 'studentNo': '2021001'},
        });
      }
      protectedCount++;
      if (protectedCount == 1) {
        return _json(401, null);
      }
      expect(options.headers['Authorization'], 'Bearer new-token');
      return _json(200, {'ok': true});
    });

    final result = await api.get<Map<String, dynamic>>(
      '/v1/protected',
      decode: (data) => data as Map<String, dynamic>,
    );

    expect(result['ok'], isTrue);
    expect(refreshCount, 1);
    expect(protectedCount, 2);
    expect(kv.token, 'new-token');
  });

  test('refresh 失败清空会话并回调未登录', () async {
    var expired = 0;
    final api = client((options) async {
      if (options.path == Endpoints.refresh) return _json(401, null);
      return _json(401, null);
    })
      ..onSessionExpired = () async => expired++;

    await expectLater(
      api.get<void>('/v1/protected', decode: (_) {}),
      throwsA(
        isA<ApiException>().having((e) => e.statusCode, 'statusCode', 401),
      ),
    );
    expect(kv.token, isNull);
    expect(expired, 1);
  });

  test('并发 401 只刷新一次，两个请求都重放', () async {
    var refreshCount = 0;
    final api = client((options) async {
      if (options.path == Endpoints.refresh) {
        refreshCount++;
        await Future<void>.delayed(const Duration(milliseconds: 20));
        return _json(200, {
          'token': 'new-token',
          'user': {'id': '1', 'name': '陈昊', 'studentNo': '2021001'},
        });
      }
      if (options.headers['Authorization'] == 'Bearer old-token') {
        return _json(401, null);
      }
      return _json(200, {'ok': options.path});
    });

    final results = await Future.wait([
      api.get<Map<String, dynamic>>(
        '/v1/a',
        decode: (data) => data as Map<String, dynamic>,
      ),
      api.get<Map<String, dynamic>>(
        '/v1/b',
        decode: (data) => data as Map<String, dynamic>,
      ),
    ]);

    expect(refreshCount, 1);
    expect(results[0]['ok'], '/v1/a');
    expect(results[1]['ok'], '/v1/b');
    expect(kv.token, 'new-token');
  });
}

ResponseBody _json(int status, Object? data) {
  return ResponseBody.fromString(
    jsonEncode({
      'code': status == 200 ? 0 : 'NOT_LOGIN',
      'message': status == 200 ? 'ok' : '登录已过期',
      'data': data,
    }),
    status,
    headers: {
      Headers.contentTypeHeader: [Headers.jsonContentType],
    },
  );
}

typedef _Handler = Future<ResponseBody> Function(RequestOptions options);

class _FakeAdapter implements HttpClientAdapter {
  _FakeAdapter(this._handler);

  final _Handler _handler;

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    await requestStream?.drain<void>();
    return _handler(options);
  }

  @override
  void close({bool force = false}) {}
}
