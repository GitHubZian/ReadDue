import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/network_providers.dart';
import '../../../core/storage/storage_providers.dart';
import '../data/dio_auth_repository.dart';
import '../data/mock_auth_repository.dart';
import '../domain/auth_repository.dart';

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthState {
  const AuthState({
    this.status = AuthStatus.unknown,
    this.session,
    this.loading = false,
    this.error,
    this.failedAttempts = 0,
    this.lockedUntil,
  });

  final AuthStatus status;
  final Session? session;
  final bool loading;
  final String? error;
  final int failedAttempts;
  final DateTime? lockedUntil;

  bool get locked =>
      lockedUntil != null && lockedUntil!.isAfter(DateTime.now());

  AuthState copyWith({
    AuthStatus? status,
    Session? session,
    bool? loading,
    String? error,
    bool clearError = false,
    int? failedAttempts,
    DateTime? lockedUntil,
    bool clearLock = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      session: session ?? this.session,
      loading: loading ?? this.loading,
      error: clearError ? null : (error ?? this.error),
      failedAttempts: failedAttempts ?? this.failedAttempts,
      lockedUntil: clearLock ? null : (lockedUntil ?? this.lockedUntil),
    );
  }
}

class AuthController extends StateNotifier<AuthState> {
  AuthController(this._repository) : super(const AuthState());

  final AuthRepository _repository;

  Future<void> restore() async {
    final session = await _repository.restore();
    state = session == null
        ? const AuthState(status: AuthStatus.unauthenticated)
        : AuthState(status: AuthStatus.authenticated, session: session);
  }

  Future<void> login({
    required String studentNo,
    required String password,
    required bool remember,
  }) async {
    if (state.locked) {
      state = state.copyWith(error: '尝试次数过多，请稍后重试');
      return;
    }
    state = state.copyWith(loading: true, clearError: true);
    try {
      final session = await _repository.login(
        studentNo: studentNo,
        password: password,
        remember: remember,
      );
      state = AuthState(status: AuthStatus.authenticated, session: session);
    } on ApiException catch (error) {
      final attempts = state.failedAttempts + 1;
      final lock = attempts >= 5
          ? DateTime.now().add(const Duration(minutes: 15))
          : null;
      state = state.copyWith(
        loading: false,
        error: messageForError(error),
        failedAttempts: lock == null ? attempts : 0,
        lockedUntil: lock,
        clearLock: lock == null,
      );
    } catch (_) {
      state = state.copyWith(
        loading: false,
        error: '登录失败，请稍后重试',
      );
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    state = const AuthState(status: AuthStatus.unauthenticated);
  }

  /// refresh 失败且本地会话已清空：切回未登录，路由守卫会跳登录页。
  void sessionExpired() {
    if (state.status == AuthStatus.unauthenticated) return;
    state = const AuthState(status: AuthStatus.unauthenticated);
  }
}

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final kv = ref.watch(kvStoreProvider);
  if (ref.watch(appUseMockProvider)) return MockAuthRepository(kv);
  return DioAuthRepository(client: ref.watch(apiClientProvider), kv: kv);
});

final authControllerProvider = StateNotifierProvider<AuthController, AuthState>(
  (ref) {
    final controller = AuthController(ref.watch(authRepositoryProvider));
    ref.read(apiClientProvider).onSessionExpired = () async {
      controller.sessionExpired();
    };
    return controller;
  },
);

/// 让 GoRouter 在登录态变化时重新执行 redirect。
class AuthRefreshNotifier extends ChangeNotifier {
  void refresh() => notifyListeners();
}

final authRefreshProvider = Provider<AuthRefreshNotifier>((ref) {
  final notifier = AuthRefreshNotifier();
  final subscription = ref.listen<AuthState>(
    authControllerProvider,
    (_, __) => notifier.refresh(),
  );
  ref.onDispose(() {
    subscription.close();
    notifier.dispose();
  });
  return notifier;
});
