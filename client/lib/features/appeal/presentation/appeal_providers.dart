import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_appeal_repository.dart';
import '../data/mock_appeal_repository.dart';
import '../domain/appeal_repository.dart';

final appealRepositoryProvider = Provider<AppealRepository>((ref) {
  if (ref.watch(appUseMockProvider)) return MockAppealRepository();
  return DioAppealRepository(ref.watch(apiClientProvider));
});

final appealsProvider = FutureProvider<AppealsPageData>(
  (ref) => ref.watch(appealRepositoryProvider).fetchAppeals(),
);

final appealTypesProvider = FutureProvider<List<AppealType>>(
  (ref) => ref.watch(appealRepositoryProvider).fetchTypes(),
);

final appealOrderOptionsProvider = FutureProvider<List<BorrowItem>>(
  (ref) => ref.watch(appealRepositoryProvider).fetchOrderOptions(),
);
