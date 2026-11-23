import 'package:dio/dio.dart';
import 'package:uuid/uuid.dart';

import '../config/app_config.dart';
import '../storage/kv_store.dart';
import 'api_exception.dart';
import 'endpoints.dart';

/// 薄封装：统一 envelope、trace、幂等键、401 刷新、GET 重试与错误映射。
class ApiClient {
  ApiClient({required KvStore kv, Dio? dio, this.onSessionExpired})
      : _kv = kv,
        _dio = dio ??
            Dio(
              BaseOptions(
                baseUrl: AppConfig.apiBaseUrl,
                connectTimeout: const Duration(seconds: 5),
                receiveTimeout: const Duration(seconds: 10),
                sendTimeout: const Duration(seconds: 10),
                contentType: Headers.jsonContentType,
                responseType: ResponseType.json,
              ),
            );

  final Dio _dio;
  final KvStore _kv;

  /// refresh 失败、本地会话已清空后回调，用于把登录态切到未登录。
  Future<void> Function()? onSessionExpired;

  /// 并发 401 只发一次 refresh；其余请求等待同一个 future。
  Future<String?>? _refreshInFlight;

  static const _uuid = Uuid();

  String get readerNo => _kv.studentNo ?? '';

  Future<T> get<T>(
    String path, {
    Map<String, dynamic>? query,
    required T Function(dynamic data) decode,
  }) {
    return _send('GET', path, query: query, decode: decode);
  }

  Future<T> post<T>(
    String path, {
    Object? body,
    required T Function(dynamic data) decode,
  }) {
    return _send('POST', path, body: body, decode: decode);
  }

  Future<T> put<T>(
    String path, {
    Object? body,
    required T Function(dynamic data) decode,
  }) {
    return _send('PUT', path, body: body, decode: decode);
  }

  Future<T> delete<T>(String path, {required T Function(dynamic data) decode}) {
    return _send('DELETE', path, decode: decode);
  }

  /// multipart 单文件上传；Dio 会按 FormData 自动补 boundary。
  Future<T> postFile<T>(
    String path, {
    required String filePath,
    required String fileName,
    required T Function(dynamic data) decode,
  }) async {
    final form = FormData.fromMap({
      'file': await MultipartFile.fromFile(filePath, filename: fileName),
    });
    return _send('POST', path, body: form, decode: decode);
  }

  Future<T> _send<T>(
    String method,
    String path, {
    Map<String, dynamic>? query,
    Object? body,
    required T Function(dynamic data) decode,
  }) async {
    final traceId = _uuid.v4();
    final headers = <String, dynamic>{
      'X-Trace-Id': traceId,
      if (method != 'GET') 'Idempotency-Key': _uuid.v4(),
    };
    final token = _kv.token;
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    Object? lastError;
    var refreshed = false;
    for (var attempt = 0;; attempt++) {
      try {
        final response = await _dio.request<dynamic>(
          path,
          data: body,
          queryParameters: query,
          options: Options(method: method, headers: headers),
        );
        return decode(_unwrap(response.data));
      } catch (error) {
        lastError = error;
        if (!refreshed && path != Endpoints.refresh && _isUnauthorized(error)) {
          refreshed = true;
          final newToken = await _refreshToken();
          if (newToken == null) break;
          headers['Authorization'] = 'Bearer $newToken';
          continue;
        }
        if (method == 'GET' && attempt < 1 && _isRetryable(error)) {
          await Future<void>.delayed(const Duration(milliseconds: 300));
          continue;
        }
        break;
      }
    }
    throw _mapError(lastError, traceId);
  }

  /// 单飞刷新：并发 401 共用同一个 refresh future。
  Future<String?> _refreshToken() {
    final inFlight = _refreshInFlight;
    if (inFlight != null) return inFlight;
    final future = _doRefresh();
    _refreshInFlight = future;
    future.whenComplete(() {
      if (identical(_refreshInFlight, future)) _refreshInFlight = null;
    });
    return future;
  }

  Future<String?> _doRefresh() async {
    final token = _kv.token;
    if (token == null || token.isEmpty) {
      await _expireSession();
      return null;
    }
    try {
      final response = await _dio.post<dynamic>(
        Endpoints.refresh,
        options: Options(
          headers: {
            'Authorization': 'Bearer $token',
            'X-Trace-Id': _uuid.v4(),
          },
        ),
      );
      final data = _unwrap(response.data);
      if (data is! Map<String, dynamic>) {
        await _expireSession();
        return null;
      }
      final newToken = (data['token'] ?? '').toString();
      if (newToken.isEmpty) {
        await _expireSession();
        return null;
      }
      final user = (data['user'] as Map<String, dynamic>?) ?? const {};
      await _kv.saveSession(
        token: newToken,
        userId: (user['id'] ?? _kv.userId ?? '').toString(),
        name: (user['name'] ?? _kv.name ?? '').toString(),
        studentNo: (user['studentNo'] ?? _kv.studentNo ?? '').toString(),
        remember: _kv.rememberMe,
      );
      return newToken;
    } catch (_) {
      await _expireSession();
      return null;
    }
  }

  Future<void> _expireSession() async {
    await _kv.clearSession();
    final callback = onSessionExpired;
    if (callback != null) await callback();
  }

  bool _isUnauthorized(Object? error) =>
      error is DioException && error.response?.statusCode == 401;

  dynamic _unwrap(dynamic data) {
    if (data is Map<String, dynamic> && data.containsKey('data')) {
      return data['data'];
    }
    return data;
  }

  bool _isRetryable(Object? error) {
    if (error is! DioException) return false;
    final status = error.response?.statusCode;
    return error.type == DioExceptionType.connectionError ||
        error.type == DioExceptionType.connectionTimeout ||
        error.type == DioExceptionType.receiveTimeout ||
        (status != null && status >= 500);
  }

  ApiException _mapError(Object? error, String traceId) {
    if (error is ApiException) return error;
    if (error is! DioException) {
      return ApiException(
        code: 'UNKNOWN',
        message: '出了点问题，请稍后重试',
        traceId: traceId,
      );
    }
    final response = error.response;
    final status = response?.statusCode;
    final body = response?.data;
    var code = 'HTTP_${status ?? 0}';
    var message = '';
    if (body is Map<String, dynamic>) {
      code = (body['code'] ?? code).toString();
      message = (body['message'] ?? '').toString();
    }
    if (error.type == DioExceptionType.connectionError ||
        error.type == DioExceptionType.connectionTimeout) {
      code = 'NETWORK_ERROR';
    } else if (error.type == DioExceptionType.receiveTimeout ||
        error.type == DioExceptionType.sendTimeout) {
      code = 'TIMEOUT';
    }
    return ApiException(
      code: code,
      message: message,
      statusCode: status,
      traceId: traceId,
    );
  }
}
