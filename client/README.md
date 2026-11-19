# 阅有期 · 读者端（Flutter）

校园图书借阅的读者端界面，覆盖登录、我的借阅、借阅详情、宽限申请、馆藏检索、预约排队、通知、我的、申诉九屏。

## 环境

- Flutter 3.x（Material 3），Dart SDK `^3.5.3`
- Android SDK（构建 APK）；iOS 需 macOS

## 运行

```bash
flutter pub get

# 连接已启动的后端（端口 8125，context-path /api；注意路径不带 /v1）
flutter run \
  --dart-define=USE_MOCK=false \
  --dart-define=API_BASE_URL=http://10.0.2.2:8125/api

# 纯界面验收：强制走本地 mock（固定“现在”为 2026-04-16 19:00）
flutter run --dart-define=USE_MOCK=true
```

`10.0.2.2` 是 Android 模拟器访问宿主机的地址；真机请改成电脑在局域网内的 IP（例如 `192.168.123.115`）。

> `USE_MOCK` 默认 `false`，登录、借阅、详情、宽限、检索、预约、通知、我的、申诉全部走真实接口；mock 仅作为离线演示开关保留在 `features/*/presentation/*_providers.dart`。
>
> 种子账号：`2021001` / `123456`（陈昊）；拉黑账号 `2021005`。登录态失效（401）会自动清除本地 token 并回到登录页。

## 质量门

```bash
dart format --set-exit-if-changed lib test
flutter analyze          # 0 warning
flutter test             # 26 条：每屏正常/错误 + 拉黑、404、幂等键、返回与表单校验
flutter build apk --debug
```

## 真机安装包

```bash
flutter build apk --release \
  --dart-define=USE_MOCK=false \
  --dart-define=API_BASE_URL=http://192.168.123.115:8125/api
```

产物 `build/app/outputs/flutter-apk/app-release.apk`（约 23.5 MB）。手机与电脑需在同一局域网；首次连接时 Windows 防火墙若弹窗，请允许 Java 的 8125 入站，否则登录会提示网络不可用。

## 目录

```
lib/
  app.dart                 MaterialApp.router
  main.dart                SharedPreferences / Hive 初始化与登录态恢复
  core/
    theme/                 tokens、排版、主题（颜色唯一来源）
    widgets/               AppCard、StatusPill、SummaryBlock、四态等
    network/               ApiClient（Bearer / X-Trace-Id / Idempotency-Key）
    models/                freezed + json_serializable 模型
    mock/                  单例假数据源
    router/                go_router 与登录守卫
  features/
    auth borrow order_detail grace search reservation notifications profile appeal
```

时间展示统一 `YYYY-MM-DD HH:mm`；ID 全部按 `String` 处理，金额为 `¥0.00` 字符串；金额、宽限天数与 SLA 截止时间均取服务端字段，客户端不重算。

## 测试

```bash
flutter test
flutter test test/features/borrow/borrow_page_test.dart   # 单屏
```

页面测试使用 `test/support/harness.dart`：真实主题 + 内存 KV + 已恢复登录态；错误态通过覆盖对应 `*RepositoryProvider` 注入失败实现。

## 在 Android 设备管理器中查看

```bash
flutter devices          # 列出已连接设备/模拟器
flutter emulators        # 列出本机 AVD
flutter emulators --launch <id>
flutter run -d <device-id>
```

Android Studio 的 Device Manager（设备管理器）中启动模拟器后，`flutter run` 会自动复用该设备；运行日志与界面即出现在设备管理器窗口。
