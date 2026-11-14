import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/storage/kv_store.dart';
import '../domain/auth_repository.dart';

/// 校园统一身份（mock）：任意非空学号/工号 + 密码可登录，写入本地会话。
class MockAuthRepository implements AuthRepository {
  MockAuthRepository(this._kv);

  final KvStore _kv;

  @override
  Future<Session> login({
    required String studentNo,
    required String password,
    required bool remember,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 320));
    if (studentNo.trim().isEmpty || password.isEmpty) {
      throw const ApiException(
        code: 'INVALID_CREDENTIALS',
        message: '学号或密码错误',
        statusCode: 401,
      );
    }
    if (studentNo == 'locked') {
      throw const ApiException(
        code: 'ACCOUNT_LOCKED',
        message: '账号已锁定，请联系图书馆',
        statusCode: 423,
      );
    }
    final session = Session(
      token: 'mock-token-$studentNo',
      userId: '1900000000000000001',
      name: '林知远',
      studentNo: studentNo.trim(),
    );
    await _kv.saveSession(
      token: session.token,
      userId: session.userId,
      name: session.name,
      studentNo: session.studentNo,
      remember: remember,
    );
    return session;
  }

  @override
  Future<Session?> restore() async {
    final token = _kv.token;
    if (token == null || token.isEmpty) return null;
    return Session(
      token: token,
      userId: _kv.userId ?? '',
      name: _kv.name ?? '',
      studentNo: _kv.studentNo ?? '',
    );
  }

  @override
  Future<void> logout() => _kv.clearSession();
}
