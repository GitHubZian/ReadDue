import 'package:client/core/network/api_exception.dart';
import 'package:client/features/profile/domain/deletion_request_repository.dart';
import 'package:client/features/profile/presentation/deletion_request_page.dart';
import 'package:client/features/profile/presentation/profile_providers.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FakeDeletionRepository implements DeletionRequestRepository {
  _FakeDeletionRepository({this.latest, this.submitError});

  DeletionRequest? latest;
  Object? submitError;
  int submitCount = 0;
  String? lastReason;

  @override
  Future<DeletionRequest?> fetchLatest() async => latest;

  @override
  Future<DeletionRequest> submit({String? reason}) async {
    submitCount++;
    lastReason = reason;
    if (submitError != null) throw submitError!;
    latest = const DeletionRequest(status: 'PENDING');
    return latest!;
  }
}

void main() {
  testWidgets('进入页面查询到 PENDING 时展示等待馆员状态卡', (tester) async {
    final repo = _FakeDeletionRepository(
      latest: const DeletionRequest(status: 'PENDING'),
    );
    await pumpPage(
      tester,
      const DeletionRequestPage(),
      overrides: [
        deletionRequestRepositoryProvider.overrideWithValue(repo),
      ],
    );

    expect(find.text('已提交，等待馆员处理'), findsOneWidget);
    expect(find.text('待审批'), findsOneWidget);
    expect(find.text('提交注销申请'), findsNothing);
  });

  testWidgets('提交申请经二次确认后切到等待状态', (tester) async {
    final repo = _FakeDeletionRepository();
    await pumpPage(
      tester,
      const DeletionRequestPage(),
      overrides: [
        deletionRequestRepositoryProvider.overrideWithValue(repo),
      ],
    );

    await tester.tap(find.text('提交注销申请'));
    await tester.pumpAndSettle();
    expect(find.text('提交申请'), findsOneWidget);

    await tester.tap(find.text('提交申请'));
    await tester.pumpAndSettle();

    expect(repo.submitCount, 1);
    expect(find.text('已提交，等待馆员处理'), findsWidgets);
    expect(find.text('提交注销申请'), findsNothing);
  });

  testWidgets('422 时展示服务端 message', (tester) async {
    final repo = _FakeDeletionRepository(
      submitError: const ApiException(
        code: 'CONFLICT_ERROR',
        message: '已有待处理的注销申请',
        statusCode: 422,
      ),
    );
    await pumpPage(
      tester,
      const DeletionRequestPage(),
      overrides: [
        deletionRequestRepositoryProvider.overrideWithValue(repo),
      ],
    );

    await tester.tap(find.text('提交注销申请'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('提交申请'));
    await tester.pumpAndSettle();

    expect(find.text('已有待处理的注销申请'), findsOneWidget);
    expect(find.text('提交注销申请'), findsOneWidget);
  });

  testWidgets('REJECTED 展示驳回原因并可重新提交', (tester) async {
    final repo = _FakeDeletionRepository(
      latest: const DeletionRequest(
        status: 'REJECTED',
        reason: '存在未归还图书，请归还后再申请',
      ),
    );
    await pumpPage(
      tester,
      const DeletionRequestPage(),
      overrides: [
        deletionRequestRepositoryProvider.overrideWithValue(repo),
      ],
    );

    expect(find.text('存在未归还图书，请归还后再申请'), findsOneWidget);
    expect(find.text('重新提交申请'), findsOneWidget);
  });
}
