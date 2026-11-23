import 'package:client/core/models/models.dart';
import 'package:client/features/search/domain/book_sort.dart';
import 'package:flutter_test/flutter_test.dart';

BookItem _book(String title, {int available = 0}) => BookItem(
      id: title,
      title: title,
      author: '作者',
      publisher: '出版社',
      availableCopies: available,
      coverLabel: title.substring(0, 1),
    );

void main() {
  test('相关度保持后端顺序', () {
    final items = [_book('B'), _book('A'), _book('C')];
    expect(
      sortBooks(items, BookSort.relevance).map((b) => b.title),
      ['B', 'A', 'C'],
    );
  });

  test('可借优先且同组保持相关度顺序', () {
    final items = [
      _book('无货一'),
      _book('有货一', available: 1),
      _book('无货二'),
      _book('有货二', available: 2),
    ];
    expect(
      sortBooks(items, BookSort.availableFirst).map((b) => b.title),
      ['有货一', '有货二', '无货一', '无货二'],
    );
  });

  test('书名 A-Z 排序', () {
    final items = [_book('C'), _book('A'), _book('B')];
    expect(
      sortBooks(items, BookSort.titleAsc).map((b) => b.title),
      ['A', 'B', 'C'],
    );
  });
}
