import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/appeal/domain/appeal_repository.dart';
import 'package:client/features/appeal/presentation/appeal_providers.dart';
import 'package:client/features/appeal/presentation/appeals_page.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingAppealRepository implements AppealRepository {
  @override
  Future<AppealsPageData> fetchAppeals() async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<List<AppealType>> fetchTypes() async => const [];

  @override
  Future<List<BorrowItem>> fetchOrderOptions() async => const [];

  @override
  Future<AppealCreateResult> submit(AppealCreateRequest request) async {
    throw UnimplementedError();
  }
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C8 正常展示工单与 SLA 说明', (tester) async {
    await pumpPage(tester, const AppealsPage());

    expect(find.text('申诉工单'), findsWidgets);
    expect(find.textContaining('A-2041'), findsWidgets);
    expect(find.textContaining('超时自动补 3 天'), findsWidgets);
  });

  testWidgets('C8 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const AppealsPage(),
      overrides: [
        appealRepositoryProvider.overrideWithValue(_FailingAppealRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
