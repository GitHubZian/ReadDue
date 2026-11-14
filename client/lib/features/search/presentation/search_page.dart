import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/enter_fade.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/small_button.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import '../../borrow/presentation/borrow_providers.dart';
import 'search_providers.dart';

/// C4 馆藏检索：300ms 防抖、全部/可借/可预约筛选、结果行。
class SearchPage extends ConsumerStatefulWidget {
  const SearchPage({super.key});

  @override
  ConsumerState<SearchPage> createState() => _SearchPageState();
}

class _SearchPageState extends ConsumerState<SearchPage> {
  static const _filters = ['全部', '可借', '可预约'];
  static const _filterValues = ['all', 'available', 'reservable'];

  final _controller = TextEditingController(text: '乡土');
  final _focusNode = FocusNode();
  Timer? _debounce;
  String _query = '乡土';
  int _filter = 0;

  @override
  void initState() {
    super.initState();
    _focusNode.addListener(() {
      if (mounted) setState(() {});
    });
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _onQueryChanged(String value) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 300), () {
      if (!mounted) return;
      setState(() => _query = value.trim());
    });
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final args = (query: _query, filter: _filterValues[_filter]);
    final result = ref.watch(searchProvider(args));
    final total = result.asData?.value.total ?? 0;

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            TabPageHeader(
              title: '检索',
              trailing: AppCard(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
                radius: 12,
                onTap: () => showToast(context, '筛选条件已是最新'),
                child: SizedBox(
                  height: 44,
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.tune, size: 18, color: AppColors.ink),
                      const SizedBox(width: 6),
                      Text(
                        '筛选',
                        style: AppText.caption.copyWith(
                          color: AppColors.ink,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 0),
              child: Container(
                height: 52,
                padding: const EdgeInsets.symmetric(horizontal: 14),
                decoration: BoxDecoration(
                  color: AppColors.card,
                  borderRadius: BorderRadius.circular(AppRadius.field),
                  border: Border.all(
                    color:
                        _focusNode.hasFocus ? AppColors.mint : AppColors.line,
                    width: _focusNode.hasFocus ? 1.5 : 1,
                  ),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.search, size: 20, color: AppColors.ink2),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextField(
                        controller: _controller,
                        focusNode: _focusNode,
                        onChanged: _onQueryChanged,
                        textInputAction: TextInputAction.search,
                        decoration: InputDecoration(
                          hintText: '书名、作者或索书号',
                          hintStyle: AppText.bodySm.copyWith(
                            color: AppColors.ink3,
                          ),
                          border: InputBorder.none,
                          enabledBorder: InputBorder.none,
                          focusedBorder: InputBorder.none,
                          disabledBorder: InputBorder.none,
                          errorBorder: InputBorder.none,
                          focusedErrorBorder: InputBorder.none,
                          isDense: true,
                        ),
                        style: AppText.bodySm.copyWith(fontSize: 14),
                      ),
                    ),
                    if (_controller.text.isNotEmpty)
                      IconButton(
                        onPressed: () {
                          _controller.clear();
                          _debounce?.cancel();
                          setState(() => _query = '');
                        },
                        tooltip: '清空',
                        icon: const Icon(Icons.close, size: 17),
                        color: AppColors.ink3,
                      ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                children: [
                  AppFilterTabs(
                    labels: _filters,
                    index: _filter,
                    onChanged: (index) => setState(() => _filter = index),
                  ),
                  const Spacer(),
                  Text(
                    '找到 $total 本 · ',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                  Text(
                    '按相关度 ⌄',
                    style: AppText.caption.copyWith(
                      color: AppColors.mintDeep,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            Expanded(
              child: AsyncBody<SearchPageData>(
                value: result,
                onRetry: () => ref.invalidate(searchProvider(args)),
                skeleton: const SkeletonList(count: 3, itemHeight: 108),
                isEmpty: (data) => data.items.isEmpty,
                empty: EmptyState(
                  title: '没有找到相关馆藏',
                  message: '试试书名、作者或索书号；也可以切换筛选条件。',
                  actionLabel: '清空关键词',
                  onAction: () {
                    _controller.clear();
                    setState(() => _query = '');
                  },
                ),
                builder: (data) => ListView.separated(
                  padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
                  itemCount: data.items.length + 1,
                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                  itemBuilder: (context, index) {
                    if (index == data.items.length) {
                      return Column(
                        children: [
                          AppCard(
                            onTap: () => showToast(context, '已加载全部结果'),
                            child: SizedBox(
                              height: 28,
                              child: Center(
                                child: Text(
                                  '查看全部 $total 条结果  →',
                                  style: AppText.bodySm.copyWith(
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 12),
                          Text(
                            '检索支持书名 / 作者 / 索书号，结果按库存实时更新',
                            textAlign: TextAlign.center,
                            style:
                                AppText.caption.copyWith(color: AppColors.ink3),
                          ),
                        ],
                      );
                    }
                    return EnterFade(
                      index: index,
                      child: _BookRow(
                        book: data.items[index],
                        onAction: () => _onAction(data.items[index]),
                      ),
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _onAction(BookItem book) async {
    if (book.status == 'AVAILABLE') {
      final ok = await showConfirmDialog(
        context,
        title: '借阅《${book.title}》',
        message: '确认借阅？到馆后凭校园卡取书，应还日以服务端为准。',
        confirmLabel: '确认借阅',
      );
      if (ok && mounted) {
        try {
          await ref.read(searchRepositoryProvider).borrow(book.id);
          if (!mounted) return;
          ref.invalidate(borrowDashboardProvider);
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('已借出《${book.title}》'),
              action: SnackBarAction(
                label: '查看我的借阅',
                textColor: AppColors.mintSoft,
                onPressed: () => context.go('/borrow'),
              ),
            ),
          );
        } catch (error) {
          if (!mounted) return;
          showToast(context, messageForError(error));
        }
      }
      return;
    }
    if (book.status == 'EBOOK') {
      showToast(context, '正在打开电子书阅读器');
      return;
    }
    showToast(context, '已加入预约队列，排位以服务端为准');
  }
}

class _BookRow extends StatelessWidget {
  const _BookRow({required this.book, required this.onAction});

  final BookItem book;
  final VoidCallback onAction;

  @override
  Widget build(BuildContext context) {
    final (pillLabel, tone, action) = switch (book.status) {
      'ON_LOAN' => ('已借出', PillTone.slate, '预约'),
      'EBOOK' => ('电子书', PillTone.amber, '阅读'),
      'RESERVABLE' => ('可预约', PillTone.slate, '预约'),
      _ => ('可借', PillTone.mint, '借阅'),
    };
    return AppCard(
      padding: const EdgeInsets.all(12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          BookCover(
            label: book.coverLabel,
            tone: switch (book.coverTone) {
              'amber' => CoverTone.amber,
              'slate' => CoverTone.slate,
              _ => CoverTone.mint,
            },
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  book.title,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: AppText.bodySm.copyWith(
                    fontSize: 17,
                    fontWeight: FontWeight.w700,
                    height: 1.3,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  '${book.author} · ${book.publisher} · ${book.year}',
                  style: AppText.caption.copyWith(color: AppColors.ink2),
                ),
                const SizedBox(height: 3),
                Text(
                  book.callNo,
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
                const SizedBox(height: 8),
                Row(
                  children: [
                    StatusPill(label: pillLabel, tone: tone),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        book.libraryLabel,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: AppText.caption.copyWith(color: AppColors.ink3),
                      ),
                    ),
                    const SizedBox(width: 8),
                    SmallButton(
                      label: action,
                      onPressed: onAction,
                      filled: action == '借阅',
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
