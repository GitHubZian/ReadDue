import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';

class AppTabItem {
  const AppTabItem({
    required this.label,
    required this.icon,
    required this.activeIcon,
  });

  final String label;
  final IconData icon;
  final IconData activeIcon;
}

/// 底栏 4 项，高 64 + 安全区；选中态薄荷底圆角块。
class AppTabBar extends StatelessWidget {
  const AppTabBar({
    super.key,
    required this.index,
    required this.onTap,
  });

  final int index;
  final ValueChanged<int> onTap;

  static const items = [
    AppTabItem(
      label: '书架',
      icon: Icons.menu_book_outlined,
      activeIcon: Icons.menu_book,
    ),
    AppTabItem(
      label: '检索',
      icon: Icons.search,
      activeIcon: Icons.search,
    ),
    AppTabItem(
      label: '预约',
      icon: Icons.calendar_today_outlined,
      activeIcon: Icons.calendar_today,
    ),
    AppTabItem(
      label: '我的',
      icon: Icons.person_outline,
      activeIcon: Icons.person,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: AppColors.bg,
        border: Border(top: BorderSide(color: AppColors.line2)),
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 64,
          child: Row(
            children: [
              for (var i = 0; i < items.length; i++)
                Expanded(
                  child: Semantics(
                    button: true,
                    selected: i == index,
                    label: items[i].label,
                    child: InkWell(
                      onTap: () => onTap(i),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Container(
                            width: 44,
                            height: 30,
                            alignment: Alignment.center,
                            decoration: BoxDecoration(
                              color: i == index
                                  ? AppColors.mintSoft
                                  : Colors.transparent,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              i == index ? items[i].activeIcon : items[i].icon,
                              size: 22,
                              color:
                                  i == index ? AppColors.ink : AppColors.ink3,
                            ),
                          ),
                          const SizedBox(height: 3),
                          Text(
                            items[i].label,
                            style: AppText.micro.copyWith(
                              fontSize: 11.5,
                              fontWeight: i == index
                                  ? FontWeight.w600
                                  : FontWeight.w400,
                              color:
                                  i == index ? AppColors.ink : AppColors.ink3,
                              height: 1.2,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
