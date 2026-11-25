import 'package:client/core/mock/mock_backend.dart';
import 'package:client/core/models/models.dart';
import 'package:client/core/network/api_exception.dart';
import 'package:client/features/profile/domain/profile_repository.dart';
import 'package:client/features/profile/presentation/profile_page.dart';
import 'package:client/features/profile/presentation/profile_providers.dart';
import 'package:flutter/material.dart';
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
  Future<ProfileContact> fetchContact() async => const ProfileContact(name: '');

  @override
  Future<ProfileContact> updateProfile({
    required String name,
    required String phone,
    required String email,
  }) async =>
      ProfileContact(name: name, phone: phone, email: email);

  @override
  Future<void> logout() async {}
}

class _StaticProfileRepository implements ProfileRepository {
  _StaticProfileRepository(this.data);

  final ProfileData data;

  @override
  Future<ProfileData> fetchProfile() async => data;

  @override
  Future<ProfileContact> fetchContact() async => ProfileContact(name: data.name);

  @override
  Future<ProfileContact> updateProfile({
    required String name,
    required String phone,
    required String email,
  }) async =>
      ProfileContact(name: name, phone: phone, email: email);

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

  testWidgets('C7 有资料时副标题显示专业年级、累计借阅取 totalBorrowed', (tester) async {
    await pumpPage(
      tester,
      const ProfilePage(),
      overrides: [
        profileRepositoryProvider.overrideWithValue(
          _StaticProfileRepository(
            const ProfileData(
              name: '陈昊',
              studentNo: '2021001',
              department: '计算机学院',
              major: '软件工程',
              grade: '2021',
              quotaUsed: 3,
              quotaTotal: 5,
              borrowingCount: 3,
              totalBorrowed: 42,
            ),
          ),
        ),
      ],
    );

    expect(find.text('2021001 · 软件工程 · 2021'), findsOneWidget);
    expect(find.text('42'), findsOneWidget);
  });

  testWidgets('C7 无资料时副标题只留学号、不出现分隔符尾巴', (tester) async {
    await pumpPage(
      tester,
      const ProfilePage(),
      overrides: [
        profileRepositoryProvider.overrideWithValue(
          _StaticProfileRepository(
            const ProfileData(name: '陈昊', studentNo: '2021001'),
          ),
        ),
      ],
    );

    expect(find.text('2021001'), findsOneWidget);
    expect(find.text('2021001 · '), findsNothing);
    expect(find.text('0'), findsWidgets);
  });

  testWidgets('C7 底部隐私政策可点，设置弹层含隐私政策与注销账号', (tester) async {
    tester.view.physicalSize = const Size(1200, 2600);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await pumpPage(tester, const ProfilePage());

    expect(find.text('隐私政策'), findsOneWidget);

    await tester.tap(find.byTooltip('设置'));
    await tester.pumpAndSettle();

    expect(find.text('隐私政策'), findsNWidgets(2));
    expect(find.text('注销账号'), findsOneWidget);
    expect(find.text('清除本地缓存'), findsOneWidget);
  });
}
