/// 统一错误模型：界面只根据它决定文案与动作。
class ApiException implements Exception {
  const ApiException({
    required this.code,
    required this.message,
    this.statusCode,
    this.traceId,
  });

  final String code;
  final String message;
  final int? statusCode;
  final String? traceId;

  bool get isNetwork => code == 'NETWORK_ERROR' || code == 'TIMEOUT';

  bool get isUnauthorized => statusCode == 401;

  bool get isForbidden => statusCode == 403;

  bool get isNotFound => statusCode == 404;

  bool get isConflict => statusCode == 409;

  bool get isValidation => statusCode == 422 || statusCode == 400;

  bool get isServerError => (statusCode ?? 0) >= 500;

  @override
  String toString() => 'ApiException($code, $statusCode)';
}

/// 把任意异常翻译成一句可展示、不泄露存在性的中文。
String messageForError(Object? error) {
  if (error is ApiException) {
    if (error.isNotFound) return '单据不存在或无权查看';
    if (error.isForbidden) return '需要馆员/管理员权限';
    if (error.isNetwork) return '网络连接不可用，请检查网络后重试';
    if (error.isServerError) return '服务暂时不可用，请稍后重试';
    if (error.message.trim().isNotEmpty) return error.message;
    return '出了点问题，请稍后重试';
  }
  return '出了点问题，请稍后重试';
}
