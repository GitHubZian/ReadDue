import '../../../core/models/models.dart';

/// C6 通知：列表、已读、免打扰与渠道偏好。
abstract class NotificationRepository {
  Future<NotificationsPageData> fetchNotifications();

  Future<void> markRead(String id);

  Future<void> markAllRead();

  Future<QuietHours> setQuietHours(bool enabled);

  Future<NotificationChannels> setChannels({bool? email, bool? push});
}
