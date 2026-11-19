// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'models.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$SessionImpl _$$SessionImplFromJson(Map<String, dynamic> json) =>
    _$SessionImpl(
      token: json['token'] as String,
      userId: json['userId'] as String,
      name: json['name'] as String,
      studentNo: json['studentNo'] as String,
    );

Map<String, dynamic> _$$SessionImplToJson(_$SessionImpl instance) =>
    <String, dynamic>{
      'token': instance.token,
      'userId': instance.userId,
      'name': instance.name,
      'studentNo': instance.studentNo,
    };

_$BorrowSummaryImpl _$$BorrowSummaryImplFromJson(Map<String, dynamic> json) =>
    _$BorrowSummaryImpl(
      borrowing: (json['borrowing'] as num?)?.toInt() ?? 0,
      dueSoon: (json['dueSoon'] as num?)?.toInt() ?? 0,
      overdue: (json['overdue'] as num?)?.toInt() ?? 0,
      returnedThisTerm: (json['returnedThisTerm'] as num?)?.toInt() ?? 0,
      creditLabel: json['creditLabel'] as String? ?? '信用良好',
      quotaUsed: (json['quotaUsed'] as num?)?.toInt() ?? 0,
      quotaTotal: (json['quotaTotal'] as num?)?.toInt() ?? 0,
      blacklisted: json['blacklisted'] as bool? ?? false,
      blacklistReason: json['blacklistReason'] as String?,
    );

Map<String, dynamic> _$$BorrowSummaryImplToJson(_$BorrowSummaryImpl instance) =>
    <String, dynamic>{
      'borrowing': instance.borrowing,
      'dueSoon': instance.dueSoon,
      'overdue': instance.overdue,
      'returnedThisTerm': instance.returnedThisTerm,
      'creditLabel': instance.creditLabel,
      'quotaUsed': instance.quotaUsed,
      'quotaTotal': instance.quotaTotal,
      'blacklisted': instance.blacklisted,
      'blacklistReason': instance.blacklistReason,
    };

_$BorrowItemImpl _$$BorrowItemImplFromJson(Map<String, dynamic> json) =>
    _$BorrowItemImpl(
      id: json['id'] as String,
      orderNo: json['orderNo'] as String,
      title: json['title'] as String,
      author: json['author'] as String,
      publisher: json['publisher'] as String? ?? '',
      callNo: json['callNo'] as String? ?? '',
      shelfCode: json['shelfCode'] as String? ?? '',
      coverLabel: json['coverLabel'] as String,
      coverTone: json['coverTone'] as String? ?? 'mint',
      borrowedAt: DateTime.parse(json['borrowedAt'] as String),
      dueAt: DateTime.parse(json['dueAt'] as String),
      effectiveDueAt: json['effectiveDueAt'] == null
          ? null
          : DateTime.parse(json['effectiveDueAt'] as String),
      overdueDays: (json['overdueDays'] as num?)?.toInt() ?? 0,
      daysLeft: (json['daysLeft'] as num?)?.toInt() ?? 0,
      renewCount: (json['renewCount'] as num?)?.toInt() ?? 0,
      dueLabel: json['dueLabel'] as String? ?? '',
      status: json['status'] as String? ?? 'BORROWED',
      canRenew: json['canRenew'] as bool? ?? true,
      gracePending: json['gracePending'] as bool? ?? false,
    );

Map<String, dynamic> _$$BorrowItemImplToJson(_$BorrowItemImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'orderNo': instance.orderNo,
      'title': instance.title,
      'author': instance.author,
      'publisher': instance.publisher,
      'callNo': instance.callNo,
      'shelfCode': instance.shelfCode,
      'coverLabel': instance.coverLabel,
      'coverTone': instance.coverTone,
      'borrowedAt': instance.borrowedAt.toIso8601String(),
      'dueAt': instance.dueAt.toIso8601String(),
      'effectiveDueAt': instance.effectiveDueAt?.toIso8601String(),
      'overdueDays': instance.overdueDays,
      'daysLeft': instance.daysLeft,
      'renewCount': instance.renewCount,
      'dueLabel': instance.dueLabel,
      'status': instance.status,
      'canRenew': instance.canRenew,
      'gracePending': instance.gracePending,
    };

_$ReadyBookImpl _$$ReadyBookImplFromJson(Map<String, dynamic> json) =>
    _$ReadyBookImpl(
      reservationId: json['reservationId'] as String,
      title: json['title'] as String,
      author: json['author'] as String,
      pickupPoint: json['pickupPoint'] as String,
      pickupCode: json['pickupCode'] as String,
      expiresAt: DateTime.parse(json['expiresAt'] as String),
      remainingHours: (json['remainingHours'] as num?)?.toInt() ?? 0,
      queueNo: (json['queueNo'] as num?)?.toInt() ?? 1,
    );

Map<String, dynamic> _$$ReadyBookImplToJson(_$ReadyBookImpl instance) =>
    <String, dynamic>{
      'reservationId': instance.reservationId,
      'title': instance.title,
      'author': instance.author,
      'pickupPoint': instance.pickupPoint,
      'pickupCode': instance.pickupCode,
      'expiresAt': instance.expiresAt.toIso8601String(),
      'remainingHours': instance.remainingHours,
      'queueNo': instance.queueNo,
    };

_$BorrowDashboardImpl _$$BorrowDashboardImplFromJson(
        Map<String, dynamic> json) =>
    _$BorrowDashboardImpl(
      summary: BorrowSummary.fromJson(json['summary'] as Map<String, dynamic>),
      dueSoon: (json['dueSoon'] as List<dynamic>?)
              ?.map((e) => BorrowItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <BorrowItem>[],
      readyBooks: (json['readyBooks'] as List<dynamic>?)
              ?.map((e) => ReadyBook.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <ReadyBook>[],
      quietHoursText: json['quietHoursText'] as String? ?? '22:00–08:00',
      feeNote: json['feeNote'] as String? ?? '逾期费用 ¥1.00 / 天 · 归还时一次结清',
      unreadCount: (json['unreadCount'] as num?)?.toInt() ?? 0,
      isOffline: json['isOffline'] as bool? ?? false,
      lastSyncAt: json['lastSyncAt'] == null
          ? null
          : DateTime.parse(json['lastSyncAt'] as String),
    );

Map<String, dynamic> _$$BorrowDashboardImplToJson(
        _$BorrowDashboardImpl instance) =>
    <String, dynamic>{
      'summary': instance.summary,
      'dueSoon': instance.dueSoon,
      'readyBooks': instance.readyBooks,
      'quietHoursText': instance.quietHoursText,
      'feeNote': instance.feeNote,
      'unreadCount': instance.unreadCount,
      'isOffline': instance.isOffline,
      'lastSyncAt': instance.lastSyncAt?.toIso8601String(),
    };

_$TimelineEventImpl _$$TimelineEventImplFromJson(Map<String, dynamic> json) =>
    _$TimelineEventImpl(
      id: json['id'] as String,
      time:
          json['time'] == null ? null : DateTime.parse(json['time'] as String),
      title: json['title'] as String,
      subtitle: json['subtitle'] as String?,
      state: json['state'] as String? ?? 'done',
    );

Map<String, dynamic> _$$TimelineEventImplToJson(_$TimelineEventImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'time': instance.time?.toIso8601String(),
      'title': instance.title,
      'subtitle': instance.subtitle,
      'state': instance.state,
    };

_$OrderDetailImpl _$$OrderDetailImplFromJson(Map<String, dynamic> json) =>
    _$OrderDetailImpl(
      id: json['id'] as String,
      orderNo: json['orderNo'] as String,
      title: json['title'] as String,
      author: json['author'] as String,
      publisher: json['publisher'] as String,
      callNo: json['callNo'] as String,
      barcode: json['barcode'] as String,
      coverLabel: json['coverLabel'] as String,
      coverTone: json['coverTone'] as String? ?? 'mint',
      status: json['status'] as String? ?? 'BORROWED',
      overdueDays: (json['overdueDays'] as num?)?.toInt() ?? 0,
      borrowedAt: DateTime.parse(json['borrowedAt'] as String),
      dueAt: DateTime.parse(json['dueAt'] as String),
      effectiveDueAt: json['effectiveDueAt'] == null
          ? null
          : DateTime.parse(json['effectiveDueAt'] as String),
      graceDays: (json['graceDays'] as num?)?.toInt() ?? 0,
      feePerDay: json['feePerDay'] as String? ?? '¥1.00 / 天',
      feeEstimate: json['feeEstimate'] as String?,
      gracePending: json['gracePending'] as bool? ?? false,
      canGrace: json['canGrace'] as bool? ?? false,
      blacklisted: json['blacklisted'] as bool? ?? false,
      returnedAt: json['returnedAt'] == null
          ? null
          : DateTime.parse(json['returnedAt'] as String),
      settleNote: json['settleNote'] as String? ?? '待归还 · 归还后自动核算，减免费用需馆员操作并留痕',
      timeline: (json['timeline'] as List<dynamic>?)
              ?.map((e) => TimelineEvent.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <TimelineEvent>[],
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$OrderDetailImplToJson(_$OrderDetailImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'orderNo': instance.orderNo,
      'title': instance.title,
      'author': instance.author,
      'publisher': instance.publisher,
      'callNo': instance.callNo,
      'barcode': instance.barcode,
      'coverLabel': instance.coverLabel,
      'coverTone': instance.coverTone,
      'status': instance.status,
      'overdueDays': instance.overdueDays,
      'borrowedAt': instance.borrowedAt.toIso8601String(),
      'dueAt': instance.dueAt.toIso8601String(),
      'effectiveDueAt': instance.effectiveDueAt?.toIso8601String(),
      'graceDays': instance.graceDays,
      'feePerDay': instance.feePerDay,
      'feeEstimate': instance.feeEstimate,
      'gracePending': instance.gracePending,
      'canGrace': instance.canGrace,
      'blacklisted': instance.blacklisted,
      'returnedAt': instance.returnedAt?.toIso8601String(),
      'settleNote': instance.settleNote,
      'timeline': instance.timeline,
      'isOffline': instance.isOffline,
    };

_$GraceSceneImpl _$$GraceSceneImplFromJson(Map<String, dynamic> json) =>
    _$GraceSceneImpl(
      code: json['code'] as String,
      label: json['label'] as String,
      description: json['description'] as String? ?? '',
      baseDays: (json['baseDays'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$GraceSceneImplToJson(_$GraceSceneImpl instance) =>
    <String, dynamic>{
      'code': instance.code,
      'label': instance.label,
      'description': instance.description,
      'baseDays': instance.baseDays,
    };

_$GraceEstimateImpl _$$GraceEstimateImplFromJson(Map<String, dynamic> json) =>
    _$GraceEstimateImpl(
      originalDueAt: DateTime.parse(json['originalDueAt'] as String),
      estimatedDueAt: DateTime.parse(json['estimatedDueAt'] as String),
      days: (json['days'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$GraceEstimateImplToJson(_$GraceEstimateImpl instance) =>
    <String, dynamic>{
      'originalDueAt': instance.originalDueAt.toIso8601String(),
      'estimatedDueAt': instance.estimatedDueAt.toIso8601String(),
      'days': instance.days,
    };

_$GraceApplicationImpl _$$GraceApplicationImplFromJson(
        Map<String, dynamic> json) =>
    _$GraceApplicationImpl(
      id: json['id'] as String,
      applyNo: json['applyNo'] as String,
      sceneCode: json['sceneCode'] as String? ?? '',
      sceneLabel: json['sceneLabel'] as String? ?? '',
      status: json['status'] as String? ?? 'PENDING',
      submittedAt: DateTime.parse(json['submittedAt'] as String),
      decidedAt: json['decidedAt'] == null
          ? null
          : DateTime.parse(json['decidedAt'] as String),
      grantedDays: (json['grantedDays'] as num?)?.toInt(),
      rejectCode: json['rejectCode'] as String?,
      evidenceText: json['evidenceText'] as String?,
      attachments: (json['attachments'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const <String>[],
    );

Map<String, dynamic> _$$GraceApplicationImplToJson(
        _$GraceApplicationImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'applyNo': instance.applyNo,
      'sceneCode': instance.sceneCode,
      'sceneLabel': instance.sceneLabel,
      'status': instance.status,
      'submittedAt': instance.submittedAt.toIso8601String(),
      'decidedAt': instance.decidedAt?.toIso8601String(),
      'grantedDays': instance.grantedDays,
      'rejectCode': instance.rejectCode,
      'evidenceText': instance.evidenceText,
      'attachments': instance.attachments,
    };

_$GracePageDataImpl _$$GracePageDataImplFromJson(Map<String, dynamic> json) =>
    _$GracePageDataImpl(
      orderId: json['orderId'] as String,
      bookTitle: json['bookTitle'] as String,
      scenes: (json['scenes'] as List<dynamic>?)
              ?.map((e) => GraceScene.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <GraceScene>[],
      estimate: json['estimate'] == null
          ? null
          : GraceEstimate.fromJson(json['estimate'] as Map<String, dynamic>),
      history: (json['history'] as List<dynamic>?)
              ?.map((e) => GraceApplication.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <GraceApplication>[],
      hasPending: json['hasPending'] as bool? ?? false,
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$GracePageDataImplToJson(_$GracePageDataImpl instance) =>
    <String, dynamic>{
      'orderId': instance.orderId,
      'bookTitle': instance.bookTitle,
      'scenes': instance.scenes,
      'estimate': instance.estimate,
      'history': instance.history,
      'hasPending': instance.hasPending,
      'isOffline': instance.isOffline,
    };

_$GraceSubmitRequestImpl _$$GraceSubmitRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$GraceSubmitRequestImpl(
      orderId: json['orderId'] as String,
      sceneCode: json['sceneCode'] as String,
      description: json['description'] as String,
      attachments: (json['attachments'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const <String>[],
    );

Map<String, dynamic> _$$GraceSubmitRequestImplToJson(
        _$GraceSubmitRequestImpl instance) =>
    <String, dynamic>{
      'orderId': instance.orderId,
      'sceneCode': instance.sceneCode,
      'description': instance.description,
      'attachments': instance.attachments,
    };

_$GraceSubmitResultImpl _$$GraceSubmitResultImplFromJson(
        Map<String, dynamic> json) =>
    _$GraceSubmitResultImpl(
      applyNo: json['applyNo'] as String,
      status: json['status'] as String? ?? 'PENDING',
      slaDueAt: json['slaDueAt'] == null
          ? null
          : DateTime.parse(json['slaDueAt'] as String),
    );

Map<String, dynamic> _$$GraceSubmitResultImplToJson(
        _$GraceSubmitResultImpl instance) =>
    <String, dynamic>{
      'applyNo': instance.applyNo,
      'status': instance.status,
      'slaDueAt': instance.slaDueAt?.toIso8601String(),
    };

_$BookItemImpl _$$BookItemImplFromJson(Map<String, dynamic> json) =>
    _$BookItemImpl(
      id: json['id'] as String,
      title: json['title'] as String,
      author: json['author'] as String,
      publisher: json['publisher'] as String,
      year: json['year'] as String? ?? '',
      callNo: json['callNo'] as String? ?? '',
      libraryLabel: json['libraryLabel'] as String? ?? '',
      status: json['status'] as String? ?? 'AVAILABLE',
      availableCopies: (json['availableCopies'] as num?)?.toInt() ?? 0,
      queueLen: (json['queueLen'] as num?)?.toInt() ?? 0,
      estimatedAvailableAt: json['estimatedAvailableAt'] == null
          ? null
          : DateTime.parse(json['estimatedAvailableAt'] as String),
      coverLabel: json['coverLabel'] as String,
      coverTone: json['coverTone'] as String? ?? 'mint',
    );

Map<String, dynamic> _$$BookItemImplToJson(_$BookItemImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'author': instance.author,
      'publisher': instance.publisher,
      'year': instance.year,
      'callNo': instance.callNo,
      'libraryLabel': instance.libraryLabel,
      'status': instance.status,
      'availableCopies': instance.availableCopies,
      'queueLen': instance.queueLen,
      'estimatedAvailableAt': instance.estimatedAvailableAt?.toIso8601String(),
      'coverLabel': instance.coverLabel,
      'coverTone': instance.coverTone,
    };

_$SearchPageDataImpl _$$SearchPageDataImplFromJson(Map<String, dynamic> json) =>
    _$SearchPageDataImpl(
      total: (json['total'] as num?)?.toInt() ?? 0,
      items: (json['items'] as List<dynamic>?)
              ?.map((e) => BookItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <BookItem>[],
      query: json['query'] as String? ?? '',
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$SearchPageDataImplToJson(
        _$SearchPageDataImpl instance) =>
    <String, dynamic>{
      'total': instance.total,
      'items': instance.items,
      'query': instance.query,
      'isOffline': instance.isOffline,
    };

_$ReservationItemImpl _$$ReservationItemImplFromJson(
        Map<String, dynamic> json) =>
    _$ReservationItemImpl(
      id: json['id'] as String,
      bookTitle: json['bookTitle'] as String,
      author: json['author'] as String,
      publisher: json['publisher'] as String? ?? '',
      coverLabel: json['coverLabel'] as String,
      coverTone: json['coverTone'] as String? ?? 'mint',
      pickupPoint: json['pickupPoint'] as String? ?? '一楼服务台',
      pickupCode: json['pickupCode'] as String? ?? '',
      readyAt: json['readyAt'] == null
          ? null
          : DateTime.parse(json['readyAt'] as String),
      expiresAt: json['expiresAt'] == null
          ? null
          : DateTime.parse(json['expiresAt'] as String),
      remainingHours: (json['remainingHours'] as num?)?.toInt() ?? 0,
      queueNo: (json['queueNo'] as num?)?.toInt() ?? 0,
      queueTotal: (json['queueTotal'] as num?)?.toInt() ?? 0,
      status: json['status'] as String? ?? 'READY',
      fulfilledAt: json['fulfilledAt'] == null
          ? null
          : DateTime.parse(json['fulfilledAt'] as String),
      returnedAt: json['returnedAt'] == null
          ? null
          : DateTime.parse(json['returnedAt'] as String),
    );

Map<String, dynamic> _$$ReservationItemImplToJson(
        _$ReservationItemImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'bookTitle': instance.bookTitle,
      'author': instance.author,
      'publisher': instance.publisher,
      'coverLabel': instance.coverLabel,
      'coverTone': instance.coverTone,
      'pickupPoint': instance.pickupPoint,
      'pickupCode': instance.pickupCode,
      'readyAt': instance.readyAt?.toIso8601String(),
      'expiresAt': instance.expiresAt?.toIso8601String(),
      'remainingHours': instance.remainingHours,
      'queueNo': instance.queueNo,
      'queueTotal': instance.queueTotal,
      'status': instance.status,
      'fulfilledAt': instance.fulfilledAt?.toIso8601String(),
      'returnedAt': instance.returnedAt?.toIso8601String(),
    };

_$ReservationsPageImpl _$$ReservationsPageImplFromJson(
        Map<String, dynamic> json) =>
    _$ReservationsPageImpl(
      ready: (json['ready'] as List<dynamic>?)
              ?.map((e) => ReservationItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <ReservationItem>[],
      completed: (json['completed'] as List<dynamic>?)
              ?.map((e) => ReservationItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <ReservationItem>[],
      cancelled: (json['cancelled'] as List<dynamic>?)
              ?.map((e) => ReservationItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <ReservationItem>[],
      ruleNote: json['ruleNote'] as String? ?? '48 小时内未取书将顺延给下一位读者',
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$ReservationsPageImplToJson(
        _$ReservationsPageImpl instance) =>
    <String, dynamic>{
      'ready': instance.ready,
      'completed': instance.completed,
      'cancelled': instance.cancelled,
      'ruleNote': instance.ruleNote,
      'isOffline': instance.isOffline,
    };

_$BookCopyImpl _$$BookCopyImplFromJson(Map<String, dynamic> json) =>
    _$BookCopyImpl(
      id: json['id'] as String,
      barcode: json['barcode'] as String,
      location: json['location'] as String? ?? '',
      status: json['status'] as String? ?? 'AVAILABLE',
    );

Map<String, dynamic> _$$BookCopyImplToJson(_$BookCopyImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'barcode': instance.barcode,
      'location': instance.location,
      'status': instance.status,
    };

_$BookDetailImpl _$$BookDetailImplFromJson(Map<String, dynamic> json) =>
    _$BookDetailImpl(
      id: json['id'] as String,
      title: json['title'] as String,
      author: json['author'] as String,
      publisher: json['publisher'] as String? ?? '',
      isbn13: json['isbn13'] as String? ?? '',
      publishDate: json['publishDate'] as String? ?? '',
      price: json['price'] as String? ?? '',
      summary: json['summary'] as String? ?? '',
      tags:
          (json['tags'] as List<dynamic>?)?.map((e) => e as String).toList() ??
              const <String>[],
      totalCopies: (json['totalCopies'] as num?)?.toInt() ?? 0,
      availableCopies: (json['availableCopies'] as num?)?.toInt() ?? 0,
      onLoanCopies: (json['onLoanCopies'] as num?)?.toInt() ?? 0,
      queueLen: (json['queueLen'] as num?)?.toInt() ?? 0,
    );

Map<String, dynamic> _$$BookDetailImplToJson(_$BookDetailImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'title': instance.title,
      'author': instance.author,
      'publisher': instance.publisher,
      'isbn13': instance.isbn13,
      'publishDate': instance.publishDate,
      'price': instance.price,
      'summary': instance.summary,
      'tags': instance.tags,
      'totalCopies': instance.totalCopies,
      'availableCopies': instance.availableCopies,
      'onLoanCopies': instance.onLoanCopies,
      'queueLen': instance.queueLen,
    };

_$BookQueueImpl _$$BookQueueImplFromJson(Map<String, dynamic> json) =>
    _$BookQueueImpl(
      waiting: (json['waiting'] as num?)?.toInt() ?? 0,
      myQueueNo: (json['myQueueNo'] as num?)?.toInt(),
      myStatus: json['myStatus'] as String?,
    );

Map<String, dynamic> _$$BookQueueImplToJson(_$BookQueueImpl instance) =>
    <String, dynamic>{
      'waiting': instance.waiting,
      'myQueueNo': instance.myQueueNo,
      'myStatus': instance.myStatus,
    };

_$BookDetailDataImpl _$$BookDetailDataImplFromJson(Map<String, dynamic> json) =>
    _$BookDetailDataImpl(
      detail: BookDetail.fromJson(json['detail'] as Map<String, dynamic>),
      copies: (json['copies'] as List<dynamic>?)
              ?.map((e) => BookCopy.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <BookCopy>[],
      queue: json['queue'] == null
          ? const BookQueue()
          : BookQueue.fromJson(json['queue'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$$BookDetailDataImplToJson(
        _$BookDetailDataImpl instance) =>
    <String, dynamic>{
      'detail': instance.detail,
      'copies': instance.copies,
      'queue': instance.queue,
    };

_$AppNotificationImpl _$$AppNotificationImplFromJson(
        Map<String, dynamic> json) =>
    _$AppNotificationImpl(
      id: json['id'] as String,
      type: json['type'] as String,
      title: json['title'] as String,
      body: json['body'] as String,
      createdAt: DateTime.parse(json['createdAt'] as String),
      read: json['read'] as bool? ?? false,
      category: json['category'] as String? ?? 'borrow',
    );

Map<String, dynamic> _$$AppNotificationImplToJson(
        _$AppNotificationImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': instance.type,
      'title': instance.title,
      'body': instance.body,
      'createdAt': instance.createdAt.toIso8601String(),
      'read': instance.read,
      'category': instance.category,
    };

_$NotificationChannelsImpl _$$NotificationChannelsImplFromJson(
        Map<String, dynamic> json) =>
    _$NotificationChannelsImpl(
      inbox: json['inbox'] as bool? ?? true,
      email: json['email'] as bool? ?? true,
      push: json['push'] as bool? ?? true,
    );

Map<String, dynamic> _$$NotificationChannelsImplToJson(
        _$NotificationChannelsImpl instance) =>
    <String, dynamic>{
      'inbox': instance.inbox,
      'email': instance.email,
      'push': instance.push,
    };

_$QuietHoursImpl _$$QuietHoursImplFromJson(Map<String, dynamic> json) =>
    _$QuietHoursImpl(
      enabled: json['enabled'] as bool? ?? true,
      start: json['start'] as String? ?? '22:00',
      end: json['end'] as String? ?? '08:00',
    );

Map<String, dynamic> _$$QuietHoursImplToJson(_$QuietHoursImpl instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'start': instance.start,
      'end': instance.end,
    };

_$NotificationsPageDataImpl _$$NotificationsPageDataImplFromJson(
        Map<String, dynamic> json) =>
    _$NotificationsPageDataImpl(
      items: (json['items'] as List<dynamic>?)
              ?.map((e) => AppNotification.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <AppNotification>[],
      unreadCount: (json['unreadCount'] as num?)?.toInt() ?? 0,
      quietHours: json['quietHours'] == null
          ? const QuietHours()
          : QuietHours.fromJson(json['quietHours'] as Map<String, dynamic>),
      channels: json['channels'] == null
          ? const NotificationChannels()
          : NotificationChannels.fromJson(
              json['channels'] as Map<String, dynamic>),
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$NotificationsPageDataImplToJson(
        _$NotificationsPageDataImpl instance) =>
    <String, dynamic>{
      'items': instance.items,
      'unreadCount': instance.unreadCount,
      'quietHours': instance.quietHours,
      'channels': instance.channels,
      'isOffline': instance.isOffline,
    };

_$ProfileDataImpl _$$ProfileDataImplFromJson(Map<String, dynamic> json) =>
    _$ProfileDataImpl(
      name: json['name'] as String,
      studentNo: json['studentNo'] as String,
      department: json['department'] as String? ?? '',
      degree: json['degree'] as String? ?? '本科',
      creditLabel: json['creditLabel'] as String? ?? '信用良好',
      quotaUsed: (json['quotaUsed'] as num?)?.toInt() ?? 0,
      quotaTotal: (json['quotaTotal'] as num?)?.toInt() ?? 0,
      borrowingCount: (json['borrowingCount'] as num?)?.toInt() ?? 0,
      totalBorrowed: (json['totalBorrowed'] as num?)?.toInt() ?? 0,
      reservationCount: (json['reservationCount'] as num?)?.toInt() ?? 0,
      returnedThisTerm: (json['returnedThisTerm'] as num?)?.toInt() ?? 0,
      overdueCount: (json['overdueCount'] as num?)?.toInt() ?? 0,
      maxRenew: (json['maxRenew'] as num?)?.toInt() ?? 1,
      loanDays: (json['loanDays'] as num?)?.toInt() ?? 30,
      unreadCount: (json['unreadCount'] as num?)?.toInt() ?? 0,
      version: json['version'] as String? ?? '1.0.0',
      blacklisted: json['blacklisted'] as bool? ?? false,
      blacklistReason: json['blacklistReason'] as String?,
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$ProfileDataImplToJson(_$ProfileDataImpl instance) =>
    <String, dynamic>{
      'name': instance.name,
      'studentNo': instance.studentNo,
      'department': instance.department,
      'degree': instance.degree,
      'creditLabel': instance.creditLabel,
      'quotaUsed': instance.quotaUsed,
      'quotaTotal': instance.quotaTotal,
      'borrowingCount': instance.borrowingCount,
      'totalBorrowed': instance.totalBorrowed,
      'reservationCount': instance.reservationCount,
      'returnedThisTerm': instance.returnedThisTerm,
      'overdueCount': instance.overdueCount,
      'maxRenew': instance.maxRenew,
      'loanDays': instance.loanDays,
      'unreadCount': instance.unreadCount,
      'version': instance.version,
      'blacklisted': instance.blacklisted,
      'blacklistReason': instance.blacklistReason,
      'isOffline': instance.isOffline,
    };

_$AppealTypeImpl _$$AppealTypeImplFromJson(Map<String, dynamic> json) =>
    _$AppealTypeImpl(
      code: json['code'] as String,
      label: json['label'] as String,
      description: json['description'] as String? ?? '',
    );

Map<String, dynamic> _$$AppealTypeImplToJson(_$AppealTypeImpl instance) =>
    <String, dynamic>{
      'code': instance.code,
      'label': instance.label,
      'description': instance.description,
    };

_$AppealTicketImpl _$$AppealTicketImplFromJson(Map<String, dynamic> json) =>
    _$AppealTicketImpl(
      id: json['id'] as String,
      ticketNo: json['ticketNo'] as String,
      type: json['type'] as String,
      typeLabel: json['typeLabel'] as String,
      bookTitle: json['bookTitle'] as String? ?? '',
      status: json['status'] as String? ?? 'OPEN',
      statusLabel: json['statusLabel'] as String? ?? '待处理',
      description: json['description'] as String? ?? '',
      rejectReason: json['rejectReason'] as String?,
      createdAt: DateTime.parse(json['createdAt'] as String),
      slaDueAt: json['slaDueAt'] == null
          ? null
          : DateTime.parse(json['slaDueAt'] as String),
      compensationDays: (json['compensationDays'] as num?)?.toInt(),
      assigneeName: json['assigneeName'] as String?,
    );

Map<String, dynamic> _$$AppealTicketImplToJson(_$AppealTicketImpl instance) =>
    <String, dynamic>{
      'id': instance.id,
      'ticketNo': instance.ticketNo,
      'type': instance.type,
      'typeLabel': instance.typeLabel,
      'bookTitle': instance.bookTitle,
      'status': instance.status,
      'statusLabel': instance.statusLabel,
      'description': instance.description,
      'rejectReason': instance.rejectReason,
      'createdAt': instance.createdAt.toIso8601String(),
      'slaDueAt': instance.slaDueAt?.toIso8601String(),
      'compensationDays': instance.compensationDays,
      'assigneeName': instance.assigneeName,
    };

_$AppealsPageDataImpl _$$AppealsPageDataImplFromJson(
        Map<String, dynamic> json) =>
    _$AppealsPageDataImpl(
      tickets: (json['tickets'] as List<dynamic>?)
              ?.map((e) => AppealTicket.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const <AppealTicket>[],
      openCount: (json['openCount'] as num?)?.toInt() ?? 0,
      doneCount: (json['doneCount'] as num?)?.toInt() ?? 0,
      slaHours: (json['slaHours'] as num?)?.toInt() ?? 2,
      isOffline: json['isOffline'] as bool? ?? false,
    );

Map<String, dynamic> _$$AppealsPageDataImplToJson(
        _$AppealsPageDataImpl instance) =>
    <String, dynamic>{
      'tickets': instance.tickets,
      'openCount': instance.openCount,
      'doneCount': instance.doneCount,
      'slaHours': instance.slaHours,
      'isOffline': instance.isOffline,
    };

_$AppealCreateRequestImpl _$$AppealCreateRequestImplFromJson(
        Map<String, dynamic> json) =>
    _$AppealCreateRequestImpl(
      orderId: json['orderId'] as String,
      type: json['type'] as String,
      description: json['description'] as String,
      attachments: (json['attachments'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const <String>[],
    );

Map<String, dynamic> _$$AppealCreateRequestImplToJson(
        _$AppealCreateRequestImpl instance) =>
    <String, dynamic>{
      'orderId': instance.orderId,
      'type': instance.type,
      'description': instance.description,
      'attachments': instance.attachments,
    };

_$AppealCreateResultImpl _$$AppealCreateResultImplFromJson(
        Map<String, dynamic> json) =>
    _$AppealCreateResultImpl(
      ticketNo: json['ticketNo'] as String,
      status: json['status'] as String? ?? 'OPEN',
      slaDueAt: json['slaDueAt'] == null
          ? null
          : DateTime.parse(json['slaDueAt'] as String),
    );

Map<String, dynamic> _$$AppealCreateResultImplToJson(
        _$AppealCreateResultImpl instance) =>
    <String, dynamic>{
      'ticketNo': instance.ticketNo,
      'status': instance.status,
      'slaDueAt': instance.slaDueAt?.toIso8601String(),
    };
