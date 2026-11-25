/// 资料编辑的前端校验；返回 null 表示通过。后端 422 仍以服务端 message 为准。
String? validateProfileName(String value) {
  final name = value.trim();
  if (name.isEmpty) return '请输入姓名';
  if (name.length > 20) return '姓名最多 20 个字符';
  return null;
}

/// 手机号与邮箱为可选项：留空表示不提供（提交空串由服务端清空）。
String? validateProfilePhone(String value) {
  final phone = value.trim();
  if (phone.isEmpty) return null;
  if (!RegExp(r'^1\d{10}$').hasMatch(phone)) return '请输入 1 开头的 11 位手机号';
  return null;
}

String? validateProfileEmail(String value) {
  final email = value.trim();
  if (email.isEmpty) return null;
  if (!email.contains('@') || !email.contains('.')) {
    return '邮箱需包含 @ 与 .';
  }
  return null;
}
