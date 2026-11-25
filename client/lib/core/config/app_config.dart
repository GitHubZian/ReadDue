/// 运行期开关：默认连真实后端；本地假数据用
/// `--dart-define=USE_MOCK=true`，接口地址用 `--dart-define=API_BASE_URL=...`。
abstract final class AppConfig {
  /// 默认连当前局域网内的后端；默认 IP 随演示网络变化，以
  /// `--dart-define=API_BASE_URL=...` 为准。Android 模拟器可用
  /// `--dart-define=API_BASE_URL=http://10.0.2.2:8125/api` 覆盖。
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://8.163.136.80:8124/api',
  );

  static const useMock = bool.fromEnvironment('USE_MOCK', defaultValue: false);
}

