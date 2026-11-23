import 'dart:convert';
import 'dart:typed_data';

import 'package:client/core/network/api_client.dart';
import 'package:client/core/network/endpoints.dart';
import 'package:client/core/storage/kv_store.dart';
import 'package:client/features/profile/data/dio_profile_repository.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  Future<DioProfileRepository> repository(
    Map<String, dynamic> me,
  ) async {
    SharedPreferences.setMockInitialValues({'auth.token': 'test-token'});
    final prefs = await SharedPreferences.getInstance();
    final dio = Dio(
      BaseOptions(
        baseUrl: 'http://127.0.0.1:8125/api',
        contentType: Headers.jsonContentType,
      ),
    );
    dio.httpClientAdapter = _FakeAdapter((options) {
      final data = switch (options.path) {
        Endpoints.me => me,
        Endpoints.myBorrows => {
            'items': [
              {'status': 'BORROWED'},
            ],
          },
        Endpoints.myReservations => {'items': <dynamic>[]},
        Endpoints.meSummary => {
            'borrowed': 3,
            'quota': 5,
            'overdue': 1,
            'returnedThisTerm': 9,
            'creditLabel': '有逾期',
          },
        Endpoints.notificationsUnread => {'unread': 2},
        _ => <String, dynamic>{},
      };
      return ResponseBody.fromString(
        jsonEncode({'code': 'OK', 'message': 'ok', 'data': data}),
        200,
        headers: {
          Headers.contentTypeHeader: [Headers.jsonContentType],
        },
      );
    });
    return DioProfileRepository(ApiClient(kv: KvStore(prefs), dio: dio));
  }

  test('/me 解析 department/major/grade 与字符串 totalBorrowed', () async {
    final repo = await repository({
      'name': '陈昊',
      'studentNo': '2021001',
      'department': '计算机学院',
      'major': '软件工程',
      'grade': '2021',
      'totalBorrowed': '7',
    });

    final profile = await repo.fetchProfile();

    expect(profile.department, '计算机学院');
    expect(profile.major, '软件工程');
    expect(profile.grade, '2021');
    expect(profile.totalBorrowed, 7);
    expect(profile.borrowingCount, 3);
  });

  test('/me 缺资料字段时为空串/0，不按当前借阅数兜底', () async {
    final repo = await repository({'name': '陈昊', 'studentNo': '2021001'});

    final profile = await repo.fetchProfile();

    expect(profile.department, '');
    expect(profile.major, '');
    expect(profile.grade, '');
    expect(profile.totalBorrowed, 0);
    expect(profile.borrowingCount, 3);
  });
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
