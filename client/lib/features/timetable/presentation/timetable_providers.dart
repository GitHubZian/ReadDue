import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_timetable_repository.dart';
import '../data/mock_timetable_repository.dart';
import '../domain/timetable.dart';
import '../domain/timetable_repository.dart';

final timetableRepositoryProvider = Provider<TimetableRepository>(
  (ref) => ref.watch(appUseMockProvider)
      ? MockTimetableRepository()
      : DioTimetableRepository(ref.watch(apiClientProvider)),
);

final timetableProvider = FutureProvider<Timetable>(
  (ref) => ref.watch(timetableRepositoryProvider).fetch(),
);

/// 今日免打扰窗口（当天课程外扩 15 分钟）。
final quietWindowsProvider = FutureProvider<List<QuietWindow>>(
  (ref) => ref
      .watch(timetableRepositoryProvider)
      .fetchQuietWindows(DateTime.now()),
);
