import '../../../core/models/models.dart';
import '../../../core/mock/mock_backend.dart';

abstract class SearchRepository {
  Future<SearchPageData> search(String query, String filter);

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
  Future<void> borrow(String bookId) => _backend.borrowBook(bookId);
}
