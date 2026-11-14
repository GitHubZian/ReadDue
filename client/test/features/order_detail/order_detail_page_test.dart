import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/order_detail/domain/order_repository.dart';
import 'package:client/features/order_detail/presentation/order_detail_page.dart';
import 'package:client/features/order_detail/presentation/order_providers.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingOrderRepository implements OrderRepository {
  @override
  Future<OrderDetail> fetchOrder(String id) async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }
}

void main() {
  const orderId = '1900000000000000001';

  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C2 正常展示书目、事实与时间线', (tester) async {
    await pumpPage(tester, const OrderDetailPage(orderId: orderId));

    expect(find.textContaining('线性代数'), findsWidgets);
    expect(find.text('办理进度'), findsOneWidget);
    expect(find.textContaining('申请宽限'), findsWidgets);
  });

  testWidgets('C2 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const OrderDetailPage(orderId: orderId),
      overrides: [
        orderRepositoryProvider.overrideWithValue(_FailingOrderRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
