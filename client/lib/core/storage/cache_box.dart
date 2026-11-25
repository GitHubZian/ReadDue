import 'dart:convert';

import 'package:hive_flutter/hive_flutter.dart';

/// C1 / C6 的 stale-while-revalidate 缓存。
class CacheBox {
  static const _boxName = 'reader_cache';

  /// 测试注入已打开的盒子；生产路径仍走 [init]。
  CacheBox.withBox(Box<String> box) : _box = box;

  CacheBox();

  Box<String>? _box;

  Future<void> init() async {
    await Hive.initFlutter();
    _box = await Hive.openBox<String>(_boxName);
  }

  dynamic read(String key) {
    final raw = _box?.get(key);
    if (raw == null) return null;
    try {
      return jsonDecode(raw);
    } on FormatException {
      return null;
    }
  }

  Future<void> write(String key, Object? value) async {
    if (value == null) return;
    await _box?.put(key, jsonEncode(value));
  }

  Future<void> clear() async {
    await _box?.clear();
  }
}
