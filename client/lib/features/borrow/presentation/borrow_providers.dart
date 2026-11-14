import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_config.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_borrow_repository.dart';
import '../domain/borrow_repository.dart';

final borrowRepositoryProvider = Provider<BorrowRepository>((ref) {
  if (AppConfig.useMock) return MockBorrowRepository();
  return DioBorrowRepository(ref.watch(apiClientProvider));
});

final borrowDashboardProvider = FutureProvider<BorrowDashboard>(
  (ref) => ref.watch(borrowRepositoryProvider).fetchDashboard(),
);
