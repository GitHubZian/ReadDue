import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/grace/domain/grace_repository.dart';
import 'package:client/features/grace/presentation/grace_page.dart';
import 'package:client/features/grace/presentation/grace_providers.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingGraceRepository implements GraceRepository {
  @override
  Future<GracePageData> fetchPage(String orderId) async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<GraceSubmitResult> submit(GraceSubmitRequest request) async {
    throw UnimplementedError();
  }
}

void main() {
  const orderId = '1900000000000000002';

  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C3 正常展示场景与预计应还日', (tester) async {
    await pumpPage(tester, const GracePage(orderId: orderId));

    expect(find.textContaining('申请延后归还'), findsWidgets);
    expect(find.textContaining('毕业离校'), findsWidgets);
    expect(find.textContaining('还差 2 项'), findsOneWidget);

    await tester.tap(find.text('毕业离校'));
    await tester.pumpAndSettle();
    expect(find.textContaining('还差 1 项'), findsOneWidget);

    await tester.dragUntilVisible(
      find.textContaining('2 小时内答复'),
      find.byType(Scrollable).first,
      const Offset(0, -300),
    );
    expect(find.textContaining('2 小时内答复'), findsWidgets);
  });

  testWidgets('C3 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const GracePage(orderId: orderId),
      overrides: [
        graceRepositoryProvider.overrideWithValue(_FailingGraceRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
