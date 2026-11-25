import 'package:client/core/storage/kv_store.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('未勾选“记住此设备”时只留内存，不落盘且冷启动需重登', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final kv = KvStore(prefs);

    await kv.saveSession(
      token: 'token-1',
      userId: 'u1',
      name: '林知远',
      studentNo: '20230114',
      remember: false,
    );

    expect(kv.token, 'token-1');
    expect(kv.userId, 'u1');
    expect(kv.name, '林知远');
    expect(kv.rememberMe, isFalse);
    expect(prefs.getString('auth.token'), isNull);
    expect(prefs.getString('auth.userId'), isNull);
    expect(prefs.getString('auth.name'), isNull);
    expect(prefs.getString('auth.studentNo'), isNull);
    expect(prefs.containsKey('auth.remember'), isFalse);

    // 模拟冷启动：新的 KvStore 没有进程内字段。
    final coldStart = KvStore(prefs);
    expect(coldStart.token, isNull);
    expect(coldStart.rememberMe, isTrue);
  });

  test('勾选“记住此设备”时持久化，冷启动可恢复', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final kv = KvStore(prefs);

    await kv.saveSession(
      token: 'token-2',
      userId: 'u2',
      name: '陈昊',
      studentNo: '2021001',
    );

    final coldStart = KvStore(prefs);
    expect(coldStart.token, 'token-2');
    expect(coldStart.name, '陈昊');
    expect(coldStart.rememberMe, isTrue);
  });

  test('先记住后取消勾选登录会清掉旧落盘会话', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final kv = KvStore(prefs);

    await kv.saveSession(
      token: 'token-old',
      userId: 'u1',
      name: '林知远',
      studentNo: '20230114',
    );
    await kv.saveSession(
      token: 'token-new',
      userId: 'u1',
      name: '林知远',
      studentNo: '20230114',
      remember: false,
    );

    expect(prefs.getString('auth.token'), isNull);
    expect(kv.token, 'token-new');
    expect(KvStore(prefs).token, isNull);
  });

  test('退出登录同时清理内存与落盘会话', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final kv = KvStore(prefs);

    await kv.saveSession(
      token: 'token-3',
      userId: 'u3',
      name: '周宁',
      studentNo: '2022002',
      remember: false,
    );
    await kv.clearSession();

    expect(kv.token, isNull);
    expect(kv.rememberMe, isTrue);
    expect(prefs.getString('auth.token'), isNull);
    expect(prefs.containsKey('auth.remember'), isFalse);
  });
}
