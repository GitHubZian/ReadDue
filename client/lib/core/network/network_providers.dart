import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../storage/storage_providers.dart';
import 'api_client.dart';

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(kv: ref.watch(kvStoreProvider)),
);
