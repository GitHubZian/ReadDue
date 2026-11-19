/// 运行期开关：默认连真实后端；本地假数据用
/// `--dart-define=USE_MOCK=true`，接口地址用 `--dart-define=API_BASE_URL=...`。
abstract final class AppConfig {
  static const apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8125/api',
  );

  static const useMock = bool.fromEnvironment('USE_MOCK', defaultValue: false);
}
