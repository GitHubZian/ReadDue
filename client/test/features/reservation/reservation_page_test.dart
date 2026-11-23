import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/reservation/domain/reservation_repository.dart';
import 'package:client/features/reservation/presentation/reservation_page.dart';
import 'package:client/features/reservation/presentation/reservation_providers.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingReservationRepository implements ReservationRepository {
  @override
  Future<ReservationsPage> fetchReservations() async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<void> cancel(String reservationId) async {}

  @override
  Future<int> activeCount() async => 0;
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C5 正常展示取书码与队列', (tester) async {
    await pumpPage(tester, const ReservationPage());

    expect(find.textContaining('M-2048'), findsWidgets);
    expect(find.textContaining('人类简史'), findsWidgets);
  });

  testWidgets('C5 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const ReservationPage(),
      overrides: [
        reservationRepositoryProvider
            .overrideWithValue(_FailingReservationRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
