import 'package:freezed_annotation/freezed_annotation.dart';

part 'models.freezed.dart';
part 'models.g.dart';

/// 读者端跨 feature 的只读模型。ID 一律 String，时间由服务端给 ISO-8601。
@freezed
class Session with _$Session {
  const factory Session({
    required String token,
    required String userId,
    required String name,
    required String studentNo,
  }) = _Session;

  factory Session.fromJson(Map<String, dynamic> json) =>
      _$SessionFromJson(json);
}

@freezed
class BorrowSummary with _$BorrowSummary {
  const factory BorrowSummary({
    @Default(0) int borrowing,
    @Default(0) int dueSoon,
    @Default(0) int overdue,
    @Default(0) int returnedThisTerm,
    @Default('信用良好') String creditLabel,
    @Default(0) int quotaUsed,
    @Default(0) int quotaTotal,
    @Default(false) bool blacklisted,
    String? blacklistReason,
  }) = _BorrowSummary;

  factory BorrowSummary.fromJson(Map<String, dynamic> json) =>
      _$BorrowSummaryFromJson(json);
}

@freezed
class BorrowItem with _$BorrowItem {
  const factory BorrowItem({
    required String id,
    required String orderNo,
    required String title,
    required String author,
    @Default('') String publisher,
    @Default('') String callNo,
    @Default('') String shelfCode,
    required String coverLabel,
    @Default('mint') String coverTone,
    required DateTime borrowedAt,
    required DateTime dueAt,
    DateTime? effectiveDueAt,
    @Default(0) int overdueDays,
    @Default(0) int daysLeft,
    @Default(0) int renewCount,
    @Default('') String dueLabel,
    @Default('BORROWED') String status,
    @Default(true) bool canRenew,
    @Default(false) bool gracePending,
  }) = _BorrowItem;

  factory BorrowItem.fromJson(Map<String, dynamic> json) =>
      _$BorrowItemFromJson(json);
}

@freezed
class ReadyBook with _$ReadyBook {
  const factory ReadyBook({
    required String reservationId,
    required String title,
    required String author,
    required String pickupPoint,
    required String pickupCode,
    required DateTime expiresAt,
    @Default(0) int remainingHours,
    @Default(1) int queueNo,
  }) = _ReadyBook;

  factory ReadyBook.fromJson(Map<String, dynamic> json) =>
      _$ReadyBookFromJson(json);
}

@freezed
class BorrowDashboard with _$BorrowDashboard {
  const factory BorrowDashboard({
    required BorrowSummary summary,
    @Default(<BorrowItem>[]) List<BorrowItem> dueSoon,
    @Default(<ReadyBook>[]) List<ReadyBook> readyBooks,
    @Default('22:00–08:00') String quietHoursText,
    @Default('逾期费用 ¥1.00 / 天 · 归还时一次结清') String feeNote,
    @Default(0) int unreadCount,
    @Default(false) bool isOffline,
    DateTime? lastSyncAt,
  }) = _BorrowDashboard;

  factory BorrowDashboard.fromJson(Map<String, dynamic> json) =>
      _$BorrowDashboardFromJson(json);
}

@freezed
class TimelineEvent with _$TimelineEvent {
  const factory TimelineEvent({
    required String id,
    DateTime? time,
    required String title,
    String? subtitle,
    @Default('done') String state,
  }) = _TimelineEvent;

  factory TimelineEvent.fromJson(Map<String, dynamic> json) =>
      _$TimelineEventFromJson(json);
}

@freezed
class OrderDetail with _$OrderDetail {
  const factory OrderDetail({
    required String id,
    required String orderNo,
    required String title,
    required String author,
    required String publisher,
    required String callNo,
    required String barcode,
    required String coverLabel,
    @Default('mint') String coverTone,
    @Default('BORROWED') String status,
    @Default(0) int overdueDays,
    required DateTime borrowedAt,
    required DateTime dueAt,
    DateTime? effectiveDueAt,
    @Default(0) int graceDays,
    @Default('¥1.00 / 天') String feePerDay,
    String? feeEstimate,
    @Default(false) bool gracePending,
    @Default(false) bool canGrace,
    @Default(false) bool blacklisted,
    DateTime? returnedAt,
    @Default('待归还 · 归还后自动核算，减免费用需馆员操作并留痕') String settleNote,
    @Default(<TimelineEvent>[]) List<TimelineEvent> timeline,
    @Default(false) bool isOffline,
  }) = _OrderDetail;

  factory OrderDetail.fromJson(Map<String, dynamic> json) =>
      _$OrderDetailFromJson(json);
}

@freezed
class GraceScene with _$GraceScene {
  const factory GraceScene({
    required String code,
    required String label,
    @Default('') String description,
    @Default(0) int baseDays,
  }) = _GraceScene;

  factory GraceScene.fromJson(Map<String, dynamic> json) =>
      _$GraceSceneFromJson(json);
}

@freezed
class GraceEstimate with _$GraceEstimate {
  const factory GraceEstimate({
    required DateTime originalDueAt,
    required DateTime estimatedDueAt,
    @Default(0) int days,
  }) = _GraceEstimate;

  factory GraceEstimate.fromJson(Map<String, dynamic> json) =>
      _$GraceEstimateFromJson(json);
}

@freezed
class GraceApplication with _$GraceApplication {
  const factory GraceApplication({
    required String id,
    required String applyNo,
    @Default('') String sceneCode,
    @Default('') String sceneLabel,
    @Default('PENDING') String status,
    required DateTime submittedAt,
    DateTime? decidedAt,
    int? grantedDays,
    String? rejectCode,
    String? evidenceText,
    @Default(<String>[]) List<String> attachments,
  }) = _GraceApplication;

  factory GraceApplication.fromJson(Map<String, dynamic> json) =>
      _$GraceApplicationFromJson(json);
}

@freezed
class GracePageData with _$GracePageData {
  const factory GracePageData({
    required String orderId,
    required String bookTitle,
    @Default(<GraceScene>[]) List<GraceScene> scenes,
    GraceEstimate? estimate,
    @Default(<GraceApplication>[]) List<GraceApplication> history,
    @Default(false) bool hasPending,
    @Default(false) bool isOffline,
  }) = _GracePageData;

  factory GracePageData.fromJson(Map<String, dynamic> json) =>
      _$GracePageDataFromJson(json);
}

@freezed
class GraceSubmitRequest with _$GraceSubmitRequest {
  const factory GraceSubmitRequest({
    required String orderId,
    required String sceneCode,
    required String description,
    @Default(<String>[]) List<String> attachments,
  }) = _GraceSubmitRequest;

  factory GraceSubmitRequest.fromJson(Map<String, dynamic> json) =>
      _$GraceSubmitRequestFromJson(json);
}

@freezed
class GraceSubmitResult with _$GraceSubmitResult {
  const factory GraceSubmitResult({
    required String applyNo,
    @Default('PENDING') String status,
    DateTime? slaDueAt,
  }) = _GraceSubmitResult;

  factory GraceSubmitResult.fromJson(Map<String, dynamic> json) =>
      _$GraceSubmitResultFromJson(json);
}

@freezed
class BookItem with _$BookItem {
  const factory BookItem({
    required String id,
    required String title,
    required String author,
    required String publisher,
    @Default('') String year,
    @Default('') String callNo,
    @Default('') String libraryLabel,
    @Default('AVAILABLE') String status,
    @Default(0) int availableCopies,
    @Default(0) int queueLen,
    DateTime? estimatedAvailableAt,
    required String coverLabel,
    @Default('mint') String coverTone,
  }) = _BookItem;

  factory BookItem.fromJson(Map<String, dynamic> json) =>
      _$BookItemFromJson(json);
}

@freezed
class SearchPageData with _$SearchPageData {
  const factory SearchPageData({
    @Default(0) int total,
    @Default(<BookItem>[]) List<BookItem> items,
    @Default('') String query,
    @Default(false) bool isOffline,
  }) = _SearchPageData;

  factory SearchPageData.fromJson(Map<String, dynamic> json) =>
      _$SearchPageDataFromJson(json);
}

@freezed
class ReservationItem with _$ReservationItem {
  const factory ReservationItem({
    required String id,
    required String bookTitle,
    required String author,
    @Default('') String publisher,
    required String coverLabel,
    @Default('mint') String coverTone,
    @Default('一楼服务台') String pickupPoint,
    @Default('') String pickupCode,
    DateTime? readyAt,
    DateTime? expiresAt,
    @Default(0) int remainingHours,
    @Default(0) int queueNo,
    @Default(0) int queueTotal,
    @Default('READY') String status,
    DateTime? fulfilledAt,
    DateTime? returnedAt,
  }) = _ReservationItem;

  factory ReservationItem.fromJson(Map<String, dynamic> json) =>
      _$ReservationItemFromJson(json);
}

@freezed
class ReservationsPage with _$ReservationsPage {
  const factory ReservationsPage({
    @Default(<ReservationItem>[]) List<ReservationItem> ready,
    @Default(<ReservationItem>[]) List<ReservationItem> completed,
    @Default(<ReservationItem>[]) List<ReservationItem> cancelled,
    @Default('48 小时内未取书将顺延给下一位读者') String ruleNote,
    @Default(false) bool isOffline,
  }) = _ReservationsPage;

  factory ReservationsPage.fromJson(Map<String, dynamic> json) =>
      _$ReservationsPageFromJson(json);
}

@freezed
class AppNotification with _$AppNotification {
  const factory AppNotification({
    required String id,
    required String type,
    required String title,
    required String body,
    required DateTime createdAt,
    @Default(false) bool read,
    @Default('borrow') String category,
  }) = _AppNotification;

  factory AppNotification.fromJson(Map<String, dynamic> json) =>
      _$AppNotificationFromJson(json);
}

@freezed
class NotificationChannels with _$NotificationChannels {
  const factory NotificationChannels({
    @Default(true) bool inbox,
    @Default(true) bool email,
    @Default(true) bool push,
  }) = _NotificationChannels;

  factory NotificationChannels.fromJson(Map<String, dynamic> json) =>
      _$NotificationChannelsFromJson(json);
}

@freezed
class QuietHours with _$QuietHours {
  const factory QuietHours({
    @Default(true) bool enabled,
    @Default('22:00') String start,
    @Default('08:00') String end,
  }) = _QuietHours;

  factory QuietHours.fromJson(Map<String, dynamic> json) =>
      _$QuietHoursFromJson(json);
}

@freezed
class NotificationsPageData with _$NotificationsPageData {
  const factory NotificationsPageData({
    @Default(<AppNotification>[]) List<AppNotification> items,
    @Default(0) int unreadCount,
    @Default(QuietHours()) QuietHours quietHours,
    @Default(NotificationChannels()) NotificationChannels channels,
    @Default(false) bool isOffline,
  }) = _NotificationsPageData;

  factory NotificationsPageData.fromJson(Map<String, dynamic> json) =>
      _$NotificationsPageDataFromJson(json);
}

@freezed
class ProfileData with _$ProfileData {
  const factory ProfileData({
    required String name,
    required String studentNo,
    @Default('') String department,
    @Default('本科') String degree,
    @Default('信用良好') String creditLabel,
    @Default(0) int quotaUsed,
    @Default(0) int quotaTotal,
    @Default(0) int borrowingCount,
    @Default(0) int totalBorrowed,
    @Default(0) int reservationCount,
    @Default(0) int returnedThisTerm,
    @Default(0) int overdueCount,
    @Default(1) int maxRenew,
    @Default(30) int loanDays,
    @Default(0) int unreadCount,
    @Default('1.0.0') String version,
    @Default(false) bool blacklisted,
    String? blacklistReason,
    @Default(false) bool isOffline,
  }) = _ProfileData;

  factory ProfileData.fromJson(Map<String, dynamic> json) =>
      _$ProfileDataFromJson(json);
}

@freezed
class AppealType with _$AppealType {
  const factory AppealType({
    required String code,
    required String label,
    @Default('') String description,
  }) = _AppealType;

  factory AppealType.fromJson(Map<String, dynamic> json) =>
      _$AppealTypeFromJson(json);
}

@freezed
class AppealTicket with _$AppealTicket {
  const factory AppealTicket({
    required String id,
    required String ticketNo,
    required String type,
    required String typeLabel,
    @Default('') String bookTitle,
    @Default('OPEN') String status,
    @Default('待处理') String statusLabel,
    @Default('') String description,
    String? rejectReason,
    required DateTime createdAt,
    DateTime? slaDueAt,
    int? compensationDays,
    String? assigneeName,
  }) = _AppealTicket;

  factory AppealTicket.fromJson(Map<String, dynamic> json) =>
      _$AppealTicketFromJson(json);
}

@freezed
class AppealsPageData with _$AppealsPageData {
  const factory AppealsPageData({
    @Default(<AppealTicket>[]) List<AppealTicket> tickets,
    @Default(0) int openCount,
    @Default(0) int doneCount,
    @Default(2) int slaHours,
    @Default(false) bool isOffline,
  }) = _AppealsPageData;

  factory AppealsPageData.fromJson(Map<String, dynamic> json) =>
      _$AppealsPageDataFromJson(json);
}

@freezed
class AppealCreateRequest with _$AppealCreateRequest {
  const factory AppealCreateRequest({
    required String orderId,
    required String type,
    required String description,
    @Default(<String>[]) List<String> attachments,
  }) = _AppealCreateRequest;

  factory AppealCreateRequest.fromJson(Map<String, dynamic> json) =>
      _$AppealCreateRequestFromJson(json);
}

@freezed
class AppealCreateResult with _$AppealCreateResult {
  const factory AppealCreateResult({
    required String ticketNo,
    @Default('OPEN') String status,
    DateTime? slaDueAt,
  }) = _AppealCreateResult;

  factory AppealCreateResult.fromJson(Map<String, dynamic> json) =>
      _$AppealCreateResultFromJson(json);
}
