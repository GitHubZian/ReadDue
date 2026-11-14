import '../../../core/models/models.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/endpoints.dart';
import '../../../core/network/reader_mappers.dart';
import '../domain/grace_repository.dart';

/// 真后端宽限：场景字典由客户端给（后端无 /graces/scenes），
/// 预计应还日由馆员核定，不在端上计算。
class DioGraceRepository implements GraceRepository {
  DioGraceRepository(this._client);

  final ApiClient _client;

  static const _scenes = <GraceScene>[
    GraceScene(
      code: 'GRADUATION',
      label: '毕业离校',
      description: '毕业离校前仍需使用',
      baseDays: 14,
    ),
    GraceScene(
      code: 'THESIS',
      label: '毕业论文',
      description: '论文写作与答辩期间',
      baseDays: 10,
    ),
    GraceScene(
      code: 'EXAM',
      label: '集中考试',
      description: '考试周复习与考试',
      baseDays: 7,
    ),
    GraceScene(
      code: 'NORMAL',
      label: '合理事由',
      description: '实习、出差等其他合理情况',
      baseDays: 3,
    ),
  ];

  static const _labels = {
    'GRADUATION': '毕业离校',
    'THESIS': '毕业论文',
    'EXAM': '集中考试',
    'NORMAL': '合理事由',
  };

  @override
  Future<GracePageData> fetchPage(String orderId) async {
    final page = await _client.get<Map<String, dynamic>>(
      Endpoints.myGraces,
      query: {'page': 1, 'size': 100},
      decode: (data) => data as Map<String, dynamic>,
    );
    final items = (page['items'] as List<dynamic>? ?? const [])
        .whereType<Map<String, dynamic>>()
        .where((json) => (json['borrowOrderId'] ?? '').toString() == orderId)
        .toList();
    final history = items.map((json) {
      final scene = (json['scene'] ?? '').toString();
      return GraceApplication(
        id: (json['id'] ?? '').toString(),
        applyNo: (json['code'] ?? '').toString(),
        sceneCode: scene,
        sceneLabel: _labels[scene] ?? scene,
        status: (json['status'] ?? 'PENDING').toString(),
        submittedAt: parseTime(json['createdAt']) ?? DateTime.now(),
        grantedDays: int.tryParse((json['approvedDays'] ?? '').toString()),
        attachments: const [],
      );
    }).toList();
    return GracePageData(
      orderId: orderId,
      bookTitle: '',
      scenes: _scenes,
      history: history,
      hasPending: history.any((item) => item.status == 'PENDING'),
    );
  }

  @override
  Future<GraceSubmitResult> submit(GraceSubmitRequest request) async {
    final data = await _client.post<Map<String, dynamic>>(
      Endpoints.graces,
      body: {
        'borrowOrderId': request.orderId,
        'sceneCode': request.sceneCode,
        'evidenceText': request.description,
      },
      decode: (data) => (data as Map<String, dynamic>?) ?? const {},
    );
    return GraceSubmitResult(
      applyNo: (data['code'] ?? '').toString(),
      status: (data['status'] ?? 'PENDING').toString(),
      slaDueAt: parseTime(data['slaDueAt']),
    );
  }
}
