import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app.dart';
import 'core/storage/cache_box.dart';
import 'core/storage/kv_store.dart';
import 'core/storage/storage_providers.dart';
import 'features/auth/presentation/auth_controller.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final prefs = await SharedPreferences.getInstance();
  final kvStore = KvStore(prefs);
  final cacheBox = CacheBox();
  await cacheBox.init();

  final container = ProviderContainer(
    overrides: [
      kvStoreProvider.overrideWithValue(kvStore),
      cacheBoxProvider.overrideWithValue(cacheBox),
    ],
  );
  await container.read(authControllerProvider.notifier).restore();

  runApp(
    UncontrolledProviderScope(
      container: container,
      child: const ReadDueApp(),
    ),
  );
}
