import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_notification_repository.dart';
import '../data/mock_notification_repository.dart';
import '../domain/notification_repository.dart';

final notificationRepositoryProvider = Provider<NotificationRepository>(
  (ref) => ref.watch(appUseMockProvider)
      ? MockNotificationRepository()
      : DioNotificationRepository(ref.watch(apiClientProvider)),
);

final notificationsProvider = FutureProvider<NotificationsPageData>(
  (ref) => ref.watch(notificationRepositoryProvider).fetchNotifications(),
);
