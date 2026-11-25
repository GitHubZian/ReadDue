import 'timetable.dart';

/// 课表免打扰：读取课表、整体保存、读取当天免打扰窗口。
abstract class TimetableRepository {
  Future<Timetable> fetch();

  Future<Timetable> save(List<Course> courses);

  Future<List<QuietWindow>> fetchQuietWindows(DateTime date);
}
