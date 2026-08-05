import 'package:flutter/foundation.dart';

import '../models/approval.dart';
import '../services/approval_service.dart';

class ApprovalController extends ChangeNotifier {
  ApprovalController(this.service);

  final ApprovalService service;

  List<ApprovalSummary> items = [];
  bool loading = false;
  bool processing = false;
  String? error;

  Future<void> loadPending() async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      items = await service.pending();
    } catch (exception) {
      error = exception.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<ApprovalDetail?> process(
    ApprovalDetail detail,
    String action,
    String? opinion,
  ) async {
    if (processing) {
      return null;
    }

    processing = true;
    error = null;
    notifyListeners();

    try {
      final result = await service.process(
        detail.approval.id,
        detail.approval.version,
        action,
        opinion,
      );
      await loadPending();
      return result;
    } catch (exception) {
      error = exception.toString();
      return null;
    } finally {
      processing = false;
      notifyListeners();
    }
  }
}
