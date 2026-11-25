import '../../../core/models/models.dart';

/// 资料编辑用的联系方式（PUT /v1/me 与 GET /v1/me 同结构）。
class ProfileContact {
  const ProfileContact({
    required this.name,
    this.phone = '',
    this.email = '',
  });

  factory ProfileContact.fromJson(Map<String, dynamic> json) => ProfileContact(
        name: (json['name'] ?? '').toString(),
        phone: (json['phone'] ?? '').toString(),
        email: (json['email'] ?? '').toString(),
      );

  final String name;
  final String phone;
  final String email;

  Map<String, dynamic> toJson() => {
        'name': name,
        'phone': phone,
        'email': email,
      };
}

/// C7 我的：身份、额度、入口与退出。
abstract class ProfileRepository {
  Future<ProfileData> fetchProfile();

  Future<ProfileContact> fetchContact();

  Future<ProfileContact> updateProfile({
    required String name,
    required String phone,
    required String email,
  });

  Future<void> logout();
}
