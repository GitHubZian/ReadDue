import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/models.dart';
import '../data/mock_notification_repository.dart';
import '../domain/notification_repository.dart';

final notificationRepositoryProvider = Provider<NotificationRepository>(
  (ref) => MockNotificationRepository(),
);

final notificationsProvider = FutureProvider<NotificationsPageData>(
  (ref) => ref.watch(notificationRepositoryProvider).fetchNotifications(),
);
