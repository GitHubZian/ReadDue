import 'dart:convert';
import 'dart:typed_data';

import 'package:client/core/network/api_client.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/core/network/endpoints.dart';
import 'package:client/core/storage/kv_store.dart';
import 'package:client/features/profile/data/dio_profile_repository.dart';
import 'package:client/features/profile/domain/profile_validation.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('资料编辑前端校验', () {
    test('姓名 1-20 个字符', () {
      expect(validateProfileName('   '), '请输入姓名');
      expect(validateProfileName('林'), isNull);
      expect(validateProfileName('a' * 21), '姓名最多 20 个字符');
    });

    test('手机号可留空，填写时须为 1 开头的 11 位数字', () {
      expect(validateProfilePhone(''), isNull);
      expect(validateProfilePhone('1380013800'), '请输入 1 开头的 11 位手机号');
      expect(validateProfilePhone('23800138000'), '请输入 1 开头的 11 位手机号');
      expect(validateProfilePhone('13800138000'), isNull);
    });

    test('邮箱可留空，填写时需同时包含 @ 与 .', () {
      expect(validateProfileEmail(''), isNull);
      expect(validateProfileEmail('lin.example.edu'), '邮箱需包含 @ 与 .');
      expect(validateProfileEmail('lin@example'), '邮箱需包含 @ 与 .');
      expect(validateProfileEmail('lin@example.com'), isNull);
    });
  });

  group('PUT /v1/me', () {
    test('提交姓名/手机/邮箱并解析返回', () async {
      Map<String, dynamic>? body;
      final repo = await _repository((options) {
        if (options.method == 'PUT' && options.path == Endpoints.me) {
          body = Map<String, dynamic>.from(options.data as Map);
          return _ok({
            'name': '陈昊',
            'phone': '13900001111',
            'email': 'chen@example.edu.cn',
          });
        }
        return _ok(<String, dynamic>{});
      });

      final contact = await repo.updateProfile(
        name: '陈昊',
        phone: '13900001111',
        email: 'chen@example.edu.cn',
      );

      expect(body, {
        'name': '陈昊',
        'phone': '13900001111',
        'email': 'chen@example.edu.cn',
      });
      expect(contact.name, '陈昊');
      expect(contact.phone, '13900001111');
      expect(contact.email, 'chen@example.edu.cn');
    });

    test('422 时透传服务端 message', () async {
      final repo = await _repository(
        (_) => ResponseBody.fromString(
          jsonEncode({
            'code': 'VALIDATION_ERROR',
            'message': '手机号格式不正确',
          }),
          422,
          headers: {
            Headers.contentTypeHeader: [Headers.jsonContentType],
          },
        ),
      );

      await expectLater(
        repo.updateProfile(name: '陈昊', phone: '123', email: 'a@b.com'),
        throwsA(
          isA<ApiException>()
              .having((error) => error.message, 'message', '手机号格式不正确')
              .having((error) => error.isValidation, 'isValidation', true),
        ),
      );
    });
  });
}

ResponseBody _ok(Object? data) => ResponseBody.fromString(
      jsonEncode({'code': 'OK', 'message': 'ok', 'data': data}),
      200,
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );

Future<DioProfileRepository> _repository(
  ResponseBody Function(RequestOptions options) handler,
) async {
  SharedPreferences.setMockInitialValues({'auth.token': 'test-token'});
  final prefs = await SharedPreferences.getInstance();
  final dio = Dio(
    BaseOptions(
      baseUrl: 'http://127.0.0.1:8125/api',
      contentType: Headers.jsonContentType,
    ),
  );
  dio.httpClientAdapter = _FakeAdapter(handler);
  return DioProfileRepository(ApiClient(kv: KvStore(prefs), dio: dio));
}

class _FakeAdapter implements HttpClientAdapter {
  _FakeAdapter(this._handler);

  final ResponseBody Function(RequestOptions options) _handler;

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
