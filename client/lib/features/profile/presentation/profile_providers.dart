import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_config.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_profile_repository.dart';
import '../data/mock_profile_repository.dart';
import '../domain/profile_repository.dart';

final profileRepositoryProvider = Provider<ProfileRepository>(
  (ref) => AppConfig.useMock
      ? MockProfileRepository()
      : DioProfileRepository(ref.watch(apiClientProvider)),
);

final profileProvider = FutureProvider<ProfileData>(
  (ref) => ref.watch(profileRepositoryProvider).fetchProfile(),
);
