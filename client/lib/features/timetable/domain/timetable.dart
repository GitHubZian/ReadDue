/// 课表与「上课免打扰」的领域模型与校验（GET/PUT /v1/me/timetable）。
library;

const weekdayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

String weekdayLabel(int weekday) =>
    isValidWeekday(weekday) ? weekdayLabels[weekday - 1] : '未知';

bool isValidWeekday(int weekday) => weekday >= 1 && weekday <= 7;

final _timePattern = RegExp(r'^([01]\d|2[0-3]):[0-5]\d$');

/// "HH:mm" → 当天分钟数；格式不合法返回 null。
int? minutesOfDay(String time) {
  if (!_timePattern.hasMatch(time)) return null;
  final parts = time.split(':');
  return int.parse(parts[0]) * 60 + int.parse(parts[1]);
}

/// 时间格式与先后顺序；返回 null 表示合法。
String? validateCourseTime(String startTime, String endTime) {
  if (!_timePattern.hasMatch(startTime) ||
      !_timePattern.hasMatch(endTime)) {
    return '时间格式应为 HH:mm';
  }
  final start = minutesOfDay(startTime)!;
  final end = minutesOfDay(endTime)!;
  if (end <= start) return '结束时间需晚于开始时间';
  return null;
}

class Course {
  const Course({
    required this.weekday,
    required this.startTime,
    required this.endTime,
  });

  factory Course.fromJson(Map<String, dynamic> json) => Course(
        weekday: (json['weekday'] as num?)?.toInt() ?? 0,
        startTime: (json['startTime'] ?? '').toString(),
        endTime: (json['endTime'] ?? '').toString(),
      );

  /// 1 = 周一 … 7 = 周日。
  final int weekday;
  final String startTime;
  final String endTime;

  String get label => '$startTime – $endTime';

  Map<String, dynamic> toJson() => {
        'weekday': weekday,
        'startTime': startTime,
        'endTime': endTime,
      };

  /// 同日且时间区间相交（含首尾相接视为不重叠）。
  bool overlaps(Course other) {
    if (weekday != other.weekday) return false;
    final aStart = minutesOfDay(startTime);
    final aEnd = minutesOfDay(endTime);
    final bStart = minutesOfDay(other.startTime);
    final bEnd = minutesOfDay(other.endTime);
    if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
      return false;
    }
    return aStart < bEnd && bStart < aEnd;
  }

  @override
  bool operator ==(Object other) =>
      other is Course &&
      other.weekday == weekday &&
      other.startTime == startTime &&
      other.endTime == endTime;

  @override
  int get hashCode => Object.hash(weekday, startTime, endTime);
}

/// 校验一节课能否加入现有课表；返回 null 表示可以保存。
String? validateCourse(Course candidate, List<Course> existing) {
  if (!isValidWeekday(candidate.weekday)) return '星期需在周一至周日之间';
  final timeError = validateCourseTime(candidate.startTime, candidate.endTime);
  if (timeError != null) return timeError;
  if (existing.any(candidate.overlaps)) return '与已有课程时间重叠';
  return null;
}

List<Course> sortCourses(Iterable<Course> courses) {
  final list = courses.toList()
    ..sort((a, b) {
      final byDay = a.weekday.compareTo(b.weekday);
      if (byDay != 0) return byDay;
      return a.startTime.compareTo(b.startTime);
    });
  return list;
}

class Timetable {
  const Timetable({
    this.confirmed = false,
    this.courses = const [],
    this.updatedAt,
  });

  factory Timetable.fromJson(Map<String, dynamic> json) => Timetable(
        confirmed: json['confirmed'] == true,
        courses: (json['courses'] as List<dynamic>? ?? const [])
            .whereType<Map<String, dynamic>>()
            .map(Course.fromJson)
            .toList(),
        updatedAt: DateTime.tryParse((json['updatedAt'] ?? '').toString()),
      );

  final bool confirmed;
  final List<Course> courses;
  final DateTime? updatedAt;

  Map<String, dynamic> toJson() => {
        'confirmed': confirmed,
        'courses': courses.map((course) => course.toJson()).toList(),
        if (updatedAt != null) 'updatedAt': updatedAt!.toIso8601String(),
      };
}

/// 当天课程外扩 15 分钟后的免打扰窗口。
class QuietWindow {
  const QuietWindow({required this.startTime, required this.endTime});

  factory QuietWindow.fromJson(Map<String, dynamic> json) => QuietWindow(
        startTime: (json['startTime'] ?? '').toString(),
        endTime: (json['endTime'] ?? '').toString(),
      );

  final String startTime;
  final String endTime;

  String get label => '$startTime – $endTime';
}
