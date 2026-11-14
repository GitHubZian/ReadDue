import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/order_repository.dart';

/// 真后端没有单条详情接口，从 /v1/borrows/self 中取同一单据。
class DioOrderRepository implements OrderRepository {
  DioOrderRepository(this._client);

  final ApiClient _client;

  @override
  Future<OrderDetail> fetchOrder(String orderId) async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myBorrows,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final items = (page['items'] as List<dynamic>? ?? const []);
    final match = items.whereType<Map<String, dynamic>>().where(
          (json) => (json['id'] ?? '').toString() == orderId,
        );
    if (match.isEmpty) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '单据不存在或无权查看',
        statusCode: 404,
      );
    }
    var gracePending = false;
    try {
      final graces = await _client.get<Map<String, dynamic>>(
        Endpoints.myGraces,
        query: {'page': 1, 'size': 100},
        decode: (data) => data as Map<String, dynamic>,
      );
      final list = (graces['items'] as List<dynamic>? ?? const []);
      gracePending = list.whereType<Map<String, dynamic>>().any(
            (json) =>
                (json['borrowOrderId'] ?? '').toString() == orderId &&
                (json['status'] ?? '') == 'PENDING',
          );
    } catch (_) {
      // 宽限状态只影响按钮，失败不阻塞详情。
    }
    return orderDetailFromJson(match.first, gracePending: gracePending);
  }
}
