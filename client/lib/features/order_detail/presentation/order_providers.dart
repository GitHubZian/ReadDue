import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_order_repository.dart';
import '../domain/order_repository.dart';

final orderRepositoryProvider = Provider<OrderRepository>((ref) {
  if (ref.watch(appUseMockProvider)) return MockOrderRepository();
  return DioOrderRepository(ref.watch(apiClientProvider));
});

final orderDetailProvider =
    FutureProvider.autoDispose.family<OrderDetail, String>(
  (ref, orderId) => ref.watch(orderRepositoryProvider).fetchOrder(orderId),
);
