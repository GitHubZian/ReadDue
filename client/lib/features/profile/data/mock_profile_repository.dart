import '../../../core/mock/mock_backend.dart';
import '../../../core/models/models.dart';
import '../domain/profile_repository.dart';

class MockProfileRepository implements ProfileRepository {
  MockProfileRepository([MockBackend? backend])
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  ProfileContact _contact = const ProfileContact(
    name: '林知远',
    phone: '13800138000',
    email: 'lin.zhiyuan@example.edu.cn',
  );

  @override
  Future<ProfileData> fetchProfile() => _backend.fetchProfile();

  @override
  Future<ProfileContact> fetchContact() async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
    return _contact;
  }

  @override
  Future<ProfileContact> updateProfile({
    required String name,
    required String phone,
    required String email,
  }) async {
    await Future<void>.delayed(const Duration(milliseconds: 160));
    _contact = ProfileContact(name: name, phone: phone, email: email);
    return _contact;
  }

  @override
  Future<void> logout() async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
  }
}
