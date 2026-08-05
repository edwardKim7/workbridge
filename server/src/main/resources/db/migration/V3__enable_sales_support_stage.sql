INSERT INTO menu (
    menu_id, parent_menu_id, menu_code, menu_name, menu_path, sort_order,
    active, created_by, updated_by
) VALUES (
    11, 2, 'SALES_ACTIVITY', '영업활동', '/web/activities', 23,
    TRUE, 1, 1
);

INSERT INTO role_menu (role_id, menu_id, can_read, can_write, created_by) VALUES
    (1, 11, TRUE, TRUE, 1),
    (2, 11, TRUE, TRUE, 1),
    (4, 11, TRUE, FALSE, 1);

-- V2에서 APPROVER가 영업기회 하위 메뉴만 보유하던 초기 데이터를 부모 메뉴 규칙에 맞게 보완합니다.
INSERT INTO role_menu (role_id, menu_id, can_read, can_write, created_by)
SELECT 3, 2, TRUE, FALSE, 1
WHERE NOT EXISTS (
    SELECT 1 FROM role_menu WHERE role_id = 3 AND menu_id = 2
);

ALTER TABLE customer
    ADD CONSTRAINT chk_customer_active_status
    CHECK (
        (status = 'INACTIVE' AND active = FALSE)
        OR (status IN ('PROSPECT', 'ACTIVE') AND active = TRUE)
    );

ALTER TABLE sales_opportunity
    ADD CONSTRAINT chk_opportunity_stage_consistency
    CHECK (
        (stage = 'WON' AND status = 'CLOSED' AND probability = 100)
        OR (stage = 'LOST' AND status = 'CLOSED' AND probability = 0)
        OR (
            stage IN ('LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION')
            AND status = 'OPEN'
            AND probability BETWEEN 0 AND 99
        )
    );

CREATE INDEX idx_opportunity_name ON sales_opportunity (opportunity_name);
CREATE INDEX idx_activity_at ON sales_activity (activity_at, activity_id);
