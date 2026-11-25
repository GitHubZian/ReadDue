import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/utils/app_date.dart';
import '../domain/timetable.dart';
import '../domain/timetable_repository.dart';

class DioTimetableRepository implements TimetableRepository {
  DioTimetableRepository(this._client);

  final ApiClient _client;

  @override
  Future<Timetable> fetch() => _client.get<Timetable>(
        Endpoints.meTimetable,
        decode: _decodeTimetable,
      );

  @override
  Future<Timetable> save(List<Course> courses) => _client.put<Timetable>(
        Endpoints.meTimetable,
        body: {'courses': courses.map((course) => course.toJson()).toList()},
        decode: (data) => _decodeTimetable(data, fallback: courses),
      );

  @override
  Future<List<QuietWindow>> fetchQuietWindows(DateTime date) =>
      _client.get<List<QuietWindow>>(
        Endpoints.meTimetableQuietWindows,
        query: {'date': AppDate.date(date)},
        decode: (data) {
          final list = data is Map
              ? (data['windows'] as List<dynamic>? ?? const [])
              : (data as List<dynamic>? ?? const []);
          return list
              .whereType<Map<String, dynamic>>()
              .map(QuietWindow.fromJson)
              .toList();
        },
      );

  Timetable _decodeTimetable(dynamic data, {List<Course>? fallback}) {
    if (data is Map) {
      return Timetable.fromJson(Map<String, dynamic>.from(data));
    }
    return Timetable(
      confirmed: true,
      courses: sortCourses(fallback ?? const []),
    );
  }
}
