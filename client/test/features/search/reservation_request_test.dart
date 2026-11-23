import 'dart:convert';
import 'dart:typed_data';

import 'package:client/core/network/api_client.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/core/network/endpoints.dart';
import 'package:client/core/storage/kv_store.dart';
import 'package:client/features/search/data/dio_search_repository.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late KvStore kv;

  setUp(() async {
    SharedPreferences.setMockInitialValues({
      'auth.token': 'test-token',
      'auth.userId': '1',
      'auth.name': '陈昊',
      'auth.studentNo': '2021001',
    });
    kv = KvStore(await SharedPreferences.getInstance());
  });

  DioSearchRepository repository(_Handler handler) {
    final dio = Dio(
      BaseOptions(
        baseUrl: 'http://127.0.0.1:8125/api',
        contentType: Headers.jsonContentType,
      ),
    );
    dio.httpClientAdapter = _FakeAdapter(handler);
    return DioSearchRepository(ApiClient(kv: kv, dio: dio));
  }

  test('预约成功：POST /v1/reservations 并解析 queuePosition', () async {
    final repo = repository((options) async {
      expect(options.path, Endpoints.reservations);
      expect(options.method, 'POST');
      expect(options.data, {'bookId': 6900000000000000002});
      return _json(200, {
        'id': '2900000000000000009',
        'bookId': '6900000000000000002',
        'bookTitle': '乡土中国·生育制度',
        'queuePosition': 3,
        'status': 'WAITING',
      });
    });

    expect(await repo.reserve('6900000000000000002'), 3);
  });

  test('预约冲突：422 原样映射为 ApiException', () async {
    final repo = repository((options) async {
      return _json(
        422,
        null,
        code: 'RULE_VIOLATION',
        message: '你已预约《算法导论》，请等待到书',
      );
    });

    await expectLater(
      repo.reserve('6900000000000000006'),
      throwsA(
        isA<ApiException>()
            .having((e) => e.statusCode, 'statusCode', 422)
            .having(
              (e) => e.message,
              'message',
              '你已预约《算法导论》，请等待到书',
            ),
      ),
    );
  });
}

ResponseBody _json(
  int status,
  Object? data, {
  Object code = 0,
  String message = 'ok',
}) {
  return ResponseBody.fromString(
    jsonEncode({'code': code, 'message': message, 'data': data}),
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
