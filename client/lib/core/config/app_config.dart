import 'package:flutter/foundation.dart';

/// 运行期开关：debug 默认走本地 mock，release 默认连后端；接后端时用
/// `--dart-define=USE_MOCK=false --dart-define=API_BASE_URL=http://10.0.2.2:8125/api`。
abstract final class AppConfig {
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8125/api',
  );

  static const useMock = bool.fromEnvironment(
    'USE_MOCK',
    defaultValue: !kReleaseMode,
  );
}
