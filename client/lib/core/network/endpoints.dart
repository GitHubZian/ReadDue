/// 接口路径只在这里出现，页面与状态层不拼字符串。
///
/// 与后端 `com.zian.backend` 的 reader controller 对齐；baseUrl 只到 `/api`。
abstract final class Endpoints {
  static const login = '/v1/auth/login';
  static const logout = '/v1/auth/logout';
  static const me = '/v1/me';

  static const myBorrows = '/v1/borrows/self';
  static String renewBorrow(String id) => '/v1/borrows/$id/renew';

  static const graces = '/v1/graces';
  static const myGraces = '/v1/graces/self';

  static const appeals = '/v1/appeals';
  static const myAppeals = '/v1/appeals/self';

  static const myReservations = '/v1/reservations/self';
  static String reservation(String id) => '/v1/reservations/$id';
}
