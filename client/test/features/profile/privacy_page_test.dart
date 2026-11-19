import 'package:client/core/network/api_exception.dart';
import 'package:client/features/profile/domain/privacy_repository.dart';
import 'package:client/features/profile/presentation/privacy_page.dart';
import 'package:client/features/profile/presentation/profile_providers.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingPrivacyRepository implements PrivacyRepository {
  @override
  Future<PrivacyData> fetch() async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<void> forget() async {}

  @override
  Future<void> setCacheConsent(bool value) async {}

  @override
  Future<void> setFrequency(NotifyTopic topic, String frequency) async {}

  @override
  Future<void> setSceneConsent(bool value) async {}
}

void main() {
  testWidgets('隐私与提醒页展示授权与主题', (tester) async {
    await pumpPage(tester, const PrivacyPage());

    expect(find.text('隐私与提醒'), findsOneWidget);
    expect(find.text('借阅场景授权'), findsOneWidget);
    expect(find.text('到期提醒'), findsOneWidget);
    expect(find.text('逾期提醒'), findsOneWidget);
    expect(find.text('提醒偏好'), findsOneWidget);
  });

  testWidgets('隐私页错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const PrivacyPage(),
      overrides: [
        privacyRepositoryProvider.overrideWithValue(
          _FailingPrivacyRepository(),
        ),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
