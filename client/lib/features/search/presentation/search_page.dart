import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/enter_fade.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/small_button.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import '../../borrow/presentation/borrow_providers.dart';
import '../../reservation/presentation/reservation_providers.dart';
import '../domain/book_sort.dart';
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

  late final String _initialQuery;
  late final TextEditingController _controller;
  final _focusNode = FocusNode();
  Timer? _debounce;
  late String _query;
  int _filter = 0;
  BookSort _sort = BookSort.relevance;
  final Map<String, int> _reserved = {};
  bool _reserveBlocked = false;
  bool _reserving = false;

  @override
  void initState() {
    super.initState();
    _initialQuery = ref.read(appUseMockProvider) ? '乡土' : '';
    _controller = TextEditingController(text: _initialQuery);
    _query = _initialQuery;
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

  Future<void> _showFilterSheet() async {
    final selected = await showModalBottomSheet<int>(
      context: context,
      useRootNavigator: true,
      backgroundColor: AppColors.bg,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
      ),
      builder: (sheetContext) => SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _sheetHandle(),
              const SizedBox(height: 16),
              const Text('筛选结果', style: AppText.h3),
              const SizedBox(height: 6),
              Text(
                '按「可借状态」筛选：可借＝有在架副本；可预约＝已全部借出、可排队。',
                style: AppText.caption.copyWith(
                  color: AppColors.ink2,
                  height: 1.6,
                ),
              ),
              const SizedBox(height: 10),
              for (var i = 0; i < _filters.length; i++)
                _SheetOption(
                  label: _filters[i],
                  subtitle: switch (i) {
                    1 => '只看当前有在架副本的书',
                    2 => '只看已全部借出、可排队的书',
                    _ => '不限可借状态',
                  },
                  selected: _filter == i,
                  onTap: () => Navigator.of(sheetContext).pop(i),
                ),
            ],
          ),
        ),
      ),
    );
    if (selected != null && mounted) setState(() => _filter = selected);
  }

  Future<void> _showSortSheet() async {
    final selected = await showModalBottomSheet<BookSort>(
      context: context,
      useRootNavigator: true,
      backgroundColor: AppColors.bg,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
      ),
      builder: (sheetContext) => SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _sheetHandle(),
              const SizedBox(height: 16),
              const Text('排序方式', style: AppText.h3),
              const SizedBox(height: 6),
              Text(
                '排序只影响当前结果列表，不改变检索关键词。',
                style: AppText.caption.copyWith(color: AppColors.ink2),
              ),
              const SizedBox(height: 10),
              for (final sort in BookSort.values)
                _SheetOption(
                  label: sort.label,
                  subtitle: sort.description,
                  selected: _sort == sort,
                  onTap: () => Navigator.of(sheetContext).pop(sort),
                ),
            ],
          ),
        ),
      ),
    );
    if (selected != null && mounted) setState(() => _sort = selected);
  }

  Widget _sheetHandle() {
    return Center(
      child: Container(
        width: 40,
        height: 4,
        decoration: BoxDecoration(
          color: AppColors.line,
          borderRadius: BorderRadius.circular(99),
        ),
      ),
    );
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
                onTap: _showFilterSheet,
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
                  const SizedBox(width: 8),
                  Expanded(
                    child: FittedBox(
                      alignment: Alignment.centerRight,
                      fit: BoxFit.scaleDown,
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            '找到 $total 本 · ',
                            style:
                                AppText.caption.copyWith(color: AppColors.ink3),
                          ),
                          InkWell(
                            onTap: _showSortSheet,
                            borderRadius: BorderRadius.circular(6),
                            child: Padding(
                              padding: const EdgeInsets.symmetric(vertical: 4),
                              child: Text(
                                '${_sort.label} ⌄',
                                style: AppText.caption.copyWith(
                                  color: AppColors.mintDeep,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
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
                empty: LayoutBuilder(
                  builder: (context, constraints) => SingleChildScrollView(
                    padding: EdgeInsets.only(
                      bottom: MediaQuery.viewInsetsOf(context).bottom,
                    ),
                    child: ConstrainedBox(
                      constraints:
                          BoxConstraints(minHeight: constraints.maxHeight),
                      child: EmptyState(
                        title: '没有找到相关馆藏',
                        message: '试试书名、作者或索书号；也可以切换筛选条件。',
                        actionLabel: '清空关键词',
                        onAction: () {
                          _controller.clear();
                          setState(() => _query = '');
                        },
                      ),
                    ),
                  ),
                ),
                builder: (data) {
                  final items = sortBooks(data.items, _sort);
                  return ListView.separated(
                    padding: const EdgeInsets.fromLTRB(20, 4, 20, 20),
                    itemCount: items.length + 1,
                    separatorBuilder: (_, __) => const SizedBox(height: 10),
                    itemBuilder: (context, index) {
                      if (index == items.length) {
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
                              style: AppText.caption
                                  .copyWith(color: AppColors.ink3),
                            ),
                          ],
                        );
                      }
                      return EnterFade(
                        index: index,
                        child: _BookRow(
                          book: items[index],
                          reservedPosition: _reserved[items[index].id],
                          reserveEnabled: !_reserveBlocked,
                          onTap: () => _openDetail(items[index]),
                          onAction: () => _onAction(items[index]),
                        ),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _openDetail(BookItem book) async {
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useRootNavigator: true,
      backgroundColor: AppColors.bg,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
      ),
      builder: (_) => _BookDetailSheet(
        book: book,
        onBorrow: _borrow,
        onReserve: _reserve,
        reservedPosition: _reserved[book.id],
        reserveEnabled: !_reserveBlocked,
      ),
    );
  }

  Future<void> _onAction(BookItem book) async {
    if (book.status == 'AVAILABLE') {
      await _borrow(book);
      return;
    }
    if (book.status == 'EBOOK') {
      showToast(context, '正在打开电子书阅读器');
      return;
    }
    await _reserve(book);
  }

  Future<void> _reserve(BookItem book) async {
    if (_reserved.containsKey(book.id) || _reserving) return;
    if (_reserveBlocked) {
      showToast(context, '预约服务暂不可用，请稍后重试');
      return;
    }
    final ok = await _confirmReserve(book);
    if (!ok || !mounted) return;
    _reserving = true;
    try {
      final position =
          await ref.read(searchRepositoryProvider).reserve(book.id);
      if (!mounted) return;
      setState(() => _reserved[book.id] = position);
      ref.invalidate(reservationsProvider);
      ref.invalidate(reservationBadgeProvider);
      showToast(
        context,
        position > 0 ? '已预约《${book.title}》，排位 $position' : '已预约《${book.title}》',
      );
    } catch (error) {
      if (!mounted) return;
      if (error is ApiException && (error.isNetwork || error.isServerError)) {
        setState(() => _reserveBlocked = true);
      }
      showToast(context, messageForError(error));
    } finally {
      _reserving = false;
    }
  }

  Future<bool> _confirmReserve(BookItem book) async {
    final result = await showModalBottomSheet<bool>(
      context: context,
      useRootNavigator: true,
      backgroundColor: AppColors.bg,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
      ),
      builder: (sheetContext) => SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.line,
                    borderRadius: BorderRadius.circular(99),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Text('预约《${book.title}》', style: AppText.h3),
              const SizedBox(height: 8),
              Text(
                '预计排在第 ${book.queueLen + 1} 位；到书后保留 48 小时，逾期自动顺延下一位。',
                style: AppText.caption.copyWith(
                  color: AppColors.ink2,
                  height: 1.6,
                ),
              ),
              const SizedBox(height: 18),
              SizedBox(
                width: double.infinity,
                child: PrimaryButton(
                  label: '确认预约',
                  onPressed: () => Navigator.of(sheetContext).pop(true),
                ),
              ),
              Center(
                child: TextButton(
                  onPressed: () => Navigator.of(sheetContext).pop(false),
                  child: const Text('再想想'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
    return result ?? false;
  }

  Future<void> _borrow(BookItem book) async {
    final ok = await showConfirmDialog(
      context,
      title: '借阅《${book.title}》',
      message: '确认借阅？到馆后凭校园卡取书，应还日以服务端为准。',
      confirmLabel: '确认借阅',
    );
    if (!ok || !mounted) return;
    try {
      await ref.read(searchRepositoryProvider).borrow(book.id);
      if (!mounted) return;
      ref.invalidate(borrowDashboardProvider);
      ref.invalidate(
        searchProvider((query: _query, filter: _filterValues[_filter])),
      );
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
}

class _BookRow extends StatelessWidget {
  const _BookRow({
    required this.book,
    required this.reservedPosition,
    required this.reserveEnabled,
    required this.onTap,
    required this.onAction,
  });

  final BookItem book;
  final int? reservedPosition;
  final bool reserveEnabled;
  final VoidCallback onTap;
  final VoidCallback onAction;

  @override
  Widget build(BuildContext context) {
    final (baseLabel, tone, action) = switch (book.status) {
      'ON_LOAN' => ('已借出', PillTone.slate, '预约'),
      'EBOOK' => ('电子书', PillTone.amber, '阅读'),
      'RESERVABLE' => ('可预约', PillTone.slate, '预约'),
      _ => ('可借', PillTone.mint, '借阅'),
    };
    final reserved = reservedPosition != null;
    final pillLabel = reserved ? '已预约 · 排位 $reservedPosition' : baseLabel;
    final actionLabel = reserved
        ? '已预约'
        : (action == '预约' && !reserveEnabled ? '暂不可预约' : action);
    final actionEnabled = reserved ? false : (action != '预约' || reserveEnabled);
    return AppCard(
      padding: const EdgeInsets.all(12),
      onTap: onTap,
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
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Expanded(
                      child: LayoutBuilder(
                        builder: (context, constraints) => Wrap(
                          spacing: 8,
                          runSpacing: 4,
                          crossAxisAlignment: WrapCrossAlignment.center,
                          children: [
                            StatusPill(label: pillLabel, tone: tone),
                            ConstrainedBox(
                              constraints: BoxConstraints(
                                  maxWidth: constraints.maxWidth),
                              child: Text(
                                book.libraryLabel,
                                softWrap: true,
                                style: AppText.caption
                                    .copyWith(color: AppColors.ink3),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    SmallButton(
                      label: actionLabel,
                      onPressed: actionEnabled ? onAction : null,
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

class _BookDetailSheet extends ConsumerWidget {
  const _BookDetailSheet({
    required this.book,
    required this.onBorrow,
    required this.onReserve,
    required this.reservedPosition,
    required this.reserveEnabled,
  });

  final BookItem book;
  final Future<void> Function(BookItem) onBorrow;
  final Future<void> Function(BookItem) onReserve;
  final int? reservedPosition;
  final bool reserveEnabled;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detail = ref.watch(bookDetailProvider(book.id));
    return SafeArea(
      top: false,
      child: SizedBox(
        height: MediaQuery.sizeOf(context).height * 0.86,
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 10, 20, 24),
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.line,
                  borderRadius: BorderRadius.circular(99),
                ),
              ),
            ),
            const SizedBox(height: 16),
            detail.when(
              loading: () => const SizedBox(
                height: 220,
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (error, _) => ErrorState(
                message: messageForError(error),
                onRetry: () => ref.invalidate(bookDetailProvider(book.id)),
              ),
              data: (data) => Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      BookCover(
                        label: book.coverLabel,
                        tone: book.availableCopies > 0
                            ? CoverTone.mint
                            : CoverTone.slate,
                        width: 72,
                        height: 96,
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              data.detail.title,
                              style: AppText.h3.copyWith(height: 1.25),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              '${data.detail.author} · ${data.detail.publisher}',
                              style: AppText.caption
                                  .copyWith(color: AppColors.ink2),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              'ISBN ${data.detail.isbn13}',
                              style: AppText.caption
                                  .copyWith(color: AppColors.ink3),
                            ),
                            const SizedBox(height: 10),
                            Wrap(
                              spacing: 8,
                              runSpacing: 6,
                              children: [
                                StatusPill(
                                  label: data.detail.availableCopies > 0
                                      ? '可借 ${data.detail.availableCopies} 本'
                                      : '已借出',
                                  tone: data.detail.availableCopies > 0
                                      ? PillTone.mint
                                      : PillTone.slate,
                                ),
                                if (data.detail.queueLen > 0)
                                  StatusPill(
                                    label: '${data.detail.queueLen} 人排队',
                                    tone: PillTone.amber,
                                  ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    data.detail.summary.isEmpty ? '暂无简介。' : data.detail.summary,
                    style: AppText.bodySm.copyWith(
                      color: AppColors.ink2,
                      height: 1.7,
                    ),
                  ),
                  const SizedBox(height: 14),
                  _DetailRow(
                      label: '馆藏', value: '共 ${data.detail.totalCopies} 本'),
                  _DetailRow(
                    label: '可借',
                    value: '${data.detail.availableCopies} 本',
                  ),
                  _DetailRow(
                    label: '在借',
                    value: '${data.detail.onLoanCopies} 本',
                  ),
                  _DetailRow(
                    label: '定价',
                    value: data.detail.price.isEmpty
                        ? '—'
                        : '¥ ${data.detail.price}',
                  ),
                  const SizedBox(height: 12),
                  const Text('馆藏位置', style: AppText.label),
                  const SizedBox(height: 6),
                  for (final copy in data.copies.take(4))
                    Padding(
                      padding: const EdgeInsets.only(bottom: 6),
                      child: Row(
                        children: [
                          Expanded(
                            child: Text(
                              '${copy.location.isEmpty ? '馆藏' : copy.location} · ${copy.barcode}',
                              style: AppText.caption
                                  .copyWith(color: AppColors.ink2),
                            ),
                          ),
                          StatusPill(
                            label: copy.status == 'AVAILABLE' ? '在架' : '借出',
                            tone: copy.status == 'AVAILABLE'
                                ? PillTone.mint
                                : PillTone.slate,
                          ),
                        ],
                      ),
                    ),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    child: Builder(
                      builder: (context) {
                        final canBorrow = data.detail.availableCopies > 0;
                        final reserved = reservedPosition != null;
                        final enabled = reserved || canBorrow || reserveEnabled;
                        return PrimaryButton(
                          label: reserved
                              ? '已预约 · 排位 $reservedPosition'
                              : canBorrow
                                  ? '借阅'
                                  : reserveEnabled
                                      ? '预约'
                                      : '暂不可预约',
                          onPressed: !enabled
                              ? null
                              : () async {
                                  Navigator.of(context).pop();
                                  if (canBorrow) {
                                    await onBorrow(book);
                                  } else {
                                    await onReserve(book);
                                  }
                                },
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  const _DetailRow({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Row(
        children: [
          SizedBox(
            width: 72,
            child: Text(
              label,
              style: AppText.caption.copyWith(color: AppColors.ink3),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: AppText.caption.copyWith(
                color: AppColors.ink,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _SheetOption extends StatelessWidget {
  const _SheetOption({
    required this.label,
    required this.subtitle,
    required this.selected,
    required this.onTap,
  });

  final String label;
  final String subtitle;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 10),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: AppText.bodySm.copyWith(
                      fontWeight: FontWeight.w600,
                      color: AppColors.ink,
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    subtitle,
                    style: AppText.caption.copyWith(
                      color: AppColors.ink3,
                      height: 1.5,
                    ),
                  ),
                ],
              ),
            ),
            if (selected)
              const Icon(
                Icons.check_circle_rounded,
                size: 20,
                color: AppColors.mintDeep,
              ),
          ],
        ),
      ),
    );
  }
}
