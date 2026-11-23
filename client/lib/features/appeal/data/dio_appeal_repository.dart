import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/appeal_repository.dart';

/// 真后端申诉：/v1/appeals + /v1/appeals/self。附件先传 /v1/files，
/// 提交时带 evidenceIds（后端补齐后入库，未补齐时忽略未知字段）。
class DioAppealRepository implements AppealRepository {
  DioAppealRepository(this._client);

  final ApiClient _client;

  static const _types = <AppealType>[
    AppealType(code: 'OVERDUE_TIER', label: '逾期判定'),
    AppealType(code: 'GRACE', label: '宽限结果'),
    AppealType(code: 'RENEW', label: '续借限制'),
    AppealType(code: 'REMINDER', label: '提醒送达'),
    AppealType(code: 'OTHER', label: '其他问题'),
  ];

  static const _typeLabels = {
    'OVERDUE_TIER': '逾期判定',
    'GRACE': '宽限结果',
    'RENEW': '续借限制',
    'REMINDER': '提醒送达',
    'OTHER': '其他问题',
  };

  static const _statusLabels = {
    'OPEN': '待处理',
    'ACKED': '处理中',
    'RESOLVED': '已解决',
    'REJECTED': '已驳回',
    'AUTO_COMPENSATED': '已补偿',
    'CLOSED': '已关闭',
  };

  Future<Map<String, String>> _titles() async {
    try {
      final page = await _client.get<Map<String, dynamic>>(
        Endpoints.myBorrows,
        query: {'page': 1, 'size': 100},
        decode: (data) => data as Map<String, dynamic>,
      );
      final items = (page['items'] as List<dynamic>? ?? const []);
      return {
        for (final json in items.whereType<Map<String, dynamic>>())
          (json['id'] ?? '').toString(): (json['bookTitle'] ?? '').toString(),
      };
    } catch (_) {
      return const {};
    }
  }

  @override
  Future<AppealsPageData> fetchAppeals() async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myAppeals,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final items = (page['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .toList();
    final titles = await _titles();
    final tickets = items.map((json) {
      final status = (json['status'] ?? 'OPEN').toString();
      final type = (json['targetType'] ?? 'OTHER').toString();
      return AppealTicket(
        id: (json['id'] ?? '').toString(),
        ticketNo: (json['code'] ?? json['id'] ?? '').toString(),
        type: type,
        typeLabel: _typeLabels[type] ?? type,
        bookTitle: titles[(json['targetId'] ?? '').toString()] ?? '',
        status: status,
        statusLabel: _statusLabels[status] ?? status,
        description: (json['reason'] ?? '').toString(),
        rejectReason:
            status == 'REJECTED' ? (json['resolution'] ?? '').toString() : null,
        createdAt: parseTime(json['createdAt']) ?? DateTime.now(),
        slaDueAt: parseTime(json['slaDueAt']),
        compensationDays:
            int.tryParse((json['compensationDays'] ?? '').toString()),
      );
    }).toList();
    final open =
        tickets.where((t) => const {'OPEN', 'ACKED'}.contains(t.status)).length;
    return AppealsPageData(
      tickets: tickets,
      openCount: open,
      doneCount: tickets.length - open,
    );
  }

  @override
  Future<List<AppealType>> fetchTypes() async => _types;

  @override
  Future<List<BorrowItem>> fetchOrderOptions() async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myBorrows,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final items = (page['items'] as List<dynamic>? ?? const []);
    return items
        .whereType<Map<String, dynamic>>()
        .map((json) => borrowItemFromJson(json))
        .toList();
  }

  @override
  Future<AppealCreateResult> submit(AppealCreateRequest request) async {
    final data = await _client.post<Map<String, dynamic>>(
      Endpoints.appeals,
      body: {
        'targetType': request.type,
        'targetId': request.orderId,
        'reason': request.description,
        if (request.attachments.isNotEmpty)
          'evidenceIds': request.attachments
              .map(int.tryParse)
              .whereType<int>()
              .toList(growable: false),
      },
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return AppealCreateResult(
      ticketNo: (data['code'] ?? data['id'] ?? '').toString(),
      status: (data['status'] ?? 'OPEN').toString(),
      slaDueAt: parseTime(data['slaDueAt']),
    );
  }
}
