import '../../../core/mock/mock_backend.dart';
import '../../../core/models/models.dart';
import '../domain/notification_repository.dart';

class MockNotificationRepository implements NotificationRepository {
  MockNotificationRepository([MockBackend? backend])
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<NotificationsPageData> fetchNotifications() =>
      _backend.fetchNotifications();

  @override
  Future<void> markRead(String id) => _backend.markRead(id);

  @override
  Future<void> markAllRead() => _backend.markAllRead();

  @override
  Future<QuietHours> setQuietHours(bool enabled) =>
      _backend.setQuietHours(enabled);

  @override
  Future<NotificationChannels> setChannels({bool? email, bool? push}) =>
      _backend.setChannels(email: email, push: push);
}
