import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/profile/domain/profile_repository.dart';
import 'package:client/features/profile/presentation/profile_page.dart';
import 'package:client/features/profile/presentation/profile_providers.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../support/harness.dart';

class _FailingProfileRepository implements ProfileRepository {
  @override
  Future<ProfileData> fetchProfile() async {
    throw const ApiException(
      code: 'SERVER_ERROR',
      message: '服务暂时不可用，请稍后重试',
      statusCode: 500,
    );
  }

  @override
  Future<void> logout() async {}
}

void main() {
  setUp(() => MockBackend.instance.resetForTest());

  testWidgets('C7 正常展示额度与菜单', (tester) async {
    await pumpPage(tester, const ProfilePage());

    expect(find.textContaining('林知远'), findsWidgets);
    expect(find.textContaining('借阅额度'), findsWidgets);
    expect(find.textContaining('3 / 5'), findsWidgets);
  });

  testWidgets('C7 错误态可重试', (tester) async {
    await pumpPage(
      tester,
      const ProfilePage(),
      overrides: [
        profileRepositoryProvider
            .overrideWithValue(_FailingProfileRepository()),
      ],
    );

    expect(find.text('暂时无法加载'), findsOneWidget);
    expect(find.text('重试'), findsOneWidget);
  });
}
