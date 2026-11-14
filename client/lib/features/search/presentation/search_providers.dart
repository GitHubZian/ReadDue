import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/models.dart';
import '../domain/search_repository.dart';

typedef SearchArgs = ({String query, String filter});

final searchRepositoryProvider = Provider<SearchRepository>(
  (ref) => MockSearchRepository(),
);

final searchProvider =
    FutureProvider.autoDispose.family<SearchPageData, SearchArgs>(
  (ref, args) =>
      ref.watch(searchRepositoryProvider).search(args.query, args.filter),
);
