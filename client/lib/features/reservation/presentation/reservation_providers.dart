import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_reservation_repository.dart';
import '../domain/reservation_repository.dart';

final reservationRepositoryProvider = Provider<ReservationRepository>((ref) {
  if (ref.watch(appUseMockProvider)) return MockReservationRepository();
  return DioReservationRepository(ref.watch(apiClientProvider));
});

final reservationsProvider = FutureProvider<ReservationsPage>(
  (ref) => ref.watch(reservationRepositoryProvider).fetchReservations(),
);

final reservationBadgeProvider = FutureProvider<int>(
  (ref) => ref.watch(reservationRepositoryProvider).activeCount(),
);
