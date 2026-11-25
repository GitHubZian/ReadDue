import 'package:client/features/timetable/domain/timetable.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('课表课程校验', () {
    test('时间格式不是 HH:mm 时拒绝', () {
      expect(validateCourseTime('8:00', '09:40'), '时间格式应为 HH:mm');
      expect(validateCourseTime('08:00', '9:40'), '时间格式应为 HH:mm');
      expect(validateCourseTime('08:00', '09:40'), isNull);
    });

    test('结束时间不晚于开始时间时拒绝', () {
      expect(validateCourseTime('10:00', '10:00'), '结束时间需晚于开始时间');
      expect(validateCourseTime('10:00', '09:00'), '结束时间需晚于开始时间');
    });

    test('同一天时间重叠时拒绝，首尾相接视为不重叠', () {
      const existing = Course(
        weekday: 1,
        startTime: '08:00',
        endTime: '09:40',
      );
      const overlapping = Course(
        weekday: 1,
        startTime: '09:00',
        endTime: '10:00',
      );
      const adjacent = Course(
        weekday: 1,
        startTime: '09:40',
        endTime: '10:30',
      );

      expect(validateCourse(overlapping, [existing]), '与已有课程时间重叠');
      expect(validateCourse(adjacent, [existing]), isNull);
    });

    test('星期超出 1-7 时拒绝', () {
      const invalid = Course(
        weekday: 0,
        startTime: '08:00',
        endTime: '09:00',
      );

      expect(validateCourse(invalid, const []), '星期需在周一至周日之间');
    });
  });
}
