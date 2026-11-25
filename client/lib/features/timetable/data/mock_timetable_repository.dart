import '../domain/timetable.dart';
import '../domain/timetable_repository.dart';

/// 演示态内存课表：保存后读写同一实例，免打扰窗口按当天课程外扩 15 分钟。
class MockTimetableRepository implements TimetableRepository {
  Timetable _timetable = const Timetable();

  @override
  Future<Timetable> fetch() async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
    return _timetable;
  }

  @override
  Future<Timetable> save(List<Course> courses) async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
    _timetable = Timetable(
      confirmed: true,
      courses: sortCourses(courses),
      updatedAt: DateTime.now(),
    );
    return _timetable;
  }

  @override
  Future<List<QuietWindow>> fetchQuietWindows(DateTime date) async {
    await Future<void>.delayed(const Duration(milliseconds: 80));
    final windows = _timetable.courses
        .where((course) => course.weekday == date.weekday)
        .map((course) {
          final start = minutesOfDay(course.startTime)! - 15;
          final end = minutesOfDay(course.endTime)! + 15;
          return QuietWindow(
            startTime: _format(start.clamp(0, 24 * 60 - 1).toInt()),
            endTime: _format(end.clamp(0, 24 * 60 - 1).toInt()),
          );
        })
        .toList();
    return windows;
  }

  String _format(int minutes) {
    final hour = (minutes ~/ 60).toString().padLeft(2, '0');
    final minute = (minutes % 60).toString().padLeft(2, '0');
    return '$hour:$minute';
  }
}
