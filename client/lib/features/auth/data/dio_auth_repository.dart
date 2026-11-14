import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/storage/kv_store.dart';
import '../domain/auth_repository.dart';

/// 真后端认证：POST /v1/auth/login（userNo/password）。
class DioAuthRepository implements AuthRepository {
  DioAuthRepository({required ApiClient client, required KvStore kv})
      : _client = client,
        _kv = kv;

  final ApiClient _client;
  final KvStore _kv;

  @override
  Future<Session> login({
    required String studentNo,
    required String password,
    required bool remember,
  }) async {
    final session = await _client.post<Session>(
      Endpoints.login,
      body: {'userNo': studentNo.trim(), 'password': password},
      decode: (data) {
        final map = data as Map<String, dynamic>;
        final user = (map['user'] as Map<String, dynamic>?) ?? const {};
        return Session(
          token: (map['token'] ?? '').toString(),
          userId: (user['id'] ?? '').toString(),
          name: (user['name'] ?? '').toString(),
          studentNo: (user['studentNo'] ?? studentNo).toString(),
        );
      },
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
    try {
      final user = await _client.get<Map<String, dynamic>>(
        Endpoints.me,
        decode: (data) => (data as Map<String, dynamic>?) ?? const {},
      );
      final name = (user['name'] ?? _kv.name ?? '').toString();
      final studentNo = (user['studentNo'] ?? _kv.studentNo ?? '').toString();
      await _kv.saveSession(
        token: token,
        userId: (user['id'] ?? _kv.userId ?? '').toString(),
        name: name,
        studentNo: studentNo,
        remember: _kv.rememberMe,
      );
      return Session(
        token: token,
        userId: (user['id'] ?? _kv.userId ?? '').toString(),
        name: name,
        studentNo: studentNo,
      );
    } on ApiException catch (error) {
      if (error.statusCode == 401 || error.statusCode == 403) {
        await _kv.clearSession();
        return null;
      }
      // 断网时保留本地会话，进入离线态。
      return Session(
        token: token,
        userId: _kv.userId ?? '',
        name: _kv.name ?? '',
        studentNo: _kv.studentNo ?? '',
      );
    } catch (_) {
      return Session(
        token: token,
        userId: _kv.userId ?? '',
        name: _kv.name ?? '',
        studentNo: _kv.studentNo ?? '',
      );
    }
  }

  @override
  Future<void> logout() async {
    try {
      await _client.post<void>(
        Endpoints.logout,
        decode: (_) {},
      );
    } catch (_) {
      // 本地会话必须清掉，服务端吊销失败不阻塞退出。
    }
    await _kv.clearSession();
  }
}
