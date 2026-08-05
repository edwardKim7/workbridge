INSERT INTO customer (
    business_registration_no, customer_name, representative_name,
    contact_name, contact_phone, contact_email, established_date,
    assigned_sales_id, status, active, created_by, updated_by
) VALUES
    ('0000000001', 'WorkBridge 가상고객 A', '가상대표 A', '샘플담당 A', '02-0000-1001', 'alpha@workbridge.example', '2012-03-15', 2, 'ACTIVE', TRUE, 2, 2),
    ('0000000002', 'WorkBridge 가상고객 B', '가상대표 B', '샘플담당 B', '031-000-2200', 'beta@workbridge.example', '2018-07-02', 2, 'PROSPECT', TRUE, 2, 2),
    ('0000000003', 'WorkBridge 가상고객 C', '가상대표 C', '샘플담당 C', '051-000-3300', 'gamma@workbridge.example', '2009-11-23', 2, 'INACTIVE', FALSE, 2, 2);

INSERT INTO sales_opportunity (
    opportunity_no, customer_id, opportunity_name, stage,
    expected_revenue, expected_close_date, probability, owner_employee_id,
    status, description, created_by, updated_by
) VALUES
    (
        'OPP-00000001',
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000001'),
        '통합 권한관리 고도화', 'PROPOSAL', 180000000.00, '2026-10-30', 60, 2,
        'OPEN', '제안서 검토 및 기술 질의 대응 단계', 2, 2
    ),
    (
        'OPP-00000002',
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000001'),
        '모바일 승인 체계 구축', 'NEGOTIATION', 95000000.00, '2026-09-18', 80, 2,
        'OPEN', '계약 범위와 운영지원 조건 협의 중', 2, 2
    ),
    (
        'OPP-00000003',
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000002'),
        '고객 데이터 정비 컨설팅', 'QUALIFIED', 42000000.00, '2026-12-12', 35, 2,
        'OPEN', '현행 데이터 품질 진단 완료', 2, 2
    ),
    (
        'OPP-00000004',
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000001'),
        '2026 유지보수 계약', 'WON', 72000000.00, '2026-06-30', 100, 2,
        'CLOSED', '최종 계약 체결 완료', 2, 2
    );

INSERT INTO sales_activity (
    customer_id, opportunity_id, employee_id, activity_type,
    subject, activity_at, location, content, created_by, updated_by
) VALUES
    (
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000001'),
        (SELECT opportunity_id FROM sales_opportunity WHERE opportunity_no = 'OPP-00000001'),
        2, 'VISIT', '제안 범위 협의 방문', CURRENT_TIMESTAMP, 'WorkBridge 가상고객 A 회의실',
        '권한 신청·승인·회수 범위와 단계별 오픈 계획을 협의했습니다.', 2, 2
    ),
    (
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000001'),
        (SELECT opportunity_id FROM sales_opportunity WHERE opportunity_no = 'OPP-00000002'),
        2, 'MEETING', '모바일 승인 보안 검토', CURRENT_TIMESTAMP, '온라인 회의',
        '토큰 만료와 단말 분실 시 로그아웃 정책을 확인했습니다.', 2, 2
    ),
    (
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000002'),
        (SELECT opportunity_id FROM sales_opportunity WHERE opportunity_no = 'OPP-00000003'),
        2, 'CALL', '데이터 진단 결과 안내', CURRENT_TIMESTAMP, NULL,
        '중복 사업자번호와 필수값 누락 현황을 공유했습니다.', 2, 2
    ),
    (
        (SELECT customer_id FROM customer WHERE business_registration_no = '0000000001'),
        NULL, 2, 'EMAIL', '정기 운영 리포트 발송', CURRENT_TIMESTAMP, NULL,
        '월간 장애 및 요청 처리 현황을 전달했습니다.', 2, 2
    );
