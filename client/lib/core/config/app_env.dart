import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app_config.dart';

/// 运行环境开关。生产默认连真实后端；测试通过 override 强制走 mock。
final appUseMockProvider = Provider<bool>((_) => AppConfig.useMock);
