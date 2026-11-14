import 'package:shared_preferences/shared_preferences.dart';

/// 本地只存 token、身份摘要、偏好摘要与同步时间，不存手机号等敏感字段。
class KvStore {
  KvStore(this._prefs);

  final SharedPreferences _prefs;

  static const _tokenKey = 'auth.token';
  static const _userIdKey = 'auth.userId';
  static const _nameKey = 'auth.name';
  static const _studentNoKey = 'auth.studentNo';
  static const _rememberKey = 'auth.remember';
  static const _lastSyncKey = 'sync.lastAt';
  static const _unreadKey = 'notify.unread';
  static const _quietKey = 'notify.quiet';

  String? get token => _prefs.getString(_tokenKey);

  String? get userId => _prefs.getString(_userIdKey);

  String? get name => _prefs.getString(_nameKey);

  String? get studentNo => _prefs.getString(_studentNoKey);

  bool get rememberMe => _prefs.getBool(_rememberKey) ?? true;

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
    await _prefs.setString(_tokenKey, token);
    await _prefs.setString(_userIdKey, userId);
    await _prefs.setString(_nameKey, name);
    await _prefs.setString(_studentNoKey, studentNo);
    await _prefs.setBool(_rememberKey, remember);
  }

  Future<void> clearSession() async {
    await _prefs.remove(_tokenKey);
    await _prefs.remove(_userIdKey);
    await _prefs.remove(_nameKey);
    await _prefs.remove(_studentNoKey);
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
