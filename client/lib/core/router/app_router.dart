import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/appeal/presentation/appeal_create_page.dart';
import '../../features/appeal/presentation/appeals_page.dart';
import '../../features/auth/presentation/auth_controller.dart';
import '../../features/auth/presentation/login_page.dart';
import '../../features/borrow/presentation/borrow_page.dart';
import '../../features/grace/presentation/grace_page.dart';
import '../../features/notifications/presentation/notifications_page.dart';
import '../../features/order_detail/presentation/order_detail_page.dart';
import '../../features/profile/presentation/profile_page.dart';
import '../../features/reservation/presentation/reservation_page.dart';
import '../../features/reservation/presentation/reservation_providers.dart';
import '../widgets/app_tab_bar.dart';
import '../widgets/branch_fade.dart';
import '../../features/search/presentation/search_page.dart';
import '../theme/tokens.dart';
import '../widgets/states.dart';

/// 应用路由：登录守卫 + 四 Tab 外壳 + 全屏详情页。
final routerProvider = Provider<GoRouter>((ref) {
  final refresh = ref.watch(authRefreshProvider);

  return GoRouter(
    initialLocation: '/borrow',
    refreshListenable: refresh,
    redirect: (context, state) {
      final auth = ref.read(authControllerProvider);
      final atLogin = state.matchedLocation == '/login';
      switch (auth.status) {
        case AuthStatus.unknown:
          return null;
        case AuthStatus.unauthenticated:
          if (atLogin) return null;
          return Uri(
            path: '/login',
            queryParameters: {'returnTo': state.uri.toString()},
          ).toString();
        case AuthStatus.authenticated:
          if (!atLogin) return null;
          final target = state.uri.queryParameters['returnTo'];
          return target == null || target.isEmpty ? '/borrow' : target;
      }
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),
      StatefulShellRoute.indexedStack(
        builder: (context, state, shell) => AppShell(navigationShell: shell),
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/borrow',
                builder: (context, state) => const BorrowPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/search',
                builder: (context, state) => const SearchPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/reservations',
                builder: (context, state) => const ReservationPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/me',
                builder: (context, state) => const ProfilePage(),
              ),
            ],
          ),
        ],
      ),
      GoRoute(
        path: '/orders/:id',
        builder: (context, state) =>
            OrderDetailPage(orderId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/orders/:id/grace',
        builder: (context, state) =>
            GracePage(orderId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/notifications',
        builder: (context, state) => const NotificationsPage(),
      ),
      GoRoute(
        path: '/appeals/new',
        builder: (context, state) => const AppealCreatePage(),
      ),
      GoRoute(
        path: '/appeals',
        builder: (context, state) => const AppealsPage(),
      ),
    ],
    errorBuilder: (context, state) => const _NotFoundPage(),
  );
});

class AppShell extends ConsumerWidget {
  const AppShell({super.key, required this.navigationShell});

  final StatefulNavigationShell navigationShell;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final badge = ref.watch(reservationBadgeProvider).value ?? 0;
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: BranchFade(
        index: navigationShell.currentIndex,
        child: navigationShell,
      ),
      bottomNavigationBar: AppTabBar(
        index: navigationShell.currentIndex,
        badgeCount: badge,
        onTap: (index) => navigationShell.goBranch(
          index,
          initialLocation: index == navigationShell.currentIndex,
        ),
      ),
    );
  }
}

class _NotFoundPage extends StatelessWidget {
  const _NotFoundPage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        child: EmptyState(
          title: '页面不存在',
          message: '链接可能已失效，或你没有查看该单据的权限。',
          icon: Icons.link_off,
          actionLabel: '返回借阅',
          onAction: () => context.go('/borrow'),
        ),
      ),
    );
  }
}
