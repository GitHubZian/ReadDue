import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_env.dart';
import '../../../core/models/models.dart';
import '../../../core/network/network_providers.dart';
import '../data/dio_search_repository.dart';
import '../domain/search_repository.dart';

typedef SearchArgs = ({String query, String filter});

final searchRepositoryProvider = Provider<SearchRepository>(
  (ref) => ref.watch(appUseMockProvider)
      ? MockSearchRepository()
      : DioSearchRepository(ref.watch(apiClientProvider)),
);

final searchProvider =
    FutureProvider.autoDispose.family<SearchPageData, SearchArgs>(
  (ref, args) =>
      ref.watch(searchRepositoryProvider).search(args.query, args.filter),
);

final bookDetailProvider =
    FutureProvider.autoDispose.family<BookDetailData, String>(
  (ref, id) => ref.watch(searchRepositoryProvider).fetchDetail(id),
);
