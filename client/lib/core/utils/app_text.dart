/// 封面短名：按 rune 取前两个字，避免把 emoji/代理对截断。
String coverLabelOf(String title) {
  final text = title.trim();
  if (text.isEmpty) return '书';
  final runes = text.runes.toList();
  return String.fromCharCodes(runes.take(2));
}
