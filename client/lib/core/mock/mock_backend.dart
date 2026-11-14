import '../models/models.dart';
import '../network/api_exception.dart';

/// 单一 mock 数据源：让 C1–C9 在同一份数据上互相影响（续借改期、建单变审核中、
/// 标记已读同步角标）。后端补齐后只替换各 feature 的 Repository，本类不进入生产路径。
class MockBackend {
  MockBackend._() {
    _seed();
  }

  static final MockBackend instance = MockBackend._();

  /// 固定「现在」，让临期/逾期/保留时长可复现。
  final DateTime now = DateTime(2026, 4, 16, 19, 0);

  bool offline = false;
  bool blacklisted = false;
  String? blacklistReason;

  late List<BorrowItem> _borrows;
  late List<ReservationItem> _completedReservations;
  late List<ReservationItem> _cancelledReservations;
  late List<AppNotification> _notifications;
  late List<AppealTicket> _appeals;
  late GraceEstimate _graceEstimate;
  late List<GraceApplication> _graceHistory;
  NotificationChannels _channels = const NotificationChannels();
  QuietHours _quietHours = const QuietHours();
  ProfileData _profile = const ProfileData(
    name: '林知远',
    studentNo: '20230114',
    department: '新闻传播学院',
    degree: '本科',
    quotaUsed: 3,
    quotaTotal: 5,
    borrowingCount: 3,
    totalBorrowed: 26,
    reservationCount: 1,
    returnedThisTerm: 9,
    overdueCount: 1,
    maxRenew: 1,
    loanDays: 30,
  );

  Future<void> _delay() =>
      Future<void>.delayed(const Duration(milliseconds: 220));

  void _seed() {
    _borrows = [
      BorrowItem(
        id: '1900000000000000001',
        orderNo: 'B-2026-0416-001',
        title: '线性代数应该这样学（第 3 版）',
        author: '阿克斯勒',
        publisher: '人民邮电出版社',
        callNo: 'O151.2 / A1',
        shelfCode: 'A-03-12',
        coverLabel: '线代',
        coverTone: 'mint',
        borrowedAt: DateTime(2026, 3, 19, 10, 22),
        dueAt: DateTime(2026, 4, 18),
        daysLeft: 2,
        dueLabel: '剩余 2 天',
        status: 'BORROWED',
        canRenew: true,
      ),
      BorrowItem(
        id: '1900000000000000002',
        orderNo: 'B-2026-0404-007',
        title: '乡土中国',
        author: '费孝通',
        publisher: '北京大学出版社',
        callNo: 'C912.82 / F1',
        shelfCode: 'B-02-07',
        coverLabel: '乡土',
        coverTone: 'amber',
        borrowedAt: DateTime(2026, 4, 4, 10, 22),
        dueAt: DateTime(2026, 4, 14),
        overdueDays: 2,
        daysLeft: -2,
        dueLabel: '已逾期 2 天',
        status: 'OVERDUE',
        canRenew: false,
      ),
      BorrowItem(
        id: '1900000000000000003',
        orderNo: 'B-2026-0320-011',
        title: '算法导论',
        author: '科尔曼',
        publisher: '机械工业出版社',
        callNo: 'TP301.6 / C1',
        shelfCode: 'C-01-03',
        coverLabel: '算法',
        coverTone: 'slate',
        borrowedAt: DateTime(2026, 3, 20, 14, 8),
        dueAt: DateTime(2026, 5, 4),
        daysLeft: 18,
        renewCount: 1,
        dueLabel: '剩余 18 天',
        status: 'BORROWED',
        canRenew: false,
      ),
    ];

    _completedReservations = [
      ReservationItem(
        id: '2900000000000000001',
        bookTitle: '社会学的想象力',
        author: '米尔斯',
        publisher: '三联书店',
        coverLabel: '社会',
        coverTone: 'slate',
        status: 'FULFILLED',
        fulfilledAt: DateTime(2026, 4, 2),
        returnedAt: DateTime(2026, 5, 2),
        queueNo: 1,
        queueTotal: 2,
      ),
      ReservationItem(
        id: '2900000000000000002',
        bookTitle: '经济学原理',
        author: '曼昆',
        publisher: '北京大学出版社',
        coverLabel: '经济',
        coverTone: 'mint',
        status: 'FULFILLED',
        fulfilledAt: DateTime(2026, 3, 18),
        returnedAt: DateTime(2026, 4, 10),
        queueNo: 2,
        queueTotal: 3,
      ),
    ];
    _cancelledReservations = [];

    _notifications = [
      AppNotification(
        id: '3900000000000000001',
        type: 'overdue',
        title: '《乡土中国》已逾期 2 天',
        body: '这本书已过应还日 2 天。可以申请宽限，也可以直接归还。归还后自动核算。',
        createdAt: DateTime(2026, 4, 14, 9, 20),
        category: 'borrow',
      ),
      AppNotification(
        id: '3900000000000000002',
        type: 'grace',
        title: '宽限申请已通过',
        body: '《算法导论》的应还日已顺延 3 天。批准依据与审批馆员可在借阅详情查看。',
        createdAt: DateTime(2026, 4, 15, 16, 4),
        category: 'borrow',
      ),
      AppNotification(
        id: '3900000000000000003',
        type: 'reservation',
        title: '《人类简史》已到馆',
        body: '请于 2026-04-18 18:00 前到一楼服务台取书，逾期将顺延给下一位读者。',
        createdAt: DateTime(2026, 4, 16, 8, 30),
        read: true,
        category: 'borrow',
      ),
      AppNotification(
        id: '3900000000000000004',
        type: 'system',
        title: '免打扰时段说明',
        body: '已按课表设置 22:00–08:00 免打扰；履约类通知仍会送达站内信。',
        createdAt: DateTime(2026, 4, 10, 12, 0),
        read: true,
        category: 'system',
      ),
      AppNotification(
        id: '3900000000000000005',
        type: 'reservation',
        title: '《社会学的想象力》预约已生效',
        body: '你已在队列中预留成功，到书后会第一时间通知。',
        createdAt: DateTime(2026, 4, 9, 10, 0),
        read: true,
        category: 'borrow',
      ),
    ];

    _appeals = [
      AppealTicket(
        id: '4900000000000000001',
        ticketNo: 'A-2041',
        type: 'OVERDUE_TIER',
        typeLabel: '逾期判定有误',
        bookTitle: '乡土中国',
        status: 'OPEN',
        statusLabel: '待处理',
        description: '4 月 12 日已在自助机归还，但系统仍显示逾期，请核实归还记录。',
        createdAt: DateTime(2026, 4, 14, 9, 20),
        slaDueAt: DateTime(2026, 4, 14, 11, 20),
      ),
      AppealTicket(
        id: '4900000000000000002',
        ticketNo: 'A-2038',
        type: 'REMINDER',
        typeLabel: '提醒未收到',
        bookTitle: '算法导论',
        status: 'AUTO_COMPENSATED',
        statusLabel: '已补偿',
        description: '到期前 1 天的邮件没有收到，导致错过续借时间。',
        createdAt: DateTime(2026, 4, 8, 10, 12),
        compensationDays: 3,
        assigneeName: '馆员 · 周敏',
      ),
      AppealTicket(
        id: '4900000000000000003',
        ticketNo: 'A-2011',
        type: 'OVERDUE_TIER',
        typeLabel: '费用计算争议',
        bookTitle: '社会学的想象力',
        status: 'REJECTED',
        statusLabel: '已驳回',
        description: '认为逾期天数计算多算了 1 天。',
        rejectReason: '逾期事实成立，费用按日费率计算无误。',
        createdAt: DateTime(2026, 3, 28, 16, 40),
      ),
    ];

    _graceEstimate = GraceEstimate(
      originalDueAt: DateTime(2026, 4, 14),
      estimatedDueAt: DateTime(2026, 4, 20),
      days: 6,
    );
    _graceHistory = [
      GraceApplication(
        id: '5900000000000000001',
        applyNo: 'G-2026-0318',
        sceneCode: 'NORMAL',
        sceneLabel: '合理事由',
        status: 'APPROVED',
        submittedAt: DateTime(2026, 3, 18, 10, 12),
        decidedAt: DateTime(2026, 3, 18, 11, 40),
        grantedDays: 3,
        evidenceText: '常规事由 3 天 × Kt 1.00 × Kq 1.00 = 3 天',
      ),
    ];
  }

  /// 测试用：把单例恢复到种子状态，避免用例互相影响。
  void resetForTest() {
    _seed();
    offline = false;
    blacklisted = false;
    blacklistReason = null;
    _channels = const NotificationChannels();
    _quietHours = const QuietHours();
    _profile = const ProfileData(
      name: '林知远',
      studentNo: '20230114',
      department: '新闻传播学院',
      degree: '本科',
      quotaUsed: 3,
      quotaTotal: 5,
      borrowingCount: 3,
      totalBorrowed: 26,
      reservationCount: 1,
      returnedThisTerm: 9,
      overdueCount: 1,
      maxRenew: 1,
      loanDays: 30,
    );
  }

  // ---------- 借阅 ----------

  BorrowDashboard dashboard() {
    final active = _borrows.where((b) => b.status != 'RETURNED').toList()
      ..sort((a, b) => a.dueAt.compareTo(b.dueAt));
    final dueSoon = active
        .where((b) => b.dueAt.difference(now).inDays <= 2 && b.overdueDays == 0)
        .length;
    final overdue = active.where((b) => b.overdueDays > 0).length;
    return BorrowDashboard(
      summary: BorrowSummary(
        borrowing: active.length,
        dueSoon: dueSoon,
        overdue: overdue,
        returnedThisTerm: _profile.returnedThisTerm,
        creditLabel: '信用良好',
        quotaUsed: active.length,
        quotaTotal: _profile.quotaTotal,
        blacklisted: blacklisted,
        blacklistReason: blacklistReason,
      ),
      dueSoon: active,
      readyBooks: [
        ReadyBook(
          reservationId: '2900000000000000003',
          title: '人类简史',
          author: '尤瓦尔·赫拉利',
          pickupPoint: '一楼服务台',
          pickupCode: 'M-2048',
          expiresAt: DateTime(2026, 4, 18, 18, 0),
          remainingHours: 47,
          queueNo: 1,
        ),
      ],
      quietHoursText: '${_quietHours.start}–${_quietHours.end}',
      unreadCount: _notifications.where((n) => !n.read).length,
      isOffline: offline,
      lastSyncAt: now,
    );
  }

  Future<BorrowDashboard> fetchDashboard() async {
    await _delay();
    return dashboard();
  }

  /// 模拟借出：写入一条借阅并占用额度，让 C4 → C1 的闭环在 mock 上成立。
  Future<void> borrowBook(String bookId) async {
    await _delay();
    if (blacklisted) {
      throw const ApiException(
        code: 'ACCOUNT_BLACKLISTED',
        message: '账号已暂停借阅权限，可归还与申诉',
        statusCode: 403,
      );
    }
    final book = _books.where((b) => b.id == bookId);
    if (book.isEmpty) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '图书不存在或已下架',
        statusCode: 404,
      );
    }
    final active = _borrows.where((b) => b.status != 'RETURNED').toList();
    if (active.length >= _profile.quotaTotal) {
      throw const ApiException(
        code: 'BORROW_LIMITED',
        message: '借阅额度已满，归还后可继续借阅',
        statusCode: 422,
      );
    }
    if (active.any((b) => b.title == book.first.title)) {
      throw const ApiException(
        code: 'DUPLICATE_LOAN',
        message: '这本书已在借阅中',
        statusCode: 409,
      );
    }
    final dueAt = now.add(const Duration(days: 30));
    _borrows = [
      BorrowItem(
        id: '19000000000000001${(100 + _borrows.length)}',
        orderNo: 'B-2026-0416-0${(20 + _borrows.length)}',
        title: book.first.title,
        author: book.first.author,
        publisher: book.first.publisher,
        callNo: book.first.callNo,
        shelfCode: 'A-03-12',
        coverLabel: book.first.coverLabel,
        coverTone: book.first.coverTone,
        borrowedAt: now,
        dueAt: dueAt,
        daysLeft: 30,
        dueLabel: '剩余 30 天',
        status: 'BORROWED',
        canRenew: true,
      ),
      ..._borrows,
    ];
  }

  BorrowItem _findBorrow(String id) {
    final match = _borrows.where((b) => b.id == id);
    if (match.isEmpty) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '单据不存在或无权查看',
        statusCode: 404,
      );
    }
    return match.first;
  }

  Future<OrderDetail> fetchOrder(String id) async {
    await _delay();
    final borrow = _findBorrow(id);
    final isOverdue = borrow.overdueDays > 0;
    return OrderDetail(
      id: borrow.id,
      orderNo: borrow.orderNo,
      title: borrow.title,
      author: borrow.author,
      publisher: borrow.publisher,
      callNo: borrow.callNo,
      barcode: '8871 2043',
      coverLabel: borrow.coverLabel,
      coverTone: borrow.coverTone,
      status: borrow.status,
      overdueDays: borrow.overdueDays,
      borrowedAt: borrow.borrowedAt,
      dueAt: borrow.dueAt,
      effectiveDueAt: borrow.effectiveDueAt ?? borrow.dueAt,
      graceDays: 0,
      feePerDay: '¥1.00 / 天',
      feeEstimate: isOverdue ? '¥2.00' : null,
      gracePending: borrow.gracePending,
      canGrace: !blacklisted && !borrow.gracePending,
      blacklisted: blacklisted,
      timeline: _timelineFor(borrow),
      isOffline: offline,
    );
  }

  List<TimelineEvent> _timelineFor(BorrowItem borrow) {
    if (borrow.id != '1900000000000000002') {
      return [
        TimelineEvent(
          id: 't1',
          time: borrow.borrowedAt,
          title: '借出',
          subtitle: '自助借还机 A-03-12 · 王澄 已确认',
        ),
        TimelineEvent(
          id: 't2',
          time: borrow.dueAt.subtract(const Duration(days: 3)),
          title: '到期前 3 天提醒',
          subtitle: '站内已送达',
        ),
        TimelineEvent(
          id: 't3',
          time: borrow.dueAt,
          title: '应还日',
          subtitle: '待归还 · 归还后自动核算',
          state: 'current',
        ),
      ];
    }
    return [
      TimelineEvent(
        id: 't1',
        time: DateTime(2026, 4, 4, 10, 22),
        title: '借出',
        subtitle: '自助借还机 A-03-12 · 王澄 已确认',
      ),
      TimelineEvent(
        id: 't2',
        time: DateTime(2026, 4, 11, 9, 0),
        title: '到期前 3 天提醒',
        subtitle: '站内已送达',
      ),
      TimelineEvent(
        id: 't3',
        time: DateTime(2026, 4, 13, 9, 0),
        title: '到期前 1 天提醒',
        subtitle: '站内 + 邮件',
      ),
      TimelineEvent(
        id: 't4',
        time: DateTime(2026, 4, 14, 0, 5),
        title: '进入逾期',
        subtitle: '可申请宽限或归还结清',
        state: 'current',
      ),
    ];
  }

  Future<DateTime> renew(String id) async {
    await _delay();
    final index = _borrows.indexWhere((b) => b.id == id);
    if (index < 0) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '单据不存在或无权查看',
        statusCode: 404,
      );
    }
    if (blacklisted) {
      throw const ApiException(
        code: 'ACCOUNT_BLACKLISTED',
        message: '账号已暂停借阅权限，可归还与申诉',
        statusCode: 422,
      );
    }
    final borrow = _borrows[index];
    if (borrow.overdueDays > 0) {
      throw const ApiException(
        code: 'OVERDUE',
        message: '已逾期，请先归还或申请宽限',
        statusCode: 422,
      );
    }
    if (borrow.renewCount >= 1) {
      throw const ApiException(
        code: 'RENEW_LIMIT_REACHED',
        message: '已续借 1 次',
        statusCode: 422,
      );
    }
    final newDue = borrow.dueAt.add(const Duration(days: 15));
    _borrows[index] = borrow.copyWith(
      dueAt: newDue,
      renewCount: borrow.renewCount + 1,
      canRenew: false,
    );
    return newDue;
  }

  Future<void> returnBook(String id) async {
    await _delay();
    final index = _borrows.indexWhere((b) => b.id == id);
    if (index < 0) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '单据不存在或无权查看',
        statusCode: 404,
      );
    }
    final borrow = _borrows[index];
    _borrows[index] = borrow.copyWith(status: 'RETURNED');
    _profile = _profile.copyWith(
      returnedThisTerm: _profile.returnedThisTerm + 1,
    );
  }

  // ---------- 宽限 ----------

  GracePageData gracePage(String orderId) {
    final borrow = _findBorrow(orderId);
    return GracePageData(
      orderId: orderId,
      bookTitle: borrow.title,
      scenes: const [
        GraceScene(
          code: 'GRADUATION',
          label: '毕业离校',
          description: '毕业季离校手续、退宿与托运',
          baseDays: 14,
        ),
        GraceScene(
          code: 'THESIS',
          label: '毕业论文',
          description: '开题、送审与答辩集中期',
          baseDays: 10,
        ),
        GraceScene(
          code: 'EXAM',
          label: '集中考试',
          description: '期末或资格考试的复习周',
          baseDays: 7,
        ),
        GraceScene(
          code: 'NORMAL',
          label: '合理事由',
          description: '校外实习、家庭事务等其他情况',
          baseDays: 3,
        ),
      ],
      estimate: _graceEstimate,
      history: _graceHistory,
      hasPending: borrow.gracePending,
      isOffline: offline,
    );
  }

  Future<GracePageData> fetchGracePage(String orderId) async {
    await _delay();
    return gracePage(orderId);
  }

  Future<GraceSubmitResult> submitGrace(GraceSubmitRequest request) async {
    await _delay();
    final index = _borrows.indexWhere((b) => b.id == request.orderId);
    if (index < 0) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '单据不存在或无权查看',
        statusCode: 404,
      );
    }
    final borrow = _borrows[index];
    if (blacklisted) {
      throw const ApiException(
        code: 'ACCOUNT_BLACKLISTED',
        message: '账号已暂停借阅权限，可归还与申诉',
        statusCode: 422,
      );
    }
    if (borrow.gracePending) {
      throw const ApiException(
        code: 'GRACE_PENDING_EXISTS',
        message: '已有一笔宽限申请正在审核',
        statusCode: 422,
      );
    }
    if (request.description.trim().length < 10) {
      throw const ApiException(
        code: 'INVALID_ARGUMENT',
        message: '说明至少 10 个字',
        statusCode: 400,
      );
    }
    final applyNo = 'G-2026-${1000 + _graceHistory.length}';
    _graceHistory = [
      GraceApplication(
        id: '590000000000000000${_graceHistory.length + 2}',
        applyNo: applyNo,
        sceneCode: request.sceneCode,
        sceneLabel: '申请中',
        status: 'PENDING',
        submittedAt: now,
        evidenceText: _graceEstimate.days > 0
            ? '依据：场景 ${request.sceneCode} · 预计 ${_graceEstimate.days} 天'
            : null,
        attachments: request.attachments,
      ),
      ..._graceHistory,
    ];
    _borrows[index] = borrow.copyWith(gracePending: true);
    return GraceSubmitResult(
      applyNo: applyNo,
      slaDueAt: now.add(const Duration(hours: 2)),
    );
  }

  // ---------- 检索 ----------

  List<BookItem> get _books => [
        const BookItem(
          id: '6900000000000000001',
          title: '乡土中国',
          author: '费孝通',
          publisher: '北京大学出版社',
          year: '2012',
          callNo: 'C912.82 / F1',
          libraryLabel: '3 楼 A 区 · 2 本可借',
          status: 'AVAILABLE',
          availableCopies: 2,
          queueLen: 0,
          coverLabel: '乡土',
          coverTone: 'mint',
        ),
        const BookItem(
          id: '6900000000000000002',
          title: '乡土中国·生育制度',
          author: '费孝通',
          publisher: '三联书店',
          year: '2021',
          callNo: 'C912.82 / F2',
          libraryLabel: '3 楼 A 区 · 1 本馆藏',
          status: 'ON_LOAN',
          availableCopies: 0,
          queueLen: 2,
          coverLabel: '乡土',
          coverTone: 'slate',
        ),
        const BookItem(
          id: '6900000000000000003',
          title: '乡土重建',
          author: '费孝通',
          publisher: '观察社',
          year: '1948',
          callNo: 'C912.82 / F3',
          libraryLabel: '电子资源 · 无需到馆',
          status: 'EBOOK',
          coverLabel: '乡土',
          coverTone: 'amber',
        ),
        const BookItem(
          id: '6900000000000000004',
          title: '线性代数应该这样学（第 3 版）',
          author: '阿克斯勒',
          publisher: '人民邮电出版社',
          year: '2021',
          callNo: 'O151.2 / A1',
          libraryLabel: '4 楼 B 区 · 1 本可借',
          status: 'AVAILABLE',
          availableCopies: 1,
          coverLabel: '线代',
          coverTone: 'mint',
        ),
        const BookItem(
          id: '6900000000000000005',
          title: '人类简史',
          author: '尤瓦尔·赫拉利',
          publisher: '中信出版社',
          year: '2017',
          callNo: 'K02 / H1',
          libraryLabel: '2 楼 C 区 · 3 本可借',
          status: 'AVAILABLE',
          availableCopies: 3,
          coverLabel: '人类',
          coverTone: 'mint',
        ),
        const BookItem(
          id: '6900000000000000006',
          title: '算法导论',
          author: '科尔曼',
          publisher: '机械工业出版社',
          year: '2013',
          callNo: 'TP301.6 / C1',
          libraryLabel: '5 楼 A 区 · 全部借出',
          status: 'ON_LOAN',
          availableCopies: 0,
          queueLen: 4,
          coverLabel: '算法',
          coverTone: 'slate',
        ),
        const BookItem(
          id: '6900000000000000007',
          title: '社会学的想象力',
          author: '米尔斯',
          publisher: '三联书店',
          year: '2016',
          callNo: 'C91 / M1',
          libraryLabel: '3 楼 B 区 · 1 本可借',
          status: 'AVAILABLE',
          availableCopies: 1,
          coverLabel: '社会',
          coverTone: 'mint',
        ),
        const BookItem(
          id: '6900000000000000008',
          title: '经济学原理',
          author: '曼昆',
          publisher: '北京大学出版社',
          year: '2015',
          callNo: 'F0 / M1',
          libraryLabel: '2 楼 A 区 · 全部借出',
          status: 'ON_LOAN',
          availableCopies: 0,
          queueLen: 1,
          coverLabel: '经济',
          coverTone: 'slate',
        ),
        const BookItem(
          id: '6900000000000000009',
          title: '百年孤独',
          author: '马尔克斯',
          publisher: '南海出版公司',
          year: '2011',
          callNo: 'I775.45 / M1',
          libraryLabel: '3 楼 C 区 · 2 本可借',
          status: 'AVAILABLE',
          availableCopies: 2,
          coverLabel: '百年',
          coverTone: 'amber',
        ),
        const BookItem(
          id: '6900000000000000010',
          title: '三体',
          author: '刘慈欣',
          publisher: '重庆出版社',
          year: '2008',
          callNo: 'I247.5 / L1',
          libraryLabel: '3 楼 C 区 · 全部借出',
          status: 'ON_LOAN',
          availableCopies: 0,
          queueLen: 6,
          coverLabel: '三体',
          coverTone: 'slate',
        ),
        const BookItem(
          id: '6900000000000000011',
          title: '局外人',
          author: '加缪',
          publisher: '上海译文出版社',
          year: '2013',
          callNo: 'I565.45 / J1',
          libraryLabel: '3 楼 C 区 · 1 本可借',
          status: 'AVAILABLE',
          availableCopies: 1,
          coverLabel: '局外',
          coverTone: 'mint',
        ),
        const BookItem(
          id: '6900000000000000012',
          title: '文献检索与利用',
          author: '王细荣',
          publisher: '高等教育出版社',
          year: '2019',
          callNo: 'G254.9 / W1',
          libraryLabel: '4 楼 A 区 · 2 本可借',
          status: 'AVAILABLE',
          availableCopies: 2,
          coverLabel: '文献',
          coverTone: 'mint',
        ),
      ];

  Future<SearchPageData> search(String query, String filter) async {
    await _delay();
    final keyword = query.trim().toLowerCase();
    var items = _books.where((book) {
      if (keyword.isEmpty) return true;
      return book.title.toLowerCase().contains(keyword) ||
          book.author.toLowerCase().contains(keyword) ||
          book.callNo.toLowerCase().contains(keyword);
    }).toList();
    if (filter == 'available') {
      items = items.where((b) => b.status == 'AVAILABLE').toList();
    } else if (filter == 'reservable') {
      items = items
          .where((b) => b.status == 'ON_LOAN' || b.status == 'RESERVABLE')
          .toList();
    }
    return SearchPageData(
      total: items.length,
      items: items,
      query: query,
      isOffline: offline,
    );
  }

  // ---------- 预约 ----------

  ReservationsPage reservations() {
    return ReservationsPage(
      ready: [
        ReservationItem(
          id: '2900000000000000003',
          bookTitle: '人类简史',
          author: '尤瓦尔·赫拉利',
          publisher: '中信出版社',
          coverLabel: '人类',
          coverTone: 'mint',
          pickupPoint: '一楼服务台',
          pickupCode: 'M-2048',
          readyAt: DateTime(2026, 4, 16, 8, 30),
          expiresAt: DateTime(2026, 4, 18, 18, 0),
          remainingHours: 47,
          queueNo: 1,
          queueTotal: 3,
          status: 'READY',
        ),
      ],
      completed: _completedReservations,
      cancelled: _cancelledReservations,
      isOffline: offline,
    );
  }

  Future<ReservationsPage> fetchReservations() async {
    await _delay();
    return reservations();
  }

  Future<void> cancelReservation(String id) async {
    await _delay();
    final source = reservations().ready;
    final index = source.indexWhere((item) => item.id == id);
    if (index < 0) {
      throw const ApiException(
        code: 'NOT_FOUND',
        message: '单据不存在或无权查看',
        statusCode: 404,
      );
    }
    final item = source[index];
    _cancelledReservations = [
      item.copyWith(status: 'CANCELLED'),
      ..._cancelledReservations,
    ];
    _profile = _profile.copyWith(
      reservationCount: (_profile.reservationCount - 1).clamp(0, 999),
    );
  }

  // ---------- 通知 ----------

  NotificationsPageData notifications() {
    final sorted = [..._notifications]
      ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
    return NotificationsPageData(
      items: sorted,
      unreadCount: sorted.where((n) => !n.read).length,
      quietHours: _quietHours,
      channels: _channels,
      isOffline: offline,
    );
  }

  Future<NotificationsPageData> fetchNotifications() async {
    await _delay();
    return notifications();
  }

  Future<void> markRead(String id) async {
    await _delay();
    final index = _notifications.indexWhere((n) => n.id == id);
    if (index >= 0) {
      _notifications[index] = _notifications[index].copyWith(read: true);
    }
  }

  Future<void> markAllRead() async {
    await _delay();
    _notifications = _notifications.map((n) => n.copyWith(read: true)).toList();
  }

  Future<QuietHours> setQuietHours(bool enabled) async {
    await _delay();
    _quietHours = _quietHours.copyWith(enabled: enabled);
    return _quietHours;
  }

  Future<NotificationChannels> setChannels({
    bool? email,
    bool? push,
  }) async {
    await _delay();
    _channels = _channels.copyWith(
      email: email ?? _channels.email,
      push: push ?? _channels.push,
    );
    return _channels;
  }

  // ---------- 我的 ----------

  ProfileData profile() {
    final unread = _notifications.where((n) => !n.read).length;
    return _profile.copyWith(
      borrowingCount: _borrows.where((b) => b.status != 'RETURNED').length,
      quotaUsed: _borrows.where((b) => b.status != 'RETURNED').length,
      reservationCount: reservations().ready.length,
      unreadCount: unread,
      blacklisted: blacklisted,
      blacklistReason: blacklistReason,
      isOffline: offline,
    );
  }

  Future<ProfileData> fetchProfile() async {
    await _delay();
    return profile();
  }

  // ---------- 申诉 ----------

  AppealsPageData appeals() {
    final sorted = [..._appeals]
      ..sort((a, b) => b.createdAt.compareTo(a.createdAt));
    final open = sorted
        .where((t) =>
            t.status == 'OPEN' ||
            t.status == 'ACKED' ||
            t.status == 'APPEALING')
        .length;
    return AppealsPageData(
      tickets: sorted,
      openCount: open,
      doneCount: sorted.length - open,
      isOffline: offline,
    );
  }

  Future<AppealsPageData> fetchAppeals() async {
    await _delay();
    return appeals();
  }

  Future<List<AppealType>> appealTypes() async {
    await _delay();
    return const [
      AppealType(code: 'OVERDUE_TIER', label: '逾期判定有误'),
      AppealType(code: 'GRACE', label: '宽限结果异议'),
      AppealType(code: 'REMINDER', label: '提醒未收到'),
      AppealType(code: 'RENEW', label: '续借被拒'),
      AppealType(code: 'OTHER', label: '其他问题'),
    ];
  }

  Future<AppealCreateResult> submitAppeal(AppealCreateRequest request) async {
    await _delay();
    if (request.description.trim().length < 15) {
      throw const ApiException(
        code: 'INVALID_ARGUMENT',
        message: '说明至少 15 个字',
        statusCode: 400,
      );
    }
    final ticketNo = 'A-${2042 + _appeals.length}';
    _appeals = [
      AppealTicket(
        id: '490000000000000000${_appeals.length + 4}',
        ticketNo: ticketNo,
        type: request.type,
        typeLabel: '新申诉',
        bookTitle: '乡土中国',
        status: 'OPEN',
        statusLabel: '待处理',
        description: request.description,
        createdAt: now,
        slaDueAt: now.add(const Duration(hours: 2)),
      ),
      ..._appeals,
    ];
    return AppealCreateResult(
      ticketNo: ticketNo,
      slaDueAt: now.add(const Duration(hours: 2)),
    );
  }
}
