-- 전자결재 데모 데이터와 모바일 토큰 조회 인덱스
CREATE INDEX idx_refresh_token_active_hash ON refresh_token (token_hash, revoked_at, expires_at);

INSERT INTO approval_request (
    approval_no, approval_type, business_type, business_id,
    requester_id, approver_id, title, request_payload, status,
    requested_at, processed_at, created_by, updated_by, version
) VALUES
(
    'APR-DEMO-0001', 'SALES_EXPENSE', 'OPPORTUNITY',
    (SELECT opportunity_id FROM sales_opportunity WHERE opportunity_no='OPP-00000001'),
    2, 3, '제안 협의 출장비 결재',
    '{"amount":180000,"expenseCategory":"출장비","visitDate":null,"discountRate":null,"description":"고객 제안 범위 협의를 위한 출장비"}',
    'REQUESTED', CURRENT_TIMESTAMP, NULL, 2, 2, 1
),
(
    'APR-DEMO-0002', 'VISIT_PLAN', 'OPPORTUNITY',
    (SELECT opportunity_id FROM sales_opportunity WHERE opportunity_no='OPP-00000002'),
    2, 3, '모바일 승인 보안 검토 방문 계획',
    '{"amount":null,"expenseCategory":null,"visitDate":"2026-08-20","discountRate":null,"description":"고객 보안 담당자 방문"}',
    'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 3, 2
),
(
    'APR-DEMO-0003', 'DISCOUNT_EXCEPTION', 'OPPORTUNITY',
    (SELECT opportunity_id FROM sales_opportunity WHERE opportunity_no='OPP-00000003'),
    2, 3, '데이터 정비 컨설팅 할인율 예외',
    '{"amount":null,"expenseCategory":null,"visitDate":null,"discountRate":12.50,"description":"초기 진단 범위 확대에 따른 예외 할인"}',
    'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 3, 2
);

INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, NULL, 'DRAFT', 'CREATED', requester_id, NULL, 'SEED-APR-0001', created_at FROM approval_request WHERE approval_no='APR-DEMO-0001';
INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, 'DRAFT', 'REQUESTED', 'REQUESTED', requester_id, '검토 부탁드립니다.', 'SEED-APR-0001', requested_at FROM approval_request WHERE approval_no='APR-DEMO-0001';

INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, NULL, 'DRAFT', 'CREATED', requester_id, NULL, 'SEED-APR-0002', created_at FROM approval_request WHERE approval_no='APR-DEMO-0002';
INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, 'DRAFT', 'REQUESTED', 'REQUESTED', requester_id, NULL, 'SEED-APR-0002', requested_at FROM approval_request WHERE approval_no='APR-DEMO-0002';
INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, 'REQUESTED', 'APPROVED', 'APPROVED', approver_id, '방문 목적과 일정 확인 완료', 'SEED-APR-0002', processed_at FROM approval_request WHERE approval_no='APR-DEMO-0002';

INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, NULL, 'DRAFT', 'CREATED', requester_id, NULL, 'SEED-APR-0003', created_at FROM approval_request WHERE approval_no='APR-DEMO-0003';
INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, 'DRAFT', 'REQUESTED', 'REQUESTED', requester_id, NULL, 'SEED-APR-0003', requested_at FROM approval_request WHERE approval_no='APR-DEMO-0003';
INSERT INTO approval_history (approval_id, from_status, to_status, action, actor_id, opinion, trace_id, acted_at)
SELECT approval_id, 'REQUESTED', 'REJECTED', 'REJECTED', approver_id, '할인 근거와 고객 요청 자료를 보완해 주세요.', 'SEED-APR-0003', processed_at FROM approval_request WHERE approval_no='APR-DEMO-0003';
