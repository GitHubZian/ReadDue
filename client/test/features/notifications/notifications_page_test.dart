import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/notifications/domain/notification_repository.dart';
import 'package:client/features/notifications/presentation/notifications_page.dart';
import 'package:client/features/notifications/presentation/notifications_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingNotificationRepository implements NotificationRepository {
  @override
  Future<NotificationsPageData> fetchNotifications() async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<void> markRead(String id) async {}

  @override
  Future<void> markAllRead() async {}

  @override
  Future<QuietHours> setQuietHours(bool enabled) async =>
      QuietHours(enabled: enabled);

  @override
  Future<NotificationChannels> setChannels({bool? email, bool? push}) async =>
      const NotificationChannels();
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C6 正常展示未读与分段', (tester) async {
    await pumpPage(tester, const NotificationsPage());

    expect(find.text('消息中心'), findsOneWidget);
    expect(find.byIcon(Icons.arrow_back_ios_new), findsOneWidget);
    expect(find.textContaining('全部已读'), findsWidgets);
    expect(find.textContaining('乡土中国'), findsWidgets);
    expect(find.textContaining('免打扰'), findsWidgets);
  });

  testWidgets('C6 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const NotificationsPage(),
      overrides: [
        notificationRepositoryProvider
            .overrideWithValue(_FailingNotificationRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
