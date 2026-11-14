import 'package:intl/intl.dart';

/// 时间展示统一在这里：接口 ISO-8601 +08:00，界面 YYYY-MM-DD HH:mm。
abstract final class AppDate {
  static final _dateTime = DateFormat('yyyy-MM-dd HH:mm');
  static final _date = DateFormat('yyyy-MM-dd');
  static final _time = DateFormat('HH:mm');

  static String dateTime(DateTime value) => _dateTime.format(value.toLocal());

  static String date(DateTime value) => _date.format(value.toLocal());

  static String time(DateTime value) => _time.format(value.toLocal());

  /// 原型里的「4 月 18 日」。
  static String monthDay(DateTime value) {
    final local = value.toLocal();
    return '${local.month} 月 ${local.day} 日';
  }
}
