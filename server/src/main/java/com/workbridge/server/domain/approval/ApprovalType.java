package com.workbridge.server.domain.approval;

public enum ApprovalType {
    SALES_EXPENSE("영업비용 결재"), VISIT_PLAN("고객 방문 계획 승인"), DISCOUNT_EXCEPTION("할인율 예외 승인");
    private final String displayName;
    ApprovalType(String displayName){this.displayName=displayName;}
    public String getDisplayName(){return displayName;}
}
