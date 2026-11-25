/// 接口路径只在这里出现，页面与状态层不拼字符串。
///
/// 与后端 `com.zian.backend` 的 reader controller 对齐；baseUrl 只到 `/api`。
abstract final class Endpoints {
  static const login = '/v1/auth/login';
  static const refresh = '/v1/auth/refresh';
  static const logout = '/v1/auth/logout';
  static const me = '/v1/me';

  static const myBorrows = '/v1/borrows/self';
  static const borrows = '/v1/borrows';
  static String renewBorrow(String id) => '/v1/borrows/$id/renew';

  static const fileUpload = '/v1/files';

  static const graces = '/v1/graces';
  static const myGraces = '/v1/graces/self';
  static String graceDetail(String id) => '/v1/graces/$id';

  static String file(String id) => '/v1/files/$id';

  static const appeals = '/v1/appeals';
  static const myAppeals = '/v1/appeals/self';

  static const reservations = '/v1/reservations';
  static const myReservations = '/v1/reservations/self';
  static String reservation(String id) => '/v1/reservations/$id';

  static const books = '/v1/books';
  static String book(String id) => '/v1/books/$id';
  static String bookCopies(String id) => '/v1/books/$id/copies';
  static String bookQueue(String id) => '/v1/books/$id/queue';

  static const notifications = '/v1/notifications';
  static const notificationsUnread = '/v1/notifications/unread-count';
  static const notificationsRead = '/v1/notifications/read';

  static const meSummary = '/v1/me/summary';
  static const meChannels = '/v1/me/channels';
  static const meQuietHours = '/v1/me/quiet-hours';
  static const meNotifyTopics = '/v1/me/notify-topics';
  static const meNotifyPreferences = '/v1/me/notify-preferences';
  static const meConsents = '/v1/me/consents';
  static const meForget = '/v1/me/forget';

  static const meTimetable = '/v1/me/timetable';
  static const meTimetableQuietWindows = '/v1/me/timetable/quiet-windows';
}
