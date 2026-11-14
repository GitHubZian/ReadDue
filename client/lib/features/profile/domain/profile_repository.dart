import '../../../core/models/models.dart';

/// C7 我的：身份、额度、入口与退出。
abstract class ProfileRepository {
  Future<ProfileData> fetchProfile();

  Future<void> logout();
}
