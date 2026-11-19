import '../domain/privacy_repository.dart';

/// 本地演示用的隐私/提醒偏好。
class MockPrivacyRepository implements PrivacyRepository {
  bool _sceneConsent = true;
  bool _cacheConsent = true;
  bool _forgotten = false;
  final Map<String, String> _frequencies = {
    'BORROW_DUE': 'DAILY',
    'BORROW_OVERDUE': 'DAILY',
    'RESERVATION_READY': 'REALTIME',
    'GRACE_RESULT': 'REALTIME',
    'APPEAL_SLA': 'REALTIME',
    'NOTICE_GENERAL': 'DAILY',
  };

  static const _topics = [
    NotifyTopic(
      code: 'BORROW_DUE',
      name: '到期提醒',
      priority: 'P1',
      frequency: 'DAILY',
      channels: ['INBOX', 'EMAIL'],
      locked: false,
    ),
    NotifyTopic(
      code: 'BORROW_OVERDUE',
      name: '逾期提醒',
      priority: 'P0',
      frequency: 'DAILY',
      channels: ['INBOX', 'EMAIL'],
      locked: true,
    ),
    NotifyTopic(
      code: 'RESERVATION_READY',
      name: '到书通知',
      priority: 'P0',
      frequency: 'REALTIME',
      channels: ['INBOX', 'EMAIL'],
      locked: true,
    ),
    NotifyTopic(
      code: 'GRACE_RESULT',
      name: '宽限结果',
      priority: 'P0',
      frequency: 'REALTIME',
      channels: ['INBOX'],
      locked: true,
    ),
    NotifyTopic(
      code: 'APPEAL_SLA',
      name: '申诉进度',
      priority: 'P1',
      frequency: 'REALTIME',
      channels: ['INBOX'],
      locked: false,
    ),
    NotifyTopic(
      code: 'NOTICE_GENERAL',
      name: '一般通知',
      priority: 'P2',
      frequency: 'DAILY',
      channels: ['INBOX'],
      locked: false,
    ),
  ];

  @override
  Future<PrivacyData> fetch() async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
    return PrivacyData(
      sceneConsent: _sceneConsent,
      cacheConsent: _cacheConsent,
      forgotten: _forgotten,
      topics: [
        for (final topic in _topics)
          NotifyTopic(
            code: topic.code,
            name: topic.name,
            priority: topic.priority,
            frequency: _frequencies[topic.code] ?? topic.frequency,
            channels: topic.channels,
            locked: topic.locked,
          ),
      ],
    );
  }

  @override
  Future<void> setSceneConsent(bool value) async => _sceneConsent = value;

  @override
  Future<void> setCacheConsent(bool value) async => _cacheConsent = value;

  @override
  Future<void> setFrequency(NotifyTopic topic, String frequency) async {
    _frequencies[topic.code] = frequency;
  }

  @override
  Future<void> forget() async => _forgotten = true;
}
