import 'package:shared_preferences/shared_preferences.dart';

/// 本地只存 token、身份摘要、偏好摘要与同步时间，不存手机号等敏感字段。
class KvStore {
  KvStore(this._prefs);

  final SharedPreferences _prefs;

  /// 未勾选「记住此设备」时只存在进程内：冷启动后为空，需要重新登录。
  String? _memoryToken;
  String? _memoryUserId;
  String? _memoryName;
  String? _memoryStudentNo;
  bool? _memoryRemember;

  static const _tokenKey = 'auth.token';
  static const _userIdKey = 'auth.userId';
  static const _nameKey = 'auth.name';
  static const _studentNoKey = 'auth.studentNo';
  static const _rememberKey = 'auth.remember';
  static const _lastSyncKey = 'sync.lastAt';
  static const _unreadKey = 'notify.unread';
  static const _quietKey = 'notify.quiet';

  String? get token => _memoryToken ?? _prefs.getString(_tokenKey);

  String? get userId => _memoryUserId ?? _prefs.getString(_userIdKey);

  String? get name => _memoryName ?? _prefs.getString(_nameKey);

  String? get studentNo => _memoryStudentNo ?? _prefs.getString(_studentNoKey);

  bool get rememberMe => _memoryRemember ?? _prefs.getBool(_rememberKey) ?? true;

  DateTime? get lastSyncAt {
    final raw = _prefs.getString(_lastSyncKey);
    return raw == null ? null : DateTime.tryParse(raw);
  }

  int get unreadCount => _prefs.getInt(_unreadKey) ?? 0;

  String get quietHoursText => _prefs.getString(_quietKey) ?? '';

  Future<void> saveSession({
    required String token,
    required String userId,
    required String name,
    required String studentNo,
    bool remember = true,
  }) async {
    if (remember) {
      _memoryToken = null;
      _memoryUserId = null;
      _memoryName = null;
      _memoryStudentNo = null;
      _memoryRemember = null;
      await _prefs.setString(_tokenKey, token);
      await _prefs.setString(_userIdKey, userId);
      await _prefs.setString(_nameKey, name);
      await _prefs.setString(_studentNoKey, studentNo);
      await _prefs.setBool(_rememberKey, true);
      return;
    }
    // 未勾选「记住此设备」：只留进程内，并清掉上一次可能记住的会话。
    _memoryToken = token;
    _memoryUserId = userId;
    _memoryName = name;
    _memoryStudentNo = studentNo;
    _memoryRemember = false;
    await _clearPersistedSession();
  }

  Future<void> clearSession() async {
    _memoryToken = null;
    _memoryUserId = null;
    _memoryName = null;
    _memoryStudentNo = null;
    _memoryRemember = null;
    await _clearPersistedSession();
  }

  Future<void> _clearPersistedSession() async {
    await _prefs.remove(_tokenKey);
    await _prefs.remove(_userIdKey);
    await _prefs.remove(_nameKey);
    await _prefs.remove(_studentNoKey);
    await _prefs.remove(_rememberKey);
  }

  Future<void> setLastSyncAt(DateTime value) async {
    await _prefs.setString(_lastSyncKey, value.toIso8601String());
  }

  Future<void> saveUnread(int value) async {
    await _prefs.setInt(_unreadKey, value);
  }

  Future<void> saveQuietHours(String text) async {
    await _prefs.setString(_quietKey, text);
  }
}
