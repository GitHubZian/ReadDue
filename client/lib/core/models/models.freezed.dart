// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint
// ignore_for_file: unused_element, deprecated_member_use, deprecated_member_use_from_same_package, use_function_type_syntax_for_parameters, unnecessary_const, avoid_init_to_null, invalid_override_different_default_values_named, prefer_expression_function_bodies, annotate_overrides, invalid_annotation_target, unnecessary_question_mark

part of 'models.dart';

// **************************************************************************
// FreezedGenerator
// **************************************************************************

T _$identity<T>(T value) => value;

final _privateConstructorUsedError = UnsupportedError(
    'It seems like you constructed your class using `MyClass._()`. This constructor is only meant to be used by freezed and you are not supposed to need it nor use it.\nPlease check the documentation here for more information: https://github.com/rrousselGit/freezed#adding-getters-and-methods-to-our-models');

Session _$SessionFromJson(Map<String, dynamic> json) {
  return _Session.fromJson(json);
}

/// @nodoc
mixin _$Session {
  String get token => throw _privateConstructorUsedError;
  String get userId => throw _privateConstructorUsedError;
  String get name => throw _privateConstructorUsedError;
  String get studentNo => throw _privateConstructorUsedError;

  /// Serializes this Session to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of Session
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $SessionCopyWith<Session> get copyWith => throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SessionCopyWith<$Res> {
  factory $SessionCopyWith(Session value, $Res Function(Session) then) =
      _$SessionCopyWithImpl<$Res, Session>;
  @useResult
  $Res call({String token, String userId, String name, String studentNo});
}

/// @nodoc
class _$SessionCopyWithImpl<$Res, $Val extends Session>
    implements $SessionCopyWith<$Res> {
  _$SessionCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of Session
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? token = null,
    Object? userId = null,
    Object? name = null,
    Object? studentNo = null,
  }) {
    return _then(_value.copyWith(
      token: null == token
          ? _value.token
          : token // ignore: cast_nullable_to_non_nullable
              as String,
      userId: null == userId
          ? _value.userId
          : userId // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      studentNo: null == studentNo
          ? _value.studentNo
          : studentNo // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SessionImplCopyWith<$Res> implements $SessionCopyWith<$Res> {
  factory _$$SessionImplCopyWith(
          _$SessionImpl value, $Res Function(_$SessionImpl) then) =
      __$$SessionImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String token, String userId, String name, String studentNo});
}

/// @nodoc
class __$$SessionImplCopyWithImpl<$Res>
    extends _$SessionCopyWithImpl<$Res, _$SessionImpl>
    implements _$$SessionImplCopyWith<$Res> {
  __$$SessionImplCopyWithImpl(
      _$SessionImpl _value, $Res Function(_$SessionImpl) _then)
      : super(_value, _then);

  /// Create a copy of Session
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? token = null,
    Object? userId = null,
    Object? name = null,
    Object? studentNo = null,
  }) {
    return _then(_$SessionImpl(
      token: null == token
          ? _value.token
          : token // ignore: cast_nullable_to_non_nullable
              as String,
      userId: null == userId
          ? _value.userId
          : userId // ignore: cast_nullable_to_non_nullable
              as String,
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      studentNo: null == studentNo
          ? _value.studentNo
          : studentNo // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SessionImpl implements _Session {
  const _$SessionImpl(
      {required this.token,
      required this.userId,
      required this.name,
      required this.studentNo});

  factory _$SessionImpl.fromJson(Map<String, dynamic> json) =>
      _$$SessionImplFromJson(json);

  @override
  final String token;
  @override
  final String userId;
  @override
  final String name;
  @override
  final String studentNo;

  @override
  String toString() {
    return 'Session(token: $token, userId: $userId, name: $name, studentNo: $studentNo)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SessionImpl &&
            (identical(other.token, token) || other.token == token) &&
            (identical(other.userId, userId) || other.userId == userId) &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.studentNo, studentNo) ||
                other.studentNo == studentNo));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, token, userId, name, studentNo);

  /// Create a copy of Session
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$SessionImplCopyWith<_$SessionImpl> get copyWith =>
      __$$SessionImplCopyWithImpl<_$SessionImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SessionImplToJson(
      this,
    );
  }
}

abstract class _Session implements Session {
  const factory _Session(
      {required final String token,
      required final String userId,
      required final String name,
      required final String studentNo}) = _$SessionImpl;

  factory _Session.fromJson(Map<String, dynamic> json) = _$SessionImpl.fromJson;

  @override
  String get token;
  @override
  String get userId;
  @override
  String get name;
  @override
  String get studentNo;

  /// Create a copy of Session
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$SessionImplCopyWith<_$SessionImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BorrowSummary _$BorrowSummaryFromJson(Map<String, dynamic> json) {
  return _BorrowSummary.fromJson(json);
}

/// @nodoc
mixin _$BorrowSummary {
  int get borrowing => throw _privateConstructorUsedError;
  int get dueSoon => throw _privateConstructorUsedError;
  int get overdue => throw _privateConstructorUsedError;
  int get returnedThisTerm => throw _privateConstructorUsedError;
  String get creditLabel => throw _privateConstructorUsedError;
  int get quotaUsed => throw _privateConstructorUsedError;
  int get quotaTotal => throw _privateConstructorUsedError;
  bool get blacklisted => throw _privateConstructorUsedError;
  String? get blacklistReason => throw _privateConstructorUsedError;

  /// Serializes this BorrowSummary to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BorrowSummary
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BorrowSummaryCopyWith<BorrowSummary> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BorrowSummaryCopyWith<$Res> {
  factory $BorrowSummaryCopyWith(
          BorrowSummary value, $Res Function(BorrowSummary) then) =
      _$BorrowSummaryCopyWithImpl<$Res, BorrowSummary>;
  @useResult
  $Res call(
      {int borrowing,
      int dueSoon,
      int overdue,
      int returnedThisTerm,
      String creditLabel,
      int quotaUsed,
      int quotaTotal,
      bool blacklisted,
      String? blacklistReason});
}

/// @nodoc
class _$BorrowSummaryCopyWithImpl<$Res, $Val extends BorrowSummary>
    implements $BorrowSummaryCopyWith<$Res> {
  _$BorrowSummaryCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BorrowSummary
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? borrowing = null,
    Object? dueSoon = null,
    Object? overdue = null,
    Object? returnedThisTerm = null,
    Object? creditLabel = null,
    Object? quotaUsed = null,
    Object? quotaTotal = null,
    Object? blacklisted = null,
    Object? blacklistReason = freezed,
  }) {
    return _then(_value.copyWith(
      borrowing: null == borrowing
          ? _value.borrowing
          : borrowing // ignore: cast_nullable_to_non_nullable
              as int,
      dueSoon: null == dueSoon
          ? _value.dueSoon
          : dueSoon // ignore: cast_nullable_to_non_nullable
              as int,
      overdue: null == overdue
          ? _value.overdue
          : overdue // ignore: cast_nullable_to_non_nullable
              as int,
      returnedThisTerm: null == returnedThisTerm
          ? _value.returnedThisTerm
          : returnedThisTerm // ignore: cast_nullable_to_non_nullable
              as int,
      creditLabel: null == creditLabel
          ? _value.creditLabel
          : creditLabel // ignore: cast_nullable_to_non_nullable
              as String,
      quotaUsed: null == quotaUsed
          ? _value.quotaUsed
          : quotaUsed // ignore: cast_nullable_to_non_nullable
              as int,
      quotaTotal: null == quotaTotal
          ? _value.quotaTotal
          : quotaTotal // ignore: cast_nullable_to_non_nullable
              as int,
      blacklisted: null == blacklisted
          ? _value.blacklisted
          : blacklisted // ignore: cast_nullable_to_non_nullable
              as bool,
      blacklistReason: freezed == blacklistReason
          ? _value.blacklistReason
          : blacklistReason // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BorrowSummaryImplCopyWith<$Res>
    implements $BorrowSummaryCopyWith<$Res> {
  factory _$$BorrowSummaryImplCopyWith(
          _$BorrowSummaryImpl value, $Res Function(_$BorrowSummaryImpl) then) =
      __$$BorrowSummaryImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {int borrowing,
      int dueSoon,
      int overdue,
      int returnedThisTerm,
      String creditLabel,
      int quotaUsed,
      int quotaTotal,
      bool blacklisted,
      String? blacklistReason});
}

/// @nodoc
class __$$BorrowSummaryImplCopyWithImpl<$Res>
    extends _$BorrowSummaryCopyWithImpl<$Res, _$BorrowSummaryImpl>
    implements _$$BorrowSummaryImplCopyWith<$Res> {
  __$$BorrowSummaryImplCopyWithImpl(
      _$BorrowSummaryImpl _value, $Res Function(_$BorrowSummaryImpl) _then)
      : super(_value, _then);

  /// Create a copy of BorrowSummary
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? borrowing = null,
    Object? dueSoon = null,
    Object? overdue = null,
    Object? returnedThisTerm = null,
    Object? creditLabel = null,
    Object? quotaUsed = null,
    Object? quotaTotal = null,
    Object? blacklisted = null,
    Object? blacklistReason = freezed,
  }) {
    return _then(_$BorrowSummaryImpl(
      borrowing: null == borrowing
          ? _value.borrowing
          : borrowing // ignore: cast_nullable_to_non_nullable
              as int,
      dueSoon: null == dueSoon
          ? _value.dueSoon
          : dueSoon // ignore: cast_nullable_to_non_nullable
              as int,
      overdue: null == overdue
          ? _value.overdue
          : overdue // ignore: cast_nullable_to_non_nullable
              as int,
      returnedThisTerm: null == returnedThisTerm
          ? _value.returnedThisTerm
          : returnedThisTerm // ignore: cast_nullable_to_non_nullable
              as int,
      creditLabel: null == creditLabel
          ? _value.creditLabel
          : creditLabel // ignore: cast_nullable_to_non_nullable
              as String,
      quotaUsed: null == quotaUsed
          ? _value.quotaUsed
          : quotaUsed // ignore: cast_nullable_to_non_nullable
              as int,
      quotaTotal: null == quotaTotal
          ? _value.quotaTotal
          : quotaTotal // ignore: cast_nullable_to_non_nullable
              as int,
      blacklisted: null == blacklisted
          ? _value.blacklisted
          : blacklisted // ignore: cast_nullable_to_non_nullable
              as bool,
      blacklistReason: freezed == blacklistReason
          ? _value.blacklistReason
          : blacklistReason // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BorrowSummaryImpl implements _BorrowSummary {
  const _$BorrowSummaryImpl(
      {this.borrowing = 0,
      this.dueSoon = 0,
      this.overdue = 0,
      this.returnedThisTerm = 0,
      this.creditLabel = '信用良好',
      this.quotaUsed = 0,
      this.quotaTotal = 0,
      this.blacklisted = false,
      this.blacklistReason});

  factory _$BorrowSummaryImpl.fromJson(Map<String, dynamic> json) =>
      _$$BorrowSummaryImplFromJson(json);

  @override
  @JsonKey()
  final int borrowing;
  @override
  @JsonKey()
  final int dueSoon;
  @override
  @JsonKey()
  final int overdue;
  @override
  @JsonKey()
  final int returnedThisTerm;
  @override
  @JsonKey()
  final String creditLabel;
  @override
  @JsonKey()
  final int quotaUsed;
  @override
  @JsonKey()
  final int quotaTotal;
  @override
  @JsonKey()
  final bool blacklisted;
  @override
  final String? blacklistReason;

  @override
  String toString() {
    return 'BorrowSummary(borrowing: $borrowing, dueSoon: $dueSoon, overdue: $overdue, returnedThisTerm: $returnedThisTerm, creditLabel: $creditLabel, quotaUsed: $quotaUsed, quotaTotal: $quotaTotal, blacklisted: $blacklisted, blacklistReason: $blacklistReason)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BorrowSummaryImpl &&
            (identical(other.borrowing, borrowing) ||
                other.borrowing == borrowing) &&
            (identical(other.dueSoon, dueSoon) || other.dueSoon == dueSoon) &&
            (identical(other.overdue, overdue) || other.overdue == overdue) &&
            (identical(other.returnedThisTerm, returnedThisTerm) ||
                other.returnedThisTerm == returnedThisTerm) &&
            (identical(other.creditLabel, creditLabel) ||
                other.creditLabel == creditLabel) &&
            (identical(other.quotaUsed, quotaUsed) ||
                other.quotaUsed == quotaUsed) &&
            (identical(other.quotaTotal, quotaTotal) ||
                other.quotaTotal == quotaTotal) &&
            (identical(other.blacklisted, blacklisted) ||
                other.blacklisted == blacklisted) &&
            (identical(other.blacklistReason, blacklistReason) ||
                other.blacklistReason == blacklistReason));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      borrowing,
      dueSoon,
      overdue,
      returnedThisTerm,
      creditLabel,
      quotaUsed,
      quotaTotal,
      blacklisted,
      blacklistReason);

  /// Create a copy of BorrowSummary
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BorrowSummaryImplCopyWith<_$BorrowSummaryImpl> get copyWith =>
      __$$BorrowSummaryImplCopyWithImpl<_$BorrowSummaryImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BorrowSummaryImplToJson(
      this,
    );
  }
}

abstract class _BorrowSummary implements BorrowSummary {
  const factory _BorrowSummary(
      {final int borrowing,
      final int dueSoon,
      final int overdue,
      final int returnedThisTerm,
      final String creditLabel,
      final int quotaUsed,
      final int quotaTotal,
      final bool blacklisted,
      final String? blacklistReason}) = _$BorrowSummaryImpl;

  factory _BorrowSummary.fromJson(Map<String, dynamic> json) =
      _$BorrowSummaryImpl.fromJson;

  @override
  int get borrowing;
  @override
  int get dueSoon;
  @override
  int get overdue;
  @override
  int get returnedThisTerm;
  @override
  String get creditLabel;
  @override
  int get quotaUsed;
  @override
  int get quotaTotal;
  @override
  bool get blacklisted;
  @override
  String? get blacklistReason;

  /// Create a copy of BorrowSummary
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BorrowSummaryImplCopyWith<_$BorrowSummaryImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BorrowItem _$BorrowItemFromJson(Map<String, dynamic> json) {
  return _BorrowItem.fromJson(json);
}

/// @nodoc
mixin _$BorrowItem {
  String get id => throw _privateConstructorUsedError;
  String get orderNo => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String get author => throw _privateConstructorUsedError;
  String get publisher => throw _privateConstructorUsedError;
  String get callNo => throw _privateConstructorUsedError;
  String get shelfCode => throw _privateConstructorUsedError;
  String get coverLabel => throw _privateConstructorUsedError;
  String get coverTone => throw _privateConstructorUsedError;
  DateTime get borrowedAt => throw _privateConstructorUsedError;
  DateTime get dueAt => throw _privateConstructorUsedError;
  DateTime? get effectiveDueAt => throw _privateConstructorUsedError;
  int get overdueDays => throw _privateConstructorUsedError;
  int get daysLeft => throw _privateConstructorUsedError;
  int get renewCount => throw _privateConstructorUsedError;
  String get dueLabel => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  bool get canRenew => throw _privateConstructorUsedError;
  bool get gracePending => throw _privateConstructorUsedError;

  /// Serializes this BorrowItem to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BorrowItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BorrowItemCopyWith<BorrowItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BorrowItemCopyWith<$Res> {
  factory $BorrowItemCopyWith(
          BorrowItem value, $Res Function(BorrowItem) then) =
      _$BorrowItemCopyWithImpl<$Res, BorrowItem>;
  @useResult
  $Res call(
      {String id,
      String orderNo,
      String title,
      String author,
      String publisher,
      String callNo,
      String shelfCode,
      String coverLabel,
      String coverTone,
      DateTime borrowedAt,
      DateTime dueAt,
      DateTime? effectiveDueAt,
      int overdueDays,
      int daysLeft,
      int renewCount,
      String dueLabel,
      String status,
      bool canRenew,
      bool gracePending});
}

/// @nodoc
class _$BorrowItemCopyWithImpl<$Res, $Val extends BorrowItem>
    implements $BorrowItemCopyWith<$Res> {
  _$BorrowItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BorrowItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? orderNo = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? callNo = null,
    Object? shelfCode = null,
    Object? coverLabel = null,
    Object? coverTone = null,
    Object? borrowedAt = null,
    Object? dueAt = null,
    Object? effectiveDueAt = freezed,
    Object? overdueDays = null,
    Object? daysLeft = null,
    Object? renewCount = null,
    Object? dueLabel = null,
    Object? status = null,
    Object? canRenew = null,
    Object? gracePending = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      orderNo: null == orderNo
          ? _value.orderNo
          : orderNo // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      callNo: null == callNo
          ? _value.callNo
          : callNo // ignore: cast_nullable_to_non_nullable
              as String,
      shelfCode: null == shelfCode
          ? _value.shelfCode
          : shelfCode // ignore: cast_nullable_to_non_nullable
              as String,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
      borrowedAt: null == borrowedAt
          ? _value.borrowedAt
          : borrowedAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      dueAt: null == dueAt
          ? _value.dueAt
          : dueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      effectiveDueAt: freezed == effectiveDueAt
          ? _value.effectiveDueAt
          : effectiveDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      overdueDays: null == overdueDays
          ? _value.overdueDays
          : overdueDays // ignore: cast_nullable_to_non_nullable
              as int,
      daysLeft: null == daysLeft
          ? _value.daysLeft
          : daysLeft // ignore: cast_nullable_to_non_nullable
              as int,
      renewCount: null == renewCount
          ? _value.renewCount
          : renewCount // ignore: cast_nullable_to_non_nullable
              as int,
      dueLabel: null == dueLabel
          ? _value.dueLabel
          : dueLabel // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      canRenew: null == canRenew
          ? _value.canRenew
          : canRenew // ignore: cast_nullable_to_non_nullable
              as bool,
      gracePending: null == gracePending
          ? _value.gracePending
          : gracePending // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BorrowItemImplCopyWith<$Res>
    implements $BorrowItemCopyWith<$Res> {
  factory _$$BorrowItemImplCopyWith(
          _$BorrowItemImpl value, $Res Function(_$BorrowItemImpl) then) =
      __$$BorrowItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String orderNo,
      String title,
      String author,
      String publisher,
      String callNo,
      String shelfCode,
      String coverLabel,
      String coverTone,
      DateTime borrowedAt,
      DateTime dueAt,
      DateTime? effectiveDueAt,
      int overdueDays,
      int daysLeft,
      int renewCount,
      String dueLabel,
      String status,
      bool canRenew,
      bool gracePending});
}

/// @nodoc
class __$$BorrowItemImplCopyWithImpl<$Res>
    extends _$BorrowItemCopyWithImpl<$Res, _$BorrowItemImpl>
    implements _$$BorrowItemImplCopyWith<$Res> {
  __$$BorrowItemImplCopyWithImpl(
      _$BorrowItemImpl _value, $Res Function(_$BorrowItemImpl) _then)
      : super(_value, _then);

  /// Create a copy of BorrowItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? orderNo = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? callNo = null,
    Object? shelfCode = null,
    Object? coverLabel = null,
    Object? coverTone = null,
    Object? borrowedAt = null,
    Object? dueAt = null,
    Object? effectiveDueAt = freezed,
    Object? overdueDays = null,
    Object? daysLeft = null,
    Object? renewCount = null,
    Object? dueLabel = null,
    Object? status = null,
    Object? canRenew = null,
    Object? gracePending = null,
  }) {
    return _then(_$BorrowItemImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      orderNo: null == orderNo
          ? _value.orderNo
          : orderNo // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      callNo: null == callNo
          ? _value.callNo
          : callNo // ignore: cast_nullable_to_non_nullable
              as String,
      shelfCode: null == shelfCode
          ? _value.shelfCode
          : shelfCode // ignore: cast_nullable_to_non_nullable
              as String,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
      borrowedAt: null == borrowedAt
          ? _value.borrowedAt
          : borrowedAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      dueAt: null == dueAt
          ? _value.dueAt
          : dueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      effectiveDueAt: freezed == effectiveDueAt
          ? _value.effectiveDueAt
          : effectiveDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      overdueDays: null == overdueDays
          ? _value.overdueDays
          : overdueDays // ignore: cast_nullable_to_non_nullable
              as int,
      daysLeft: null == daysLeft
          ? _value.daysLeft
          : daysLeft // ignore: cast_nullable_to_non_nullable
              as int,
      renewCount: null == renewCount
          ? _value.renewCount
          : renewCount // ignore: cast_nullable_to_non_nullable
              as int,
      dueLabel: null == dueLabel
          ? _value.dueLabel
          : dueLabel // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      canRenew: null == canRenew
          ? _value.canRenew
          : canRenew // ignore: cast_nullable_to_non_nullable
              as bool,
      gracePending: null == gracePending
          ? _value.gracePending
          : gracePending // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BorrowItemImpl implements _BorrowItem {
  const _$BorrowItemImpl(
      {required this.id,
      required this.orderNo,
      required this.title,
      required this.author,
      this.publisher = '',
      this.callNo = '',
      this.shelfCode = '',
      required this.coverLabel,
      this.coverTone = 'mint',
      required this.borrowedAt,
      required this.dueAt,
      this.effectiveDueAt,
      this.overdueDays = 0,
      this.daysLeft = 0,
      this.renewCount = 0,
      this.dueLabel = '',
      this.status = 'BORROWED',
      this.canRenew = true,
      this.gracePending = false});

  factory _$BorrowItemImpl.fromJson(Map<String, dynamic> json) =>
      _$$BorrowItemImplFromJson(json);

  @override
  final String id;
  @override
  final String orderNo;
  @override
  final String title;
  @override
  final String author;
  @override
  @JsonKey()
  final String publisher;
  @override
  @JsonKey()
  final String callNo;
  @override
  @JsonKey()
  final String shelfCode;
  @override
  final String coverLabel;
  @override
  @JsonKey()
  final String coverTone;
  @override
  final DateTime borrowedAt;
  @override
  final DateTime dueAt;
  @override
  final DateTime? effectiveDueAt;
  @override
  @JsonKey()
  final int overdueDays;
  @override
  @JsonKey()
  final int daysLeft;
  @override
  @JsonKey()
  final int renewCount;
  @override
  @JsonKey()
  final String dueLabel;
  @override
  @JsonKey()
  final String status;
  @override
  @JsonKey()
  final bool canRenew;
  @override
  @JsonKey()
  final bool gracePending;

  @override
  String toString() {
    return 'BorrowItem(id: $id, orderNo: $orderNo, title: $title, author: $author, publisher: $publisher, callNo: $callNo, shelfCode: $shelfCode, coverLabel: $coverLabel, coverTone: $coverTone, borrowedAt: $borrowedAt, dueAt: $dueAt, effectiveDueAt: $effectiveDueAt, overdueDays: $overdueDays, daysLeft: $daysLeft, renewCount: $renewCount, dueLabel: $dueLabel, status: $status, canRenew: $canRenew, gracePending: $gracePending)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BorrowItemImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.orderNo, orderNo) || other.orderNo == orderNo) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.author, author) || other.author == author) &&
            (identical(other.publisher, publisher) ||
                other.publisher == publisher) &&
            (identical(other.callNo, callNo) || other.callNo == callNo) &&
            (identical(other.shelfCode, shelfCode) ||
                other.shelfCode == shelfCode) &&
            (identical(other.coverLabel, coverLabel) ||
                other.coverLabel == coverLabel) &&
            (identical(other.coverTone, coverTone) ||
                other.coverTone == coverTone) &&
            (identical(other.borrowedAt, borrowedAt) ||
                other.borrowedAt == borrowedAt) &&
            (identical(other.dueAt, dueAt) || other.dueAt == dueAt) &&
            (identical(other.effectiveDueAt, effectiveDueAt) ||
                other.effectiveDueAt == effectiveDueAt) &&
            (identical(other.overdueDays, overdueDays) ||
                other.overdueDays == overdueDays) &&
            (identical(other.daysLeft, daysLeft) ||
                other.daysLeft == daysLeft) &&
            (identical(other.renewCount, renewCount) ||
                other.renewCount == renewCount) &&
            (identical(other.dueLabel, dueLabel) ||
                other.dueLabel == dueLabel) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.canRenew, canRenew) ||
                other.canRenew == canRenew) &&
            (identical(other.gracePending, gracePending) ||
                other.gracePending == gracePending));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hashAll([
        runtimeType,
        id,
        orderNo,
        title,
        author,
        publisher,
        callNo,
        shelfCode,
        coverLabel,
        coverTone,
        borrowedAt,
        dueAt,
        effectiveDueAt,
        overdueDays,
        daysLeft,
        renewCount,
        dueLabel,
        status,
        canRenew,
        gracePending
      ]);

  /// Create a copy of BorrowItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BorrowItemImplCopyWith<_$BorrowItemImpl> get copyWith =>
      __$$BorrowItemImplCopyWithImpl<_$BorrowItemImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BorrowItemImplToJson(
      this,
    );
  }
}

abstract class _BorrowItem implements BorrowItem {
  const factory _BorrowItem(
      {required final String id,
      required final String orderNo,
      required final String title,
      required final String author,
      final String publisher,
      final String callNo,
      final String shelfCode,
      required final String coverLabel,
      final String coverTone,
      required final DateTime borrowedAt,
      required final DateTime dueAt,
      final DateTime? effectiveDueAt,
      final int overdueDays,
      final int daysLeft,
      final int renewCount,
      final String dueLabel,
      final String status,
      final bool canRenew,
      final bool gracePending}) = _$BorrowItemImpl;

  factory _BorrowItem.fromJson(Map<String, dynamic> json) =
      _$BorrowItemImpl.fromJson;

  @override
  String get id;
  @override
  String get orderNo;
  @override
  String get title;
  @override
  String get author;
  @override
  String get publisher;
  @override
  String get callNo;
  @override
  String get shelfCode;
  @override
  String get coverLabel;
  @override
  String get coverTone;
  @override
  DateTime get borrowedAt;
  @override
  DateTime get dueAt;
  @override
  DateTime? get effectiveDueAt;
  @override
  int get overdueDays;
  @override
  int get daysLeft;
  @override
  int get renewCount;
  @override
  String get dueLabel;
  @override
  String get status;
  @override
  bool get canRenew;
  @override
  bool get gracePending;

  /// Create a copy of BorrowItem
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BorrowItemImplCopyWith<_$BorrowItemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

ReadyBook _$ReadyBookFromJson(Map<String, dynamic> json) {
  return _ReadyBook.fromJson(json);
}

/// @nodoc
mixin _$ReadyBook {
  String get reservationId => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String get author => throw _privateConstructorUsedError;
  String get pickupPoint => throw _privateConstructorUsedError;
  String get pickupCode => throw _privateConstructorUsedError;
  DateTime get expiresAt => throw _privateConstructorUsedError;
  int get remainingHours => throw _privateConstructorUsedError;
  int get queueNo => throw _privateConstructorUsedError;

  /// Serializes this ReadyBook to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ReadyBook
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ReadyBookCopyWith<ReadyBook> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ReadyBookCopyWith<$Res> {
  factory $ReadyBookCopyWith(ReadyBook value, $Res Function(ReadyBook) then) =
      _$ReadyBookCopyWithImpl<$Res, ReadyBook>;
  @useResult
  $Res call(
      {String reservationId,
      String title,
      String author,
      String pickupPoint,
      String pickupCode,
      DateTime expiresAt,
      int remainingHours,
      int queueNo});
}

/// @nodoc
class _$ReadyBookCopyWithImpl<$Res, $Val extends ReadyBook>
    implements $ReadyBookCopyWith<$Res> {
  _$ReadyBookCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ReadyBook
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? reservationId = null,
    Object? title = null,
    Object? author = null,
    Object? pickupPoint = null,
    Object? pickupCode = null,
    Object? expiresAt = null,
    Object? remainingHours = null,
    Object? queueNo = null,
  }) {
    return _then(_value.copyWith(
      reservationId: null == reservationId
          ? _value.reservationId
          : reservationId // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      pickupPoint: null == pickupPoint
          ? _value.pickupPoint
          : pickupPoint // ignore: cast_nullable_to_non_nullable
              as String,
      pickupCode: null == pickupCode
          ? _value.pickupCode
          : pickupCode // ignore: cast_nullable_to_non_nullable
              as String,
      expiresAt: null == expiresAt
          ? _value.expiresAt
          : expiresAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      remainingHours: null == remainingHours
          ? _value.remainingHours
          : remainingHours // ignore: cast_nullable_to_non_nullable
              as int,
      queueNo: null == queueNo
          ? _value.queueNo
          : queueNo // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ReadyBookImplCopyWith<$Res>
    implements $ReadyBookCopyWith<$Res> {
  factory _$$ReadyBookImplCopyWith(
          _$ReadyBookImpl value, $Res Function(_$ReadyBookImpl) then) =
      __$$ReadyBookImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String reservationId,
      String title,
      String author,
      String pickupPoint,
      String pickupCode,
      DateTime expiresAt,
      int remainingHours,
      int queueNo});
}

/// @nodoc
class __$$ReadyBookImplCopyWithImpl<$Res>
    extends _$ReadyBookCopyWithImpl<$Res, _$ReadyBookImpl>
    implements _$$ReadyBookImplCopyWith<$Res> {
  __$$ReadyBookImplCopyWithImpl(
      _$ReadyBookImpl _value, $Res Function(_$ReadyBookImpl) _then)
      : super(_value, _then);

  /// Create a copy of ReadyBook
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? reservationId = null,
    Object? title = null,
    Object? author = null,
    Object? pickupPoint = null,
    Object? pickupCode = null,
    Object? expiresAt = null,
    Object? remainingHours = null,
    Object? queueNo = null,
  }) {
    return _then(_$ReadyBookImpl(
      reservationId: null == reservationId
          ? _value.reservationId
          : reservationId // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      pickupPoint: null == pickupPoint
          ? _value.pickupPoint
          : pickupPoint // ignore: cast_nullable_to_non_nullable
              as String,
      pickupCode: null == pickupCode
          ? _value.pickupCode
          : pickupCode // ignore: cast_nullable_to_non_nullable
              as String,
      expiresAt: null == expiresAt
          ? _value.expiresAt
          : expiresAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      remainingHours: null == remainingHours
          ? _value.remainingHours
          : remainingHours // ignore: cast_nullable_to_non_nullable
              as int,
      queueNo: null == queueNo
          ? _value.queueNo
          : queueNo // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ReadyBookImpl implements _ReadyBook {
  const _$ReadyBookImpl(
      {required this.reservationId,
      required this.title,
      required this.author,
      required this.pickupPoint,
      required this.pickupCode,
      required this.expiresAt,
      this.remainingHours = 0,
      this.queueNo = 1});

  factory _$ReadyBookImpl.fromJson(Map<String, dynamic> json) =>
      _$$ReadyBookImplFromJson(json);

  @override
  final String reservationId;
  @override
  final String title;
  @override
  final String author;
  @override
  final String pickupPoint;
  @override
  final String pickupCode;
  @override
  final DateTime expiresAt;
  @override
  @JsonKey()
  final int remainingHours;
  @override
  @JsonKey()
  final int queueNo;

  @override
  String toString() {
    return 'ReadyBook(reservationId: $reservationId, title: $title, author: $author, pickupPoint: $pickupPoint, pickupCode: $pickupCode, expiresAt: $expiresAt, remainingHours: $remainingHours, queueNo: $queueNo)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ReadyBookImpl &&
            (identical(other.reservationId, reservationId) ||
                other.reservationId == reservationId) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.author, author) || other.author == author) &&
            (identical(other.pickupPoint, pickupPoint) ||
                other.pickupPoint == pickupPoint) &&
            (identical(other.pickupCode, pickupCode) ||
                other.pickupCode == pickupCode) &&
            (identical(other.expiresAt, expiresAt) ||
                other.expiresAt == expiresAt) &&
            (identical(other.remainingHours, remainingHours) ||
                other.remainingHours == remainingHours) &&
            (identical(other.queueNo, queueNo) || other.queueNo == queueNo));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, reservationId, title, author,
      pickupPoint, pickupCode, expiresAt, remainingHours, queueNo);

  /// Create a copy of ReadyBook
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ReadyBookImplCopyWith<_$ReadyBookImpl> get copyWith =>
      __$$ReadyBookImplCopyWithImpl<_$ReadyBookImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ReadyBookImplToJson(
      this,
    );
  }
}

abstract class _ReadyBook implements ReadyBook {
  const factory _ReadyBook(
      {required final String reservationId,
      required final String title,
      required final String author,
      required final String pickupPoint,
      required final String pickupCode,
      required final DateTime expiresAt,
      final int remainingHours,
      final int queueNo}) = _$ReadyBookImpl;

  factory _ReadyBook.fromJson(Map<String, dynamic> json) =
      _$ReadyBookImpl.fromJson;

  @override
  String get reservationId;
  @override
  String get title;
  @override
  String get author;
  @override
  String get pickupPoint;
  @override
  String get pickupCode;
  @override
  DateTime get expiresAt;
  @override
  int get remainingHours;
  @override
  int get queueNo;

  /// Create a copy of ReadyBook
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ReadyBookImplCopyWith<_$ReadyBookImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BorrowDashboard _$BorrowDashboardFromJson(Map<String, dynamic> json) {
  return _BorrowDashboard.fromJson(json);
}

/// @nodoc
mixin _$BorrowDashboard {
  BorrowSummary get summary => throw _privateConstructorUsedError;
  List<BorrowItem> get dueSoon => throw _privateConstructorUsedError;
  List<ReadyBook> get readyBooks => throw _privateConstructorUsedError;
  String get quietHoursText => throw _privateConstructorUsedError;
  String get feeNote => throw _privateConstructorUsedError;
  int get unreadCount => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;
  DateTime? get lastSyncAt => throw _privateConstructorUsedError;

  /// Serializes this BorrowDashboard to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BorrowDashboard
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BorrowDashboardCopyWith<BorrowDashboard> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BorrowDashboardCopyWith<$Res> {
  factory $BorrowDashboardCopyWith(
          BorrowDashboard value, $Res Function(BorrowDashboard) then) =
      _$BorrowDashboardCopyWithImpl<$Res, BorrowDashboard>;
  @useResult
  $Res call(
      {BorrowSummary summary,
      List<BorrowItem> dueSoon,
      List<ReadyBook> readyBooks,
      String quietHoursText,
      String feeNote,
      int unreadCount,
      bool isOffline,
      DateTime? lastSyncAt});

  $BorrowSummaryCopyWith<$Res> get summary;
}

/// @nodoc
class _$BorrowDashboardCopyWithImpl<$Res, $Val extends BorrowDashboard>
    implements $BorrowDashboardCopyWith<$Res> {
  _$BorrowDashboardCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BorrowDashboard
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? summary = null,
    Object? dueSoon = null,
    Object? readyBooks = null,
    Object? quietHoursText = null,
    Object? feeNote = null,
    Object? unreadCount = null,
    Object? isOffline = null,
    Object? lastSyncAt = freezed,
  }) {
    return _then(_value.copyWith(
      summary: null == summary
          ? _value.summary
          : summary // ignore: cast_nullable_to_non_nullable
              as BorrowSummary,
      dueSoon: null == dueSoon
          ? _value.dueSoon
          : dueSoon // ignore: cast_nullable_to_non_nullable
              as List<BorrowItem>,
      readyBooks: null == readyBooks
          ? _value.readyBooks
          : readyBooks // ignore: cast_nullable_to_non_nullable
              as List<ReadyBook>,
      quietHoursText: null == quietHoursText
          ? _value.quietHoursText
          : quietHoursText // ignore: cast_nullable_to_non_nullable
              as String,
      feeNote: null == feeNote
          ? _value.feeNote
          : feeNote // ignore: cast_nullable_to_non_nullable
              as String,
      unreadCount: null == unreadCount
          ? _value.unreadCount
          : unreadCount // ignore: cast_nullable_to_non_nullable
              as int,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
      lastSyncAt: freezed == lastSyncAt
          ? _value.lastSyncAt
          : lastSyncAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }

  /// Create a copy of BorrowDashboard
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $BorrowSummaryCopyWith<$Res> get summary {
    return $BorrowSummaryCopyWith<$Res>(_value.summary, (value) {
      return _then(_value.copyWith(summary: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$BorrowDashboardImplCopyWith<$Res>
    implements $BorrowDashboardCopyWith<$Res> {
  factory _$$BorrowDashboardImplCopyWith(_$BorrowDashboardImpl value,
          $Res Function(_$BorrowDashboardImpl) then) =
      __$$BorrowDashboardImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {BorrowSummary summary,
      List<BorrowItem> dueSoon,
      List<ReadyBook> readyBooks,
      String quietHoursText,
      String feeNote,
      int unreadCount,
      bool isOffline,
      DateTime? lastSyncAt});

  @override
  $BorrowSummaryCopyWith<$Res> get summary;
}

/// @nodoc
class __$$BorrowDashboardImplCopyWithImpl<$Res>
    extends _$BorrowDashboardCopyWithImpl<$Res, _$BorrowDashboardImpl>
    implements _$$BorrowDashboardImplCopyWith<$Res> {
  __$$BorrowDashboardImplCopyWithImpl(
      _$BorrowDashboardImpl _value, $Res Function(_$BorrowDashboardImpl) _then)
      : super(_value, _then);

  /// Create a copy of BorrowDashboard
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? summary = null,
    Object? dueSoon = null,
    Object? readyBooks = null,
    Object? quietHoursText = null,
    Object? feeNote = null,
    Object? unreadCount = null,
    Object? isOffline = null,
    Object? lastSyncAt = freezed,
  }) {
    return _then(_$BorrowDashboardImpl(
      summary: null == summary
          ? _value.summary
          : summary // ignore: cast_nullable_to_non_nullable
              as BorrowSummary,
      dueSoon: null == dueSoon
          ? _value._dueSoon
          : dueSoon // ignore: cast_nullable_to_non_nullable
              as List<BorrowItem>,
      readyBooks: null == readyBooks
          ? _value._readyBooks
          : readyBooks // ignore: cast_nullable_to_non_nullable
              as List<ReadyBook>,
      quietHoursText: null == quietHoursText
          ? _value.quietHoursText
          : quietHoursText // ignore: cast_nullable_to_non_nullable
              as String,
      feeNote: null == feeNote
          ? _value.feeNote
          : feeNote // ignore: cast_nullable_to_non_nullable
              as String,
      unreadCount: null == unreadCount
          ? _value.unreadCount
          : unreadCount // ignore: cast_nullable_to_non_nullable
              as int,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
      lastSyncAt: freezed == lastSyncAt
          ? _value.lastSyncAt
          : lastSyncAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BorrowDashboardImpl implements _BorrowDashboard {
  const _$BorrowDashboardImpl(
      {required this.summary,
      final List<BorrowItem> dueSoon = const <BorrowItem>[],
      final List<ReadyBook> readyBooks = const <ReadyBook>[],
      this.quietHoursText = '22:00–08:00',
      this.feeNote = '逾期费用 ¥1.00 / 天 · 归还时一次结清',
      this.unreadCount = 0,
      this.isOffline = false,
      this.lastSyncAt})
      : _dueSoon = dueSoon,
        _readyBooks = readyBooks;

  factory _$BorrowDashboardImpl.fromJson(Map<String, dynamic> json) =>
      _$$BorrowDashboardImplFromJson(json);

  @override
  final BorrowSummary summary;
  final List<BorrowItem> _dueSoon;
  @override
  @JsonKey()
  List<BorrowItem> get dueSoon {
    if (_dueSoon is EqualUnmodifiableListView) return _dueSoon;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_dueSoon);
  }

  final List<ReadyBook> _readyBooks;
  @override
  @JsonKey()
  List<ReadyBook> get readyBooks {
    if (_readyBooks is EqualUnmodifiableListView) return _readyBooks;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_readyBooks);
  }

  @override
  @JsonKey()
  final String quietHoursText;
  @override
  @JsonKey()
  final String feeNote;
  @override
  @JsonKey()
  final int unreadCount;
  @override
  @JsonKey()
  final bool isOffline;
  @override
  final DateTime? lastSyncAt;

  @override
  String toString() {
    return 'BorrowDashboard(summary: $summary, dueSoon: $dueSoon, readyBooks: $readyBooks, quietHoursText: $quietHoursText, feeNote: $feeNote, unreadCount: $unreadCount, isOffline: $isOffline, lastSyncAt: $lastSyncAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BorrowDashboardImpl &&
            (identical(other.summary, summary) || other.summary == summary) &&
            const DeepCollectionEquality().equals(other._dueSoon, _dueSoon) &&
            const DeepCollectionEquality()
                .equals(other._readyBooks, _readyBooks) &&
            (identical(other.quietHoursText, quietHoursText) ||
                other.quietHoursText == quietHoursText) &&
            (identical(other.feeNote, feeNote) || other.feeNote == feeNote) &&
            (identical(other.unreadCount, unreadCount) ||
                other.unreadCount == unreadCount) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline) &&
            (identical(other.lastSyncAt, lastSyncAt) ||
                other.lastSyncAt == lastSyncAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      summary,
      const DeepCollectionEquality().hash(_dueSoon),
      const DeepCollectionEquality().hash(_readyBooks),
      quietHoursText,
      feeNote,
      unreadCount,
      isOffline,
      lastSyncAt);

  /// Create a copy of BorrowDashboard
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BorrowDashboardImplCopyWith<_$BorrowDashboardImpl> get copyWith =>
      __$$BorrowDashboardImplCopyWithImpl<_$BorrowDashboardImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BorrowDashboardImplToJson(
      this,
    );
  }
}

abstract class _BorrowDashboard implements BorrowDashboard {
  const factory _BorrowDashboard(
      {required final BorrowSummary summary,
      final List<BorrowItem> dueSoon,
      final List<ReadyBook> readyBooks,
      final String quietHoursText,
      final String feeNote,
      final int unreadCount,
      final bool isOffline,
      final DateTime? lastSyncAt}) = _$BorrowDashboardImpl;

  factory _BorrowDashboard.fromJson(Map<String, dynamic> json) =
      _$BorrowDashboardImpl.fromJson;

  @override
  BorrowSummary get summary;
  @override
  List<BorrowItem> get dueSoon;
  @override
  List<ReadyBook> get readyBooks;
  @override
  String get quietHoursText;
  @override
  String get feeNote;
  @override
  int get unreadCount;
  @override
  bool get isOffline;
  @override
  DateTime? get lastSyncAt;

  /// Create a copy of BorrowDashboard
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BorrowDashboardImplCopyWith<_$BorrowDashboardImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

TimelineEvent _$TimelineEventFromJson(Map<String, dynamic> json) {
  return _TimelineEvent.fromJson(json);
}

/// @nodoc
mixin _$TimelineEvent {
  String get id => throw _privateConstructorUsedError;
  DateTime? get time => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String? get subtitle => throw _privateConstructorUsedError;
  String get state => throw _privateConstructorUsedError;

  /// Serializes this TimelineEvent to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of TimelineEvent
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $TimelineEventCopyWith<TimelineEvent> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $TimelineEventCopyWith<$Res> {
  factory $TimelineEventCopyWith(
          TimelineEvent value, $Res Function(TimelineEvent) then) =
      _$TimelineEventCopyWithImpl<$Res, TimelineEvent>;
  @useResult
  $Res call(
      {String id,
      DateTime? time,
      String title,
      String? subtitle,
      String state});
}

/// @nodoc
class _$TimelineEventCopyWithImpl<$Res, $Val extends TimelineEvent>
    implements $TimelineEventCopyWith<$Res> {
  _$TimelineEventCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of TimelineEvent
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? time = freezed,
    Object? title = null,
    Object? subtitle = freezed,
    Object? state = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      time: freezed == time
          ? _value.time
          : time // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      subtitle: freezed == subtitle
          ? _value.subtitle
          : subtitle // ignore: cast_nullable_to_non_nullable
              as String?,
      state: null == state
          ? _value.state
          : state // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$TimelineEventImplCopyWith<$Res>
    implements $TimelineEventCopyWith<$Res> {
  factory _$$TimelineEventImplCopyWith(
          _$TimelineEventImpl value, $Res Function(_$TimelineEventImpl) then) =
      __$$TimelineEventImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      DateTime? time,
      String title,
      String? subtitle,
      String state});
}

/// @nodoc
class __$$TimelineEventImplCopyWithImpl<$Res>
    extends _$TimelineEventCopyWithImpl<$Res, _$TimelineEventImpl>
    implements _$$TimelineEventImplCopyWith<$Res> {
  __$$TimelineEventImplCopyWithImpl(
      _$TimelineEventImpl _value, $Res Function(_$TimelineEventImpl) _then)
      : super(_value, _then);

  /// Create a copy of TimelineEvent
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? time = freezed,
    Object? title = null,
    Object? subtitle = freezed,
    Object? state = null,
  }) {
    return _then(_$TimelineEventImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      time: freezed == time
          ? _value.time
          : time // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      subtitle: freezed == subtitle
          ? _value.subtitle
          : subtitle // ignore: cast_nullable_to_non_nullable
              as String?,
      state: null == state
          ? _value.state
          : state // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$TimelineEventImpl implements _TimelineEvent {
  const _$TimelineEventImpl(
      {required this.id,
      this.time,
      required this.title,
      this.subtitle,
      this.state = 'done'});

  factory _$TimelineEventImpl.fromJson(Map<String, dynamic> json) =>
      _$$TimelineEventImplFromJson(json);

  @override
  final String id;
  @override
  final DateTime? time;
  @override
  final String title;
  @override
  final String? subtitle;
  @override
  @JsonKey()
  final String state;

  @override
  String toString() {
    return 'TimelineEvent(id: $id, time: $time, title: $title, subtitle: $subtitle, state: $state)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$TimelineEventImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.time, time) || other.time == time) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.subtitle, subtitle) ||
                other.subtitle == subtitle) &&
            (identical(other.state, state) || other.state == state));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode =>
      Object.hash(runtimeType, id, time, title, subtitle, state);

  /// Create a copy of TimelineEvent
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$TimelineEventImplCopyWith<_$TimelineEventImpl> get copyWith =>
      __$$TimelineEventImplCopyWithImpl<_$TimelineEventImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$TimelineEventImplToJson(
      this,
    );
  }
}

abstract class _TimelineEvent implements TimelineEvent {
  const factory _TimelineEvent(
      {required final String id,
      final DateTime? time,
      required final String title,
      final String? subtitle,
      final String state}) = _$TimelineEventImpl;

  factory _TimelineEvent.fromJson(Map<String, dynamic> json) =
      _$TimelineEventImpl.fromJson;

  @override
  String get id;
  @override
  DateTime? get time;
  @override
  String get title;
  @override
  String? get subtitle;
  @override
  String get state;

  /// Create a copy of TimelineEvent
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$TimelineEventImplCopyWith<_$TimelineEventImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

OrderDetail _$OrderDetailFromJson(Map<String, dynamic> json) {
  return _OrderDetail.fromJson(json);
}

/// @nodoc
mixin _$OrderDetail {
  String get id => throw _privateConstructorUsedError;
  String get orderNo => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String get author => throw _privateConstructorUsedError;
  String get publisher => throw _privateConstructorUsedError;
  String get callNo => throw _privateConstructorUsedError;
  String get barcode => throw _privateConstructorUsedError;
  String get coverLabel => throw _privateConstructorUsedError;
  String get coverTone => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  int get overdueDays => throw _privateConstructorUsedError;
  DateTime get borrowedAt => throw _privateConstructorUsedError;
  DateTime get dueAt => throw _privateConstructorUsedError;
  DateTime? get effectiveDueAt => throw _privateConstructorUsedError;
  int get graceDays => throw _privateConstructorUsedError;
  String get feePerDay => throw _privateConstructorUsedError;
  String? get feeEstimate => throw _privateConstructorUsedError;
  bool get gracePending => throw _privateConstructorUsedError;
  bool get canGrace => throw _privateConstructorUsedError;
  bool get blacklisted => throw _privateConstructorUsedError;
  DateTime? get returnedAt => throw _privateConstructorUsedError;
  String get settleNote => throw _privateConstructorUsedError;
  List<TimelineEvent> get timeline => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this OrderDetail to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of OrderDetail
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $OrderDetailCopyWith<OrderDetail> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $OrderDetailCopyWith<$Res> {
  factory $OrderDetailCopyWith(
          OrderDetail value, $Res Function(OrderDetail) then) =
      _$OrderDetailCopyWithImpl<$Res, OrderDetail>;
  @useResult
  $Res call(
      {String id,
      String orderNo,
      String title,
      String author,
      String publisher,
      String callNo,
      String barcode,
      String coverLabel,
      String coverTone,
      String status,
      int overdueDays,
      DateTime borrowedAt,
      DateTime dueAt,
      DateTime? effectiveDueAt,
      int graceDays,
      String feePerDay,
      String? feeEstimate,
      bool gracePending,
      bool canGrace,
      bool blacklisted,
      DateTime? returnedAt,
      String settleNote,
      List<TimelineEvent> timeline,
      bool isOffline});
}

/// @nodoc
class _$OrderDetailCopyWithImpl<$Res, $Val extends OrderDetail>
    implements $OrderDetailCopyWith<$Res> {
  _$OrderDetailCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of OrderDetail
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? orderNo = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? callNo = null,
    Object? barcode = null,
    Object? coverLabel = null,
    Object? coverTone = null,
    Object? status = null,
    Object? overdueDays = null,
    Object? borrowedAt = null,
    Object? dueAt = null,
    Object? effectiveDueAt = freezed,
    Object? graceDays = null,
    Object? feePerDay = null,
    Object? feeEstimate = freezed,
    Object? gracePending = null,
    Object? canGrace = null,
    Object? blacklisted = null,
    Object? returnedAt = freezed,
    Object? settleNote = null,
    Object? timeline = null,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      orderNo: null == orderNo
          ? _value.orderNo
          : orderNo // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      callNo: null == callNo
          ? _value.callNo
          : callNo // ignore: cast_nullable_to_non_nullable
              as String,
      barcode: null == barcode
          ? _value.barcode
          : barcode // ignore: cast_nullable_to_non_nullable
              as String,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      overdueDays: null == overdueDays
          ? _value.overdueDays
          : overdueDays // ignore: cast_nullable_to_non_nullable
              as int,
      borrowedAt: null == borrowedAt
          ? _value.borrowedAt
          : borrowedAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      dueAt: null == dueAt
          ? _value.dueAt
          : dueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      effectiveDueAt: freezed == effectiveDueAt
          ? _value.effectiveDueAt
          : effectiveDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      graceDays: null == graceDays
          ? _value.graceDays
          : graceDays // ignore: cast_nullable_to_non_nullable
              as int,
      feePerDay: null == feePerDay
          ? _value.feePerDay
          : feePerDay // ignore: cast_nullable_to_non_nullable
              as String,
      feeEstimate: freezed == feeEstimate
          ? _value.feeEstimate
          : feeEstimate // ignore: cast_nullable_to_non_nullable
              as String?,
      gracePending: null == gracePending
          ? _value.gracePending
          : gracePending // ignore: cast_nullable_to_non_nullable
              as bool,
      canGrace: null == canGrace
          ? _value.canGrace
          : canGrace // ignore: cast_nullable_to_non_nullable
              as bool,
      blacklisted: null == blacklisted
          ? _value.blacklisted
          : blacklisted // ignore: cast_nullable_to_non_nullable
              as bool,
      returnedAt: freezed == returnedAt
          ? _value.returnedAt
          : returnedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      settleNote: null == settleNote
          ? _value.settleNote
          : settleNote // ignore: cast_nullable_to_non_nullable
              as String,
      timeline: null == timeline
          ? _value.timeline
          : timeline // ignore: cast_nullable_to_non_nullable
              as List<TimelineEvent>,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$OrderDetailImplCopyWith<$Res>
    implements $OrderDetailCopyWith<$Res> {
  factory _$$OrderDetailImplCopyWith(
          _$OrderDetailImpl value, $Res Function(_$OrderDetailImpl) then) =
      __$$OrderDetailImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String orderNo,
      String title,
      String author,
      String publisher,
      String callNo,
      String barcode,
      String coverLabel,
      String coverTone,
      String status,
      int overdueDays,
      DateTime borrowedAt,
      DateTime dueAt,
      DateTime? effectiveDueAt,
      int graceDays,
      String feePerDay,
      String? feeEstimate,
      bool gracePending,
      bool canGrace,
      bool blacklisted,
      DateTime? returnedAt,
      String settleNote,
      List<TimelineEvent> timeline,
      bool isOffline});
}

/// @nodoc
class __$$OrderDetailImplCopyWithImpl<$Res>
    extends _$OrderDetailCopyWithImpl<$Res, _$OrderDetailImpl>
    implements _$$OrderDetailImplCopyWith<$Res> {
  __$$OrderDetailImplCopyWithImpl(
      _$OrderDetailImpl _value, $Res Function(_$OrderDetailImpl) _then)
      : super(_value, _then);

  /// Create a copy of OrderDetail
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? orderNo = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? callNo = null,
    Object? barcode = null,
    Object? coverLabel = null,
    Object? coverTone = null,
    Object? status = null,
    Object? overdueDays = null,
    Object? borrowedAt = null,
    Object? dueAt = null,
    Object? effectiveDueAt = freezed,
    Object? graceDays = null,
    Object? feePerDay = null,
    Object? feeEstimate = freezed,
    Object? gracePending = null,
    Object? canGrace = null,
    Object? blacklisted = null,
    Object? returnedAt = freezed,
    Object? settleNote = null,
    Object? timeline = null,
    Object? isOffline = null,
  }) {
    return _then(_$OrderDetailImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      orderNo: null == orderNo
          ? _value.orderNo
          : orderNo // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      callNo: null == callNo
          ? _value.callNo
          : callNo // ignore: cast_nullable_to_non_nullable
              as String,
      barcode: null == barcode
          ? _value.barcode
          : barcode // ignore: cast_nullable_to_non_nullable
              as String,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      overdueDays: null == overdueDays
          ? _value.overdueDays
          : overdueDays // ignore: cast_nullable_to_non_nullable
              as int,
      borrowedAt: null == borrowedAt
          ? _value.borrowedAt
          : borrowedAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      dueAt: null == dueAt
          ? _value.dueAt
          : dueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      effectiveDueAt: freezed == effectiveDueAt
          ? _value.effectiveDueAt
          : effectiveDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      graceDays: null == graceDays
          ? _value.graceDays
          : graceDays // ignore: cast_nullable_to_non_nullable
              as int,
      feePerDay: null == feePerDay
          ? _value.feePerDay
          : feePerDay // ignore: cast_nullable_to_non_nullable
              as String,
      feeEstimate: freezed == feeEstimate
          ? _value.feeEstimate
          : feeEstimate // ignore: cast_nullable_to_non_nullable
              as String?,
      gracePending: null == gracePending
          ? _value.gracePending
          : gracePending // ignore: cast_nullable_to_non_nullable
              as bool,
      canGrace: null == canGrace
          ? _value.canGrace
          : canGrace // ignore: cast_nullable_to_non_nullable
              as bool,
      blacklisted: null == blacklisted
          ? _value.blacklisted
          : blacklisted // ignore: cast_nullable_to_non_nullable
              as bool,
      returnedAt: freezed == returnedAt
          ? _value.returnedAt
          : returnedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      settleNote: null == settleNote
          ? _value.settleNote
          : settleNote // ignore: cast_nullable_to_non_nullable
              as String,
      timeline: null == timeline
          ? _value._timeline
          : timeline // ignore: cast_nullable_to_non_nullable
              as List<TimelineEvent>,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$OrderDetailImpl implements _OrderDetail {
  const _$OrderDetailImpl(
      {required this.id,
      required this.orderNo,
      required this.title,
      required this.author,
      required this.publisher,
      required this.callNo,
      required this.barcode,
      required this.coverLabel,
      this.coverTone = 'mint',
      this.status = 'BORROWED',
      this.overdueDays = 0,
      required this.borrowedAt,
      required this.dueAt,
      this.effectiveDueAt,
      this.graceDays = 0,
      this.feePerDay = '¥1.00 / 天',
      this.feeEstimate,
      this.gracePending = false,
      this.canGrace = false,
      this.blacklisted = false,
      this.returnedAt,
      this.settleNote = '待归还 · 归还后自动核算，减免费用需馆员操作并留痕',
      final List<TimelineEvent> timeline = const <TimelineEvent>[],
      this.isOffline = false})
      : _timeline = timeline;

  factory _$OrderDetailImpl.fromJson(Map<String, dynamic> json) =>
      _$$OrderDetailImplFromJson(json);

  @override
  final String id;
  @override
  final String orderNo;
  @override
  final String title;
  @override
  final String author;
  @override
  final String publisher;
  @override
  final String callNo;
  @override
  final String barcode;
  @override
  final String coverLabel;
  @override
  @JsonKey()
  final String coverTone;
  @override
  @JsonKey()
  final String status;
  @override
  @JsonKey()
  final int overdueDays;
  @override
  final DateTime borrowedAt;
  @override
  final DateTime dueAt;
  @override
  final DateTime? effectiveDueAt;
  @override
  @JsonKey()
  final int graceDays;
  @override
  @JsonKey()
  final String feePerDay;
  @override
  final String? feeEstimate;
  @override
  @JsonKey()
  final bool gracePending;
  @override
  @JsonKey()
  final bool canGrace;
  @override
  @JsonKey()
  final bool blacklisted;
  @override
  final DateTime? returnedAt;
  @override
  @JsonKey()
  final String settleNote;
  final List<TimelineEvent> _timeline;
  @override
  @JsonKey()
  List<TimelineEvent> get timeline {
    if (_timeline is EqualUnmodifiableListView) return _timeline;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_timeline);
  }

  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'OrderDetail(id: $id, orderNo: $orderNo, title: $title, author: $author, publisher: $publisher, callNo: $callNo, barcode: $barcode, coverLabel: $coverLabel, coverTone: $coverTone, status: $status, overdueDays: $overdueDays, borrowedAt: $borrowedAt, dueAt: $dueAt, effectiveDueAt: $effectiveDueAt, graceDays: $graceDays, feePerDay: $feePerDay, feeEstimate: $feeEstimate, gracePending: $gracePending, canGrace: $canGrace, blacklisted: $blacklisted, returnedAt: $returnedAt, settleNote: $settleNote, timeline: $timeline, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$OrderDetailImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.orderNo, orderNo) || other.orderNo == orderNo) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.author, author) || other.author == author) &&
            (identical(other.publisher, publisher) ||
                other.publisher == publisher) &&
            (identical(other.callNo, callNo) || other.callNo == callNo) &&
            (identical(other.barcode, barcode) || other.barcode == barcode) &&
            (identical(other.coverLabel, coverLabel) ||
                other.coverLabel == coverLabel) &&
            (identical(other.coverTone, coverTone) ||
                other.coverTone == coverTone) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.overdueDays, overdueDays) ||
                other.overdueDays == overdueDays) &&
            (identical(other.borrowedAt, borrowedAt) ||
                other.borrowedAt == borrowedAt) &&
            (identical(other.dueAt, dueAt) || other.dueAt == dueAt) &&
            (identical(other.effectiveDueAt, effectiveDueAt) ||
                other.effectiveDueAt == effectiveDueAt) &&
            (identical(other.graceDays, graceDays) ||
                other.graceDays == graceDays) &&
            (identical(other.feePerDay, feePerDay) ||
                other.feePerDay == feePerDay) &&
            (identical(other.feeEstimate, feeEstimate) ||
                other.feeEstimate == feeEstimate) &&
            (identical(other.gracePending, gracePending) ||
                other.gracePending == gracePending) &&
            (identical(other.canGrace, canGrace) ||
                other.canGrace == canGrace) &&
            (identical(other.blacklisted, blacklisted) ||
                other.blacklisted == blacklisted) &&
            (identical(other.returnedAt, returnedAt) ||
                other.returnedAt == returnedAt) &&
            (identical(other.settleNote, settleNote) ||
                other.settleNote == settleNote) &&
            const DeepCollectionEquality().equals(other._timeline, _timeline) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hashAll([
        runtimeType,
        id,
        orderNo,
        title,
        author,
        publisher,
        callNo,
        barcode,
        coverLabel,
        coverTone,
        status,
        overdueDays,
        borrowedAt,
        dueAt,
        effectiveDueAt,
        graceDays,
        feePerDay,
        feeEstimate,
        gracePending,
        canGrace,
        blacklisted,
        returnedAt,
        settleNote,
        const DeepCollectionEquality().hash(_timeline),
        isOffline
      ]);

  /// Create a copy of OrderDetail
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$OrderDetailImplCopyWith<_$OrderDetailImpl> get copyWith =>
      __$$OrderDetailImplCopyWithImpl<_$OrderDetailImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$OrderDetailImplToJson(
      this,
    );
  }
}

abstract class _OrderDetail implements OrderDetail {
  const factory _OrderDetail(
      {required final String id,
      required final String orderNo,
      required final String title,
      required final String author,
      required final String publisher,
      required final String callNo,
      required final String barcode,
      required final String coverLabel,
      final String coverTone,
      final String status,
      final int overdueDays,
      required final DateTime borrowedAt,
      required final DateTime dueAt,
      final DateTime? effectiveDueAt,
      final int graceDays,
      final String feePerDay,
      final String? feeEstimate,
      final bool gracePending,
      final bool canGrace,
      final bool blacklisted,
      final DateTime? returnedAt,
      final String settleNote,
      final List<TimelineEvent> timeline,
      final bool isOffline}) = _$OrderDetailImpl;

  factory _OrderDetail.fromJson(Map<String, dynamic> json) =
      _$OrderDetailImpl.fromJson;

  @override
  String get id;
  @override
  String get orderNo;
  @override
  String get title;
  @override
  String get author;
  @override
  String get publisher;
  @override
  String get callNo;
  @override
  String get barcode;
  @override
  String get coverLabel;
  @override
  String get coverTone;
  @override
  String get status;
  @override
  int get overdueDays;
  @override
  DateTime get borrowedAt;
  @override
  DateTime get dueAt;
  @override
  DateTime? get effectiveDueAt;
  @override
  int get graceDays;
  @override
  String get feePerDay;
  @override
  String? get feeEstimate;
  @override
  bool get gracePending;
  @override
  bool get canGrace;
  @override
  bool get blacklisted;
  @override
  DateTime? get returnedAt;
  @override
  String get settleNote;
  @override
  List<TimelineEvent> get timeline;
  @override
  bool get isOffline;

  /// Create a copy of OrderDetail
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$OrderDetailImplCopyWith<_$OrderDetailImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

GraceScene _$GraceSceneFromJson(Map<String, dynamic> json) {
  return _GraceScene.fromJson(json);
}

/// @nodoc
mixin _$GraceScene {
  String get code => throw _privateConstructorUsedError;
  String get label => throw _privateConstructorUsedError;
  String get description => throw _privateConstructorUsedError;
  int get baseDays => throw _privateConstructorUsedError;

  /// Serializes this GraceScene to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GraceScene
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GraceSceneCopyWith<GraceScene> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GraceSceneCopyWith<$Res> {
  factory $GraceSceneCopyWith(
          GraceScene value, $Res Function(GraceScene) then) =
      _$GraceSceneCopyWithImpl<$Res, GraceScene>;
  @useResult
  $Res call({String code, String label, String description, int baseDays});
}

/// @nodoc
class _$GraceSceneCopyWithImpl<$Res, $Val extends GraceScene>
    implements $GraceSceneCopyWith<$Res> {
  _$GraceSceneCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GraceScene
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? label = null,
    Object? description = null,
    Object? baseDays = null,
  }) {
    return _then(_value.copyWith(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      label: null == label
          ? _value.label
          : label // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      baseDays: null == baseDays
          ? _value.baseDays
          : baseDays // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$GraceSceneImplCopyWith<$Res>
    implements $GraceSceneCopyWith<$Res> {
  factory _$$GraceSceneImplCopyWith(
          _$GraceSceneImpl value, $Res Function(_$GraceSceneImpl) then) =
      __$$GraceSceneImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String code, String label, String description, int baseDays});
}

/// @nodoc
class __$$GraceSceneImplCopyWithImpl<$Res>
    extends _$GraceSceneCopyWithImpl<$Res, _$GraceSceneImpl>
    implements _$$GraceSceneImplCopyWith<$Res> {
  __$$GraceSceneImplCopyWithImpl(
      _$GraceSceneImpl _value, $Res Function(_$GraceSceneImpl) _then)
      : super(_value, _then);

  /// Create a copy of GraceScene
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? label = null,
    Object? description = null,
    Object? baseDays = null,
  }) {
    return _then(_$GraceSceneImpl(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      label: null == label
          ? _value.label
          : label // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      baseDays: null == baseDays
          ? _value.baseDays
          : baseDays // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GraceSceneImpl implements _GraceScene {
  const _$GraceSceneImpl(
      {required this.code,
      required this.label,
      this.description = '',
      this.baseDays = 0});

  factory _$GraceSceneImpl.fromJson(Map<String, dynamic> json) =>
      _$$GraceSceneImplFromJson(json);

  @override
  final String code;
  @override
  final String label;
  @override
  @JsonKey()
  final String description;
  @override
  @JsonKey()
  final int baseDays;

  @override
  String toString() {
    return 'GraceScene(code: $code, label: $label, description: $description, baseDays: $baseDays)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GraceSceneImpl &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.label, label) || other.label == label) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.baseDays, baseDays) ||
                other.baseDays == baseDays));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode =>
      Object.hash(runtimeType, code, label, description, baseDays);

  /// Create a copy of GraceScene
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GraceSceneImplCopyWith<_$GraceSceneImpl> get copyWith =>
      __$$GraceSceneImplCopyWithImpl<_$GraceSceneImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GraceSceneImplToJson(
      this,
    );
  }
}

abstract class _GraceScene implements GraceScene {
  const factory _GraceScene(
      {required final String code,
      required final String label,
      final String description,
      final int baseDays}) = _$GraceSceneImpl;

  factory _GraceScene.fromJson(Map<String, dynamic> json) =
      _$GraceSceneImpl.fromJson;

  @override
  String get code;
  @override
  String get label;
  @override
  String get description;
  @override
  int get baseDays;

  /// Create a copy of GraceScene
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GraceSceneImplCopyWith<_$GraceSceneImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

GraceEstimate _$GraceEstimateFromJson(Map<String, dynamic> json) {
  return _GraceEstimate.fromJson(json);
}

/// @nodoc
mixin _$GraceEstimate {
  DateTime get originalDueAt => throw _privateConstructorUsedError;
  DateTime get estimatedDueAt => throw _privateConstructorUsedError;
  int get days => throw _privateConstructorUsedError;

  /// Serializes this GraceEstimate to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GraceEstimate
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GraceEstimateCopyWith<GraceEstimate> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GraceEstimateCopyWith<$Res> {
  factory $GraceEstimateCopyWith(
          GraceEstimate value, $Res Function(GraceEstimate) then) =
      _$GraceEstimateCopyWithImpl<$Res, GraceEstimate>;
  @useResult
  $Res call({DateTime originalDueAt, DateTime estimatedDueAt, int days});
}

/// @nodoc
class _$GraceEstimateCopyWithImpl<$Res, $Val extends GraceEstimate>
    implements $GraceEstimateCopyWith<$Res> {
  _$GraceEstimateCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GraceEstimate
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? originalDueAt = null,
    Object? estimatedDueAt = null,
    Object? days = null,
  }) {
    return _then(_value.copyWith(
      originalDueAt: null == originalDueAt
          ? _value.originalDueAt
          : originalDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      estimatedDueAt: null == estimatedDueAt
          ? _value.estimatedDueAt
          : estimatedDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      days: null == days
          ? _value.days
          : days // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$GraceEstimateImplCopyWith<$Res>
    implements $GraceEstimateCopyWith<$Res> {
  factory _$$GraceEstimateImplCopyWith(
          _$GraceEstimateImpl value, $Res Function(_$GraceEstimateImpl) then) =
      __$$GraceEstimateImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({DateTime originalDueAt, DateTime estimatedDueAt, int days});
}

/// @nodoc
class __$$GraceEstimateImplCopyWithImpl<$Res>
    extends _$GraceEstimateCopyWithImpl<$Res, _$GraceEstimateImpl>
    implements _$$GraceEstimateImplCopyWith<$Res> {
  __$$GraceEstimateImplCopyWithImpl(
      _$GraceEstimateImpl _value, $Res Function(_$GraceEstimateImpl) _then)
      : super(_value, _then);

  /// Create a copy of GraceEstimate
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? originalDueAt = null,
    Object? estimatedDueAt = null,
    Object? days = null,
  }) {
    return _then(_$GraceEstimateImpl(
      originalDueAt: null == originalDueAt
          ? _value.originalDueAt
          : originalDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      estimatedDueAt: null == estimatedDueAt
          ? _value.estimatedDueAt
          : estimatedDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      days: null == days
          ? _value.days
          : days // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GraceEstimateImpl implements _GraceEstimate {
  const _$GraceEstimateImpl(
      {required this.originalDueAt,
      required this.estimatedDueAt,
      this.days = 0});

  factory _$GraceEstimateImpl.fromJson(Map<String, dynamic> json) =>
      _$$GraceEstimateImplFromJson(json);

  @override
  final DateTime originalDueAt;
  @override
  final DateTime estimatedDueAt;
  @override
  @JsonKey()
  final int days;

  @override
  String toString() {
    return 'GraceEstimate(originalDueAt: $originalDueAt, estimatedDueAt: $estimatedDueAt, days: $days)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GraceEstimateImpl &&
            (identical(other.originalDueAt, originalDueAt) ||
                other.originalDueAt == originalDueAt) &&
            (identical(other.estimatedDueAt, estimatedDueAt) ||
                other.estimatedDueAt == estimatedDueAt) &&
            (identical(other.days, days) || other.days == days));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode =>
      Object.hash(runtimeType, originalDueAt, estimatedDueAt, days);

  /// Create a copy of GraceEstimate
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GraceEstimateImplCopyWith<_$GraceEstimateImpl> get copyWith =>
      __$$GraceEstimateImplCopyWithImpl<_$GraceEstimateImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GraceEstimateImplToJson(
      this,
    );
  }
}

abstract class _GraceEstimate implements GraceEstimate {
  const factory _GraceEstimate(
      {required final DateTime originalDueAt,
      required final DateTime estimatedDueAt,
      final int days}) = _$GraceEstimateImpl;

  factory _GraceEstimate.fromJson(Map<String, dynamic> json) =
      _$GraceEstimateImpl.fromJson;

  @override
  DateTime get originalDueAt;
  @override
  DateTime get estimatedDueAt;
  @override
  int get days;

  /// Create a copy of GraceEstimate
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GraceEstimateImplCopyWith<_$GraceEstimateImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

GraceApplication _$GraceApplicationFromJson(Map<String, dynamic> json) {
  return _GraceApplication.fromJson(json);
}

/// @nodoc
mixin _$GraceApplication {
  String get id => throw _privateConstructorUsedError;
  String get applyNo => throw _privateConstructorUsedError;
  String get sceneCode => throw _privateConstructorUsedError;
  String get sceneLabel => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  DateTime get submittedAt => throw _privateConstructorUsedError;
  DateTime? get decidedAt => throw _privateConstructorUsedError;
  int? get grantedDays => throw _privateConstructorUsedError;
  String? get rejectCode => throw _privateConstructorUsedError;
  String? get evidenceText => throw _privateConstructorUsedError;
  List<String> get attachments => throw _privateConstructorUsedError;

  /// Serializes this GraceApplication to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GraceApplication
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GraceApplicationCopyWith<GraceApplication> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GraceApplicationCopyWith<$Res> {
  factory $GraceApplicationCopyWith(
          GraceApplication value, $Res Function(GraceApplication) then) =
      _$GraceApplicationCopyWithImpl<$Res, GraceApplication>;
  @useResult
  $Res call(
      {String id,
      String applyNo,
      String sceneCode,
      String sceneLabel,
      String status,
      DateTime submittedAt,
      DateTime? decidedAt,
      int? grantedDays,
      String? rejectCode,
      String? evidenceText,
      List<String> attachments});
}

/// @nodoc
class _$GraceApplicationCopyWithImpl<$Res, $Val extends GraceApplication>
    implements $GraceApplicationCopyWith<$Res> {
  _$GraceApplicationCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GraceApplication
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? applyNo = null,
    Object? sceneCode = null,
    Object? sceneLabel = null,
    Object? status = null,
    Object? submittedAt = null,
    Object? decidedAt = freezed,
    Object? grantedDays = freezed,
    Object? rejectCode = freezed,
    Object? evidenceText = freezed,
    Object? attachments = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      applyNo: null == applyNo
          ? _value.applyNo
          : applyNo // ignore: cast_nullable_to_non_nullable
              as String,
      sceneCode: null == sceneCode
          ? _value.sceneCode
          : sceneCode // ignore: cast_nullable_to_non_nullable
              as String,
      sceneLabel: null == sceneLabel
          ? _value.sceneLabel
          : sceneLabel // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      submittedAt: null == submittedAt
          ? _value.submittedAt
          : submittedAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      decidedAt: freezed == decidedAt
          ? _value.decidedAt
          : decidedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      grantedDays: freezed == grantedDays
          ? _value.grantedDays
          : grantedDays // ignore: cast_nullable_to_non_nullable
              as int?,
      rejectCode: freezed == rejectCode
          ? _value.rejectCode
          : rejectCode // ignore: cast_nullable_to_non_nullable
              as String?,
      evidenceText: freezed == evidenceText
          ? _value.evidenceText
          : evidenceText // ignore: cast_nullable_to_non_nullable
              as String?,
      attachments: null == attachments
          ? _value.attachments
          : attachments // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$GraceApplicationImplCopyWith<$Res>
    implements $GraceApplicationCopyWith<$Res> {
  factory _$$GraceApplicationImplCopyWith(_$GraceApplicationImpl value,
          $Res Function(_$GraceApplicationImpl) then) =
      __$$GraceApplicationImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String applyNo,
      String sceneCode,
      String sceneLabel,
      String status,
      DateTime submittedAt,
      DateTime? decidedAt,
      int? grantedDays,
      String? rejectCode,
      String? evidenceText,
      List<String> attachments});
}

/// @nodoc
class __$$GraceApplicationImplCopyWithImpl<$Res>
    extends _$GraceApplicationCopyWithImpl<$Res, _$GraceApplicationImpl>
    implements _$$GraceApplicationImplCopyWith<$Res> {
  __$$GraceApplicationImplCopyWithImpl(_$GraceApplicationImpl _value,
      $Res Function(_$GraceApplicationImpl) _then)
      : super(_value, _then);

  /// Create a copy of GraceApplication
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? applyNo = null,
    Object? sceneCode = null,
    Object? sceneLabel = null,
    Object? status = null,
    Object? submittedAt = null,
    Object? decidedAt = freezed,
    Object? grantedDays = freezed,
    Object? rejectCode = freezed,
    Object? evidenceText = freezed,
    Object? attachments = null,
  }) {
    return _then(_$GraceApplicationImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      applyNo: null == applyNo
          ? _value.applyNo
          : applyNo // ignore: cast_nullable_to_non_nullable
              as String,
      sceneCode: null == sceneCode
          ? _value.sceneCode
          : sceneCode // ignore: cast_nullable_to_non_nullable
              as String,
      sceneLabel: null == sceneLabel
          ? _value.sceneLabel
          : sceneLabel // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      submittedAt: null == submittedAt
          ? _value.submittedAt
          : submittedAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      decidedAt: freezed == decidedAt
          ? _value.decidedAt
          : decidedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      grantedDays: freezed == grantedDays
          ? _value.grantedDays
          : grantedDays // ignore: cast_nullable_to_non_nullable
              as int?,
      rejectCode: freezed == rejectCode
          ? _value.rejectCode
          : rejectCode // ignore: cast_nullable_to_non_nullable
              as String?,
      evidenceText: freezed == evidenceText
          ? _value.evidenceText
          : evidenceText // ignore: cast_nullable_to_non_nullable
              as String?,
      attachments: null == attachments
          ? _value._attachments
          : attachments // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GraceApplicationImpl implements _GraceApplication {
  const _$GraceApplicationImpl(
      {required this.id,
      required this.applyNo,
      this.sceneCode = '',
      this.sceneLabel = '',
      this.status = 'PENDING',
      required this.submittedAt,
      this.decidedAt,
      this.grantedDays,
      this.rejectCode,
      this.evidenceText,
      final List<String> attachments = const <String>[]})
      : _attachments = attachments;

  factory _$GraceApplicationImpl.fromJson(Map<String, dynamic> json) =>
      _$$GraceApplicationImplFromJson(json);

  @override
  final String id;
  @override
  final String applyNo;
  @override
  @JsonKey()
  final String sceneCode;
  @override
  @JsonKey()
  final String sceneLabel;
  @override
  @JsonKey()
  final String status;
  @override
  final DateTime submittedAt;
  @override
  final DateTime? decidedAt;
  @override
  final int? grantedDays;
  @override
  final String? rejectCode;
  @override
  final String? evidenceText;
  final List<String> _attachments;
  @override
  @JsonKey()
  List<String> get attachments {
    if (_attachments is EqualUnmodifiableListView) return _attachments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_attachments);
  }

  @override
  String toString() {
    return 'GraceApplication(id: $id, applyNo: $applyNo, sceneCode: $sceneCode, sceneLabel: $sceneLabel, status: $status, submittedAt: $submittedAt, decidedAt: $decidedAt, grantedDays: $grantedDays, rejectCode: $rejectCode, evidenceText: $evidenceText, attachments: $attachments)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GraceApplicationImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.applyNo, applyNo) || other.applyNo == applyNo) &&
            (identical(other.sceneCode, sceneCode) ||
                other.sceneCode == sceneCode) &&
            (identical(other.sceneLabel, sceneLabel) ||
                other.sceneLabel == sceneLabel) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.submittedAt, submittedAt) ||
                other.submittedAt == submittedAt) &&
            (identical(other.decidedAt, decidedAt) ||
                other.decidedAt == decidedAt) &&
            (identical(other.grantedDays, grantedDays) ||
                other.grantedDays == grantedDays) &&
            (identical(other.rejectCode, rejectCode) ||
                other.rejectCode == rejectCode) &&
            (identical(other.evidenceText, evidenceText) ||
                other.evidenceText == evidenceText) &&
            const DeepCollectionEquality()
                .equals(other._attachments, _attachments));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      applyNo,
      sceneCode,
      sceneLabel,
      status,
      submittedAt,
      decidedAt,
      grantedDays,
      rejectCode,
      evidenceText,
      const DeepCollectionEquality().hash(_attachments));

  /// Create a copy of GraceApplication
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GraceApplicationImplCopyWith<_$GraceApplicationImpl> get copyWith =>
      __$$GraceApplicationImplCopyWithImpl<_$GraceApplicationImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GraceApplicationImplToJson(
      this,
    );
  }
}

abstract class _GraceApplication implements GraceApplication {
  const factory _GraceApplication(
      {required final String id,
      required final String applyNo,
      final String sceneCode,
      final String sceneLabel,
      final String status,
      required final DateTime submittedAt,
      final DateTime? decidedAt,
      final int? grantedDays,
      final String? rejectCode,
      final String? evidenceText,
      final List<String> attachments}) = _$GraceApplicationImpl;

  factory _GraceApplication.fromJson(Map<String, dynamic> json) =
      _$GraceApplicationImpl.fromJson;

  @override
  String get id;
  @override
  String get applyNo;
  @override
  String get sceneCode;
  @override
  String get sceneLabel;
  @override
  String get status;
  @override
  DateTime get submittedAt;
  @override
  DateTime? get decidedAt;
  @override
  int? get grantedDays;
  @override
  String? get rejectCode;
  @override
  String? get evidenceText;
  @override
  List<String> get attachments;

  /// Create a copy of GraceApplication
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GraceApplicationImplCopyWith<_$GraceApplicationImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

GracePageData _$GracePageDataFromJson(Map<String, dynamic> json) {
  return _GracePageData.fromJson(json);
}

/// @nodoc
mixin _$GracePageData {
  String get orderId => throw _privateConstructorUsedError;
  String get bookTitle => throw _privateConstructorUsedError;
  List<GraceScene> get scenes => throw _privateConstructorUsedError;
  GraceEstimate? get estimate => throw _privateConstructorUsedError;
  List<GraceApplication> get history => throw _privateConstructorUsedError;
  bool get hasPending => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this GracePageData to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GracePageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GracePageDataCopyWith<GracePageData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GracePageDataCopyWith<$Res> {
  factory $GracePageDataCopyWith(
          GracePageData value, $Res Function(GracePageData) then) =
      _$GracePageDataCopyWithImpl<$Res, GracePageData>;
  @useResult
  $Res call(
      {String orderId,
      String bookTitle,
      List<GraceScene> scenes,
      GraceEstimate? estimate,
      List<GraceApplication> history,
      bool hasPending,
      bool isOffline});

  $GraceEstimateCopyWith<$Res>? get estimate;
}

/// @nodoc
class _$GracePageDataCopyWithImpl<$Res, $Val extends GracePageData>
    implements $GracePageDataCopyWith<$Res> {
  _$GracePageDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GracePageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? orderId = null,
    Object? bookTitle = null,
    Object? scenes = null,
    Object? estimate = freezed,
    Object? history = null,
    Object? hasPending = null,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      orderId: null == orderId
          ? _value.orderId
          : orderId // ignore: cast_nullable_to_non_nullable
              as String,
      bookTitle: null == bookTitle
          ? _value.bookTitle
          : bookTitle // ignore: cast_nullable_to_non_nullable
              as String,
      scenes: null == scenes
          ? _value.scenes
          : scenes // ignore: cast_nullable_to_non_nullable
              as List<GraceScene>,
      estimate: freezed == estimate
          ? _value.estimate
          : estimate // ignore: cast_nullable_to_non_nullable
              as GraceEstimate?,
      history: null == history
          ? _value.history
          : history // ignore: cast_nullable_to_non_nullable
              as List<GraceApplication>,
      hasPending: null == hasPending
          ? _value.hasPending
          : hasPending // ignore: cast_nullable_to_non_nullable
              as bool,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }

  /// Create a copy of GracePageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $GraceEstimateCopyWith<$Res>? get estimate {
    if (_value.estimate == null) {
      return null;
    }

    return $GraceEstimateCopyWith<$Res>(_value.estimate!, (value) {
      return _then(_value.copyWith(estimate: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$GracePageDataImplCopyWith<$Res>
    implements $GracePageDataCopyWith<$Res> {
  factory _$$GracePageDataImplCopyWith(
          _$GracePageDataImpl value, $Res Function(_$GracePageDataImpl) then) =
      __$$GracePageDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String orderId,
      String bookTitle,
      List<GraceScene> scenes,
      GraceEstimate? estimate,
      List<GraceApplication> history,
      bool hasPending,
      bool isOffline});

  @override
  $GraceEstimateCopyWith<$Res>? get estimate;
}

/// @nodoc
class __$$GracePageDataImplCopyWithImpl<$Res>
    extends _$GracePageDataCopyWithImpl<$Res, _$GracePageDataImpl>
    implements _$$GracePageDataImplCopyWith<$Res> {
  __$$GracePageDataImplCopyWithImpl(
      _$GracePageDataImpl _value, $Res Function(_$GracePageDataImpl) _then)
      : super(_value, _then);

  /// Create a copy of GracePageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? orderId = null,
    Object? bookTitle = null,
    Object? scenes = null,
    Object? estimate = freezed,
    Object? history = null,
    Object? hasPending = null,
    Object? isOffline = null,
  }) {
    return _then(_$GracePageDataImpl(
      orderId: null == orderId
          ? _value.orderId
          : orderId // ignore: cast_nullable_to_non_nullable
              as String,
      bookTitle: null == bookTitle
          ? _value.bookTitle
          : bookTitle // ignore: cast_nullable_to_non_nullable
              as String,
      scenes: null == scenes
          ? _value._scenes
          : scenes // ignore: cast_nullable_to_non_nullable
              as List<GraceScene>,
      estimate: freezed == estimate
          ? _value.estimate
          : estimate // ignore: cast_nullable_to_non_nullable
              as GraceEstimate?,
      history: null == history
          ? _value._history
          : history // ignore: cast_nullable_to_non_nullable
              as List<GraceApplication>,
      hasPending: null == hasPending
          ? _value.hasPending
          : hasPending // ignore: cast_nullable_to_non_nullable
              as bool,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GracePageDataImpl implements _GracePageData {
  const _$GracePageDataImpl(
      {required this.orderId,
      required this.bookTitle,
      final List<GraceScene> scenes = const <GraceScene>[],
      this.estimate,
      final List<GraceApplication> history = const <GraceApplication>[],
      this.hasPending = false,
      this.isOffline = false})
      : _scenes = scenes,
        _history = history;

  factory _$GracePageDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$GracePageDataImplFromJson(json);

  @override
  final String orderId;
  @override
  final String bookTitle;
  final List<GraceScene> _scenes;
  @override
  @JsonKey()
  List<GraceScene> get scenes {
    if (_scenes is EqualUnmodifiableListView) return _scenes;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_scenes);
  }

  @override
  final GraceEstimate? estimate;
  final List<GraceApplication> _history;
  @override
  @JsonKey()
  List<GraceApplication> get history {
    if (_history is EqualUnmodifiableListView) return _history;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_history);
  }

  @override
  @JsonKey()
  final bool hasPending;
  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'GracePageData(orderId: $orderId, bookTitle: $bookTitle, scenes: $scenes, estimate: $estimate, history: $history, hasPending: $hasPending, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GracePageDataImpl &&
            (identical(other.orderId, orderId) || other.orderId == orderId) &&
            (identical(other.bookTitle, bookTitle) ||
                other.bookTitle == bookTitle) &&
            const DeepCollectionEquality().equals(other._scenes, _scenes) &&
            (identical(other.estimate, estimate) ||
                other.estimate == estimate) &&
            const DeepCollectionEquality().equals(other._history, _history) &&
            (identical(other.hasPending, hasPending) ||
                other.hasPending == hasPending) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      orderId,
      bookTitle,
      const DeepCollectionEquality().hash(_scenes),
      estimate,
      const DeepCollectionEquality().hash(_history),
      hasPending,
      isOffline);

  /// Create a copy of GracePageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GracePageDataImplCopyWith<_$GracePageDataImpl> get copyWith =>
      __$$GracePageDataImplCopyWithImpl<_$GracePageDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GracePageDataImplToJson(
      this,
    );
  }
}

abstract class _GracePageData implements GracePageData {
  const factory _GracePageData(
      {required final String orderId,
      required final String bookTitle,
      final List<GraceScene> scenes,
      final GraceEstimate? estimate,
      final List<GraceApplication> history,
      final bool hasPending,
      final bool isOffline}) = _$GracePageDataImpl;

  factory _GracePageData.fromJson(Map<String, dynamic> json) =
      _$GracePageDataImpl.fromJson;

  @override
  String get orderId;
  @override
  String get bookTitle;
  @override
  List<GraceScene> get scenes;
  @override
  GraceEstimate? get estimate;
  @override
  List<GraceApplication> get history;
  @override
  bool get hasPending;
  @override
  bool get isOffline;

  /// Create a copy of GracePageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GracePageDataImplCopyWith<_$GracePageDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

GraceSubmitRequest _$GraceSubmitRequestFromJson(Map<String, dynamic> json) {
  return _GraceSubmitRequest.fromJson(json);
}

/// @nodoc
mixin _$GraceSubmitRequest {
  String get orderId => throw _privateConstructorUsedError;
  String get sceneCode => throw _privateConstructorUsedError;
  String get description => throw _privateConstructorUsedError;
  List<String> get attachments => throw _privateConstructorUsedError;

  /// Serializes this GraceSubmitRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GraceSubmitRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GraceSubmitRequestCopyWith<GraceSubmitRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GraceSubmitRequestCopyWith<$Res> {
  factory $GraceSubmitRequestCopyWith(
          GraceSubmitRequest value, $Res Function(GraceSubmitRequest) then) =
      _$GraceSubmitRequestCopyWithImpl<$Res, GraceSubmitRequest>;
  @useResult
  $Res call(
      {String orderId,
      String sceneCode,
      String description,
      List<String> attachments});
}

/// @nodoc
class _$GraceSubmitRequestCopyWithImpl<$Res, $Val extends GraceSubmitRequest>
    implements $GraceSubmitRequestCopyWith<$Res> {
  _$GraceSubmitRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GraceSubmitRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? orderId = null,
    Object? sceneCode = null,
    Object? description = null,
    Object? attachments = null,
  }) {
    return _then(_value.copyWith(
      orderId: null == orderId
          ? _value.orderId
          : orderId // ignore: cast_nullable_to_non_nullable
              as String,
      sceneCode: null == sceneCode
          ? _value.sceneCode
          : sceneCode // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      attachments: null == attachments
          ? _value.attachments
          : attachments // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$GraceSubmitRequestImplCopyWith<$Res>
    implements $GraceSubmitRequestCopyWith<$Res> {
  factory _$$GraceSubmitRequestImplCopyWith(_$GraceSubmitRequestImpl value,
          $Res Function(_$GraceSubmitRequestImpl) then) =
      __$$GraceSubmitRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String orderId,
      String sceneCode,
      String description,
      List<String> attachments});
}

/// @nodoc
class __$$GraceSubmitRequestImplCopyWithImpl<$Res>
    extends _$GraceSubmitRequestCopyWithImpl<$Res, _$GraceSubmitRequestImpl>
    implements _$$GraceSubmitRequestImplCopyWith<$Res> {
  __$$GraceSubmitRequestImplCopyWithImpl(_$GraceSubmitRequestImpl _value,
      $Res Function(_$GraceSubmitRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of GraceSubmitRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? orderId = null,
    Object? sceneCode = null,
    Object? description = null,
    Object? attachments = null,
  }) {
    return _then(_$GraceSubmitRequestImpl(
      orderId: null == orderId
          ? _value.orderId
          : orderId // ignore: cast_nullable_to_non_nullable
              as String,
      sceneCode: null == sceneCode
          ? _value.sceneCode
          : sceneCode // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      attachments: null == attachments
          ? _value._attachments
          : attachments // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GraceSubmitRequestImpl implements _GraceSubmitRequest {
  const _$GraceSubmitRequestImpl(
      {required this.orderId,
      required this.sceneCode,
      required this.description,
      final List<String> attachments = const <String>[]})
      : _attachments = attachments;

  factory _$GraceSubmitRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$GraceSubmitRequestImplFromJson(json);

  @override
  final String orderId;
  @override
  final String sceneCode;
  @override
  final String description;
  final List<String> _attachments;
  @override
  @JsonKey()
  List<String> get attachments {
    if (_attachments is EqualUnmodifiableListView) return _attachments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_attachments);
  }

  @override
  String toString() {
    return 'GraceSubmitRequest(orderId: $orderId, sceneCode: $sceneCode, description: $description, attachments: $attachments)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GraceSubmitRequestImpl &&
            (identical(other.orderId, orderId) || other.orderId == orderId) &&
            (identical(other.sceneCode, sceneCode) ||
                other.sceneCode == sceneCode) &&
            (identical(other.description, description) ||
                other.description == description) &&
            const DeepCollectionEquality()
                .equals(other._attachments, _attachments));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, orderId, sceneCode, description,
      const DeepCollectionEquality().hash(_attachments));

  /// Create a copy of GraceSubmitRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GraceSubmitRequestImplCopyWith<_$GraceSubmitRequestImpl> get copyWith =>
      __$$GraceSubmitRequestImplCopyWithImpl<_$GraceSubmitRequestImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GraceSubmitRequestImplToJson(
      this,
    );
  }
}

abstract class _GraceSubmitRequest implements GraceSubmitRequest {
  const factory _GraceSubmitRequest(
      {required final String orderId,
      required final String sceneCode,
      required final String description,
      final List<String> attachments}) = _$GraceSubmitRequestImpl;

  factory _GraceSubmitRequest.fromJson(Map<String, dynamic> json) =
      _$GraceSubmitRequestImpl.fromJson;

  @override
  String get orderId;
  @override
  String get sceneCode;
  @override
  String get description;
  @override
  List<String> get attachments;

  /// Create a copy of GraceSubmitRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GraceSubmitRequestImplCopyWith<_$GraceSubmitRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

GraceSubmitResult _$GraceSubmitResultFromJson(Map<String, dynamic> json) {
  return _GraceSubmitResult.fromJson(json);
}

/// @nodoc
mixin _$GraceSubmitResult {
  String get applyNo => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  DateTime? get slaDueAt => throw _privateConstructorUsedError;

  /// Serializes this GraceSubmitResult to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of GraceSubmitResult
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $GraceSubmitResultCopyWith<GraceSubmitResult> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $GraceSubmitResultCopyWith<$Res> {
  factory $GraceSubmitResultCopyWith(
          GraceSubmitResult value, $Res Function(GraceSubmitResult) then) =
      _$GraceSubmitResultCopyWithImpl<$Res, GraceSubmitResult>;
  @useResult
  $Res call({String applyNo, String status, DateTime? slaDueAt});
}

/// @nodoc
class _$GraceSubmitResultCopyWithImpl<$Res, $Val extends GraceSubmitResult>
    implements $GraceSubmitResultCopyWith<$Res> {
  _$GraceSubmitResultCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of GraceSubmitResult
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? applyNo = null,
    Object? status = null,
    Object? slaDueAt = freezed,
  }) {
    return _then(_value.copyWith(
      applyNo: null == applyNo
          ? _value.applyNo
          : applyNo // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      slaDueAt: freezed == slaDueAt
          ? _value.slaDueAt
          : slaDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$GraceSubmitResultImplCopyWith<$Res>
    implements $GraceSubmitResultCopyWith<$Res> {
  factory _$$GraceSubmitResultImplCopyWith(_$GraceSubmitResultImpl value,
          $Res Function(_$GraceSubmitResultImpl) then) =
      __$$GraceSubmitResultImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String applyNo, String status, DateTime? slaDueAt});
}

/// @nodoc
class __$$GraceSubmitResultImplCopyWithImpl<$Res>
    extends _$GraceSubmitResultCopyWithImpl<$Res, _$GraceSubmitResultImpl>
    implements _$$GraceSubmitResultImplCopyWith<$Res> {
  __$$GraceSubmitResultImplCopyWithImpl(_$GraceSubmitResultImpl _value,
      $Res Function(_$GraceSubmitResultImpl) _then)
      : super(_value, _then);

  /// Create a copy of GraceSubmitResult
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? applyNo = null,
    Object? status = null,
    Object? slaDueAt = freezed,
  }) {
    return _then(_$GraceSubmitResultImpl(
      applyNo: null == applyNo
          ? _value.applyNo
          : applyNo // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      slaDueAt: freezed == slaDueAt
          ? _value.slaDueAt
          : slaDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$GraceSubmitResultImpl implements _GraceSubmitResult {
  const _$GraceSubmitResultImpl(
      {required this.applyNo, this.status = 'PENDING', this.slaDueAt});

  factory _$GraceSubmitResultImpl.fromJson(Map<String, dynamic> json) =>
      _$$GraceSubmitResultImplFromJson(json);

  @override
  final String applyNo;
  @override
  @JsonKey()
  final String status;
  @override
  final DateTime? slaDueAt;

  @override
  String toString() {
    return 'GraceSubmitResult(applyNo: $applyNo, status: $status, slaDueAt: $slaDueAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$GraceSubmitResultImpl &&
            (identical(other.applyNo, applyNo) || other.applyNo == applyNo) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.slaDueAt, slaDueAt) ||
                other.slaDueAt == slaDueAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, applyNo, status, slaDueAt);

  /// Create a copy of GraceSubmitResult
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$GraceSubmitResultImplCopyWith<_$GraceSubmitResultImpl> get copyWith =>
      __$$GraceSubmitResultImplCopyWithImpl<_$GraceSubmitResultImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$GraceSubmitResultImplToJson(
      this,
    );
  }
}

abstract class _GraceSubmitResult implements GraceSubmitResult {
  const factory _GraceSubmitResult(
      {required final String applyNo,
      final String status,
      final DateTime? slaDueAt}) = _$GraceSubmitResultImpl;

  factory _GraceSubmitResult.fromJson(Map<String, dynamic> json) =
      _$GraceSubmitResultImpl.fromJson;

  @override
  String get applyNo;
  @override
  String get status;
  @override
  DateTime? get slaDueAt;

  /// Create a copy of GraceSubmitResult
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$GraceSubmitResultImplCopyWith<_$GraceSubmitResultImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BookItem _$BookItemFromJson(Map<String, dynamic> json) {
  return _BookItem.fromJson(json);
}

/// @nodoc
mixin _$BookItem {
  String get id => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String get author => throw _privateConstructorUsedError;
  String get publisher => throw _privateConstructorUsedError;
  String get year => throw _privateConstructorUsedError;
  String get callNo => throw _privateConstructorUsedError;
  String get libraryLabel => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  int get availableCopies => throw _privateConstructorUsedError;
  int get queueLen => throw _privateConstructorUsedError;
  DateTime? get estimatedAvailableAt => throw _privateConstructorUsedError;
  String get coverLabel => throw _privateConstructorUsedError;
  String get coverTone => throw _privateConstructorUsedError;

  /// Serializes this BookItem to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BookItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BookItemCopyWith<BookItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BookItemCopyWith<$Res> {
  factory $BookItemCopyWith(BookItem value, $Res Function(BookItem) then) =
      _$BookItemCopyWithImpl<$Res, BookItem>;
  @useResult
  $Res call(
      {String id,
      String title,
      String author,
      String publisher,
      String year,
      String callNo,
      String libraryLabel,
      String status,
      int availableCopies,
      int queueLen,
      DateTime? estimatedAvailableAt,
      String coverLabel,
      String coverTone});
}

/// @nodoc
class _$BookItemCopyWithImpl<$Res, $Val extends BookItem>
    implements $BookItemCopyWith<$Res> {
  _$BookItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BookItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? year = null,
    Object? callNo = null,
    Object? libraryLabel = null,
    Object? status = null,
    Object? availableCopies = null,
    Object? queueLen = null,
    Object? estimatedAvailableAt = freezed,
    Object? coverLabel = null,
    Object? coverTone = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      year: null == year
          ? _value.year
          : year // ignore: cast_nullable_to_non_nullable
              as String,
      callNo: null == callNo
          ? _value.callNo
          : callNo // ignore: cast_nullable_to_non_nullable
              as String,
      libraryLabel: null == libraryLabel
          ? _value.libraryLabel
          : libraryLabel // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      availableCopies: null == availableCopies
          ? _value.availableCopies
          : availableCopies // ignore: cast_nullable_to_non_nullable
              as int,
      queueLen: null == queueLen
          ? _value.queueLen
          : queueLen // ignore: cast_nullable_to_non_nullable
              as int,
      estimatedAvailableAt: freezed == estimatedAvailableAt
          ? _value.estimatedAvailableAt
          : estimatedAvailableAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BookItemImplCopyWith<$Res>
    implements $BookItemCopyWith<$Res> {
  factory _$$BookItemImplCopyWith(
          _$BookItemImpl value, $Res Function(_$BookItemImpl) then) =
      __$$BookItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String title,
      String author,
      String publisher,
      String year,
      String callNo,
      String libraryLabel,
      String status,
      int availableCopies,
      int queueLen,
      DateTime? estimatedAvailableAt,
      String coverLabel,
      String coverTone});
}

/// @nodoc
class __$$BookItemImplCopyWithImpl<$Res>
    extends _$BookItemCopyWithImpl<$Res, _$BookItemImpl>
    implements _$$BookItemImplCopyWith<$Res> {
  __$$BookItemImplCopyWithImpl(
      _$BookItemImpl _value, $Res Function(_$BookItemImpl) _then)
      : super(_value, _then);

  /// Create a copy of BookItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? year = null,
    Object? callNo = null,
    Object? libraryLabel = null,
    Object? status = null,
    Object? availableCopies = null,
    Object? queueLen = null,
    Object? estimatedAvailableAt = freezed,
    Object? coverLabel = null,
    Object? coverTone = null,
  }) {
    return _then(_$BookItemImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      year: null == year
          ? _value.year
          : year // ignore: cast_nullable_to_non_nullable
              as String,
      callNo: null == callNo
          ? _value.callNo
          : callNo // ignore: cast_nullable_to_non_nullable
              as String,
      libraryLabel: null == libraryLabel
          ? _value.libraryLabel
          : libraryLabel // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      availableCopies: null == availableCopies
          ? _value.availableCopies
          : availableCopies // ignore: cast_nullable_to_non_nullable
              as int,
      queueLen: null == queueLen
          ? _value.queueLen
          : queueLen // ignore: cast_nullable_to_non_nullable
              as int,
      estimatedAvailableAt: freezed == estimatedAvailableAt
          ? _value.estimatedAvailableAt
          : estimatedAvailableAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BookItemImpl implements _BookItem {
  const _$BookItemImpl(
      {required this.id,
      required this.title,
      required this.author,
      required this.publisher,
      this.year = '',
      this.callNo = '',
      this.libraryLabel = '',
      this.status = 'AVAILABLE',
      this.availableCopies = 0,
      this.queueLen = 0,
      this.estimatedAvailableAt,
      required this.coverLabel,
      this.coverTone = 'mint'});

  factory _$BookItemImpl.fromJson(Map<String, dynamic> json) =>
      _$$BookItemImplFromJson(json);

  @override
  final String id;
  @override
  final String title;
  @override
  final String author;
  @override
  final String publisher;
  @override
  @JsonKey()
  final String year;
  @override
  @JsonKey()
  final String callNo;
  @override
  @JsonKey()
  final String libraryLabel;
  @override
  @JsonKey()
  final String status;
  @override
  @JsonKey()
  final int availableCopies;
  @override
  @JsonKey()
  final int queueLen;
  @override
  final DateTime? estimatedAvailableAt;
  @override
  final String coverLabel;
  @override
  @JsonKey()
  final String coverTone;

  @override
  String toString() {
    return 'BookItem(id: $id, title: $title, author: $author, publisher: $publisher, year: $year, callNo: $callNo, libraryLabel: $libraryLabel, status: $status, availableCopies: $availableCopies, queueLen: $queueLen, estimatedAvailableAt: $estimatedAvailableAt, coverLabel: $coverLabel, coverTone: $coverTone)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BookItemImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.author, author) || other.author == author) &&
            (identical(other.publisher, publisher) ||
                other.publisher == publisher) &&
            (identical(other.year, year) || other.year == year) &&
            (identical(other.callNo, callNo) || other.callNo == callNo) &&
            (identical(other.libraryLabel, libraryLabel) ||
                other.libraryLabel == libraryLabel) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.availableCopies, availableCopies) ||
                other.availableCopies == availableCopies) &&
            (identical(other.queueLen, queueLen) ||
                other.queueLen == queueLen) &&
            (identical(other.estimatedAvailableAt, estimatedAvailableAt) ||
                other.estimatedAvailableAt == estimatedAvailableAt) &&
            (identical(other.coverLabel, coverLabel) ||
                other.coverLabel == coverLabel) &&
            (identical(other.coverTone, coverTone) ||
                other.coverTone == coverTone));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      title,
      author,
      publisher,
      year,
      callNo,
      libraryLabel,
      status,
      availableCopies,
      queueLen,
      estimatedAvailableAt,
      coverLabel,
      coverTone);

  /// Create a copy of BookItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BookItemImplCopyWith<_$BookItemImpl> get copyWith =>
      __$$BookItemImplCopyWithImpl<_$BookItemImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BookItemImplToJson(
      this,
    );
  }
}

abstract class _BookItem implements BookItem {
  const factory _BookItem(
      {required final String id,
      required final String title,
      required final String author,
      required final String publisher,
      final String year,
      final String callNo,
      final String libraryLabel,
      final String status,
      final int availableCopies,
      final int queueLen,
      final DateTime? estimatedAvailableAt,
      required final String coverLabel,
      final String coverTone}) = _$BookItemImpl;

  factory _BookItem.fromJson(Map<String, dynamic> json) =
      _$BookItemImpl.fromJson;

  @override
  String get id;
  @override
  String get title;
  @override
  String get author;
  @override
  String get publisher;
  @override
  String get year;
  @override
  String get callNo;
  @override
  String get libraryLabel;
  @override
  String get status;
  @override
  int get availableCopies;
  @override
  int get queueLen;
  @override
  DateTime? get estimatedAvailableAt;
  @override
  String get coverLabel;
  @override
  String get coverTone;

  /// Create a copy of BookItem
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BookItemImplCopyWith<_$BookItemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

SearchPageData _$SearchPageDataFromJson(Map<String, dynamic> json) {
  return _SearchPageData.fromJson(json);
}

/// @nodoc
mixin _$SearchPageData {
  int get total => throw _privateConstructorUsedError;
  List<BookItem> get items => throw _privateConstructorUsedError;
  String get query => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this SearchPageData to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of SearchPageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $SearchPageDataCopyWith<SearchPageData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $SearchPageDataCopyWith<$Res> {
  factory $SearchPageDataCopyWith(
          SearchPageData value, $Res Function(SearchPageData) then) =
      _$SearchPageDataCopyWithImpl<$Res, SearchPageData>;
  @useResult
  $Res call({int total, List<BookItem> items, String query, bool isOffline});
}

/// @nodoc
class _$SearchPageDataCopyWithImpl<$Res, $Val extends SearchPageData>
    implements $SearchPageDataCopyWith<$Res> {
  _$SearchPageDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of SearchPageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? total = null,
    Object? items = null,
    Object? query = null,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      total: null == total
          ? _value.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
      items: null == items
          ? _value.items
          : items // ignore: cast_nullable_to_non_nullable
              as List<BookItem>,
      query: null == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as String,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$SearchPageDataImplCopyWith<$Res>
    implements $SearchPageDataCopyWith<$Res> {
  factory _$$SearchPageDataImplCopyWith(_$SearchPageDataImpl value,
          $Res Function(_$SearchPageDataImpl) then) =
      __$$SearchPageDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int total, List<BookItem> items, String query, bool isOffline});
}

/// @nodoc
class __$$SearchPageDataImplCopyWithImpl<$Res>
    extends _$SearchPageDataCopyWithImpl<$Res, _$SearchPageDataImpl>
    implements _$$SearchPageDataImplCopyWith<$Res> {
  __$$SearchPageDataImplCopyWithImpl(
      _$SearchPageDataImpl _value, $Res Function(_$SearchPageDataImpl) _then)
      : super(_value, _then);

  /// Create a copy of SearchPageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? total = null,
    Object? items = null,
    Object? query = null,
    Object? isOffline = null,
  }) {
    return _then(_$SearchPageDataImpl(
      total: null == total
          ? _value.total
          : total // ignore: cast_nullable_to_non_nullable
              as int,
      items: null == items
          ? _value._items
          : items // ignore: cast_nullable_to_non_nullable
              as List<BookItem>,
      query: null == query
          ? _value.query
          : query // ignore: cast_nullable_to_non_nullable
              as String,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$SearchPageDataImpl implements _SearchPageData {
  const _$SearchPageDataImpl(
      {this.total = 0,
      final List<BookItem> items = const <BookItem>[],
      this.query = '',
      this.isOffline = false})
      : _items = items;

  factory _$SearchPageDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$SearchPageDataImplFromJson(json);

  @override
  @JsonKey()
  final int total;
  final List<BookItem> _items;
  @override
  @JsonKey()
  List<BookItem> get items {
    if (_items is EqualUnmodifiableListView) return _items;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_items);
  }

  @override
  @JsonKey()
  final String query;
  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'SearchPageData(total: $total, items: $items, query: $query, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$SearchPageDataImpl &&
            (identical(other.total, total) || other.total == total) &&
            const DeepCollectionEquality().equals(other._items, _items) &&
            (identical(other.query, query) || other.query == query) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, total,
      const DeepCollectionEquality().hash(_items), query, isOffline);

  /// Create a copy of SearchPageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$SearchPageDataImplCopyWith<_$SearchPageDataImpl> get copyWith =>
      __$$SearchPageDataImplCopyWithImpl<_$SearchPageDataImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$SearchPageDataImplToJson(
      this,
    );
  }
}

abstract class _SearchPageData implements SearchPageData {
  const factory _SearchPageData(
      {final int total,
      final List<BookItem> items,
      final String query,
      final bool isOffline}) = _$SearchPageDataImpl;

  factory _SearchPageData.fromJson(Map<String, dynamic> json) =
      _$SearchPageDataImpl.fromJson;

  @override
  int get total;
  @override
  List<BookItem> get items;
  @override
  String get query;
  @override
  bool get isOffline;

  /// Create a copy of SearchPageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$SearchPageDataImplCopyWith<_$SearchPageDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

ReservationItem _$ReservationItemFromJson(Map<String, dynamic> json) {
  return _ReservationItem.fromJson(json);
}

/// @nodoc
mixin _$ReservationItem {
  String get id => throw _privateConstructorUsedError;
  String get bookTitle => throw _privateConstructorUsedError;
  String get author => throw _privateConstructorUsedError;
  String get publisher => throw _privateConstructorUsedError;
  String get coverLabel => throw _privateConstructorUsedError;
  String get coverTone => throw _privateConstructorUsedError;
  String get pickupPoint => throw _privateConstructorUsedError;
  String get pickupCode => throw _privateConstructorUsedError;
  DateTime? get readyAt => throw _privateConstructorUsedError;
  DateTime? get expiresAt => throw _privateConstructorUsedError;
  int get remainingHours => throw _privateConstructorUsedError;
  int get queueNo => throw _privateConstructorUsedError;
  int get queueTotal => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  DateTime? get fulfilledAt => throw _privateConstructorUsedError;
  DateTime? get returnedAt => throw _privateConstructorUsedError;

  /// Serializes this ReservationItem to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ReservationItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ReservationItemCopyWith<ReservationItem> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ReservationItemCopyWith<$Res> {
  factory $ReservationItemCopyWith(
          ReservationItem value, $Res Function(ReservationItem) then) =
      _$ReservationItemCopyWithImpl<$Res, ReservationItem>;
  @useResult
  $Res call(
      {String id,
      String bookTitle,
      String author,
      String publisher,
      String coverLabel,
      String coverTone,
      String pickupPoint,
      String pickupCode,
      DateTime? readyAt,
      DateTime? expiresAt,
      int remainingHours,
      int queueNo,
      int queueTotal,
      String status,
      DateTime? fulfilledAt,
      DateTime? returnedAt});
}

/// @nodoc
class _$ReservationItemCopyWithImpl<$Res, $Val extends ReservationItem>
    implements $ReservationItemCopyWith<$Res> {
  _$ReservationItemCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ReservationItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? bookTitle = null,
    Object? author = null,
    Object? publisher = null,
    Object? coverLabel = null,
    Object? coverTone = null,
    Object? pickupPoint = null,
    Object? pickupCode = null,
    Object? readyAt = freezed,
    Object? expiresAt = freezed,
    Object? remainingHours = null,
    Object? queueNo = null,
    Object? queueTotal = null,
    Object? status = null,
    Object? fulfilledAt = freezed,
    Object? returnedAt = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      bookTitle: null == bookTitle
          ? _value.bookTitle
          : bookTitle // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
      pickupPoint: null == pickupPoint
          ? _value.pickupPoint
          : pickupPoint // ignore: cast_nullable_to_non_nullable
              as String,
      pickupCode: null == pickupCode
          ? _value.pickupCode
          : pickupCode // ignore: cast_nullable_to_non_nullable
              as String,
      readyAt: freezed == readyAt
          ? _value.readyAt
          : readyAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      expiresAt: freezed == expiresAt
          ? _value.expiresAt
          : expiresAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      remainingHours: null == remainingHours
          ? _value.remainingHours
          : remainingHours // ignore: cast_nullable_to_non_nullable
              as int,
      queueNo: null == queueNo
          ? _value.queueNo
          : queueNo // ignore: cast_nullable_to_non_nullable
              as int,
      queueTotal: null == queueTotal
          ? _value.queueTotal
          : queueTotal // ignore: cast_nullable_to_non_nullable
              as int,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      fulfilledAt: freezed == fulfilledAt
          ? _value.fulfilledAt
          : fulfilledAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      returnedAt: freezed == returnedAt
          ? _value.returnedAt
          : returnedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ReservationItemImplCopyWith<$Res>
    implements $ReservationItemCopyWith<$Res> {
  factory _$$ReservationItemImplCopyWith(_$ReservationItemImpl value,
          $Res Function(_$ReservationItemImpl) then) =
      __$$ReservationItemImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String bookTitle,
      String author,
      String publisher,
      String coverLabel,
      String coverTone,
      String pickupPoint,
      String pickupCode,
      DateTime? readyAt,
      DateTime? expiresAt,
      int remainingHours,
      int queueNo,
      int queueTotal,
      String status,
      DateTime? fulfilledAt,
      DateTime? returnedAt});
}

/// @nodoc
class __$$ReservationItemImplCopyWithImpl<$Res>
    extends _$ReservationItemCopyWithImpl<$Res, _$ReservationItemImpl>
    implements _$$ReservationItemImplCopyWith<$Res> {
  __$$ReservationItemImplCopyWithImpl(
      _$ReservationItemImpl _value, $Res Function(_$ReservationItemImpl) _then)
      : super(_value, _then);

  /// Create a copy of ReservationItem
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? bookTitle = null,
    Object? author = null,
    Object? publisher = null,
    Object? coverLabel = null,
    Object? coverTone = null,
    Object? pickupPoint = null,
    Object? pickupCode = null,
    Object? readyAt = freezed,
    Object? expiresAt = freezed,
    Object? remainingHours = null,
    Object? queueNo = null,
    Object? queueTotal = null,
    Object? status = null,
    Object? fulfilledAt = freezed,
    Object? returnedAt = freezed,
  }) {
    return _then(_$ReservationItemImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      bookTitle: null == bookTitle
          ? _value.bookTitle
          : bookTitle // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      coverLabel: null == coverLabel
          ? _value.coverLabel
          : coverLabel // ignore: cast_nullable_to_non_nullable
              as String,
      coverTone: null == coverTone
          ? _value.coverTone
          : coverTone // ignore: cast_nullable_to_non_nullable
              as String,
      pickupPoint: null == pickupPoint
          ? _value.pickupPoint
          : pickupPoint // ignore: cast_nullable_to_non_nullable
              as String,
      pickupCode: null == pickupCode
          ? _value.pickupCode
          : pickupCode // ignore: cast_nullable_to_non_nullable
              as String,
      readyAt: freezed == readyAt
          ? _value.readyAt
          : readyAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      expiresAt: freezed == expiresAt
          ? _value.expiresAt
          : expiresAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      remainingHours: null == remainingHours
          ? _value.remainingHours
          : remainingHours // ignore: cast_nullable_to_non_nullable
              as int,
      queueNo: null == queueNo
          ? _value.queueNo
          : queueNo // ignore: cast_nullable_to_non_nullable
              as int,
      queueTotal: null == queueTotal
          ? _value.queueTotal
          : queueTotal // ignore: cast_nullable_to_non_nullable
              as int,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      fulfilledAt: freezed == fulfilledAt
          ? _value.fulfilledAt
          : fulfilledAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      returnedAt: freezed == returnedAt
          ? _value.returnedAt
          : returnedAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ReservationItemImpl implements _ReservationItem {
  const _$ReservationItemImpl(
      {required this.id,
      required this.bookTitle,
      required this.author,
      this.publisher = '',
      required this.coverLabel,
      this.coverTone = 'mint',
      this.pickupPoint = '一楼服务台',
      this.pickupCode = '',
      this.readyAt,
      this.expiresAt,
      this.remainingHours = 0,
      this.queueNo = 0,
      this.queueTotal = 0,
      this.status = 'READY',
      this.fulfilledAt,
      this.returnedAt});

  factory _$ReservationItemImpl.fromJson(Map<String, dynamic> json) =>
      _$$ReservationItemImplFromJson(json);

  @override
  final String id;
  @override
  final String bookTitle;
  @override
  final String author;
  @override
  @JsonKey()
  final String publisher;
  @override
  final String coverLabel;
  @override
  @JsonKey()
  final String coverTone;
  @override
  @JsonKey()
  final String pickupPoint;
  @override
  @JsonKey()
  final String pickupCode;
  @override
  final DateTime? readyAt;
  @override
  final DateTime? expiresAt;
  @override
  @JsonKey()
  final int remainingHours;
  @override
  @JsonKey()
  final int queueNo;
  @override
  @JsonKey()
  final int queueTotal;
  @override
  @JsonKey()
  final String status;
  @override
  final DateTime? fulfilledAt;
  @override
  final DateTime? returnedAt;

  @override
  String toString() {
    return 'ReservationItem(id: $id, bookTitle: $bookTitle, author: $author, publisher: $publisher, coverLabel: $coverLabel, coverTone: $coverTone, pickupPoint: $pickupPoint, pickupCode: $pickupCode, readyAt: $readyAt, expiresAt: $expiresAt, remainingHours: $remainingHours, queueNo: $queueNo, queueTotal: $queueTotal, status: $status, fulfilledAt: $fulfilledAt, returnedAt: $returnedAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ReservationItemImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.bookTitle, bookTitle) ||
                other.bookTitle == bookTitle) &&
            (identical(other.author, author) || other.author == author) &&
            (identical(other.publisher, publisher) ||
                other.publisher == publisher) &&
            (identical(other.coverLabel, coverLabel) ||
                other.coverLabel == coverLabel) &&
            (identical(other.coverTone, coverTone) ||
                other.coverTone == coverTone) &&
            (identical(other.pickupPoint, pickupPoint) ||
                other.pickupPoint == pickupPoint) &&
            (identical(other.pickupCode, pickupCode) ||
                other.pickupCode == pickupCode) &&
            (identical(other.readyAt, readyAt) || other.readyAt == readyAt) &&
            (identical(other.expiresAt, expiresAt) ||
                other.expiresAt == expiresAt) &&
            (identical(other.remainingHours, remainingHours) ||
                other.remainingHours == remainingHours) &&
            (identical(other.queueNo, queueNo) || other.queueNo == queueNo) &&
            (identical(other.queueTotal, queueTotal) ||
                other.queueTotal == queueTotal) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.fulfilledAt, fulfilledAt) ||
                other.fulfilledAt == fulfilledAt) &&
            (identical(other.returnedAt, returnedAt) ||
                other.returnedAt == returnedAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      bookTitle,
      author,
      publisher,
      coverLabel,
      coverTone,
      pickupPoint,
      pickupCode,
      readyAt,
      expiresAt,
      remainingHours,
      queueNo,
      queueTotal,
      status,
      fulfilledAt,
      returnedAt);

  /// Create a copy of ReservationItem
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ReservationItemImplCopyWith<_$ReservationItemImpl> get copyWith =>
      __$$ReservationItemImplCopyWithImpl<_$ReservationItemImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ReservationItemImplToJson(
      this,
    );
  }
}

abstract class _ReservationItem implements ReservationItem {
  const factory _ReservationItem(
      {required final String id,
      required final String bookTitle,
      required final String author,
      final String publisher,
      required final String coverLabel,
      final String coverTone,
      final String pickupPoint,
      final String pickupCode,
      final DateTime? readyAt,
      final DateTime? expiresAt,
      final int remainingHours,
      final int queueNo,
      final int queueTotal,
      final String status,
      final DateTime? fulfilledAt,
      final DateTime? returnedAt}) = _$ReservationItemImpl;

  factory _ReservationItem.fromJson(Map<String, dynamic> json) =
      _$ReservationItemImpl.fromJson;

  @override
  String get id;
  @override
  String get bookTitle;
  @override
  String get author;
  @override
  String get publisher;
  @override
  String get coverLabel;
  @override
  String get coverTone;
  @override
  String get pickupPoint;
  @override
  String get pickupCode;
  @override
  DateTime? get readyAt;
  @override
  DateTime? get expiresAt;
  @override
  int get remainingHours;
  @override
  int get queueNo;
  @override
  int get queueTotal;
  @override
  String get status;
  @override
  DateTime? get fulfilledAt;
  @override
  DateTime? get returnedAt;

  /// Create a copy of ReservationItem
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ReservationItemImplCopyWith<_$ReservationItemImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

ReservationsPage _$ReservationsPageFromJson(Map<String, dynamic> json) {
  return _ReservationsPage.fromJson(json);
}

/// @nodoc
mixin _$ReservationsPage {
  List<ReservationItem> get ready => throw _privateConstructorUsedError;
  List<ReservationItem> get completed => throw _privateConstructorUsedError;
  List<ReservationItem> get cancelled => throw _privateConstructorUsedError;
  String get ruleNote => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this ReservationsPage to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ReservationsPage
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ReservationsPageCopyWith<ReservationsPage> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ReservationsPageCopyWith<$Res> {
  factory $ReservationsPageCopyWith(
          ReservationsPage value, $Res Function(ReservationsPage) then) =
      _$ReservationsPageCopyWithImpl<$Res, ReservationsPage>;
  @useResult
  $Res call(
      {List<ReservationItem> ready,
      List<ReservationItem> completed,
      List<ReservationItem> cancelled,
      String ruleNote,
      bool isOffline});
}

/// @nodoc
class _$ReservationsPageCopyWithImpl<$Res, $Val extends ReservationsPage>
    implements $ReservationsPageCopyWith<$Res> {
  _$ReservationsPageCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ReservationsPage
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ready = null,
    Object? completed = null,
    Object? cancelled = null,
    Object? ruleNote = null,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      ready: null == ready
          ? _value.ready
          : ready // ignore: cast_nullable_to_non_nullable
              as List<ReservationItem>,
      completed: null == completed
          ? _value.completed
          : completed // ignore: cast_nullable_to_non_nullable
              as List<ReservationItem>,
      cancelled: null == cancelled
          ? _value.cancelled
          : cancelled // ignore: cast_nullable_to_non_nullable
              as List<ReservationItem>,
      ruleNote: null == ruleNote
          ? _value.ruleNote
          : ruleNote // ignore: cast_nullable_to_non_nullable
              as String,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ReservationsPageImplCopyWith<$Res>
    implements $ReservationsPageCopyWith<$Res> {
  factory _$$ReservationsPageImplCopyWith(_$ReservationsPageImpl value,
          $Res Function(_$ReservationsPageImpl) then) =
      __$$ReservationsPageImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {List<ReservationItem> ready,
      List<ReservationItem> completed,
      List<ReservationItem> cancelled,
      String ruleNote,
      bool isOffline});
}

/// @nodoc
class __$$ReservationsPageImplCopyWithImpl<$Res>
    extends _$ReservationsPageCopyWithImpl<$Res, _$ReservationsPageImpl>
    implements _$$ReservationsPageImplCopyWith<$Res> {
  __$$ReservationsPageImplCopyWithImpl(_$ReservationsPageImpl _value,
      $Res Function(_$ReservationsPageImpl) _then)
      : super(_value, _then);

  /// Create a copy of ReservationsPage
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ready = null,
    Object? completed = null,
    Object? cancelled = null,
    Object? ruleNote = null,
    Object? isOffline = null,
  }) {
    return _then(_$ReservationsPageImpl(
      ready: null == ready
          ? _value._ready
          : ready // ignore: cast_nullable_to_non_nullable
              as List<ReservationItem>,
      completed: null == completed
          ? _value._completed
          : completed // ignore: cast_nullable_to_non_nullable
              as List<ReservationItem>,
      cancelled: null == cancelled
          ? _value._cancelled
          : cancelled // ignore: cast_nullable_to_non_nullable
              as List<ReservationItem>,
      ruleNote: null == ruleNote
          ? _value.ruleNote
          : ruleNote // ignore: cast_nullable_to_non_nullable
              as String,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ReservationsPageImpl implements _ReservationsPage {
  const _$ReservationsPageImpl(
      {final List<ReservationItem> ready = const <ReservationItem>[],
      final List<ReservationItem> completed = const <ReservationItem>[],
      final List<ReservationItem> cancelled = const <ReservationItem>[],
      this.ruleNote = '48 小时内未取书将顺延给下一位读者',
      this.isOffline = false})
      : _ready = ready,
        _completed = completed,
        _cancelled = cancelled;

  factory _$ReservationsPageImpl.fromJson(Map<String, dynamic> json) =>
      _$$ReservationsPageImplFromJson(json);

  final List<ReservationItem> _ready;
  @override
  @JsonKey()
  List<ReservationItem> get ready {
    if (_ready is EqualUnmodifiableListView) return _ready;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_ready);
  }

  final List<ReservationItem> _completed;
  @override
  @JsonKey()
  List<ReservationItem> get completed {
    if (_completed is EqualUnmodifiableListView) return _completed;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_completed);
  }

  final List<ReservationItem> _cancelled;
  @override
  @JsonKey()
  List<ReservationItem> get cancelled {
    if (_cancelled is EqualUnmodifiableListView) return _cancelled;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_cancelled);
  }

  @override
  @JsonKey()
  final String ruleNote;
  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'ReservationsPage(ready: $ready, completed: $completed, cancelled: $cancelled, ruleNote: $ruleNote, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ReservationsPageImpl &&
            const DeepCollectionEquality().equals(other._ready, _ready) &&
            const DeepCollectionEquality()
                .equals(other._completed, _completed) &&
            const DeepCollectionEquality()
                .equals(other._cancelled, _cancelled) &&
            (identical(other.ruleNote, ruleNote) ||
                other.ruleNote == ruleNote) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(_ready),
      const DeepCollectionEquality().hash(_completed),
      const DeepCollectionEquality().hash(_cancelled),
      ruleNote,
      isOffline);

  /// Create a copy of ReservationsPage
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ReservationsPageImplCopyWith<_$ReservationsPageImpl> get copyWith =>
      __$$ReservationsPageImplCopyWithImpl<_$ReservationsPageImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ReservationsPageImplToJson(
      this,
    );
  }
}

abstract class _ReservationsPage implements ReservationsPage {
  const factory _ReservationsPage(
      {final List<ReservationItem> ready,
      final List<ReservationItem> completed,
      final List<ReservationItem> cancelled,
      final String ruleNote,
      final bool isOffline}) = _$ReservationsPageImpl;

  factory _ReservationsPage.fromJson(Map<String, dynamic> json) =
      _$ReservationsPageImpl.fromJson;

  @override
  List<ReservationItem> get ready;
  @override
  List<ReservationItem> get completed;
  @override
  List<ReservationItem> get cancelled;
  @override
  String get ruleNote;
  @override
  bool get isOffline;

  /// Create a copy of ReservationsPage
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ReservationsPageImplCopyWith<_$ReservationsPageImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BookCopy _$BookCopyFromJson(Map<String, dynamic> json) {
  return _BookCopy.fromJson(json);
}

/// @nodoc
mixin _$BookCopy {
  String get id => throw _privateConstructorUsedError;
  String get barcode => throw _privateConstructorUsedError;
  String get location => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;

  /// Serializes this BookCopy to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BookCopy
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BookCopyCopyWith<BookCopy> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BookCopyCopyWith<$Res> {
  factory $BookCopyCopyWith(BookCopy value, $Res Function(BookCopy) then) =
      _$BookCopyCopyWithImpl<$Res, BookCopy>;
  @useResult
  $Res call({String id, String barcode, String location, String status});
}

/// @nodoc
class _$BookCopyCopyWithImpl<$Res, $Val extends BookCopy>
    implements $BookCopyCopyWith<$Res> {
  _$BookCopyCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BookCopy
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? barcode = null,
    Object? location = null,
    Object? status = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      barcode: null == barcode
          ? _value.barcode
          : barcode // ignore: cast_nullable_to_non_nullable
              as String,
      location: null == location
          ? _value.location
          : location // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BookCopyImplCopyWith<$Res>
    implements $BookCopyCopyWith<$Res> {
  factory _$$BookCopyImplCopyWith(
          _$BookCopyImpl value, $Res Function(_$BookCopyImpl) then) =
      __$$BookCopyImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String id, String barcode, String location, String status});
}

/// @nodoc
class __$$BookCopyImplCopyWithImpl<$Res>
    extends _$BookCopyCopyWithImpl<$Res, _$BookCopyImpl>
    implements _$$BookCopyImplCopyWith<$Res> {
  __$$BookCopyImplCopyWithImpl(
      _$BookCopyImpl _value, $Res Function(_$BookCopyImpl) _then)
      : super(_value, _then);

  /// Create a copy of BookCopy
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? barcode = null,
    Object? location = null,
    Object? status = null,
  }) {
    return _then(_$BookCopyImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      barcode: null == barcode
          ? _value.barcode
          : barcode // ignore: cast_nullable_to_non_nullable
              as String,
      location: null == location
          ? _value.location
          : location // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BookCopyImpl implements _BookCopy {
  const _$BookCopyImpl(
      {required this.id,
      required this.barcode,
      this.location = '',
      this.status = 'AVAILABLE'});

  factory _$BookCopyImpl.fromJson(Map<String, dynamic> json) =>
      _$$BookCopyImplFromJson(json);

  @override
  final String id;
  @override
  final String barcode;
  @override
  @JsonKey()
  final String location;
  @override
  @JsonKey()
  final String status;

  @override
  String toString() {
    return 'BookCopy(id: $id, barcode: $barcode, location: $location, status: $status)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BookCopyImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.barcode, barcode) || other.barcode == barcode) &&
            (identical(other.location, location) ||
                other.location == location) &&
            (identical(other.status, status) || other.status == status));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, id, barcode, location, status);

  /// Create a copy of BookCopy
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BookCopyImplCopyWith<_$BookCopyImpl> get copyWith =>
      __$$BookCopyImplCopyWithImpl<_$BookCopyImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BookCopyImplToJson(
      this,
    );
  }
}

abstract class _BookCopy implements BookCopy {
  const factory _BookCopy(
      {required final String id,
      required final String barcode,
      final String location,
      final String status}) = _$BookCopyImpl;

  factory _BookCopy.fromJson(Map<String, dynamic> json) =
      _$BookCopyImpl.fromJson;

  @override
  String get id;
  @override
  String get barcode;
  @override
  String get location;
  @override
  String get status;

  /// Create a copy of BookCopy
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BookCopyImplCopyWith<_$BookCopyImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BookDetail _$BookDetailFromJson(Map<String, dynamic> json) {
  return _BookDetail.fromJson(json);
}

/// @nodoc
mixin _$BookDetail {
  String get id => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String get author => throw _privateConstructorUsedError;
  String get publisher => throw _privateConstructorUsedError;
  String get isbn13 => throw _privateConstructorUsedError;
  String get publishDate => throw _privateConstructorUsedError;
  String get price => throw _privateConstructorUsedError;
  String get summary => throw _privateConstructorUsedError;
  List<String> get tags => throw _privateConstructorUsedError;
  int get totalCopies => throw _privateConstructorUsedError;
  int get availableCopies => throw _privateConstructorUsedError;
  int get onLoanCopies => throw _privateConstructorUsedError;
  int get queueLen => throw _privateConstructorUsedError;

  /// Serializes this BookDetail to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BookDetail
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BookDetailCopyWith<BookDetail> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BookDetailCopyWith<$Res> {
  factory $BookDetailCopyWith(
          BookDetail value, $Res Function(BookDetail) then) =
      _$BookDetailCopyWithImpl<$Res, BookDetail>;
  @useResult
  $Res call(
      {String id,
      String title,
      String author,
      String publisher,
      String isbn13,
      String publishDate,
      String price,
      String summary,
      List<String> tags,
      int totalCopies,
      int availableCopies,
      int onLoanCopies,
      int queueLen});
}

/// @nodoc
class _$BookDetailCopyWithImpl<$Res, $Val extends BookDetail>
    implements $BookDetailCopyWith<$Res> {
  _$BookDetailCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BookDetail
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? isbn13 = null,
    Object? publishDate = null,
    Object? price = null,
    Object? summary = null,
    Object? tags = null,
    Object? totalCopies = null,
    Object? availableCopies = null,
    Object? onLoanCopies = null,
    Object? queueLen = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      isbn13: null == isbn13
          ? _value.isbn13
          : isbn13 // ignore: cast_nullable_to_non_nullable
              as String,
      publishDate: null == publishDate
          ? _value.publishDate
          : publishDate // ignore: cast_nullable_to_non_nullable
              as String,
      price: null == price
          ? _value.price
          : price // ignore: cast_nullable_to_non_nullable
              as String,
      summary: null == summary
          ? _value.summary
          : summary // ignore: cast_nullable_to_non_nullable
              as String,
      tags: null == tags
          ? _value.tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>,
      totalCopies: null == totalCopies
          ? _value.totalCopies
          : totalCopies // ignore: cast_nullable_to_non_nullable
              as int,
      availableCopies: null == availableCopies
          ? _value.availableCopies
          : availableCopies // ignore: cast_nullable_to_non_nullable
              as int,
      onLoanCopies: null == onLoanCopies
          ? _value.onLoanCopies
          : onLoanCopies // ignore: cast_nullable_to_non_nullable
              as int,
      queueLen: null == queueLen
          ? _value.queueLen
          : queueLen // ignore: cast_nullable_to_non_nullable
              as int,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BookDetailImplCopyWith<$Res>
    implements $BookDetailCopyWith<$Res> {
  factory _$$BookDetailImplCopyWith(
          _$BookDetailImpl value, $Res Function(_$BookDetailImpl) then) =
      __$$BookDetailImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String title,
      String author,
      String publisher,
      String isbn13,
      String publishDate,
      String price,
      String summary,
      List<String> tags,
      int totalCopies,
      int availableCopies,
      int onLoanCopies,
      int queueLen});
}

/// @nodoc
class __$$BookDetailImplCopyWithImpl<$Res>
    extends _$BookDetailCopyWithImpl<$Res, _$BookDetailImpl>
    implements _$$BookDetailImplCopyWith<$Res> {
  __$$BookDetailImplCopyWithImpl(
      _$BookDetailImpl _value, $Res Function(_$BookDetailImpl) _then)
      : super(_value, _then);

  /// Create a copy of BookDetail
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? title = null,
    Object? author = null,
    Object? publisher = null,
    Object? isbn13 = null,
    Object? publishDate = null,
    Object? price = null,
    Object? summary = null,
    Object? tags = null,
    Object? totalCopies = null,
    Object? availableCopies = null,
    Object? onLoanCopies = null,
    Object? queueLen = null,
  }) {
    return _then(_$BookDetailImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      author: null == author
          ? _value.author
          : author // ignore: cast_nullable_to_non_nullable
              as String,
      publisher: null == publisher
          ? _value.publisher
          : publisher // ignore: cast_nullable_to_non_nullable
              as String,
      isbn13: null == isbn13
          ? _value.isbn13
          : isbn13 // ignore: cast_nullable_to_non_nullable
              as String,
      publishDate: null == publishDate
          ? _value.publishDate
          : publishDate // ignore: cast_nullable_to_non_nullable
              as String,
      price: null == price
          ? _value.price
          : price // ignore: cast_nullable_to_non_nullable
              as String,
      summary: null == summary
          ? _value.summary
          : summary // ignore: cast_nullable_to_non_nullable
              as String,
      tags: null == tags
          ? _value._tags
          : tags // ignore: cast_nullable_to_non_nullable
              as List<String>,
      totalCopies: null == totalCopies
          ? _value.totalCopies
          : totalCopies // ignore: cast_nullable_to_non_nullable
              as int,
      availableCopies: null == availableCopies
          ? _value.availableCopies
          : availableCopies // ignore: cast_nullable_to_non_nullable
              as int,
      onLoanCopies: null == onLoanCopies
          ? _value.onLoanCopies
          : onLoanCopies // ignore: cast_nullable_to_non_nullable
              as int,
      queueLen: null == queueLen
          ? _value.queueLen
          : queueLen // ignore: cast_nullable_to_non_nullable
              as int,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BookDetailImpl implements _BookDetail {
  const _$BookDetailImpl(
      {required this.id,
      required this.title,
      required this.author,
      this.publisher = '',
      this.isbn13 = '',
      this.publishDate = '',
      this.price = '',
      this.summary = '',
      final List<String> tags = const <String>[],
      this.totalCopies = 0,
      this.availableCopies = 0,
      this.onLoanCopies = 0,
      this.queueLen = 0})
      : _tags = tags;

  factory _$BookDetailImpl.fromJson(Map<String, dynamic> json) =>
      _$$BookDetailImplFromJson(json);

  @override
  final String id;
  @override
  final String title;
  @override
  final String author;
  @override
  @JsonKey()
  final String publisher;
  @override
  @JsonKey()
  final String isbn13;
  @override
  @JsonKey()
  final String publishDate;
  @override
  @JsonKey()
  final String price;
  @override
  @JsonKey()
  final String summary;
  final List<String> _tags;
  @override
  @JsonKey()
  List<String> get tags {
    if (_tags is EqualUnmodifiableListView) return _tags;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_tags);
  }

  @override
  @JsonKey()
  final int totalCopies;
  @override
  @JsonKey()
  final int availableCopies;
  @override
  @JsonKey()
  final int onLoanCopies;
  @override
  @JsonKey()
  final int queueLen;

  @override
  String toString() {
    return 'BookDetail(id: $id, title: $title, author: $author, publisher: $publisher, isbn13: $isbn13, publishDate: $publishDate, price: $price, summary: $summary, tags: $tags, totalCopies: $totalCopies, availableCopies: $availableCopies, onLoanCopies: $onLoanCopies, queueLen: $queueLen)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BookDetailImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.author, author) || other.author == author) &&
            (identical(other.publisher, publisher) ||
                other.publisher == publisher) &&
            (identical(other.isbn13, isbn13) || other.isbn13 == isbn13) &&
            (identical(other.publishDate, publishDate) ||
                other.publishDate == publishDate) &&
            (identical(other.price, price) || other.price == price) &&
            (identical(other.summary, summary) || other.summary == summary) &&
            const DeepCollectionEquality().equals(other._tags, _tags) &&
            (identical(other.totalCopies, totalCopies) ||
                other.totalCopies == totalCopies) &&
            (identical(other.availableCopies, availableCopies) ||
                other.availableCopies == availableCopies) &&
            (identical(other.onLoanCopies, onLoanCopies) ||
                other.onLoanCopies == onLoanCopies) &&
            (identical(other.queueLen, queueLen) ||
                other.queueLen == queueLen));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      title,
      author,
      publisher,
      isbn13,
      publishDate,
      price,
      summary,
      const DeepCollectionEquality().hash(_tags),
      totalCopies,
      availableCopies,
      onLoanCopies,
      queueLen);

  /// Create a copy of BookDetail
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BookDetailImplCopyWith<_$BookDetailImpl> get copyWith =>
      __$$BookDetailImplCopyWithImpl<_$BookDetailImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BookDetailImplToJson(
      this,
    );
  }
}

abstract class _BookDetail implements BookDetail {
  const factory _BookDetail(
      {required final String id,
      required final String title,
      required final String author,
      final String publisher,
      final String isbn13,
      final String publishDate,
      final String price,
      final String summary,
      final List<String> tags,
      final int totalCopies,
      final int availableCopies,
      final int onLoanCopies,
      final int queueLen}) = _$BookDetailImpl;

  factory _BookDetail.fromJson(Map<String, dynamic> json) =
      _$BookDetailImpl.fromJson;

  @override
  String get id;
  @override
  String get title;
  @override
  String get author;
  @override
  String get publisher;
  @override
  String get isbn13;
  @override
  String get publishDate;
  @override
  String get price;
  @override
  String get summary;
  @override
  List<String> get tags;
  @override
  int get totalCopies;
  @override
  int get availableCopies;
  @override
  int get onLoanCopies;
  @override
  int get queueLen;

  /// Create a copy of BookDetail
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BookDetailImplCopyWith<_$BookDetailImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BookQueue _$BookQueueFromJson(Map<String, dynamic> json) {
  return _BookQueue.fromJson(json);
}

/// @nodoc
mixin _$BookQueue {
  int get waiting => throw _privateConstructorUsedError;
  int? get myQueueNo => throw _privateConstructorUsedError;
  String? get myStatus => throw _privateConstructorUsedError;

  /// Serializes this BookQueue to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BookQueue
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BookQueueCopyWith<BookQueue> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BookQueueCopyWith<$Res> {
  factory $BookQueueCopyWith(BookQueue value, $Res Function(BookQueue) then) =
      _$BookQueueCopyWithImpl<$Res, BookQueue>;
  @useResult
  $Res call({int waiting, int? myQueueNo, String? myStatus});
}

/// @nodoc
class _$BookQueueCopyWithImpl<$Res, $Val extends BookQueue>
    implements $BookQueueCopyWith<$Res> {
  _$BookQueueCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BookQueue
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? waiting = null,
    Object? myQueueNo = freezed,
    Object? myStatus = freezed,
  }) {
    return _then(_value.copyWith(
      waiting: null == waiting
          ? _value.waiting
          : waiting // ignore: cast_nullable_to_non_nullable
              as int,
      myQueueNo: freezed == myQueueNo
          ? _value.myQueueNo
          : myQueueNo // ignore: cast_nullable_to_non_nullable
              as int?,
      myStatus: freezed == myStatus
          ? _value.myStatus
          : myStatus // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$BookQueueImplCopyWith<$Res>
    implements $BookQueueCopyWith<$Res> {
  factory _$$BookQueueImplCopyWith(
          _$BookQueueImpl value, $Res Function(_$BookQueueImpl) then) =
      __$$BookQueueImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({int waiting, int? myQueueNo, String? myStatus});
}

/// @nodoc
class __$$BookQueueImplCopyWithImpl<$Res>
    extends _$BookQueueCopyWithImpl<$Res, _$BookQueueImpl>
    implements _$$BookQueueImplCopyWith<$Res> {
  __$$BookQueueImplCopyWithImpl(
      _$BookQueueImpl _value, $Res Function(_$BookQueueImpl) _then)
      : super(_value, _then);

  /// Create a copy of BookQueue
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? waiting = null,
    Object? myQueueNo = freezed,
    Object? myStatus = freezed,
  }) {
    return _then(_$BookQueueImpl(
      waiting: null == waiting
          ? _value.waiting
          : waiting // ignore: cast_nullable_to_non_nullable
              as int,
      myQueueNo: freezed == myQueueNo
          ? _value.myQueueNo
          : myQueueNo // ignore: cast_nullable_to_non_nullable
              as int?,
      myStatus: freezed == myStatus
          ? _value.myStatus
          : myStatus // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BookQueueImpl implements _BookQueue {
  const _$BookQueueImpl({this.waiting = 0, this.myQueueNo, this.myStatus});

  factory _$BookQueueImpl.fromJson(Map<String, dynamic> json) =>
      _$$BookQueueImplFromJson(json);

  @override
  @JsonKey()
  final int waiting;
  @override
  final int? myQueueNo;
  @override
  final String? myStatus;

  @override
  String toString() {
    return 'BookQueue(waiting: $waiting, myQueueNo: $myQueueNo, myStatus: $myStatus)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BookQueueImpl &&
            (identical(other.waiting, waiting) || other.waiting == waiting) &&
            (identical(other.myQueueNo, myQueueNo) ||
                other.myQueueNo == myQueueNo) &&
            (identical(other.myStatus, myStatus) ||
                other.myStatus == myStatus));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, waiting, myQueueNo, myStatus);

  /// Create a copy of BookQueue
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BookQueueImplCopyWith<_$BookQueueImpl> get copyWith =>
      __$$BookQueueImplCopyWithImpl<_$BookQueueImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BookQueueImplToJson(
      this,
    );
  }
}

abstract class _BookQueue implements BookQueue {
  const factory _BookQueue(
      {final int waiting,
      final int? myQueueNo,
      final String? myStatus}) = _$BookQueueImpl;

  factory _BookQueue.fromJson(Map<String, dynamic> json) =
      _$BookQueueImpl.fromJson;

  @override
  int get waiting;
  @override
  int? get myQueueNo;
  @override
  String? get myStatus;

  /// Create a copy of BookQueue
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BookQueueImplCopyWith<_$BookQueueImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

BookDetailData _$BookDetailDataFromJson(Map<String, dynamic> json) {
  return _BookDetailData.fromJson(json);
}

/// @nodoc
mixin _$BookDetailData {
  BookDetail get detail => throw _privateConstructorUsedError;
  List<BookCopy> get copies => throw _privateConstructorUsedError;
  BookQueue get queue => throw _privateConstructorUsedError;

  /// Serializes this BookDetailData to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $BookDetailDataCopyWith<BookDetailData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $BookDetailDataCopyWith<$Res> {
  factory $BookDetailDataCopyWith(
          BookDetailData value, $Res Function(BookDetailData) then) =
      _$BookDetailDataCopyWithImpl<$Res, BookDetailData>;
  @useResult
  $Res call({BookDetail detail, List<BookCopy> copies, BookQueue queue});

  $BookDetailCopyWith<$Res> get detail;
  $BookQueueCopyWith<$Res> get queue;
}

/// @nodoc
class _$BookDetailDataCopyWithImpl<$Res, $Val extends BookDetailData>
    implements $BookDetailDataCopyWith<$Res> {
  _$BookDetailDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? detail = null,
    Object? copies = null,
    Object? queue = null,
  }) {
    return _then(_value.copyWith(
      detail: null == detail
          ? _value.detail
          : detail // ignore: cast_nullable_to_non_nullable
              as BookDetail,
      copies: null == copies
          ? _value.copies
          : copies // ignore: cast_nullable_to_non_nullable
              as List<BookCopy>,
      queue: null == queue
          ? _value.queue
          : queue // ignore: cast_nullable_to_non_nullable
              as BookQueue,
    ) as $Val);
  }

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $BookDetailCopyWith<$Res> get detail {
    return $BookDetailCopyWith<$Res>(_value.detail, (value) {
      return _then(_value.copyWith(detail: value) as $Val);
    });
  }

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $BookQueueCopyWith<$Res> get queue {
    return $BookQueueCopyWith<$Res>(_value.queue, (value) {
      return _then(_value.copyWith(queue: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$BookDetailDataImplCopyWith<$Res>
    implements $BookDetailDataCopyWith<$Res> {
  factory _$$BookDetailDataImplCopyWith(_$BookDetailDataImpl value,
          $Res Function(_$BookDetailDataImpl) then) =
      __$$BookDetailDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({BookDetail detail, List<BookCopy> copies, BookQueue queue});

  @override
  $BookDetailCopyWith<$Res> get detail;
  @override
  $BookQueueCopyWith<$Res> get queue;
}

/// @nodoc
class __$$BookDetailDataImplCopyWithImpl<$Res>
    extends _$BookDetailDataCopyWithImpl<$Res, _$BookDetailDataImpl>
    implements _$$BookDetailDataImplCopyWith<$Res> {
  __$$BookDetailDataImplCopyWithImpl(
      _$BookDetailDataImpl _value, $Res Function(_$BookDetailDataImpl) _then)
      : super(_value, _then);

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? detail = null,
    Object? copies = null,
    Object? queue = null,
  }) {
    return _then(_$BookDetailDataImpl(
      detail: null == detail
          ? _value.detail
          : detail // ignore: cast_nullable_to_non_nullable
              as BookDetail,
      copies: null == copies
          ? _value._copies
          : copies // ignore: cast_nullable_to_non_nullable
              as List<BookCopy>,
      queue: null == queue
          ? _value.queue
          : queue // ignore: cast_nullable_to_non_nullable
              as BookQueue,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$BookDetailDataImpl implements _BookDetailData {
  const _$BookDetailDataImpl(
      {required this.detail,
      final List<BookCopy> copies = const <BookCopy>[],
      this.queue = const BookQueue()})
      : _copies = copies;

  factory _$BookDetailDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$BookDetailDataImplFromJson(json);

  @override
  final BookDetail detail;
  final List<BookCopy> _copies;
  @override
  @JsonKey()
  List<BookCopy> get copies {
    if (_copies is EqualUnmodifiableListView) return _copies;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_copies);
  }

  @override
  @JsonKey()
  final BookQueue queue;

  @override
  String toString() {
    return 'BookDetailData(detail: $detail, copies: $copies, queue: $queue)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$BookDetailDataImpl &&
            (identical(other.detail, detail) || other.detail == detail) &&
            const DeepCollectionEquality().equals(other._copies, _copies) &&
            (identical(other.queue, queue) || other.queue == queue));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType, detail, const DeepCollectionEquality().hash(_copies), queue);

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$BookDetailDataImplCopyWith<_$BookDetailDataImpl> get copyWith =>
      __$$BookDetailDataImplCopyWithImpl<_$BookDetailDataImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$BookDetailDataImplToJson(
      this,
    );
  }
}

abstract class _BookDetailData implements BookDetailData {
  const factory _BookDetailData(
      {required final BookDetail detail,
      final List<BookCopy> copies,
      final BookQueue queue}) = _$BookDetailDataImpl;

  factory _BookDetailData.fromJson(Map<String, dynamic> json) =
      _$BookDetailDataImpl.fromJson;

  @override
  BookDetail get detail;
  @override
  List<BookCopy> get copies;
  @override
  BookQueue get queue;

  /// Create a copy of BookDetailData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$BookDetailDataImplCopyWith<_$BookDetailDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AppNotification _$AppNotificationFromJson(Map<String, dynamic> json) {
  return _AppNotification.fromJson(json);
}

/// @nodoc
mixin _$AppNotification {
  String get id => throw _privateConstructorUsedError;
  String get type => throw _privateConstructorUsedError;
  String get title => throw _privateConstructorUsedError;
  String get body => throw _privateConstructorUsedError;
  DateTime get createdAt => throw _privateConstructorUsedError;
  bool get read => throw _privateConstructorUsedError;
  String get category => throw _privateConstructorUsedError;

  /// Serializes this AppNotification to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AppNotification
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AppNotificationCopyWith<AppNotification> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppNotificationCopyWith<$Res> {
  factory $AppNotificationCopyWith(
          AppNotification value, $Res Function(AppNotification) then) =
      _$AppNotificationCopyWithImpl<$Res, AppNotification>;
  @useResult
  $Res call(
      {String id,
      String type,
      String title,
      String body,
      DateTime createdAt,
      bool read,
      String category});
}

/// @nodoc
class _$AppNotificationCopyWithImpl<$Res, $Val extends AppNotification>
    implements $AppNotificationCopyWith<$Res> {
  _$AppNotificationCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AppNotification
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? type = null,
    Object? title = null,
    Object? body = null,
    Object? createdAt = null,
    Object? read = null,
    Object? category = null,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      body: null == body
          ? _value.body
          : body // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: null == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      read: null == read
          ? _value.read
          : read // ignore: cast_nullable_to_non_nullable
              as bool,
      category: null == category
          ? _value.category
          : category // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AppNotificationImplCopyWith<$Res>
    implements $AppNotificationCopyWith<$Res> {
  factory _$$AppNotificationImplCopyWith(_$AppNotificationImpl value,
          $Res Function(_$AppNotificationImpl) then) =
      __$$AppNotificationImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String type,
      String title,
      String body,
      DateTime createdAt,
      bool read,
      String category});
}

/// @nodoc
class __$$AppNotificationImplCopyWithImpl<$Res>
    extends _$AppNotificationCopyWithImpl<$Res, _$AppNotificationImpl>
    implements _$$AppNotificationImplCopyWith<$Res> {
  __$$AppNotificationImplCopyWithImpl(
      _$AppNotificationImpl _value, $Res Function(_$AppNotificationImpl) _then)
      : super(_value, _then);

  /// Create a copy of AppNotification
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? type = null,
    Object? title = null,
    Object? body = null,
    Object? createdAt = null,
    Object? read = null,
    Object? category = null,
  }) {
    return _then(_$AppNotificationImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as String,
      title: null == title
          ? _value.title
          : title // ignore: cast_nullable_to_non_nullable
              as String,
      body: null == body
          ? _value.body
          : body // ignore: cast_nullable_to_non_nullable
              as String,
      createdAt: null == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      read: null == read
          ? _value.read
          : read // ignore: cast_nullable_to_non_nullable
              as bool,
      category: null == category
          ? _value.category
          : category // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AppNotificationImpl implements _AppNotification {
  const _$AppNotificationImpl(
      {required this.id,
      required this.type,
      required this.title,
      required this.body,
      required this.createdAt,
      this.read = false,
      this.category = 'borrow'});

  factory _$AppNotificationImpl.fromJson(Map<String, dynamic> json) =>
      _$$AppNotificationImplFromJson(json);

  @override
  final String id;
  @override
  final String type;
  @override
  final String title;
  @override
  final String body;
  @override
  final DateTime createdAt;
  @override
  @JsonKey()
  final bool read;
  @override
  @JsonKey()
  final String category;

  @override
  String toString() {
    return 'AppNotification(id: $id, type: $type, title: $title, body: $body, createdAt: $createdAt, read: $read, category: $category)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AppNotificationImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.type, type) || other.type == type) &&
            (identical(other.title, title) || other.title == title) &&
            (identical(other.body, body) || other.body == body) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.read, read) || other.read == read) &&
            (identical(other.category, category) ||
                other.category == category));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType, id, type, title, body, createdAt, read, category);

  /// Create a copy of AppNotification
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AppNotificationImplCopyWith<_$AppNotificationImpl> get copyWith =>
      __$$AppNotificationImplCopyWithImpl<_$AppNotificationImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AppNotificationImplToJson(
      this,
    );
  }
}

abstract class _AppNotification implements AppNotification {
  const factory _AppNotification(
      {required final String id,
      required final String type,
      required final String title,
      required final String body,
      required final DateTime createdAt,
      final bool read,
      final String category}) = _$AppNotificationImpl;

  factory _AppNotification.fromJson(Map<String, dynamic> json) =
      _$AppNotificationImpl.fromJson;

  @override
  String get id;
  @override
  String get type;
  @override
  String get title;
  @override
  String get body;
  @override
  DateTime get createdAt;
  @override
  bool get read;
  @override
  String get category;

  /// Create a copy of AppNotification
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AppNotificationImplCopyWith<_$AppNotificationImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

NotificationChannels _$NotificationChannelsFromJson(Map<String, dynamic> json) {
  return _NotificationChannels.fromJson(json);
}

/// @nodoc
mixin _$NotificationChannels {
  bool get inbox => throw _privateConstructorUsedError;
  bool get email => throw _privateConstructorUsedError;
  bool get push => throw _privateConstructorUsedError;

  /// Serializes this NotificationChannels to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of NotificationChannels
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $NotificationChannelsCopyWith<NotificationChannels> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $NotificationChannelsCopyWith<$Res> {
  factory $NotificationChannelsCopyWith(NotificationChannels value,
          $Res Function(NotificationChannels) then) =
      _$NotificationChannelsCopyWithImpl<$Res, NotificationChannels>;
  @useResult
  $Res call({bool inbox, bool email, bool push});
}

/// @nodoc
class _$NotificationChannelsCopyWithImpl<$Res,
        $Val extends NotificationChannels>
    implements $NotificationChannelsCopyWith<$Res> {
  _$NotificationChannelsCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of NotificationChannels
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? inbox = null,
    Object? email = null,
    Object? push = null,
  }) {
    return _then(_value.copyWith(
      inbox: null == inbox
          ? _value.inbox
          : inbox // ignore: cast_nullable_to_non_nullable
              as bool,
      email: null == email
          ? _value.email
          : email // ignore: cast_nullable_to_non_nullable
              as bool,
      push: null == push
          ? _value.push
          : push // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$NotificationChannelsImplCopyWith<$Res>
    implements $NotificationChannelsCopyWith<$Res> {
  factory _$$NotificationChannelsImplCopyWith(_$NotificationChannelsImpl value,
          $Res Function(_$NotificationChannelsImpl) then) =
      __$$NotificationChannelsImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({bool inbox, bool email, bool push});
}

/// @nodoc
class __$$NotificationChannelsImplCopyWithImpl<$Res>
    extends _$NotificationChannelsCopyWithImpl<$Res, _$NotificationChannelsImpl>
    implements _$$NotificationChannelsImplCopyWith<$Res> {
  __$$NotificationChannelsImplCopyWithImpl(_$NotificationChannelsImpl _value,
      $Res Function(_$NotificationChannelsImpl) _then)
      : super(_value, _then);

  /// Create a copy of NotificationChannels
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? inbox = null,
    Object? email = null,
    Object? push = null,
  }) {
    return _then(_$NotificationChannelsImpl(
      inbox: null == inbox
          ? _value.inbox
          : inbox // ignore: cast_nullable_to_non_nullable
              as bool,
      email: null == email
          ? _value.email
          : email // ignore: cast_nullable_to_non_nullable
              as bool,
      push: null == push
          ? _value.push
          : push // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$NotificationChannelsImpl implements _NotificationChannels {
  const _$NotificationChannelsImpl(
      {this.inbox = true, this.email = true, this.push = true});

  factory _$NotificationChannelsImpl.fromJson(Map<String, dynamic> json) =>
      _$$NotificationChannelsImplFromJson(json);

  @override
  @JsonKey()
  final bool inbox;
  @override
  @JsonKey()
  final bool email;
  @override
  @JsonKey()
  final bool push;

  @override
  String toString() {
    return 'NotificationChannels(inbox: $inbox, email: $email, push: $push)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$NotificationChannelsImpl &&
            (identical(other.inbox, inbox) || other.inbox == inbox) &&
            (identical(other.email, email) || other.email == email) &&
            (identical(other.push, push) || other.push == push));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, inbox, email, push);

  /// Create a copy of NotificationChannels
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$NotificationChannelsImplCopyWith<_$NotificationChannelsImpl>
      get copyWith =>
          __$$NotificationChannelsImplCopyWithImpl<_$NotificationChannelsImpl>(
              this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$NotificationChannelsImplToJson(
      this,
    );
  }
}

abstract class _NotificationChannels implements NotificationChannels {
  const factory _NotificationChannels(
      {final bool inbox,
      final bool email,
      final bool push}) = _$NotificationChannelsImpl;

  factory _NotificationChannels.fromJson(Map<String, dynamic> json) =
      _$NotificationChannelsImpl.fromJson;

  @override
  bool get inbox;
  @override
  bool get email;
  @override
  bool get push;

  /// Create a copy of NotificationChannels
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$NotificationChannelsImplCopyWith<_$NotificationChannelsImpl>
      get copyWith => throw _privateConstructorUsedError;
}

QuietHours _$QuietHoursFromJson(Map<String, dynamic> json) {
  return _QuietHours.fromJson(json);
}

/// @nodoc
mixin _$QuietHours {
  bool get enabled => throw _privateConstructorUsedError;
  String get start => throw _privateConstructorUsedError;
  String get end => throw _privateConstructorUsedError;

  /// Serializes this QuietHours to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of QuietHours
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $QuietHoursCopyWith<QuietHours> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $QuietHoursCopyWith<$Res> {
  factory $QuietHoursCopyWith(
          QuietHours value, $Res Function(QuietHours) then) =
      _$QuietHoursCopyWithImpl<$Res, QuietHours>;
  @useResult
  $Res call({bool enabled, String start, String end});
}

/// @nodoc
class _$QuietHoursCopyWithImpl<$Res, $Val extends QuietHours>
    implements $QuietHoursCopyWith<$Res> {
  _$QuietHoursCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of QuietHours
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? enabled = null,
    Object? start = null,
    Object? end = null,
  }) {
    return _then(_value.copyWith(
      enabled: null == enabled
          ? _value.enabled
          : enabled // ignore: cast_nullable_to_non_nullable
              as bool,
      start: null == start
          ? _value.start
          : start // ignore: cast_nullable_to_non_nullable
              as String,
      end: null == end
          ? _value.end
          : end // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$QuietHoursImplCopyWith<$Res>
    implements $QuietHoursCopyWith<$Res> {
  factory _$$QuietHoursImplCopyWith(
          _$QuietHoursImpl value, $Res Function(_$QuietHoursImpl) then) =
      __$$QuietHoursImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({bool enabled, String start, String end});
}

/// @nodoc
class __$$QuietHoursImplCopyWithImpl<$Res>
    extends _$QuietHoursCopyWithImpl<$Res, _$QuietHoursImpl>
    implements _$$QuietHoursImplCopyWith<$Res> {
  __$$QuietHoursImplCopyWithImpl(
      _$QuietHoursImpl _value, $Res Function(_$QuietHoursImpl) _then)
      : super(_value, _then);

  /// Create a copy of QuietHours
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? enabled = null,
    Object? start = null,
    Object? end = null,
  }) {
    return _then(_$QuietHoursImpl(
      enabled: null == enabled
          ? _value.enabled
          : enabled // ignore: cast_nullable_to_non_nullable
              as bool,
      start: null == start
          ? _value.start
          : start // ignore: cast_nullable_to_non_nullable
              as String,
      end: null == end
          ? _value.end
          : end // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$QuietHoursImpl implements _QuietHours {
  const _$QuietHoursImpl(
      {this.enabled = true, this.start = '22:00', this.end = '08:00'});

  factory _$QuietHoursImpl.fromJson(Map<String, dynamic> json) =>
      _$$QuietHoursImplFromJson(json);

  @override
  @JsonKey()
  final bool enabled;
  @override
  @JsonKey()
  final String start;
  @override
  @JsonKey()
  final String end;

  @override
  String toString() {
    return 'QuietHours(enabled: $enabled, start: $start, end: $end)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$QuietHoursImpl &&
            (identical(other.enabled, enabled) || other.enabled == enabled) &&
            (identical(other.start, start) || other.start == start) &&
            (identical(other.end, end) || other.end == end));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, enabled, start, end);

  /// Create a copy of QuietHours
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$QuietHoursImplCopyWith<_$QuietHoursImpl> get copyWith =>
      __$$QuietHoursImplCopyWithImpl<_$QuietHoursImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$QuietHoursImplToJson(
      this,
    );
  }
}

abstract class _QuietHours implements QuietHours {
  const factory _QuietHours(
      {final bool enabled,
      final String start,
      final String end}) = _$QuietHoursImpl;

  factory _QuietHours.fromJson(Map<String, dynamic> json) =
      _$QuietHoursImpl.fromJson;

  @override
  bool get enabled;
  @override
  String get start;
  @override
  String get end;

  /// Create a copy of QuietHours
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$QuietHoursImplCopyWith<_$QuietHoursImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

NotificationsPageData _$NotificationsPageDataFromJson(
    Map<String, dynamic> json) {
  return _NotificationsPageData.fromJson(json);
}

/// @nodoc
mixin _$NotificationsPageData {
  List<AppNotification> get items => throw _privateConstructorUsedError;
  int get unreadCount => throw _privateConstructorUsedError;
  QuietHours get quietHours => throw _privateConstructorUsedError;
  NotificationChannels get channels => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this NotificationsPageData to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $NotificationsPageDataCopyWith<NotificationsPageData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $NotificationsPageDataCopyWith<$Res> {
  factory $NotificationsPageDataCopyWith(NotificationsPageData value,
          $Res Function(NotificationsPageData) then) =
      _$NotificationsPageDataCopyWithImpl<$Res, NotificationsPageData>;
  @useResult
  $Res call(
      {List<AppNotification> items,
      int unreadCount,
      QuietHours quietHours,
      NotificationChannels channels,
      bool isOffline});

  $QuietHoursCopyWith<$Res> get quietHours;
  $NotificationChannelsCopyWith<$Res> get channels;
}

/// @nodoc
class _$NotificationsPageDataCopyWithImpl<$Res,
        $Val extends NotificationsPageData>
    implements $NotificationsPageDataCopyWith<$Res> {
  _$NotificationsPageDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? items = null,
    Object? unreadCount = null,
    Object? quietHours = null,
    Object? channels = null,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      items: null == items
          ? _value.items
          : items // ignore: cast_nullable_to_non_nullable
              as List<AppNotification>,
      unreadCount: null == unreadCount
          ? _value.unreadCount
          : unreadCount // ignore: cast_nullable_to_non_nullable
              as int,
      quietHours: null == quietHours
          ? _value.quietHours
          : quietHours // ignore: cast_nullable_to_non_nullable
              as QuietHours,
      channels: null == channels
          ? _value.channels
          : channels // ignore: cast_nullable_to_non_nullable
              as NotificationChannels,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $QuietHoursCopyWith<$Res> get quietHours {
    return $QuietHoursCopyWith<$Res>(_value.quietHours, (value) {
      return _then(_value.copyWith(quietHours: value) as $Val);
    });
  }

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @pragma('vm:prefer-inline')
  $NotificationChannelsCopyWith<$Res> get channels {
    return $NotificationChannelsCopyWith<$Res>(_value.channels, (value) {
      return _then(_value.copyWith(channels: value) as $Val);
    });
  }
}

/// @nodoc
abstract class _$$NotificationsPageDataImplCopyWith<$Res>
    implements $NotificationsPageDataCopyWith<$Res> {
  factory _$$NotificationsPageDataImplCopyWith(
          _$NotificationsPageDataImpl value,
          $Res Function(_$NotificationsPageDataImpl) then) =
      __$$NotificationsPageDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {List<AppNotification> items,
      int unreadCount,
      QuietHours quietHours,
      NotificationChannels channels,
      bool isOffline});

  @override
  $QuietHoursCopyWith<$Res> get quietHours;
  @override
  $NotificationChannelsCopyWith<$Res> get channels;
}

/// @nodoc
class __$$NotificationsPageDataImplCopyWithImpl<$Res>
    extends _$NotificationsPageDataCopyWithImpl<$Res,
        _$NotificationsPageDataImpl>
    implements _$$NotificationsPageDataImplCopyWith<$Res> {
  __$$NotificationsPageDataImplCopyWithImpl(_$NotificationsPageDataImpl _value,
      $Res Function(_$NotificationsPageDataImpl) _then)
      : super(_value, _then);

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? items = null,
    Object? unreadCount = null,
    Object? quietHours = null,
    Object? channels = null,
    Object? isOffline = null,
  }) {
    return _then(_$NotificationsPageDataImpl(
      items: null == items
          ? _value._items
          : items // ignore: cast_nullable_to_non_nullable
              as List<AppNotification>,
      unreadCount: null == unreadCount
          ? _value.unreadCount
          : unreadCount // ignore: cast_nullable_to_non_nullable
              as int,
      quietHours: null == quietHours
          ? _value.quietHours
          : quietHours // ignore: cast_nullable_to_non_nullable
              as QuietHours,
      channels: null == channels
          ? _value.channels
          : channels // ignore: cast_nullable_to_non_nullable
              as NotificationChannels,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$NotificationsPageDataImpl implements _NotificationsPageData {
  const _$NotificationsPageDataImpl(
      {final List<AppNotification> items = const <AppNotification>[],
      this.unreadCount = 0,
      this.quietHours = const QuietHours(),
      this.channels = const NotificationChannels(),
      this.isOffline = false})
      : _items = items;

  factory _$NotificationsPageDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$NotificationsPageDataImplFromJson(json);

  final List<AppNotification> _items;
  @override
  @JsonKey()
  List<AppNotification> get items {
    if (_items is EqualUnmodifiableListView) return _items;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_items);
  }

  @override
  @JsonKey()
  final int unreadCount;
  @override
  @JsonKey()
  final QuietHours quietHours;
  @override
  @JsonKey()
  final NotificationChannels channels;
  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'NotificationsPageData(items: $items, unreadCount: $unreadCount, quietHours: $quietHours, channels: $channels, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$NotificationsPageDataImpl &&
            const DeepCollectionEquality().equals(other._items, _items) &&
            (identical(other.unreadCount, unreadCount) ||
                other.unreadCount == unreadCount) &&
            (identical(other.quietHours, quietHours) ||
                other.quietHours == quietHours) &&
            (identical(other.channels, channels) ||
                other.channels == channels) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(_items),
      unreadCount,
      quietHours,
      channels,
      isOffline);

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$NotificationsPageDataImplCopyWith<_$NotificationsPageDataImpl>
      get copyWith => __$$NotificationsPageDataImplCopyWithImpl<
          _$NotificationsPageDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$NotificationsPageDataImplToJson(
      this,
    );
  }
}

abstract class _NotificationsPageData implements NotificationsPageData {
  const factory _NotificationsPageData(
      {final List<AppNotification> items,
      final int unreadCount,
      final QuietHours quietHours,
      final NotificationChannels channels,
      final bool isOffline}) = _$NotificationsPageDataImpl;

  factory _NotificationsPageData.fromJson(Map<String, dynamic> json) =
      _$NotificationsPageDataImpl.fromJson;

  @override
  List<AppNotification> get items;
  @override
  int get unreadCount;
  @override
  QuietHours get quietHours;
  @override
  NotificationChannels get channels;
  @override
  bool get isOffline;

  /// Create a copy of NotificationsPageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$NotificationsPageDataImplCopyWith<_$NotificationsPageDataImpl>
      get copyWith => throw _privateConstructorUsedError;
}

ProfileData _$ProfileDataFromJson(Map<String, dynamic> json) {
  return _ProfileData.fromJson(json);
}

/// @nodoc
mixin _$ProfileData {
  String get name => throw _privateConstructorUsedError;
  String get studentNo => throw _privateConstructorUsedError;
  String get department => throw _privateConstructorUsedError;
  String get degree => throw _privateConstructorUsedError;
  String get creditLabel => throw _privateConstructorUsedError;
  int get quotaUsed => throw _privateConstructorUsedError;
  int get quotaTotal => throw _privateConstructorUsedError;
  int get borrowingCount => throw _privateConstructorUsedError;
  int get totalBorrowed => throw _privateConstructorUsedError;
  int get reservationCount => throw _privateConstructorUsedError;
  int get returnedThisTerm => throw _privateConstructorUsedError;
  int get overdueCount => throw _privateConstructorUsedError;
  int get maxRenew => throw _privateConstructorUsedError;
  int get loanDays => throw _privateConstructorUsedError;
  int get unreadCount => throw _privateConstructorUsedError;
  String get version => throw _privateConstructorUsedError;
  bool get blacklisted => throw _privateConstructorUsedError;
  String? get blacklistReason => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this ProfileData to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of ProfileData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $ProfileDataCopyWith<ProfileData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $ProfileDataCopyWith<$Res> {
  factory $ProfileDataCopyWith(
          ProfileData value, $Res Function(ProfileData) then) =
      _$ProfileDataCopyWithImpl<$Res, ProfileData>;
  @useResult
  $Res call(
      {String name,
      String studentNo,
      String department,
      String degree,
      String creditLabel,
      int quotaUsed,
      int quotaTotal,
      int borrowingCount,
      int totalBorrowed,
      int reservationCount,
      int returnedThisTerm,
      int overdueCount,
      int maxRenew,
      int loanDays,
      int unreadCount,
      String version,
      bool blacklisted,
      String? blacklistReason,
      bool isOffline});
}

/// @nodoc
class _$ProfileDataCopyWithImpl<$Res, $Val extends ProfileData>
    implements $ProfileDataCopyWith<$Res> {
  _$ProfileDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of ProfileData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
    Object? studentNo = null,
    Object? department = null,
    Object? degree = null,
    Object? creditLabel = null,
    Object? quotaUsed = null,
    Object? quotaTotal = null,
    Object? borrowingCount = null,
    Object? totalBorrowed = null,
    Object? reservationCount = null,
    Object? returnedThisTerm = null,
    Object? overdueCount = null,
    Object? maxRenew = null,
    Object? loanDays = null,
    Object? unreadCount = null,
    Object? version = null,
    Object? blacklisted = null,
    Object? blacklistReason = freezed,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      studentNo: null == studentNo
          ? _value.studentNo
          : studentNo // ignore: cast_nullable_to_non_nullable
              as String,
      department: null == department
          ? _value.department
          : department // ignore: cast_nullable_to_non_nullable
              as String,
      degree: null == degree
          ? _value.degree
          : degree // ignore: cast_nullable_to_non_nullable
              as String,
      creditLabel: null == creditLabel
          ? _value.creditLabel
          : creditLabel // ignore: cast_nullable_to_non_nullable
              as String,
      quotaUsed: null == quotaUsed
          ? _value.quotaUsed
          : quotaUsed // ignore: cast_nullable_to_non_nullable
              as int,
      quotaTotal: null == quotaTotal
          ? _value.quotaTotal
          : quotaTotal // ignore: cast_nullable_to_non_nullable
              as int,
      borrowingCount: null == borrowingCount
          ? _value.borrowingCount
          : borrowingCount // ignore: cast_nullable_to_non_nullable
              as int,
      totalBorrowed: null == totalBorrowed
          ? _value.totalBorrowed
          : totalBorrowed // ignore: cast_nullable_to_non_nullable
              as int,
      reservationCount: null == reservationCount
          ? _value.reservationCount
          : reservationCount // ignore: cast_nullable_to_non_nullable
              as int,
      returnedThisTerm: null == returnedThisTerm
          ? _value.returnedThisTerm
          : returnedThisTerm // ignore: cast_nullable_to_non_nullable
              as int,
      overdueCount: null == overdueCount
          ? _value.overdueCount
          : overdueCount // ignore: cast_nullable_to_non_nullable
              as int,
      maxRenew: null == maxRenew
          ? _value.maxRenew
          : maxRenew // ignore: cast_nullable_to_non_nullable
              as int,
      loanDays: null == loanDays
          ? _value.loanDays
          : loanDays // ignore: cast_nullable_to_non_nullable
              as int,
      unreadCount: null == unreadCount
          ? _value.unreadCount
          : unreadCount // ignore: cast_nullable_to_non_nullable
              as int,
      version: null == version
          ? _value.version
          : version // ignore: cast_nullable_to_non_nullable
              as String,
      blacklisted: null == blacklisted
          ? _value.blacklisted
          : blacklisted // ignore: cast_nullable_to_non_nullable
              as bool,
      blacklistReason: freezed == blacklistReason
          ? _value.blacklistReason
          : blacklistReason // ignore: cast_nullable_to_non_nullable
              as String?,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$ProfileDataImplCopyWith<$Res>
    implements $ProfileDataCopyWith<$Res> {
  factory _$$ProfileDataImplCopyWith(
          _$ProfileDataImpl value, $Res Function(_$ProfileDataImpl) then) =
      __$$ProfileDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String name,
      String studentNo,
      String department,
      String degree,
      String creditLabel,
      int quotaUsed,
      int quotaTotal,
      int borrowingCount,
      int totalBorrowed,
      int reservationCount,
      int returnedThisTerm,
      int overdueCount,
      int maxRenew,
      int loanDays,
      int unreadCount,
      String version,
      bool blacklisted,
      String? blacklistReason,
      bool isOffline});
}

/// @nodoc
class __$$ProfileDataImplCopyWithImpl<$Res>
    extends _$ProfileDataCopyWithImpl<$Res, _$ProfileDataImpl>
    implements _$$ProfileDataImplCopyWith<$Res> {
  __$$ProfileDataImplCopyWithImpl(
      _$ProfileDataImpl _value, $Res Function(_$ProfileDataImpl) _then)
      : super(_value, _then);

  /// Create a copy of ProfileData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? name = null,
    Object? studentNo = null,
    Object? department = null,
    Object? degree = null,
    Object? creditLabel = null,
    Object? quotaUsed = null,
    Object? quotaTotal = null,
    Object? borrowingCount = null,
    Object? totalBorrowed = null,
    Object? reservationCount = null,
    Object? returnedThisTerm = null,
    Object? overdueCount = null,
    Object? maxRenew = null,
    Object? loanDays = null,
    Object? unreadCount = null,
    Object? version = null,
    Object? blacklisted = null,
    Object? blacklistReason = freezed,
    Object? isOffline = null,
  }) {
    return _then(_$ProfileDataImpl(
      name: null == name
          ? _value.name
          : name // ignore: cast_nullable_to_non_nullable
              as String,
      studentNo: null == studentNo
          ? _value.studentNo
          : studentNo // ignore: cast_nullable_to_non_nullable
              as String,
      department: null == department
          ? _value.department
          : department // ignore: cast_nullable_to_non_nullable
              as String,
      degree: null == degree
          ? _value.degree
          : degree // ignore: cast_nullable_to_non_nullable
              as String,
      creditLabel: null == creditLabel
          ? _value.creditLabel
          : creditLabel // ignore: cast_nullable_to_non_nullable
              as String,
      quotaUsed: null == quotaUsed
          ? _value.quotaUsed
          : quotaUsed // ignore: cast_nullable_to_non_nullable
              as int,
      quotaTotal: null == quotaTotal
          ? _value.quotaTotal
          : quotaTotal // ignore: cast_nullable_to_non_nullable
              as int,
      borrowingCount: null == borrowingCount
          ? _value.borrowingCount
          : borrowingCount // ignore: cast_nullable_to_non_nullable
              as int,
      totalBorrowed: null == totalBorrowed
          ? _value.totalBorrowed
          : totalBorrowed // ignore: cast_nullable_to_non_nullable
              as int,
      reservationCount: null == reservationCount
          ? _value.reservationCount
          : reservationCount // ignore: cast_nullable_to_non_nullable
              as int,
      returnedThisTerm: null == returnedThisTerm
          ? _value.returnedThisTerm
          : returnedThisTerm // ignore: cast_nullable_to_non_nullable
              as int,
      overdueCount: null == overdueCount
          ? _value.overdueCount
          : overdueCount // ignore: cast_nullable_to_non_nullable
              as int,
      maxRenew: null == maxRenew
          ? _value.maxRenew
          : maxRenew // ignore: cast_nullable_to_non_nullable
              as int,
      loanDays: null == loanDays
          ? _value.loanDays
          : loanDays // ignore: cast_nullable_to_non_nullable
              as int,
      unreadCount: null == unreadCount
          ? _value.unreadCount
          : unreadCount // ignore: cast_nullable_to_non_nullable
              as int,
      version: null == version
          ? _value.version
          : version // ignore: cast_nullable_to_non_nullable
              as String,
      blacklisted: null == blacklisted
          ? _value.blacklisted
          : blacklisted // ignore: cast_nullable_to_non_nullable
              as bool,
      blacklistReason: freezed == blacklistReason
          ? _value.blacklistReason
          : blacklistReason // ignore: cast_nullable_to_non_nullable
              as String?,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$ProfileDataImpl implements _ProfileData {
  const _$ProfileDataImpl(
      {required this.name,
      required this.studentNo,
      this.department = '',
      this.degree = '本科',
      this.creditLabel = '信用良好',
      this.quotaUsed = 0,
      this.quotaTotal = 0,
      this.borrowingCount = 0,
      this.totalBorrowed = 0,
      this.reservationCount = 0,
      this.returnedThisTerm = 0,
      this.overdueCount = 0,
      this.maxRenew = 1,
      this.loanDays = 30,
      this.unreadCount = 0,
      this.version = '1.0.0',
      this.blacklisted = false,
      this.blacklistReason,
      this.isOffline = false});

  factory _$ProfileDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$ProfileDataImplFromJson(json);

  @override
  final String name;
  @override
  final String studentNo;
  @override
  @JsonKey()
  final String department;
  @override
  @JsonKey()
  final String degree;
  @override
  @JsonKey()
  final String creditLabel;
  @override
  @JsonKey()
  final int quotaUsed;
  @override
  @JsonKey()
  final int quotaTotal;
  @override
  @JsonKey()
  final int borrowingCount;
  @override
  @JsonKey()
  final int totalBorrowed;
  @override
  @JsonKey()
  final int reservationCount;
  @override
  @JsonKey()
  final int returnedThisTerm;
  @override
  @JsonKey()
  final int overdueCount;
  @override
  @JsonKey()
  final int maxRenew;
  @override
  @JsonKey()
  final int loanDays;
  @override
  @JsonKey()
  final int unreadCount;
  @override
  @JsonKey()
  final String version;
  @override
  @JsonKey()
  final bool blacklisted;
  @override
  final String? blacklistReason;
  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'ProfileData(name: $name, studentNo: $studentNo, department: $department, degree: $degree, creditLabel: $creditLabel, quotaUsed: $quotaUsed, quotaTotal: $quotaTotal, borrowingCount: $borrowingCount, totalBorrowed: $totalBorrowed, reservationCount: $reservationCount, returnedThisTerm: $returnedThisTerm, overdueCount: $overdueCount, maxRenew: $maxRenew, loanDays: $loanDays, unreadCount: $unreadCount, version: $version, blacklisted: $blacklisted, blacklistReason: $blacklistReason, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$ProfileDataImpl &&
            (identical(other.name, name) || other.name == name) &&
            (identical(other.studentNo, studentNo) ||
                other.studentNo == studentNo) &&
            (identical(other.department, department) ||
                other.department == department) &&
            (identical(other.degree, degree) || other.degree == degree) &&
            (identical(other.creditLabel, creditLabel) ||
                other.creditLabel == creditLabel) &&
            (identical(other.quotaUsed, quotaUsed) ||
                other.quotaUsed == quotaUsed) &&
            (identical(other.quotaTotal, quotaTotal) ||
                other.quotaTotal == quotaTotal) &&
            (identical(other.borrowingCount, borrowingCount) ||
                other.borrowingCount == borrowingCount) &&
            (identical(other.totalBorrowed, totalBorrowed) ||
                other.totalBorrowed == totalBorrowed) &&
            (identical(other.reservationCount, reservationCount) ||
                other.reservationCount == reservationCount) &&
            (identical(other.returnedThisTerm, returnedThisTerm) ||
                other.returnedThisTerm == returnedThisTerm) &&
            (identical(other.overdueCount, overdueCount) ||
                other.overdueCount == overdueCount) &&
            (identical(other.maxRenew, maxRenew) ||
                other.maxRenew == maxRenew) &&
            (identical(other.loanDays, loanDays) ||
                other.loanDays == loanDays) &&
            (identical(other.unreadCount, unreadCount) ||
                other.unreadCount == unreadCount) &&
            (identical(other.version, version) || other.version == version) &&
            (identical(other.blacklisted, blacklisted) ||
                other.blacklisted == blacklisted) &&
            (identical(other.blacklistReason, blacklistReason) ||
                other.blacklistReason == blacklistReason) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hashAll([
        runtimeType,
        name,
        studentNo,
        department,
        degree,
        creditLabel,
        quotaUsed,
        quotaTotal,
        borrowingCount,
        totalBorrowed,
        reservationCount,
        returnedThisTerm,
        overdueCount,
        maxRenew,
        loanDays,
        unreadCount,
        version,
        blacklisted,
        blacklistReason,
        isOffline
      ]);

  /// Create a copy of ProfileData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$ProfileDataImplCopyWith<_$ProfileDataImpl> get copyWith =>
      __$$ProfileDataImplCopyWithImpl<_$ProfileDataImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$ProfileDataImplToJson(
      this,
    );
  }
}

abstract class _ProfileData implements ProfileData {
  const factory _ProfileData(
      {required final String name,
      required final String studentNo,
      final String department,
      final String degree,
      final String creditLabel,
      final int quotaUsed,
      final int quotaTotal,
      final int borrowingCount,
      final int totalBorrowed,
      final int reservationCount,
      final int returnedThisTerm,
      final int overdueCount,
      final int maxRenew,
      final int loanDays,
      final int unreadCount,
      final String version,
      final bool blacklisted,
      final String? blacklistReason,
      final bool isOffline}) = _$ProfileDataImpl;

  factory _ProfileData.fromJson(Map<String, dynamic> json) =
      _$ProfileDataImpl.fromJson;

  @override
  String get name;
  @override
  String get studentNo;
  @override
  String get department;
  @override
  String get degree;
  @override
  String get creditLabel;
  @override
  int get quotaUsed;
  @override
  int get quotaTotal;
  @override
  int get borrowingCount;
  @override
  int get totalBorrowed;
  @override
  int get reservationCount;
  @override
  int get returnedThisTerm;
  @override
  int get overdueCount;
  @override
  int get maxRenew;
  @override
  int get loanDays;
  @override
  int get unreadCount;
  @override
  String get version;
  @override
  bool get blacklisted;
  @override
  String? get blacklistReason;
  @override
  bool get isOffline;

  /// Create a copy of ProfileData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$ProfileDataImplCopyWith<_$ProfileDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AppealType _$AppealTypeFromJson(Map<String, dynamic> json) {
  return _AppealType.fromJson(json);
}

/// @nodoc
mixin _$AppealType {
  String get code => throw _privateConstructorUsedError;
  String get label => throw _privateConstructorUsedError;
  String get description => throw _privateConstructorUsedError;

  /// Serializes this AppealType to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AppealType
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AppealTypeCopyWith<AppealType> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppealTypeCopyWith<$Res> {
  factory $AppealTypeCopyWith(
          AppealType value, $Res Function(AppealType) then) =
      _$AppealTypeCopyWithImpl<$Res, AppealType>;
  @useResult
  $Res call({String code, String label, String description});
}

/// @nodoc
class _$AppealTypeCopyWithImpl<$Res, $Val extends AppealType>
    implements $AppealTypeCopyWith<$Res> {
  _$AppealTypeCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AppealType
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? label = null,
    Object? description = null,
  }) {
    return _then(_value.copyWith(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      label: null == label
          ? _value.label
          : label // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AppealTypeImplCopyWith<$Res>
    implements $AppealTypeCopyWith<$Res> {
  factory _$$AppealTypeImplCopyWith(
          _$AppealTypeImpl value, $Res Function(_$AppealTypeImpl) then) =
      __$$AppealTypeImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String code, String label, String description});
}

/// @nodoc
class __$$AppealTypeImplCopyWithImpl<$Res>
    extends _$AppealTypeCopyWithImpl<$Res, _$AppealTypeImpl>
    implements _$$AppealTypeImplCopyWith<$Res> {
  __$$AppealTypeImplCopyWithImpl(
      _$AppealTypeImpl _value, $Res Function(_$AppealTypeImpl) _then)
      : super(_value, _then);

  /// Create a copy of AppealType
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? code = null,
    Object? label = null,
    Object? description = null,
  }) {
    return _then(_$AppealTypeImpl(
      code: null == code
          ? _value.code
          : code // ignore: cast_nullable_to_non_nullable
              as String,
      label: null == label
          ? _value.label
          : label // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AppealTypeImpl implements _AppealType {
  const _$AppealTypeImpl(
      {required this.code, required this.label, this.description = ''});

  factory _$AppealTypeImpl.fromJson(Map<String, dynamic> json) =>
      _$$AppealTypeImplFromJson(json);

  @override
  final String code;
  @override
  final String label;
  @override
  @JsonKey()
  final String description;

  @override
  String toString() {
    return 'AppealType(code: $code, label: $label, description: $description)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AppealTypeImpl &&
            (identical(other.code, code) || other.code == code) &&
            (identical(other.label, label) || other.label == label) &&
            (identical(other.description, description) ||
                other.description == description));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, code, label, description);

  /// Create a copy of AppealType
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AppealTypeImplCopyWith<_$AppealTypeImpl> get copyWith =>
      __$$AppealTypeImplCopyWithImpl<_$AppealTypeImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AppealTypeImplToJson(
      this,
    );
  }
}

abstract class _AppealType implements AppealType {
  const factory _AppealType(
      {required final String code,
      required final String label,
      final String description}) = _$AppealTypeImpl;

  factory _AppealType.fromJson(Map<String, dynamic> json) =
      _$AppealTypeImpl.fromJson;

  @override
  String get code;
  @override
  String get label;
  @override
  String get description;

  /// Create a copy of AppealType
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AppealTypeImplCopyWith<_$AppealTypeImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AppealTicket _$AppealTicketFromJson(Map<String, dynamic> json) {
  return _AppealTicket.fromJson(json);
}

/// @nodoc
mixin _$AppealTicket {
  String get id => throw _privateConstructorUsedError;
  String get ticketNo => throw _privateConstructorUsedError;
  String get type => throw _privateConstructorUsedError;
  String get typeLabel => throw _privateConstructorUsedError;
  String get bookTitle => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  String get statusLabel => throw _privateConstructorUsedError;
  String get description => throw _privateConstructorUsedError;
  String? get rejectReason => throw _privateConstructorUsedError;
  DateTime get createdAt => throw _privateConstructorUsedError;
  DateTime? get slaDueAt => throw _privateConstructorUsedError;
  int? get compensationDays => throw _privateConstructorUsedError;
  String? get assigneeName => throw _privateConstructorUsedError;

  /// Serializes this AppealTicket to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AppealTicket
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AppealTicketCopyWith<AppealTicket> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppealTicketCopyWith<$Res> {
  factory $AppealTicketCopyWith(
          AppealTicket value, $Res Function(AppealTicket) then) =
      _$AppealTicketCopyWithImpl<$Res, AppealTicket>;
  @useResult
  $Res call(
      {String id,
      String ticketNo,
      String type,
      String typeLabel,
      String bookTitle,
      String status,
      String statusLabel,
      String description,
      String? rejectReason,
      DateTime createdAt,
      DateTime? slaDueAt,
      int? compensationDays,
      String? assigneeName});
}

/// @nodoc
class _$AppealTicketCopyWithImpl<$Res, $Val extends AppealTicket>
    implements $AppealTicketCopyWith<$Res> {
  _$AppealTicketCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AppealTicket
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? ticketNo = null,
    Object? type = null,
    Object? typeLabel = null,
    Object? bookTitle = null,
    Object? status = null,
    Object? statusLabel = null,
    Object? description = null,
    Object? rejectReason = freezed,
    Object? createdAt = null,
    Object? slaDueAt = freezed,
    Object? compensationDays = freezed,
    Object? assigneeName = freezed,
  }) {
    return _then(_value.copyWith(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      ticketNo: null == ticketNo
          ? _value.ticketNo
          : ticketNo // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as String,
      typeLabel: null == typeLabel
          ? _value.typeLabel
          : typeLabel // ignore: cast_nullable_to_non_nullable
              as String,
      bookTitle: null == bookTitle
          ? _value.bookTitle
          : bookTitle // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      statusLabel: null == statusLabel
          ? _value.statusLabel
          : statusLabel // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      rejectReason: freezed == rejectReason
          ? _value.rejectReason
          : rejectReason // ignore: cast_nullable_to_non_nullable
              as String?,
      createdAt: null == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      slaDueAt: freezed == slaDueAt
          ? _value.slaDueAt
          : slaDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      compensationDays: freezed == compensationDays
          ? _value.compensationDays
          : compensationDays // ignore: cast_nullable_to_non_nullable
              as int?,
      assigneeName: freezed == assigneeName
          ? _value.assigneeName
          : assigneeName // ignore: cast_nullable_to_non_nullable
              as String?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AppealTicketImplCopyWith<$Res>
    implements $AppealTicketCopyWith<$Res> {
  factory _$$AppealTicketImplCopyWith(
          _$AppealTicketImpl value, $Res Function(_$AppealTicketImpl) then) =
      __$$AppealTicketImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String id,
      String ticketNo,
      String type,
      String typeLabel,
      String bookTitle,
      String status,
      String statusLabel,
      String description,
      String? rejectReason,
      DateTime createdAt,
      DateTime? slaDueAt,
      int? compensationDays,
      String? assigneeName});
}

/// @nodoc
class __$$AppealTicketImplCopyWithImpl<$Res>
    extends _$AppealTicketCopyWithImpl<$Res, _$AppealTicketImpl>
    implements _$$AppealTicketImplCopyWith<$Res> {
  __$$AppealTicketImplCopyWithImpl(
      _$AppealTicketImpl _value, $Res Function(_$AppealTicketImpl) _then)
      : super(_value, _then);

  /// Create a copy of AppealTicket
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? id = null,
    Object? ticketNo = null,
    Object? type = null,
    Object? typeLabel = null,
    Object? bookTitle = null,
    Object? status = null,
    Object? statusLabel = null,
    Object? description = null,
    Object? rejectReason = freezed,
    Object? createdAt = null,
    Object? slaDueAt = freezed,
    Object? compensationDays = freezed,
    Object? assigneeName = freezed,
  }) {
    return _then(_$AppealTicketImpl(
      id: null == id
          ? _value.id
          : id // ignore: cast_nullable_to_non_nullable
              as String,
      ticketNo: null == ticketNo
          ? _value.ticketNo
          : ticketNo // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as String,
      typeLabel: null == typeLabel
          ? _value.typeLabel
          : typeLabel // ignore: cast_nullable_to_non_nullable
              as String,
      bookTitle: null == bookTitle
          ? _value.bookTitle
          : bookTitle // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      statusLabel: null == statusLabel
          ? _value.statusLabel
          : statusLabel // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      rejectReason: freezed == rejectReason
          ? _value.rejectReason
          : rejectReason // ignore: cast_nullable_to_non_nullable
              as String?,
      createdAt: null == createdAt
          ? _value.createdAt
          : createdAt // ignore: cast_nullable_to_non_nullable
              as DateTime,
      slaDueAt: freezed == slaDueAt
          ? _value.slaDueAt
          : slaDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
      compensationDays: freezed == compensationDays
          ? _value.compensationDays
          : compensationDays // ignore: cast_nullable_to_non_nullable
              as int?,
      assigneeName: freezed == assigneeName
          ? _value.assigneeName
          : assigneeName // ignore: cast_nullable_to_non_nullable
              as String?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AppealTicketImpl implements _AppealTicket {
  const _$AppealTicketImpl(
      {required this.id,
      required this.ticketNo,
      required this.type,
      required this.typeLabel,
      this.bookTitle = '',
      this.status = 'OPEN',
      this.statusLabel = '待处理',
      this.description = '',
      this.rejectReason,
      required this.createdAt,
      this.slaDueAt,
      this.compensationDays,
      this.assigneeName});

  factory _$AppealTicketImpl.fromJson(Map<String, dynamic> json) =>
      _$$AppealTicketImplFromJson(json);

  @override
  final String id;
  @override
  final String ticketNo;
  @override
  final String type;
  @override
  final String typeLabel;
  @override
  @JsonKey()
  final String bookTitle;
  @override
  @JsonKey()
  final String status;
  @override
  @JsonKey()
  final String statusLabel;
  @override
  @JsonKey()
  final String description;
  @override
  final String? rejectReason;
  @override
  final DateTime createdAt;
  @override
  final DateTime? slaDueAt;
  @override
  final int? compensationDays;
  @override
  final String? assigneeName;

  @override
  String toString() {
    return 'AppealTicket(id: $id, ticketNo: $ticketNo, type: $type, typeLabel: $typeLabel, bookTitle: $bookTitle, status: $status, statusLabel: $statusLabel, description: $description, rejectReason: $rejectReason, createdAt: $createdAt, slaDueAt: $slaDueAt, compensationDays: $compensationDays, assigneeName: $assigneeName)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AppealTicketImpl &&
            (identical(other.id, id) || other.id == id) &&
            (identical(other.ticketNo, ticketNo) ||
                other.ticketNo == ticketNo) &&
            (identical(other.type, type) || other.type == type) &&
            (identical(other.typeLabel, typeLabel) ||
                other.typeLabel == typeLabel) &&
            (identical(other.bookTitle, bookTitle) ||
                other.bookTitle == bookTitle) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.statusLabel, statusLabel) ||
                other.statusLabel == statusLabel) &&
            (identical(other.description, description) ||
                other.description == description) &&
            (identical(other.rejectReason, rejectReason) ||
                other.rejectReason == rejectReason) &&
            (identical(other.createdAt, createdAt) ||
                other.createdAt == createdAt) &&
            (identical(other.slaDueAt, slaDueAt) ||
                other.slaDueAt == slaDueAt) &&
            (identical(other.compensationDays, compensationDays) ||
                other.compensationDays == compensationDays) &&
            (identical(other.assigneeName, assigneeName) ||
                other.assigneeName == assigneeName));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      id,
      ticketNo,
      type,
      typeLabel,
      bookTitle,
      status,
      statusLabel,
      description,
      rejectReason,
      createdAt,
      slaDueAt,
      compensationDays,
      assigneeName);

  /// Create a copy of AppealTicket
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AppealTicketImplCopyWith<_$AppealTicketImpl> get copyWith =>
      __$$AppealTicketImplCopyWithImpl<_$AppealTicketImpl>(this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AppealTicketImplToJson(
      this,
    );
  }
}

abstract class _AppealTicket implements AppealTicket {
  const factory _AppealTicket(
      {required final String id,
      required final String ticketNo,
      required final String type,
      required final String typeLabel,
      final String bookTitle,
      final String status,
      final String statusLabel,
      final String description,
      final String? rejectReason,
      required final DateTime createdAt,
      final DateTime? slaDueAt,
      final int? compensationDays,
      final String? assigneeName}) = _$AppealTicketImpl;

  factory _AppealTicket.fromJson(Map<String, dynamic> json) =
      _$AppealTicketImpl.fromJson;

  @override
  String get id;
  @override
  String get ticketNo;
  @override
  String get type;
  @override
  String get typeLabel;
  @override
  String get bookTitle;
  @override
  String get status;
  @override
  String get statusLabel;
  @override
  String get description;
  @override
  String? get rejectReason;
  @override
  DateTime get createdAt;
  @override
  DateTime? get slaDueAt;
  @override
  int? get compensationDays;
  @override
  String? get assigneeName;

  /// Create a copy of AppealTicket
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AppealTicketImplCopyWith<_$AppealTicketImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AppealsPageData _$AppealsPageDataFromJson(Map<String, dynamic> json) {
  return _AppealsPageData.fromJson(json);
}

/// @nodoc
mixin _$AppealsPageData {
  List<AppealTicket> get tickets => throw _privateConstructorUsedError;
  int get openCount => throw _privateConstructorUsedError;
  int get doneCount => throw _privateConstructorUsedError;
  int get slaHours => throw _privateConstructorUsedError;
  bool get isOffline => throw _privateConstructorUsedError;

  /// Serializes this AppealsPageData to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AppealsPageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AppealsPageDataCopyWith<AppealsPageData> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppealsPageDataCopyWith<$Res> {
  factory $AppealsPageDataCopyWith(
          AppealsPageData value, $Res Function(AppealsPageData) then) =
      _$AppealsPageDataCopyWithImpl<$Res, AppealsPageData>;
  @useResult
  $Res call(
      {List<AppealTicket> tickets,
      int openCount,
      int doneCount,
      int slaHours,
      bool isOffline});
}

/// @nodoc
class _$AppealsPageDataCopyWithImpl<$Res, $Val extends AppealsPageData>
    implements $AppealsPageDataCopyWith<$Res> {
  _$AppealsPageDataCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AppealsPageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? tickets = null,
    Object? openCount = null,
    Object? doneCount = null,
    Object? slaHours = null,
    Object? isOffline = null,
  }) {
    return _then(_value.copyWith(
      tickets: null == tickets
          ? _value.tickets
          : tickets // ignore: cast_nullable_to_non_nullable
              as List<AppealTicket>,
      openCount: null == openCount
          ? _value.openCount
          : openCount // ignore: cast_nullable_to_non_nullable
              as int,
      doneCount: null == doneCount
          ? _value.doneCount
          : doneCount // ignore: cast_nullable_to_non_nullable
              as int,
      slaHours: null == slaHours
          ? _value.slaHours
          : slaHours // ignore: cast_nullable_to_non_nullable
              as int,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AppealsPageDataImplCopyWith<$Res>
    implements $AppealsPageDataCopyWith<$Res> {
  factory _$$AppealsPageDataImplCopyWith(_$AppealsPageDataImpl value,
          $Res Function(_$AppealsPageDataImpl) then) =
      __$$AppealsPageDataImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {List<AppealTicket> tickets,
      int openCount,
      int doneCount,
      int slaHours,
      bool isOffline});
}

/// @nodoc
class __$$AppealsPageDataImplCopyWithImpl<$Res>
    extends _$AppealsPageDataCopyWithImpl<$Res, _$AppealsPageDataImpl>
    implements _$$AppealsPageDataImplCopyWith<$Res> {
  __$$AppealsPageDataImplCopyWithImpl(
      _$AppealsPageDataImpl _value, $Res Function(_$AppealsPageDataImpl) _then)
      : super(_value, _then);

  /// Create a copy of AppealsPageData
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? tickets = null,
    Object? openCount = null,
    Object? doneCount = null,
    Object? slaHours = null,
    Object? isOffline = null,
  }) {
    return _then(_$AppealsPageDataImpl(
      tickets: null == tickets
          ? _value._tickets
          : tickets // ignore: cast_nullable_to_non_nullable
              as List<AppealTicket>,
      openCount: null == openCount
          ? _value.openCount
          : openCount // ignore: cast_nullable_to_non_nullable
              as int,
      doneCount: null == doneCount
          ? _value.doneCount
          : doneCount // ignore: cast_nullable_to_non_nullable
              as int,
      slaHours: null == slaHours
          ? _value.slaHours
          : slaHours // ignore: cast_nullable_to_non_nullable
              as int,
      isOffline: null == isOffline
          ? _value.isOffline
          : isOffline // ignore: cast_nullable_to_non_nullable
              as bool,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AppealsPageDataImpl implements _AppealsPageData {
  const _$AppealsPageDataImpl(
      {final List<AppealTicket> tickets = const <AppealTicket>[],
      this.openCount = 0,
      this.doneCount = 0,
      this.slaHours = 2,
      this.isOffline = false})
      : _tickets = tickets;

  factory _$AppealsPageDataImpl.fromJson(Map<String, dynamic> json) =>
      _$$AppealsPageDataImplFromJson(json);

  final List<AppealTicket> _tickets;
  @override
  @JsonKey()
  List<AppealTicket> get tickets {
    if (_tickets is EqualUnmodifiableListView) return _tickets;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_tickets);
  }

  @override
  @JsonKey()
  final int openCount;
  @override
  @JsonKey()
  final int doneCount;
  @override
  @JsonKey()
  final int slaHours;
  @override
  @JsonKey()
  final bool isOffline;

  @override
  String toString() {
    return 'AppealsPageData(tickets: $tickets, openCount: $openCount, doneCount: $doneCount, slaHours: $slaHours, isOffline: $isOffline)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AppealsPageDataImpl &&
            const DeepCollectionEquality().equals(other._tickets, _tickets) &&
            (identical(other.openCount, openCount) ||
                other.openCount == openCount) &&
            (identical(other.doneCount, doneCount) ||
                other.doneCount == doneCount) &&
            (identical(other.slaHours, slaHours) ||
                other.slaHours == slaHours) &&
            (identical(other.isOffline, isOffline) ||
                other.isOffline == isOffline));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(
      runtimeType,
      const DeepCollectionEquality().hash(_tickets),
      openCount,
      doneCount,
      slaHours,
      isOffline);

  /// Create a copy of AppealsPageData
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AppealsPageDataImplCopyWith<_$AppealsPageDataImpl> get copyWith =>
      __$$AppealsPageDataImplCopyWithImpl<_$AppealsPageDataImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AppealsPageDataImplToJson(
      this,
    );
  }
}

abstract class _AppealsPageData implements AppealsPageData {
  const factory _AppealsPageData(
      {final List<AppealTicket> tickets,
      final int openCount,
      final int doneCount,
      final int slaHours,
      final bool isOffline}) = _$AppealsPageDataImpl;

  factory _AppealsPageData.fromJson(Map<String, dynamic> json) =
      _$AppealsPageDataImpl.fromJson;

  @override
  List<AppealTicket> get tickets;
  @override
  int get openCount;
  @override
  int get doneCount;
  @override
  int get slaHours;
  @override
  bool get isOffline;

  /// Create a copy of AppealsPageData
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AppealsPageDataImplCopyWith<_$AppealsPageDataImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AppealCreateRequest _$AppealCreateRequestFromJson(Map<String, dynamic> json) {
  return _AppealCreateRequest.fromJson(json);
}

/// @nodoc
mixin _$AppealCreateRequest {
  String get orderId => throw _privateConstructorUsedError;
  String get type => throw _privateConstructorUsedError;
  String get description => throw _privateConstructorUsedError;
  List<String> get attachments => throw _privateConstructorUsedError;

  /// Serializes this AppealCreateRequest to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AppealCreateRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AppealCreateRequestCopyWith<AppealCreateRequest> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppealCreateRequestCopyWith<$Res> {
  factory $AppealCreateRequestCopyWith(
          AppealCreateRequest value, $Res Function(AppealCreateRequest) then) =
      _$AppealCreateRequestCopyWithImpl<$Res, AppealCreateRequest>;
  @useResult
  $Res call(
      {String orderId,
      String type,
      String description,
      List<String> attachments});
}

/// @nodoc
class _$AppealCreateRequestCopyWithImpl<$Res, $Val extends AppealCreateRequest>
    implements $AppealCreateRequestCopyWith<$Res> {
  _$AppealCreateRequestCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AppealCreateRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? orderId = null,
    Object? type = null,
    Object? description = null,
    Object? attachments = null,
  }) {
    return _then(_value.copyWith(
      orderId: null == orderId
          ? _value.orderId
          : orderId // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      attachments: null == attachments
          ? _value.attachments
          : attachments // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AppealCreateRequestImplCopyWith<$Res>
    implements $AppealCreateRequestCopyWith<$Res> {
  factory _$$AppealCreateRequestImplCopyWith(_$AppealCreateRequestImpl value,
          $Res Function(_$AppealCreateRequestImpl) then) =
      __$$AppealCreateRequestImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call(
      {String orderId,
      String type,
      String description,
      List<String> attachments});
}

/// @nodoc
class __$$AppealCreateRequestImplCopyWithImpl<$Res>
    extends _$AppealCreateRequestCopyWithImpl<$Res, _$AppealCreateRequestImpl>
    implements _$$AppealCreateRequestImplCopyWith<$Res> {
  __$$AppealCreateRequestImplCopyWithImpl(_$AppealCreateRequestImpl _value,
      $Res Function(_$AppealCreateRequestImpl) _then)
      : super(_value, _then);

  /// Create a copy of AppealCreateRequest
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? orderId = null,
    Object? type = null,
    Object? description = null,
    Object? attachments = null,
  }) {
    return _then(_$AppealCreateRequestImpl(
      orderId: null == orderId
          ? _value.orderId
          : orderId // ignore: cast_nullable_to_non_nullable
              as String,
      type: null == type
          ? _value.type
          : type // ignore: cast_nullable_to_non_nullable
              as String,
      description: null == description
          ? _value.description
          : description // ignore: cast_nullable_to_non_nullable
              as String,
      attachments: null == attachments
          ? _value._attachments
          : attachments // ignore: cast_nullable_to_non_nullable
              as List<String>,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AppealCreateRequestImpl implements _AppealCreateRequest {
  const _$AppealCreateRequestImpl(
      {required this.orderId,
      required this.type,
      required this.description,
      final List<String> attachments = const <String>[]})
      : _attachments = attachments;

  factory _$AppealCreateRequestImpl.fromJson(Map<String, dynamic> json) =>
      _$$AppealCreateRequestImplFromJson(json);

  @override
  final String orderId;
  @override
  final String type;
  @override
  final String description;
  final List<String> _attachments;
  @override
  @JsonKey()
  List<String> get attachments {
    if (_attachments is EqualUnmodifiableListView) return _attachments;
    // ignore: implicit_dynamic_type
    return EqualUnmodifiableListView(_attachments);
  }

  @override
  String toString() {
    return 'AppealCreateRequest(orderId: $orderId, type: $type, description: $description, attachments: $attachments)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AppealCreateRequestImpl &&
            (identical(other.orderId, orderId) || other.orderId == orderId) &&
            (identical(other.type, type) || other.type == type) &&
            (identical(other.description, description) ||
                other.description == description) &&
            const DeepCollectionEquality()
                .equals(other._attachments, _attachments));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, orderId, type, description,
      const DeepCollectionEquality().hash(_attachments));

  /// Create a copy of AppealCreateRequest
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AppealCreateRequestImplCopyWith<_$AppealCreateRequestImpl> get copyWith =>
      __$$AppealCreateRequestImplCopyWithImpl<_$AppealCreateRequestImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AppealCreateRequestImplToJson(
      this,
    );
  }
}

abstract class _AppealCreateRequest implements AppealCreateRequest {
  const factory _AppealCreateRequest(
      {required final String orderId,
      required final String type,
      required final String description,
      final List<String> attachments}) = _$AppealCreateRequestImpl;

  factory _AppealCreateRequest.fromJson(Map<String, dynamic> json) =
      _$AppealCreateRequestImpl.fromJson;

  @override
  String get orderId;
  @override
  String get type;
  @override
  String get description;
  @override
  List<String> get attachments;

  /// Create a copy of AppealCreateRequest
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AppealCreateRequestImplCopyWith<_$AppealCreateRequestImpl> get copyWith =>
      throw _privateConstructorUsedError;
}

AppealCreateResult _$AppealCreateResultFromJson(Map<String, dynamic> json) {
  return _AppealCreateResult.fromJson(json);
}

/// @nodoc
mixin _$AppealCreateResult {
  String get ticketNo => throw _privateConstructorUsedError;
  String get status => throw _privateConstructorUsedError;
  DateTime? get slaDueAt => throw _privateConstructorUsedError;

  /// Serializes this AppealCreateResult to a JSON map.
  Map<String, dynamic> toJson() => throw _privateConstructorUsedError;

  /// Create a copy of AppealCreateResult
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  $AppealCreateResultCopyWith<AppealCreateResult> get copyWith =>
      throw _privateConstructorUsedError;
}

/// @nodoc
abstract class $AppealCreateResultCopyWith<$Res> {
  factory $AppealCreateResultCopyWith(
          AppealCreateResult value, $Res Function(AppealCreateResult) then) =
      _$AppealCreateResultCopyWithImpl<$Res, AppealCreateResult>;
  @useResult
  $Res call({String ticketNo, String status, DateTime? slaDueAt});
}

/// @nodoc
class _$AppealCreateResultCopyWithImpl<$Res, $Val extends AppealCreateResult>
    implements $AppealCreateResultCopyWith<$Res> {
  _$AppealCreateResultCopyWithImpl(this._value, this._then);

  // ignore: unused_field
  final $Val _value;
  // ignore: unused_field
  final $Res Function($Val) _then;

  /// Create a copy of AppealCreateResult
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ticketNo = null,
    Object? status = null,
    Object? slaDueAt = freezed,
  }) {
    return _then(_value.copyWith(
      ticketNo: null == ticketNo
          ? _value.ticketNo
          : ticketNo // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      slaDueAt: freezed == slaDueAt
          ? _value.slaDueAt
          : slaDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ) as $Val);
  }
}

/// @nodoc
abstract class _$$AppealCreateResultImplCopyWith<$Res>
    implements $AppealCreateResultCopyWith<$Res> {
  factory _$$AppealCreateResultImplCopyWith(_$AppealCreateResultImpl value,
          $Res Function(_$AppealCreateResultImpl) then) =
      __$$AppealCreateResultImplCopyWithImpl<$Res>;
  @override
  @useResult
  $Res call({String ticketNo, String status, DateTime? slaDueAt});
}

/// @nodoc
class __$$AppealCreateResultImplCopyWithImpl<$Res>
    extends _$AppealCreateResultCopyWithImpl<$Res, _$AppealCreateResultImpl>
    implements _$$AppealCreateResultImplCopyWith<$Res> {
  __$$AppealCreateResultImplCopyWithImpl(_$AppealCreateResultImpl _value,
      $Res Function(_$AppealCreateResultImpl) _then)
      : super(_value, _then);

  /// Create a copy of AppealCreateResult
  /// with the given fields replaced by the non-null parameter values.
  @pragma('vm:prefer-inline')
  @override
  $Res call({
    Object? ticketNo = null,
    Object? status = null,
    Object? slaDueAt = freezed,
  }) {
    return _then(_$AppealCreateResultImpl(
      ticketNo: null == ticketNo
          ? _value.ticketNo
          : ticketNo // ignore: cast_nullable_to_non_nullable
              as String,
      status: null == status
          ? _value.status
          : status // ignore: cast_nullable_to_non_nullable
              as String,
      slaDueAt: freezed == slaDueAt
          ? _value.slaDueAt
          : slaDueAt // ignore: cast_nullable_to_non_nullable
              as DateTime?,
    ));
  }
}

/// @nodoc
@JsonSerializable()
class _$AppealCreateResultImpl implements _AppealCreateResult {
  const _$AppealCreateResultImpl(
      {required this.ticketNo, this.status = 'OPEN', this.slaDueAt});

  factory _$AppealCreateResultImpl.fromJson(Map<String, dynamic> json) =>
      _$$AppealCreateResultImplFromJson(json);

  @override
  final String ticketNo;
  @override
  @JsonKey()
  final String status;
  @override
  final DateTime? slaDueAt;

  @override
  String toString() {
    return 'AppealCreateResult(ticketNo: $ticketNo, status: $status, slaDueAt: $slaDueAt)';
  }

  @override
  bool operator ==(Object other) {
    return identical(this, other) ||
        (other.runtimeType == runtimeType &&
            other is _$AppealCreateResultImpl &&
            (identical(other.ticketNo, ticketNo) ||
                other.ticketNo == ticketNo) &&
            (identical(other.status, status) || other.status == status) &&
            (identical(other.slaDueAt, slaDueAt) ||
                other.slaDueAt == slaDueAt));
  }

  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  int get hashCode => Object.hash(runtimeType, ticketNo, status, slaDueAt);

  /// Create a copy of AppealCreateResult
  /// with the given fields replaced by the non-null parameter values.
  @JsonKey(includeFromJson: false, includeToJson: false)
  @override
  @pragma('vm:prefer-inline')
  _$$AppealCreateResultImplCopyWith<_$AppealCreateResultImpl> get copyWith =>
      __$$AppealCreateResultImplCopyWithImpl<_$AppealCreateResultImpl>(
          this, _$identity);

  @override
  Map<String, dynamic> toJson() {
    return _$$AppealCreateResultImplToJson(
      this,
    );
  }
}

abstract class _AppealCreateResult implements AppealCreateResult {
  const factory _AppealCreateResult(
      {required final String ticketNo,
      final String status,
      final DateTime? slaDueAt}) = _$AppealCreateResultImpl;

  factory _AppealCreateResult.fromJson(Map<String, dynamic> json) =
      _$AppealCreateResultImpl.fromJson;

  @override
  String get ticketNo;
  @override
  String get status;
  @override
  DateTime? get slaDueAt;

  /// Create a copy of AppealCreateResult
  /// with the given fields replaced by the non-null parameter values.
  @override
  @JsonKey(includeFromJson: false, includeToJson: false)
  _$$AppealCreateResultImplCopyWith<_$AppealCreateResultImpl> get copyWith =>
      throw _privateConstructorUsedError;
}
