INSERT INTO department (
    department_id, department_code, department_name, parent_department_id, active, created_by, updated_by
) VALUES
    (1, 'HQ', '본사', NULL, TRUE, NULL, NULL);

INSERT INTO department (
    department_id, department_code, department_name, parent_department_id, active, created_by, updated_by
) VALUES
    (2, 'SALES', '영업팀', 1, TRUE, NULL, NULL),
    (3, 'MANAGEMENT', '경영지원팀', 1, TRUE, NULL, NULL),
    (4, 'IT', 'IT운영팀', 1, TRUE, NULL, NULL);

INSERT INTO `role` (
    role_id, role_code, role_name, description, active, created_by, updated_by
) VALUES
    (1, 'ADMIN', '관리자', '조직·권한·이관·인터페이스 운영 권한', TRUE, NULL, NULL),
    (2, 'SALES', '영업 담당자', '고객·영업기회·영업활동·결재 요청 권한', TRUE, NULL, NULL),
    (3, 'APPROVER', '승인자', '결재 조회·승인·반려 권한', TRUE, NULL, NULL),
    (4, 'VIEWER', '조회 사용자', '업무 데이터 조회 전용 권한', TRUE, NULL, NULL);

INSERT INTO employee (
    employee_id, employee_no, login_id, password_hash, employee_name, email,
    department_id, active, login_fail_count, created_by, updated_by
) VALUES
    (1, 'WB-0001', 'admin', '$2y$10$kxIhQFtMMcXM6G4SWmvv6uN0m3ULIwUBCK.Sbg8jw71Ed6TFrgRGW', '관리자', 'admin@workbridge.example', 4, TRUE, 0, NULL, NULL),
    (2, 'WB-1001', 'sales', '$2y$10$67rDtEYyQoto/T19ITZi3u/dovQpaSCMyTNZE5yVuPUsX/em7wBTC', '김영업', 'sales@workbridge.example', 2, TRUE, 0, 1, 1),
    (3, 'WB-2001', 'approver', '$2y$10$NstVtmu0xWZ9BxzpORIRX.XrbV8jNOwh5bVGJYABJw.JkKn5cOpRi', '박승인', 'approver@workbridge.example', 3, TRUE, 0, 1, 1),
    (4, 'WB-3001', 'viewer', '$2y$10$ctVImBdP6HR1W0k73jmdhep3wkwRaXrlc42Jvrpo32z0ZdjOGN2Ua', '이조회', 'viewer@workbridge.example', 1, TRUE, 0, 1, 1);

INSERT INTO employee_role (employee_id, role_id, assigned_by) VALUES
    (1, 1, 1),
    (2, 2, 1),
    (3, 3, 1),
    (4, 4, 1);

INSERT INTO menu (
    menu_id, parent_menu_id, menu_code, menu_name, menu_path, sort_order, active, created_by, updated_by
) VALUES
    (1, NULL, 'DASHBOARD', '대시보드', '/web/dashboard', 10, TRUE, 1, 1),
    (2, NULL, 'SALES_SUPPORT', '영업지원', NULL, 20, TRUE, 1, 1),
    (5, NULL, 'APPROVAL', '전자결재', '/web/approvals', 30, TRUE, 1, 1),
    (6, NULL, 'ADMINISTRATION', '관리', NULL, 40, TRUE, 1, 1);

INSERT INTO menu (
    menu_id, parent_menu_id, menu_code, menu_name, menu_path, sort_order, active, created_by, updated_by
) VALUES
    (3, 2, 'CUSTOMER', '고객 관리', '/web/customers', 21, TRUE, 1, 1),
    (4, 2, 'OPPORTUNITY', '영업기회', '/web/opportunities', 22, TRUE, 1, 1),
    (7, 6, 'EMPLOYEE_ROLE', '사용자·권한', '/web/admin/employees', 41, TRUE, 1, 1),
    (8, 6, 'INTERFACE_OPERATION', '인터페이스 운영', '/web/admin/interfaces', 42, TRUE, 1, 1),
    (9, 6, 'DATA_MIGRATION', '데이터 이관', '/web/admin/migrations', 43, TRUE, 1, 1),
    (10, 6, 'SYSTEM_OPERATION', '시스템 운영', '/web/admin/operations', 44, TRUE, 1, 1);

INSERT INTO role_menu (role_id, menu_id, can_read, can_write, created_by) VALUES
    (1, 1, TRUE, TRUE, 1), (1, 2, TRUE, TRUE, 1), (1, 3, TRUE, TRUE, 1),
    (1, 4, TRUE, TRUE, 1), (1, 5, TRUE, TRUE, 1), (1, 6, TRUE, TRUE, 1),
    (1, 7, TRUE, TRUE, 1), (1, 8, TRUE, TRUE, 1), (1, 9, TRUE, TRUE, 1),
    (1, 10, TRUE, TRUE, 1),
    (2, 1, TRUE, FALSE, 1), (2, 2, TRUE, TRUE, 1), (2, 3, TRUE, TRUE, 1),
    (2, 4, TRUE, TRUE, 1), (2, 5, TRUE, TRUE, 1),
    (3, 1, TRUE, FALSE, 1), (3, 4, TRUE, FALSE, 1), (3, 5, TRUE, TRUE, 1),
    (4, 1, TRUE, FALSE, 1), (4, 2, TRUE, FALSE, 1), (4, 3, TRUE, FALSE, 1),
    (4, 4, TRUE, FALSE, 1), (4, 5, TRUE, FALSE, 1);
