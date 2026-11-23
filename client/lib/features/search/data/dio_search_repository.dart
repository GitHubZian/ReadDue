import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/search_repository.dart';

/// 真后端检索：/v1/books 四端点 + 借出。
class DioSearchRepository implements SearchRepository {
  DioSearchRepository(this._client);

  final ApiClient _client;

  @override
  Future<SearchPageData> search(String query, String filter) async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.books,
      query: {'q': query, 'page': 1, 'size': 50},
      decode: (data) => data as Map<String, dynamic>,
    );
    final all = (page['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .map(bookItemFromJson)
        .toList();
    final items = switch (filter) {
      'available' || '可借' => all.where((b) => b.availableCopies > 0).toList(),
      'reservable' ||
      '可预约' =>
        all.where((b) => b.availableCopies == 0).toList(),
      _ => all,
    };
    return SearchPageData(
      total: items.length,
      items: items,
      query: query,
    );
  }

  @override
  Future<BookDetailData> fetchDetail(String bookId) async {
    final detailJson = await _client.get<Map<String, dynamic>>(
      Endpoints.book(bookId),
      decode: (data) => data as Map<String, dynamic>,
    );
    final copiesJson = await _client.get<List<dynamic>>(
      Endpoints.bookCopies(bookId),
      decode: (data) => data as List<dynamic>,
    );
    final queueJson = await _client.get<Map<String, dynamic>>(
      Endpoints.bookQueue(bookId),
      decode: (data) => data as Map<String, dynamic>,
    );
    return BookDetailData(
      detail: bookDetailFromJson(detailJson),
      copies: copiesJson
          .whereType<Map<String, dynamic>>()
          .map(
            (json) => BookCopy(
              id: (json['id'] ?? '').toString(),
              barcode: (json['barcode'] ?? '').toString(),
              location: (json['location'] ?? '').toString(),
              status: (json['status'] ?? 'AVAILABLE').toString(),
            ),
          )
          .toList(),
      queue: BookQueue(
        waiting: (queueJson['waiting'] as num?)?.toInt() ?? 0,
        myQueueNo: (queueJson['myQueueNo'] as num?)?.toInt(),
        myStatus: queueJson['myStatus']?.toString(),
      ),
    );
  }

  @override
  Future<void> borrow(String bookId) async {
    final detail = await fetchDetail(bookId);
    BookCopy? available;
    for (final copy in detail.copies) {
      if (copy.status == 'AVAILABLE') {
        available = copy;
        break;
      }
    }
    if (available == null) {
      throw const ApiException(
        code: 'COPY_UNAVAILABLE',
        message: '该副本当前不可借出',
        statusCode: 422,
      );
    }
    await _client.post<void>(
      Endpoints.borrows,
      body: {'readerNo': _client.readerNo, 'barcode': available.barcode},
      decode: (_) {},
    );
  }

  @override
  Future<int> reserve(String bookId) async {
    final data = await _client.post<Map<String, dynamic>>(
      Endpoints.reservations,
      body: {'bookId': int.tryParse(bookId) ?? bookId},
      decode: (data) => data as Map<String, dynamic>,
    );
    return int.tryParse((data['queuePosition'] ?? 0).toString()) ?? 0;
  }
}
