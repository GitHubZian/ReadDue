import '../../../core/models/models.dart';

/// 检索结果的客户端排序：后端只返回相关度顺序，其余两种在本地排序。
enum BookSort {
  relevance('按相关度', '保持后端相关度顺序，最匹配关键词的排前面。'),
  availableFirst('可借优先', '有在架副本的书排前面，其余保持相关度顺序。'),
  titleAsc('书名 A-Z', '按书名字符顺序排列，同序时保持相关度。');

  const BookSort(this.label, this.description);

  final String label;
  final String description;
}

List<BookItem> sortBooks(List<BookItem> items, BookSort sort) {
  if (sort == BookSort.relevance) return List.of(items);
  final indexed = [for (var i = 0; i < items.length; i++) (i, items[i])];
  indexed.sort((a, b) {
    int byMode;
    if (sort == BookSort.availableFirst) {
      byMode = (a.$2.availableCopies > 0 ? 0 : 1)
          .compareTo(b.$2.availableCopies > 0 ? 0 : 1);
    } else {
      byMode = a.$2.title.toLowerCase().compareTo(b.$2.title.toLowerCase());
    }
    return byMode != 0 ? byMode : a.$1.compareTo(b.$1);
  });
  return [for (final entry in indexed) entry.$2];
}
