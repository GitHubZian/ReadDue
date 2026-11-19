import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class SearchRepository {
  Future<SearchPageData> search(String query, String filter);

  Future<BookDetailData> fetchDetail(String bookId);

  Future<void> borrow(String bookId);
}

class MockSearchRepository implements SearchRepository {
  MockSearchRepository({MockBackend? backend})
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<SearchPageData> search(String query, String filter) =>
      _backend.search(query, filter);

  @override
  Future<BookDetailData> fetchDetail(String bookId) async {
    final page = await _backend.search('', '全部');
    final book = page.items.firstWhere(
      (b) => b.id == bookId,
      orElse: () => page.items.first,
    );
    return BookDetailData(
      detail: BookDetail(
        id: book.id,
        title: book.title,
        author: book.author,
        publisher: book.publisher,
        isbn13: '978-7-0000-0000-0',
        publishDate: book.year.isEmpty ? '2012-01-01' : '${book.year}-01-01',
        price: '¥ 48.00',
        summary: '馆藏图书，可在馆内借阅或在线预约。',
        tags: const ['馆藏', '可借阅'],
        totalCopies: book.availableCopies + 1,
        availableCopies: book.availableCopies,
        onLoanCopies: 1,
        queueLen: book.queueLen,
      ),
      copies: List.generate(
        book.availableCopies + 1,
        (i) => BookCopy(
          id: '${book.id}-copy-$i',
          barcode: 'BC${book.id.substring(book.id.length - 4)}$i',
          location: '3 楼 A 区',
          status: i < book.availableCopies ? 'AVAILABLE' : 'ON_LOAN',
        ),
      ),
      queue: BookQueue(waiting: book.queueLen),
    );
  }

  @override
  Future<void> borrow(String bookId) => _backend.borrowBook(bookId);
}
