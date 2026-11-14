import '../../../core/models/models.dart';

/// 登录/登出/恢复会话。后端补齐后换成 Dio 实现，页面与状态层不改。
abstract class AuthRepository {
  Future<Session> login({
    required String studentNo,
    required String password,
    required bool remember,
  });

  Future<Session?> restore();

  Future<void> logout();
}
