import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_grace_repository.dart';
import '../domain/grace_repository.dart';

final graceRepositoryProvider = Provider<GraceRepository>((ref) {
  if (ref.watch(appUseMockProvider)) return MockGraceRepository();
  return DioGraceRepository(ref.watch(apiClientProvider));
});

final gracePageProvider =
    FutureProvider.autoDispose.family<GracePageData, String>(
  (ref, orderId) => ref.watch(graceRepositoryProvider).fetchPage(orderId),
);
