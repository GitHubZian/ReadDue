import '../../../core/models/models.dart';

/// C8 申诉：工单列表、类型字典、可关联单据与提交。
abstract class AppealRepository {
  Future<AppealsPageData> fetchAppeals();

  Future<List<AppealType>> fetchTypes();

  Future<List<BorrowItem>> fetchOrderOptions();

  Future<AppealCreateResult> submit(AppealCreateRequest request);
}
