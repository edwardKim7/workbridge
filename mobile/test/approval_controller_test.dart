import 'package:flutter_test/flutter_test.dart';
import 'package:workbridge_mobile/models/approval.dart';
import 'package:workbridge_mobile/services/approval_service.dart';
import 'package:workbridge_mobile/state/approval_controller.dart';

class FakeApprovalService implements ApprovalService {
  int calls = 0;

  @override
  dynamic noSuchMethod(Invocation invocation) {
    return super.noSuchMethod(invocation);
  }

  @override
  Future<ApprovalDetail> process(
    int id,
    int version,
    String action,
    String? opinion,
  ) async {
    calls++;
    await Future<void>.delayed(const Duration(milliseconds: 20));
    return approvalDetail;
  }

  @override
  Future<List<ApprovalSummary>> pending() async => [];
}

final approvalSummary = ApprovalSummary(
  id: 1,
  no: 'APR',
  title: '결재',
  type: 'SALES_EXPENSE',
  status: 'REQUESTED',
  requesterName: 'A',
  approverName: 'B',
  version: 1,
);

final approvalDetail = ApprovalDetail(
  approval: approvalSummary,
  histories: const [],
  canApprove: true,
  canReject: true,
);

void main() {
  test('승인 처리 중 두 번째 호출을 차단한다', () async {
    final service = FakeApprovalService();
    final controller = ApprovalController(service);

    final first = controller.process(approvalDetail, 'approve', null);
    final second = controller.process(approvalDetail, 'approve', null);

    await Future.wait([first, second]);
    expect(service.calls, 1);
  });
}
