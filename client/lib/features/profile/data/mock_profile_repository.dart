import '../../../core/mock/mock_backend.dart';
import '../../../core/models/models.dart';
import '../domain/profile_repository.dart';

class MockProfileRepository implements ProfileRepository {
  MockProfileRepository([MockBackend? backend])
      : _backend = backend ?? MockBackend.instance;

  final MockBackend _backend;

  @override
  Future<ProfileData> fetchProfile() => _backend.fetchProfile();

  @override
  Future<void> logout() async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
  }
}
