/// 通知主题偏好：后端只允许在 REALTIME/DAILY/WEEKLY 之间调整，
/// P0 主题锁定为必达，不展示可编辑入口。
class NotifyTopic {
  const NotifyTopic({
    required this.code,
    required this.name,
    required this.priority,
    required this.frequency,
    required this.channels,
    required this.locked,
  });

  final String code;
  final String name;
  final String priority;
  final String frequency;
  final List<String> channels;
  final bool locked;

  String get frequencyLabel => switch (frequency) {
        'REALTIME' => '实时',
        'WEEKLY' => '每周汇总',
        _ => '每日汇总',
      };
}

class PrivacyData {
  const PrivacyData({
    required this.sceneConsent,
    required this.cacheConsent,
    required this.forgotten,
    required this.topics,
  });

  final bool sceneConsent;
  final bool cacheConsent;
  final bool forgotten;
  final List<NotifyTopic> topics;
}

abstract class PrivacyRepository {
  Future<PrivacyData> fetch();

  Future<void> setSceneConsent(bool value);

  Future<void> setCacheConsent(bool value);

  Future<void> setFrequency(NotifyTopic topic, String frequency);

  Future<void> forget();
}
