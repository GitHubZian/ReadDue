import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../domain/privacy_repository.dart';

/// 真后端隐私授权与提醒偏好。
class DioPrivacyRepository implements PrivacyRepository {
  DioPrivacyRepository(this._client);

  final ApiClient _client;

  @override
  Future<PrivacyData> fetch() async {
    final results = await Future.wait<Map<String, dynamic>>([
      _client.get<Map<String, dynamic>>(
        Endpoints.meConsents,
        decode: (data) => (data as Map<String, dynamic>?) ?? const {},
      ),
      _client.get<Map<String, dynamic>>(
        Endpoints.meNotifyTopics,
        decode: (data) => {'items': data},
      ),
    ]);
    final consents = results[0];
    final rawTopics = (results[1]['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>();

    return PrivacyData(
      sceneConsent: consents['sceneConsent'] == true,
      cacheConsent: consents['cacheConsent'] == true,
      forgotten: consents['forgotten'] == true,
      topics: rawTopics
          .map(
            (json) => NotifyTopic(
              code: (json['topicCode'] ?? '').toString(),
              name: (json['name'] ?? '').toString(),
              priority: (json['priority'] ?? 'P1').toString(),
              frequency: (json['frequency'] ?? 'DAILY').toString(),
              channels: (json['channels'] as List<dynamic>? ?? const [])
                  .map((e) => e.toString())
                  .toList(),
              locked: json['locked'] == true,
            ),
          )
          .where((topic) => topic.code.isNotEmpty)
          .toList(),
    );
  }

  @override
  Future<void> setSceneConsent(bool value) => _putConsent({
        'sceneConsent': value,
      });

  @override
  Future<void> setCacheConsent(bool value) => _putConsent({
        'cacheConsent': value,
      });

  Future<void> _putConsent(Map<String, dynamic> body) async {
    await _client.put<void>(Endpoints.meConsents, body: body, decode: (_) {});
  }

  @override
  Future<void> setFrequency(NotifyTopic topic, String frequency) async {
    await _client.put<void>(
      Endpoints.meNotifyPreferences,
      body: {
        'items': [
          {
            'topicCode': topic.code,
            'frequency': frequency,
            'channels': topic.channels,
          },
        ],
      },
      decode: (_) {},
    );
  }

  @override
  Future<void> forget() async {
    await _client.post<void>(Endpoints.meForget, decode: (_) {});
  }
}
