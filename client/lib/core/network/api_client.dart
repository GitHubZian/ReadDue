import 'package:dio/dio.dart';
import 'package:uuid/uuid.dart';

import '../config/app_config.dart';
import '../storage/kv_store.dart';
import 'api_exception.dart';

/// 薄封装：统一 envelope、trace、幂等键、GET 重试与错误映射。
class ApiClient {
  ApiClient({required KvStore kv, Dio? dio})
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
  static const _uuid = Uuid();

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
    for (var attempt = 0; attempt < 2; attempt++) {
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
        final retryable = method == 'GET' && _isRetryable(error);
        if (!retryable || attempt == 1) break;
        await Future<void>.delayed(const Duration(milliseconds: 300));
      }
    }
    throw _mapError(lastError, traceId);
  }

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
