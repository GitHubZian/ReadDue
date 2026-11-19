import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_privacy_repository.dart';
import '../data/dio_profile_repository.dart';
import '../data/mock_privacy_repository.dart';
import '../data/mock_profile_repository.dart';
import '../domain/privacy_repository.dart';
import '../domain/profile_repository.dart';

final profileRepositoryProvider = Provider<ProfileRepository>(
  (ref) => ref.watch(appUseMockProvider)
      ? MockProfileRepository()
      : DioProfileRepository(ref.watch(apiClientProvider)),
);

final profileProvider = FutureProvider<ProfileData>(
  (ref) => ref.watch(profileRepositoryProvider).fetchProfile(),
);

final privacyRepositoryProvider = Provider<PrivacyRepository>(
  (ref) => ref.watch(appUseMockProvider)
      ? MockPrivacyRepository()
      : DioPrivacyRepository(ref.watch(apiClientProvider)),
);

final privacyProvider = FutureProvider<PrivacyData>(
  (ref) => ref.watch(privacyRepositoryProvider).fetch(),
);
