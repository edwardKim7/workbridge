class ApprovalSummary {
  const ApprovalSummary({
    required this.id,
    required this.no,
    required this.title,
    required this.type,
    required this.status,
    required this.requesterName,
    required this.approverName,
    required this.version,
    this.requestedAt,
  });

  final int id;
  final String no;
  final String title;
  final String type;
  final String status;
  final String requesterName;
  final String approverName;
  final int version;
  final DateTime? requestedAt;

  factory ApprovalSummary.fromJson(Map<String, dynamic> json) {
    return ApprovalSummary(
      id: (json['approvalId'] as num).toInt(),
      no: json['approvalNo'] as String,
      title: json['title'] as String,
      type: json['approvalType'] as String,
      status: json['status'] as String,
      requesterName: json['requesterName'] as String,
      approverName: json['approverName'] as String,
      version: (json['version'] as num).toInt(),
      requestedAt: json['requestedAt'] == null
          ? null
          : DateTime.parse(json['requestedAt'] as String),
    );
  }
}

class ApprovalHistory {
  const ApprovalHistory({
    required this.action,
    required this.actorName,
    required this.toStatus,
    required this.actedAt,
    this.opinion,
  });

  final String action;
  final String actorName;
  final String toStatus;
  final DateTime actedAt;
  final String? opinion;

  factory ApprovalHistory.fromJson(Map<String, dynamic> json) {
    return ApprovalHistory(
      action: json['action'] as String,
      actorName: json['actorName'] as String,
      toStatus: json['toStatus'] as String,
      actedAt: DateTime.parse(json['actedAt'] as String),
      opinion: json['opinion'] as String?,
    );
  }
}

class ApprovalDetail {
  const ApprovalDetail({
    required this.approval,
    required this.histories,
    required this.canApprove,
    required this.canReject,
  });

  final ApprovalSummary approval;
  final List<ApprovalHistory> histories;
  final bool canApprove;
  final bool canReject;

  factory ApprovalDetail.fromJson(Map<String, dynamic> json) {
    return ApprovalDetail(
      approval: ApprovalSummary.fromJson(
        json['approval'] as Map<String, dynamic>,
      ),
      histories: (json['histories'] as List<dynamic>? ?? const [])
          .map(
            (item) => ApprovalHistory.fromJson(
              item as Map<String, dynamic>,
            ),
          )
          .toList(),
      canApprove: json['canApprove'] as bool? ?? false,
      canReject: json['canReject'] as bool? ?? false,
    );
  }
}
