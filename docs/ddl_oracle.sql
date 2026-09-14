-- ==========================================
-- 기존 테이블 및 제약조건 삭제 (초기화용)
-- ==========================================
DROP TABLE reservation CASCADE CONSTRAINTS;
DROP TABLE com_resource CASCADE CONSTRAINTS;
DROP TABLE notice CASCADE CONSTRAINTS;
DROP TABLE task CASCADE CONSTRAINTS;
DROP TABLE project_member CASCADE CONSTRAINTS;
DROP TABLE project CASCADE CONSTRAINTS; 
DROP TABLE appr_line CASCADE CONSTRAINTS;
DROP TABLE appr_doc CASCADE CONSTRAINTS;
DROP TABLE appr_form CASCADE CONSTRAINTS;
DROP TABLE emp_auth CASCADE CONSTRAINTS;
DROP TABLE authority CASCADE CONSTRAINTS;
DROP TABLE employee CASCADE CONSTRAINTS;
DROP TABLE emp_position CASCADE CONSTRAINTS;
DROP TABLE department CASCADE CONSTRAINTS;
DROP TABLE company CASCADE CONSTRAINTS;

DROP TABLE evaluation_ai_report CASCADE CONSTRAINTS;
DROP TABLE performance_evaluation CASCADE CONSTRAINTS;
DROP TABLE evaluation_period CASCADE CONSTRAINTS;

DROP TABLE dept_transfer_log CASCADE CONSTRAINTS;


-- 기존 시퀀스 삭제 (필요 시 주석 해제 후 사용)
DROP SEQUENCE seq_company;
DROP SEQUENCE seq_department;
DROP SEQUENCE seq_position;
DROP SEQUENCE seq_employee;
DROP SEQUENCE seq_authority;
DROP SEQUENCE seq_emp_auth;
DROP SEQUENCE appr_form_seq;
DROP SEQUENCE appr_doc_seq;
DROP SEQUENCE appr_line_seq;
DROP SEQUENCE seq_project;
DROP SEQUENCE seq_project_member;
DROP SEQUENCE seq_task;
DROP SEQUENCE seq_notice;
DROP SEQUENCE seq_com_resource;
DROP SEQUENCE seq_reservation;

DROP SEQUENCE seq_period;
DROP SEQUENCE seq_eval;
DROP SEQUENCE seq_report;

DROP SEQUENCE seq_dept_transfer_log;

-- 기존 트리거 삭제 (필요 시 주석 해제 후 사용)
DROP TRIGGER trg_period_updated;
DROP TRIGGER trg_period_pk;
DROP TRIGGER trg_eval_updated;
DROP TRIGGER trg_eval_pk;
DROP TRIGGER trg_report_updated;
DROP TRIGGER trg_report_pk;



-- ==========================================
-- 1. 회사 (company)
-- ==========================================
CREATE TABLE company (
  com_id            NUMBER NOT NULL,
  industry_grp_code VARCHAR2(100) NOT NULL,
  industry_code     VARCHAR2(100) NOT NULL,
  com_name          VARCHAR2(100) NOT NULL,
  com_ceo           VARCHAR2(100) NOT NULL,
  biz_no            VARCHAR2(45) NOT NULL,
  com_tel           VARCHAR2(100),
  com_logo          VARCHAR2(500),
  created_at        DATE DEFAULT SYSDATE NOT NULL,
  updated_at        DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_company PRIMARY KEY (com_id),
  CONSTRAINT uq_company_biz_no UNIQUE (biz_no)
);
 
CREATE SEQUENCE seq_company START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER trg_company_bi
BEFORE INSERT ON company
FOR EACH ROW
BEGIN
  IF :NEW.com_id IS NULL THEN
    SELECT seq_company.NEXTVAL INTO :NEW.com_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_company_bu
BEFORE UPDATE ON company
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/

-- ==========================================
-- 2. 부서 (department)
-- ==========================================
CREATE TABLE department (
  dept_id     NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  parent_id   NUMBER,
  dept_name   VARCHAR2(100),
  dept_code   VARCHAR2(100),
  depth       NUMBER,
  sort_order  NUMBER,
  emp_id      NUMBER,
  dept_status VARCHAR2(100) DEFAULT 'ACTIVE',
  created_at  DATE DEFAULT SYSDATE,
  updated_at  DATE DEFAULT SYSDATE,
  CONSTRAINT pk_department PRIMARY KEY (dept_id),
  CONSTRAINT uq_department_com_code UNIQUE (com_id, dept_code),
  CONSTRAINT ck_department_status CHECK (dept_status IN ('ACTIVE','PENDING_DELETE','DELETED')),
  CONSTRAINT fk_department_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_department_department1
    FOREIGN KEY (parent_id) REFERENCES department (dept_id)
    ON DELETE CASCADE
);
 
CREATE SEQUENCE seq_department START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER trg_department_bi
BEFORE INSERT ON department
FOR EACH ROW
BEGIN
  IF :NEW.dept_id IS NULL THEN
    SELECT seq_department.NEXTVAL INTO :NEW.dept_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_department_bu
BEFORE UPDATE ON department
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/
 
CREATE INDEX fk_department_department1_idx ON department (parent_id);
-- com_id + dept_status + parent_id 복합 인덱스
-- (특정 회사, ACTIVE 상태 부서만 조회 후 트리 순회하는 패턴에 최적)
CREATE INDEX idx_department_com_status_parent ON department (com_id, dept_status, parent_id);
-- dept_status 필터링까지 인덱스에서 처리
CREATE INDEX idx_department_parent_status ON department (parent_id, dept_status);

-- ==========================================
-- 3. 직급 (emp_position)
-- ==========================================
CREATE TABLE emp_position (
    pos_id    NUMBER(10)   NOT NULL,
    pos_code  VARCHAR2(20) NOT NULL,
    pos_name  VARCHAR2(20) NOT NULL,
    pos_order NUMBER(10)   NOT NULL,
    com_id    NUMBER(10)   NOT NULL,
    CONSTRAINT pk_pos PRIMARY KEY (pos_id),
    CONSTRAINT uq_pos_com_code UNIQUE (com_id, pos_code),
    CONSTRAINT fk_pos_com FOREIGN KEY (com_id) REFERENCES company (com_id)
);

CREATE SEQUENCE seq_position START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_emp_position_pk
BEFORE INSERT ON emp_position
FOR EACH ROW
BEGIN
    IF :NEW.pos_id IS NULL THEN
        :NEW.pos_id := seq_position.NEXTVAL;
    END IF;
END;
/

-- ==========================================
-- 4. 사원 (employee)
-- ==========================================
CREATE TABLE employee (
    emp_id      NUMBER(10)      NOT NULL,
    emp_no      VARCHAR2(20)    NOT NULL,
    emp_name    VARCHAR2(50)    NOT NULL,
    emp_pass    VARCHAR2(500)   NOT NULL,
    emp_email   VARCHAR2(100)   NOT NULL,
    emp_mobile  VARCHAR2(20)    NOT NULL,
    emp_status  VARCHAR2(10)    DEFAULT '재직',
    hire_date   DATE,
    created_at  DATE      DEFAULT SYSDATE,
    updated_at  DATE      DEFAULT SYSDATE,
    com_id      NUMBER(10)      NOT NULL,
    pos_id      NUMBER(10)      NOT NULL,
    dept_id     NUMBER(10)      NOT NULL,
    CONSTRAINT pk_emp PRIMARY KEY (emp_id),
    CONSTRAINT fk_emp_com FOREIGN KEY (com_id)  REFERENCES company(com_id),
    CONSTRAINT fk_emp_pos FOREIGN KEY (pos_id)  REFERENCES emp_position(pos_id),
    CONSTRAINT fk_emp_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id)
);

CREATE SEQUENCE seq_employee START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_employee_pk
BEFORE INSERT ON employee
FOR EACH ROW
BEGIN
    IF :NEW.emp_id IS NULL THEN
       :NEW.emp_id := seq_employee.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_employee_updated_at
BEFORE UPDATE ON employee
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSDATE;
END;
/

CREATE INDEX fk_emp_pos_idx  ON employee (pos_id);
CREATE INDEX fk_emp_dept_idx ON employee (dept_id);

-- ==========================================
-- 5. 권한 (authority)
-- ==========================================
CREATE TABLE authority (
    aut_id    NUMBER       NOT NULL,
    com_id    NUMBER       NOT NULL,
    aut_name  VARCHAR2(45),
    CONSTRAINT pk_authority        PRIMARY KEY (aut_id),
    CONSTRAINT uq_authority_com_name UNIQUE (com_id, aut_name),
    CONSTRAINT fk_authority_company1 FOREIGN KEY (com_id) REFERENCES company (com_id)
);

CREATE SEQUENCE seq_authority START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_authority_bi
BEFORE INSERT ON authority
FOR EACH ROW
BEGIN
  IF :NEW.aut_id IS NULL THEN
    SELECT seq_authority.NEXTVAL INTO :NEW.aut_id FROM dual;
  END IF;
END;
/

-- ==========================================
-- 6. 권한-사원 (emp_auth)
-- ==========================================
CREATE TABLE emp_auth (
  emp_aut_id  NUMBER NOT NULL,
  emp_id      NUMBER NOT NULL,
  aut_id      NUMBER NOT NULL,
  CONSTRAINT pk_emp_auth PRIMARY KEY (emp_aut_id),
  CONSTRAINT uq_emp_auth UNIQUE (emp_id, aut_id),
  CONSTRAINT fk_emp_auth_emp FOREIGN KEY (emp_id) REFERENCES employee (emp_id),
  CONSTRAINT fk_emp_auth_auth FOREIGN KEY (aut_id) REFERENCES authority (aut_id)
);

CREATE SEQUENCE seq_emp_auth START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_emp_auth_bi
BEFORE INSERT ON emp_auth
FOR EACH ROW
BEGIN
  IF :NEW.emp_aut_id IS NULL THEN
    SELECT seq_emp_auth.NEXTVAL INTO :NEW.emp_aut_id FROM dual;
  END IF;
END;
/

-- ==========================================
-- 7. 결재 양식 (appr_form)
-- ==========================================
CREATE TABLE appr_form (
  for_id      NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  for_code    VARCHAR2(50) NOT NULL,
  for_title   VARCHAR2(50) NOT NULL,
  for_content CLOB NULL,
  for_schema CLOB,
  for_status  NUMBER(1) NOT NULL,
  is_deleted  NUMBER(1) DEFAULT 0 NOT NULL,
  for_version NUMBER DEFAULT 1 NOT NULL,
  created_at  DATE DEFAULT SYSDATE NOT NULL,
  updated_at  DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_appr_form PRIMARY KEY (for_id, for_version),
  CONSTRAINT uq_appr_form_com_code UNIQUE (com_id, for_code, for_version),
  CONSTRAINT fk_appr_form_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT chk_appr_form_status CHECK (for_status IN (0,1)),
  CONSTRAINT chk_appr_form_delete CHECK (is_deleted IN (0,1)),
  CONSTRAINT ck_appr_form_content_xor CHECK (
    (for_content IS NOT NULL AND for_schema IS NULL)
    OR
    (for_content IS NULL AND for_schema IS NOT NULL))
);
 
CREATE SEQUENCE appr_form_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER appr_form_trg
BEFORE INSERT ON appr_form
FOR EACH ROW
BEGIN
  IF :NEW.for_id IS NULL THEN
    SELECT appr_form_seq.NEXTVAL INTO :NEW.for_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_appr_form_bu
BEFORE UPDATE ON appr_form
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/

-- ==========================================
-- 8. 결재 문서 (appr_doc)
-- ==========================================
CREATE TABLE appr_doc (
  doc_id       NUMBER,
  emp_id       NUMBER NOT NULL,
  for_id       NUMBER NOT NULL,
  com_id       NUMBER NOT NULL,
  doc_title    VARCHAR2(100) NOT NULL,
  doc_content  CLOB NOT NULL,
  doc_status   VARCHAR2(20) DEFAULT 'ING' NOT NULL,
  created_at   DATE DEFAULT SYSDATE NOT NULL,
  updated_at   DATE DEFAULT SYSDATE NOT NULL,
  is_important NUMBER(1) DEFAULT 0 NOT NULL,
  doc_revision NUMBER DEFAULT 1 NOT NULL,
  for_version  NUMBER DEFAULT 1 NOT NULL,
  CONSTRAINT pk_appr_doc PRIMARY KEY (doc_id),
  CONSTRAINT ck_appr_doc_important CHECK (is_important IN (0,1)),
  CONSTRAINT ck_appr_doc_status CHECK (doc_status IN ('ING','APP','REJ','CAN')),
  

  CONSTRAINT fk_appr_doc_appr_form1
    FOREIGN KEY (for_id, for_version) REFERENCES appr_form (for_id, for_version),
    
  CONSTRAINT fk_appr_doc_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_appr_doc_employee1
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id)
);
 
CREATE SEQUENCE appr_doc_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER appr_doc_trg
BEFORE INSERT ON appr_doc
FOR EACH ROW
BEGIN
  IF :NEW.doc_id IS NULL THEN
    SELECT appr_doc_seq.NEXTVAL INTO :NEW.doc_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_appr_doc_bu
BEFORE UPDATE ON appr_doc
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/
 
CREATE INDEX fk_appr_doc_appr_form1_idx ON appr_doc (for_id);
CREATE INDEX fk_appr_doc_company1_idx   ON appr_doc (com_id);
CREATE INDEX fk_appr_doc_employee1_idx  ON appr_doc (emp_id);

-- ==========================================
-- 9. 결재 라인 (appr_line)
-- ==========================================
CREATE TABLE appr_line (
  lin_id       NUMBER NOT NULL,
  doc_id       NUMBER NOT NULL,
  emp_id       NUMBER NOT NULL,
  lin_order    NUMBER NOT NULL,
  lin_status   VARCHAR2(20) NOT NULL,
  lin_approved DATE NULL,
  CONSTRAINT pk_appr_line PRIMARY KEY (lin_id),
  CONSTRAINT uq_appr_line_doc_order UNIQUE (doc_id, lin_order),
  CONSTRAINT ck_appr_line_status CHECK (lin_status IN ('NOT','WAI','APP','REJ')),
  CONSTRAINT fk_appr_line_appr_doc1
    FOREIGN KEY (doc_id) REFERENCES appr_doc (doc_id),
  CONSTRAINT fk_appr_line_employee1
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id)
);
 
CREATE SEQUENCE appr_line_seq START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER appr_line_trg
BEFORE INSERT ON appr_line
FOR EACH ROW
BEGIN
  IF :NEW.lin_id IS NULL THEN
    SELECT appr_line_seq.NEXTVAL INTO :NEW.lin_id FROM dual;
  END IF;
END;
/
 
CREATE INDEX fk_appr_line_employee1_idx ON appr_line (emp_id);

-- ==========================================
-- 10. 프로젝트 (project)
-- ==========================================
CREATE TABLE project (
  pro_id            NUMBER NOT NULL,
  emp_id            NUMBER NOT NULL,
  com_id            NUMBER NOT NULL,
  pro_name          VARCHAR2(100) NOT NULL,
  pro_desc          VARCHAR2(1000),
  pro_status        VARCHAR2(20) DEFAULT 'TODO' NOT NULL,
  start_date        DATE NOT NULL,
  end_date          DATE NOT NULL,
  actual_start_date DATE,
  actual_end_date   DATE,
  created_at        DATE DEFAULT SYSDATE,
  updated_at        DATE DEFAULT SYSDATE,
  CONSTRAINT pk_project PRIMARY KEY (pro_id),
  CONSTRAINT ck_project_status CHECK (pro_status IN ('TODO','DOING','DONE')),
  CONSTRAINT ck_project_dates CHECK (end_date >= start_date),
  CONSTRAINT fk_project_employee1 FOREIGN KEY (emp_id)
    REFERENCES employee (emp_id),
  CONSTRAINT fk_project_company1 FOREIGN KEY (com_id)
    REFERENCES company (com_id)
);

CREATE SEQUENCE seq_project START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_project_bi
BEFORE INSERT ON project
FOR EACH ROW
BEGIN
  IF :NEW.pro_id IS NULL THEN
    SELECT seq_project.NEXTVAL INTO :NEW.pro_id FROM dual;
  END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_project_bu
BEFORE UPDATE ON project
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;

  IF :OLD.pro_status != 'DOING' AND :NEW.pro_status = 'DOING'
     AND :NEW.actual_start_date IS NULL THEN
    :NEW.actual_start_date := SYSDATE;
  END IF;

  IF :NEW.pro_status = 'DONE' AND :NEW.actual_end_date IS NULL THEN
    :NEW.actual_end_date := SYSDATE;
  END IF;
END;
/

CREATE INDEX fk_project_employee1_idx ON project (emp_id);
CREATE INDEX fk_project_company1_idx  ON project (com_id);


-- ==========================================
-- 11. 프로젝트 멤버 (project_member)
-- ==========================================
CREATE TABLE project_member (
  pm_id          NUMBER NOT NULL,
  project_pro_id NUMBER NOT NULL,
  emp_id         NUMBER NOT NULL,
  member_role    VARCHAR2(50) NOT NULL,
  joined_at      DATE NOT NULL,
  CONSTRAINT pk_project_member PRIMARY KEY (pm_id),
  CONSTRAINT uq_project_member UNIQUE (project_pro_id, emp_id),
  CONSTRAINT fk_project_member_project1
    FOREIGN KEY (project_pro_id) REFERENCES project (pro_id),
  CONSTRAINT fk_project_member_employee1
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id)
);

CREATE SEQUENCE seq_project_member START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_project_member_bi
BEFORE INSERT ON project_member
FOR EACH ROW
BEGIN
  IF :NEW.pm_id IS NULL THEN
    SELECT seq_project_member.NEXTVAL INTO :NEW.pm_id FROM dual;
  END IF;
END;
/

CREATE INDEX fk_project_member_employee1_idx ON project_member (emp_id);

-- ==========================================
-- 12. 태스크 (task)
-- ==========================================
CREATE TABLE task (
  task_id           NUMBER NOT NULL,
  pro_id            NUMBER NOT NULL,
  pm_id             NUMBER NOT NULL,
  com_id            NUMBER NOT NULL,
  parent_task_id    NUMBER,
  task_name         VARCHAR2(100),
  task_desc         VARCHAR2(1000),
  task_status       VARCHAR2(20) DEFAULT 'TODO' NOT NULL,
  task_start_date   DATE NOT NULL,
  task_end_date     DATE NOT NULL,
  actual_start_date DATE,
  actual_end_date   DATE,
  created_at        DATE DEFAULT SYSDATE NOT NULL,
  updated_at        DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_task PRIMARY KEY (task_id),
  CONSTRAINT ck_task_status CHECK (task_status IN ('TODO','DOING','DONE')),
  CONSTRAINT ck_task_dates CHECK (task_end_date >= task_start_date),
  CONSTRAINT ck_task_not_self_parent CHECK (parent_task_id != task_id),
  CONSTRAINT fk_task_project1 FOREIGN KEY (pro_id) REFERENCES project (pro_id),
  CONSTRAINT fk_task_company1 FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_task_project_member FOREIGN KEY (pm_id) REFERENCES project_member (pm_id),
  CONSTRAINT fk_task_parent FOREIGN KEY (parent_task_id) REFERENCES task (task_id) ON DELETE SET NULL
);

CREATE SEQUENCE seq_task START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_task_bi
BEFORE INSERT ON task
FOR EACH ROW
BEGIN
  IF :NEW.task_id IS NULL THEN
    SELECT seq_task.NEXTVAL INTO :NEW.task_id FROM dual;
  END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_task_bu
BEFORE UPDATE ON task
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;

  IF :OLD.task_status != 'DOING' AND :NEW.task_status = 'DOING'
     AND :NEW.actual_start_date IS NULL THEN
    :NEW.actual_start_date := SYSDATE;
  END IF;

  IF :NEW.task_status = 'DONE' AND :NEW.actual_end_date IS NULL THEN
    :NEW.actual_end_date := SYSDATE;
  END IF;
END;
/

CREATE INDEX fk_task_project1_idx        ON task (pro_id);
CREATE INDEX fk_task_company1_idx        ON task (com_id);
CREATE INDEX fk_task_project_member_idx  ON task (pm_id);
CREATE INDEX fk_task_parent_idx          ON task (parent_task_id);

-- ==========================================
-- 13. 전사공지 (notice)
-- ==========================================
CREATE TABLE notice (
  bno         NUMBER NOT NULL,
  emp_id      NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  btitle      VARCHAR2(200) NOT NULL,
  bcontent    CLOB NOT NULL,
  bhit        NUMBER DEFAULT 0 NOT NULL,
  bfile       VARCHAR2(500),
  created_at  DATE DEFAULT SYSDATE NOT NULL,
  updated_at  DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_notice PRIMARY KEY (bno),
  CONSTRAINT fk_notice_employee1
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id),
  CONSTRAINT fk_notice_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id)
);
 
CREATE SEQUENCE seq_notice START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER trg_notice_bi
BEFORE INSERT ON notice
FOR EACH ROW
BEGIN
  IF :NEW.bno IS NULL THEN
    SELECT seq_notice.NEXTVAL INTO :NEW.bno FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_notice_bu
BEFORE UPDATE ON notice
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/
 
CREATE INDEX fk_notice_employee1_idx ON notice (emp_id);
CREATE INDEX fk_notice_company1_idx  ON notice (com_id);

-- ==========================================
-- 14. 자원관리 (com_resource)
-- ==========================================
CREATE TABLE com_resource (
  res_id          NUMBER NOT NULL,
  com_id          NUMBER NOT NULL,
  res_code        VARCHAR2(50) NOT NULL,
  res_name        VARCHAR2(100) NOT NULL,
  res_type        VARCHAR2(20) NOT NULL,
  quantity        NUMBER NOT NULL,
  location        VARCHAR2(200),                 -- [신규] 위치 (예: 본관 3층, 지하주차장 차고)
  capacity        NUMBER,                        -- [신규] 수용인원 (ROOM 타입에서 주로 사용, EQUIPMENT/VEHICLE는 NULL 허용)
  res_status      VARCHAR2(20) DEFAULT 'AVAILABLE' NOT NULL,  -- [신규] AVAILABLE / MAINTENANCE / DISABLED
  manager_emp_id  NUMBER,                        -- [신규] 자원 담당자 (employee FK, nullable)
  remark          VARCHAR2(255),
  created_at      DATE DEFAULT SYSDATE,
  updated_at      DATE DEFAULT SYSDATE,
  CONSTRAINT pk_com_resource PRIMARY KEY (res_id),
  CONSTRAINT uq_com_resource_com_code UNIQUE (com_id, res_code),
  CONSTRAINT ck_com_resource_type CHECK (res_type IN ('ROOM','EQUIPMENT','VEHICLE')),
  CONSTRAINT ck_com_resource_qty CHECK (quantity >= 0),
  CONSTRAINT ck_com_resource_capacity CHECK (capacity IS NULL OR capacity > 0),
  CONSTRAINT ck_com_resource_status CHECK (res_status IN ('AVAILABLE','MAINTENANCE','DISABLED')),
  CONSTRAINT fk_com_resource_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_com_resource_manager1
    FOREIGN KEY (manager_emp_id) REFERENCES employee (emp_id)
);
 
CREATE SEQUENCE seq_com_resource START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER trg_com_resource_bi
BEFORE INSERT ON com_resource
FOR EACH ROW
BEGIN
  IF :NEW.res_id IS NULL THEN
    SELECT seq_com_resource.NEXTVAL INTO :NEW.res_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_com_resource_bu
BEFORE UPDATE ON com_resource
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/
 
CREATE INDEX fk_com_resource_manager1_idx ON com_resource (manager_emp_id);

-- ==========================================
-- 15. 자원예약 (reservation)
-- ==========================================
CREATE TABLE reservation (
  rev_id          NUMBER NOT NULL,
  res_id          NUMBER NOT NULL,
  com_id          NUMBER NOT NULL,
  emp_id          NUMBER NOT NULL,
  quantity        NUMBER NOT NULL,
  status          VARCHAR2(10) DEFAULT 'WAI' NOT NULL,   -- WAI(대기) / APP(승인) / REJ(반려)
  start_dt        TIMESTAMP NOT NULL,             -- [신규] 예약 시작 일시 (충돌 감지의 전제조건)
  end_dt          TIMESTAMP NOT NULL,              -- [신규] 예약 종료 일시
  return_dt       TIMESTAMP,                       -- [신규] 실제 반납 일시 (EQUIPMENT/VEHICLE, 승인 후 채워짐)
  approved_emp_id NUMBER,                          -- [신규] 승인/반려 처리자 (employee FK, nullable)
  approved_at     DATE,                            -- [신규] 승인/반려 처리 일시
  reject_reason   VARCHAR2(500),                   -- [신규] 반려 사유
  remark          VARCHAR2(255),                   -- 신청자 비고 (승인/반려 사유와 분리됨)
  created_at      DATE DEFAULT SYSDATE NOT NULL,    -- [신규] 예약 신청 생성일시
  updated_at      DATE DEFAULT SYSDATE,
  CONSTRAINT pk_reservation PRIMARY KEY (rev_id),
  CONSTRAINT ck_reservation_status CHECK (status IN ('WAI','APP','REJ')),
  CONSTRAINT ck_reservation_qty CHECK (quantity > 0),
  CONSTRAINT ck_reservation_dates CHECK (end_dt >= start_dt),
  CONSTRAINT fk_reservation_employee1
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id),
  CONSTRAINT fk_reservation_approver1
    FOREIGN KEY (approved_emp_id) REFERENCES employee (emp_id),
  CONSTRAINT fk_reservation_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_reservation_com_resource1
    FOREIGN KEY (res_id) REFERENCES com_resource (res_id)
);
 
CREATE SEQUENCE seq_reservation START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER trg_reservation_bi
BEFORE INSERT ON reservation
FOR EACH ROW
BEGIN
  IF :NEW.rev_id IS NULL THEN
    SELECT seq_reservation.NEXTVAL INTO :NEW.rev_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_reservation_bu
BEFORE UPDATE ON reservation
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/
 
CREATE INDEX fk_reservation_employee1_idx ON reservation (emp_id);
CREATE INDEX fk_reservation_approver1_idx  ON reservation (approved_emp_id);
CREATE INDEX fk_reservation_company1_idx   ON reservation (com_id);
CREATE INDEX fk_reservation_resource1_idx  ON reservation (res_id);
 
-- 시간대 충돌 감지 조회에 자주 쓰이는 복합 인덱스
-- (같은 자원(res_id)에 대해 특정 시간대와 겹치는 예약이 있는지 조회할 때 사용)
CREATE INDEX ix_reservation_res_time ON reservation (res_id, start_dt, end_dt);

-- ==========================================
-- 15. 부서 이관 이력 (dept_transfer_log)
-- ==========================================

CREATE TABLE dept_transfer_log (
  log_id            NUMBER NOT NULL,
  com_id            NUMBER NOT NULL,
  origin_dept_id    NUMBER NOT NULL,
  target_dept_id    NUMBER NOT NULL,
  emp_id            NUMBER NOT NULL,
  ai_recommended    VARCHAR2(1) DEFAULT 'N' NOT NULL,  -- AI 추천을 그대로 수용했는지 (Y/N)
  ai_reason         VARCHAR2(1000),                    -- AI가 준 추천 사유 (없으면 NULL)
  handover_snapshot CLOB,                               -- 이관 시점의 결재/자원 텍스트 요약 스냅샷
  created_by        NUMBER NOT NULL,                    -- 처리한 관리자 emp_id
  created_at        DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_dept_transfer_log PRIMARY KEY (log_id),
  CONSTRAINT ck_dtl_ai_recommended CHECK (ai_recommended IN ('Y', 'N')),
  CONSTRAINT fk_dtl_com          FOREIGN KEY (com_id)         REFERENCES company (com_id),
  CONSTRAINT fk_dtl_origin_dept  FOREIGN KEY (origin_dept_id) REFERENCES department (dept_id),
  CONSTRAINT fk_dtl_target_dept FOREIGN KEY (target_dept_id) REFERENCES department (dept_id),
  CONSTRAINT fk_dtl_emp          FOREIGN KEY (emp_id)         REFERENCES employee (emp_id),
  CONSTRAINT fk_dtl_created_by   FOREIGN KEY (created_by)     REFERENCES employee (emp_id)
);
 
CREATE INDEX fk_dtl_com_idx          ON dept_transfer_log (com_id);
CREATE INDEX fk_dtl_origin_dept_idx  ON dept_transfer_log (origin_dept_id);
CREATE INDEX fk_dtl_target_dept_idx ON dept_transfer_log (target_dept_id);
CREATE INDEX fk_dtl_emp_idx          ON dept_transfer_log (emp_id);
CREATE INDEX fk_dtl_created_by_idx   ON dept_transfer_log (created_by);
 
CREATE SEQUENCE seq_dept_transfer_log START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- ==========================================
-- 16. 평가 회차 (evaluation_period)
-- ==========================================
CREATE TABLE evaluation_period (
    period_id       NUMBER          NOT NULL,
    com_id          NUMBER          NOT NULL,
    eval_year       NUMBER(4)       NOT NULL,
    eval_term       VARCHAR2(10)    NOT NULL,
    title           VARCHAR2(100)   NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    period_status   VARCHAR2(20)    DEFAULT 'READY' NOT NULL,
    created_at      DATE            DEFAULT SYSDATE NOT NULL,
    updated_at      DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_period PRIMARY KEY (period_id),
    CONSTRAINT fk_period_com FOREIGN KEY (com_id) REFERENCES company (com_id),

    CONSTRAINT ck_period_status CHECK (period_status IN ('READY', 'OPEN', 'CLOSED', 'REPORTING', 'REPORTING_FAILED', 'REPORTED')),
    CONSTRAINT ck_period_term CHECK (eval_term IN ('H1', 'H2', 'ANNUAL')),
    CONSTRAINT ck_period_date CHECK (end_date >= start_date),
    CONSTRAINT ck_period_year CHECK (eval_year BETWEEN 2000 AND 2100),

    -- 같은 회사에 같은 회차 중복 방지
    CONSTRAINT uq_period_com_year_term UNIQUE (com_id, eval_year, eval_term)
);

COMMENT ON TABLE  evaluation_period IS '인사평가 회차';
COMMENT ON COLUMN evaluation_period.period_id     IS '평가 회차 PK';
COMMENT ON COLUMN evaluation_period.com_id        IS '회사 FK';
COMMENT ON COLUMN evaluation_period.eval_year     IS '평가 연도 (예: 2026)';
COMMENT ON COLUMN evaluation_period.eval_term     IS '평가 반기: H1(상반기) / H2(하반기) / ANNUAL(연간)';
COMMENT ON COLUMN evaluation_period.title         IS '회차 제목 (예: 2026년 상반기 정기평가)';
COMMENT ON COLUMN evaluation_period.start_date    IS '평가 시작일';
COMMENT ON COLUMN evaluation_period.end_date      IS '평가 종료일';
COMMENT ON COLUMN evaluation_period.period_status IS '상태: READY/OPEN/CLOSED/REPORTED';

CREATE INDEX idx_period_com ON evaluation_period (com_id);

CREATE SEQUENCE seq_period START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_period_pk
BEFORE INSERT ON evaluation_period
FOR EACH ROW
BEGIN
    IF :NEW.period_id IS NULL THEN
        :NEW.period_id := seq_period.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_period_updated
BEFORE UPDATE ON evaluation_period
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSDATE;
END;
/

-- ==========================================
-- 16. 평가 상세 (performance_evaluation)
-- ==========================================
CREATE TABLE performance_evaluation (
    eval_id             NUMBER          NOT NULL,
    period_id           NUMBER          NOT NULL,
    target_emp_id       NUMBER          NOT NULL,
    evaluator_id        NUMBER          NOT NULL,
    eval_type           VARCHAR2(20)    DEFAULT 'LEADER' NOT NULL,

    -- 5개 항목 점수
    score_performance   NUMBER(1),
    score_expertise     NUMBER(1),
    score_teamwork      NUMBER(1),
    score_attitude      NUMBER(1),
    score_growth        NUMBER(1),

    -- 가중 총점 (5개 항목 * 가중치의 합, 0.00~5.00)
    weighted_score      NUMBER(3,2),

    -- 서술형 코멘트 (강점/개선점 분리 - AI 프롬프트 품질용)
    strength_comment    CLOB,
    improvement_comment CLOB,

    eval_status         VARCHAR2(20)    DEFAULT 'DRAFT' NOT NULL,
    created_at          DATE            DEFAULT SYSDATE NOT NULL,
    updated_at          DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_eval PRIMARY KEY (eval_id),
    CONSTRAINT fk_eval_period    FOREIGN KEY (period_id)     REFERENCES evaluation_period (period_id),
    CONSTRAINT fk_eval_target    FOREIGN KEY (target_emp_id) REFERENCES employee (emp_id),
    CONSTRAINT fk_eval_evaluator FOREIGN KEY (evaluator_id)  REFERENCES employee (emp_id),

    CONSTRAINT ck_eval_type   CHECK (eval_type IN ('SELF', 'PEER', 'LEADER')),
    CONSTRAINT ck_eval_status CHECK (eval_status IN ('DRAFT', 'SUBMITTED')),

    -- 점수 범위 (NULL 허용은 DRAFT 상태 대응)
    CONSTRAINT ck_eval_score_perf   CHECK (score_performance IS NULL OR score_performance BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_score_exp    CHECK (score_expertise   IS NULL OR score_expertise   BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_score_team   CHECK (score_teamwork    IS NULL OR score_teamwork    BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_score_att    CHECK (score_attitude    IS NULL OR score_attitude    BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_score_grow   CHECK (score_growth      IS NULL OR score_growth      BETWEEN 1 AND 5),
    CONSTRAINT ck_eval_weighted     CHECK (weighted_score    IS NULL OR weighted_score    BETWEEN 1 AND 5),

    -- eval_type 별 정합성
    -- SELF: target = evaluator 이어야 함
    -- LEADER/PEER: target != evaluator 이어야 함
    CONSTRAINT ck_eval_self_match CHECK (
        (eval_type = 'SELF' AND target_emp_id = evaluator_id) OR
        (eval_type IN ('LEADER', 'PEER') AND target_emp_id <> evaluator_id)
    ),

    -- 같은 회차에 (대상, 평가자, 유형) 중복 방지
    CONSTRAINT uq_eval UNIQUE (period_id, target_emp_id, evaluator_id, eval_type)
);

COMMENT ON TABLE  performance_evaluation IS '인사평가 상세 - 개별 평가';
COMMENT ON COLUMN performance_evaluation.eval_id             IS '평가 PK';
COMMENT ON COLUMN performance_evaluation.period_id           IS '회차 FK';
COMMENT ON COLUMN performance_evaluation.target_emp_id       IS '피평가자';
COMMENT ON COLUMN performance_evaluation.evaluator_id        IS '평가자';
COMMENT ON COLUMN performance_evaluation.eval_type           IS '평가 유형: LEADER 기본, 확장용 SELF/PEER';
COMMENT ON COLUMN performance_evaluation.score_performance   IS '성과 점수 1~5';
COMMENT ON COLUMN performance_evaluation.score_expertise     IS '전문성 점수 1~5';
COMMENT ON COLUMN performance_evaluation.score_teamwork      IS '협업 점수 1~5';
COMMENT ON COLUMN performance_evaluation.score_attitude      IS '태도 점수 1~5';
COMMENT ON COLUMN performance_evaluation.score_growth        IS '성장 점수 1~5';
COMMENT ON COLUMN performance_evaluation.weighted_score      IS '가중 총점 (성과40 전문성20 협업20 태도10 성장10)';
COMMENT ON COLUMN performance_evaluation.strength_comment    IS '강점 서술 (AI 프롬프트용)';
COMMENT ON COLUMN performance_evaluation.improvement_comment IS '개선점 서술 (AI 프롬프트용)';
COMMENT ON COLUMN performance_evaluation.eval_status         IS 'DRAFT/SUBMITTED';

CREATE INDEX idx_eval_period_target ON performance_evaluation (period_id, target_emp_id);
CREATE INDEX idx_eval_evaluator     ON performance_evaluation (evaluator_id);

CREATE SEQUENCE seq_eval START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_eval_pk
BEFORE INSERT ON performance_evaluation
FOR EACH ROW
BEGIN
    IF :NEW.eval_id IS NULL THEN
        :NEW.eval_id := seq_eval.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_eval_updated
BEFORE UPDATE ON performance_evaluation
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSDATE;
END;
/


-- ==========================================
-- 16. AI 종합 리포트 (evaluation_ai_report)
-- ==========================================
CREATE TABLE evaluation_ai_report (
    report_id            NUMBER          NOT NULL,
    period_id            NUMBER          NOT NULL,
    emp_id               NUMBER          NOT NULL,

    -- 5개 항목 평균
    avg_performance      NUMBER(3,2),
    avg_expertise        NUMBER(3,2),
    avg_teamwork         NUMBER(3,2),
    avg_attitude         NUMBER(3,2),
    avg_growth           NUMBER(3,2),

    -- 종합 지표
    overall_score        NUMBER(3,2),
    grade                VARCHAR2(5),

    -- AI 요약
    ai_summary           CLOB,

    -- 감성 분석
    sentiment_positive   NUMBER(5,2),
    sentiment_neutral    NUMBER(5,2),
    sentiment_negative   NUMBER(5,2),
    sentiment_label      VARCHAR2(20),

    -- AI 추적성
    model_name           VARCHAR2(50),
    generated_at         DATE            DEFAULT SYSDATE,

    created_at           DATE            DEFAULT SYSDATE NOT NULL,
    updated_at           DATE            DEFAULT SYSDATE NOT NULL,

    CONSTRAINT pk_report PRIMARY KEY (report_id),
    CONSTRAINT fk_report_period FOREIGN KEY (period_id) REFERENCES evaluation_period (period_id),
    CONSTRAINT fk_report_emp    FOREIGN KEY (emp_id)    REFERENCES employee (emp_id),
    CONSTRAINT uq_report UNIQUE (period_id, emp_id),

    -- 평균 점수 범위
    CONSTRAINT ck_report_avg_perf CHECK (avg_performance IS NULL OR avg_performance BETWEEN 1 AND 5),
    CONSTRAINT ck_report_avg_exp  CHECK (avg_expertise   IS NULL OR avg_expertise   BETWEEN 1 AND 5),
    CONSTRAINT ck_report_avg_team CHECK (avg_teamwork    IS NULL OR avg_teamwork    BETWEEN 1 AND 5),
    CONSTRAINT ck_report_avg_att  CHECK (avg_attitude    IS NULL OR avg_attitude    BETWEEN 1 AND 5),
    CONSTRAINT ck_report_avg_grow CHECK (avg_growth      IS NULL OR avg_growth      BETWEEN 1 AND 5),
    CONSTRAINT ck_report_overall  CHECK (overall_score   IS NULL OR overall_score   BETWEEN 0 AND 5),

    -- 등급
    CONSTRAINT ck_report_grade CHECK (grade IS NULL OR grade IN ('S', 'A', 'B', 'C', 'D')),

    -- 감성 비율 개별
    CONSTRAINT ck_report_sent_pos CHECK (sentiment_positive IS NULL OR sentiment_positive BETWEEN 0 AND 100),
    CONSTRAINT ck_report_sent_neu CHECK (sentiment_neutral  IS NULL OR sentiment_neutral  BETWEEN 0 AND 100),
    CONSTRAINT ck_report_sent_neg CHECK (sentiment_negative IS NULL OR sentiment_negative BETWEEN 0 AND 100),

    -- 감성 비율 합계 100
    CONSTRAINT ck_report_sent_sum CHECK (
    (sentiment_positive IS NULL AND sentiment_neutral IS NULL AND sentiment_negative IS NULL) OR
    (ABS((sentiment_positive + sentiment_neutral + sentiment_negative) - 100) < 0.01)
    ),

    -- 감성 라벨
    CONSTRAINT ck_report_sent_label CHECK (sentiment_label IS NULL OR sentiment_label IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE'))
);

COMMENT ON TABLE  evaluation_ai_report IS 'AI 종합 리포트 - 회차별 사원별 1건';
COMMENT ON COLUMN evaluation_ai_report.report_id          IS '리포트 PK';
COMMENT ON COLUMN evaluation_ai_report.period_id          IS '회차 FK';
COMMENT ON COLUMN evaluation_ai_report.emp_id             IS '대상 사원';
COMMENT ON COLUMN evaluation_ai_report.avg_performance    IS '성과 평균';
COMMENT ON COLUMN evaluation_ai_report.avg_expertise      IS '전문성 평균';
COMMENT ON COLUMN evaluation_ai_report.avg_teamwork       IS '협업 평균';
COMMENT ON COLUMN evaluation_ai_report.avg_attitude       IS '태도 평균';
COMMENT ON COLUMN evaluation_ai_report.avg_growth         IS '성장 평균';
COMMENT ON COLUMN evaluation_ai_report.overall_score      IS '가중 총점 (5개 평균의 가중합)';
COMMENT ON COLUMN evaluation_ai_report.grade              IS '등급: S/A/B/C/D';
COMMENT ON COLUMN evaluation_ai_report.ai_summary         IS 'OpenAI 생성 종합 요약';
COMMENT ON COLUMN evaluation_ai_report.sentiment_positive IS '긍정 비율 %';
COMMENT ON COLUMN evaluation_ai_report.sentiment_neutral  IS '중립 비율 %';
COMMENT ON COLUMN evaluation_ai_report.sentiment_negative IS '부정 비율 %';
COMMENT ON COLUMN evaluation_ai_report.sentiment_label    IS '감성 요약 라벨: POSITIVE/NEUTRAL/NEGATIVE';
COMMENT ON COLUMN evaluation_ai_report.model_name         IS 'AI 모델명 (예: gpt-4o-mini)';
COMMENT ON COLUMN evaluation_ai_report.generated_at       IS 'AI 생성 시점';

-- v1의 idx_report_period_emp는 uq_report UNIQUE 제약이 만드는 인덱스와 중복이므로 제거

CREATE SEQUENCE seq_report START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE OR REPLACE TRIGGER trg_report_pk
BEFORE INSERT ON evaluation_ai_report
FOR EACH ROW
BEGIN
    IF :NEW.report_id IS NULL THEN
        :NEW.report_id := seq_report.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_report_updated
BEFORE UPDATE ON evaluation_ai_report
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSDATE;
END;
/

COMMIT;

-- ═══════════════════════════════════════════════════════════

--  email_send_log

--  - 온보딩/알림 이메일 발송 이력 (Audit Trail)

--  - 목적:

--    · 중복 발송 방지 (UNIQUE(emp_id, mail_type))

--    · 실패 이력 추적 (status='F', error_msg)

--    · afterCommit 이후 서버 다운 등으로 누락된 건 스케줄러가 재발송할 근거

--  - Oracle 18c XE 기준

-- ═══════════════════════════════════════════════════════════


create table email_send_log (
    log_id      number(10)      not null,
    emp_id      number(10)      not null,
    mail_type   varchar2(30)    not null,   -- 'WELCOME' | 'FOLLOWUP_3DAY' | ...

    status      char(1)         default 'P' not null,  -- P(rocessing) | S(uccess) | F(ail)

    sent_at     date            default sysdate not null,
    error_msg   varchar2(500),
    created_at  date            default sysdate not null,
    updated_at  date            default sysdate not null,

    constraint pk_email_send_log         primary key (log_id),
    constraint fk_esl_emp                foreign key (emp_id) references employee(emp_id),
    constraint uk_esl_emp_type           unique (emp_id, mail_type),
    constraint ck_esl_status             check (status in ('P', 'S', 'F')),
    constraint ck_esl_mail_type          check (mail_type in ('WELCOME', 'FOLLOWUP_3DAY'))
);

comment on table  email_send_log             is '온보딩/알림 이메일 발송 이력';
comment on column email_send_log.mail_type   is '이메일 종류 (WELCOME=환영, FOLLOWUP_3DAY=3일 적응)';
comment on column email_send_log.status      is 'P=처리중, S=성공, F=실패';
comment on column email_send_log.error_msg   is '실패 시 예외 메시지 (VARCHAR 500 자름)';

create sequence seq_email_log start with 1 increment by 1 nocache;

-- 스케줄러 조회 성능용 인덱스 (mail_type + sent_at)

create index idx_esl_type_date on email_send_log(mail_type, sent_at);

-- 확장 시 mail_type check 제약을 늘리려면:

-- alter table email_send_log drop constraint ck_esl_mail_type;

-- alter table email_send_log add constraint ck_esl_mail_type

--   check (mail_type in ('WELCOME', 'FOLLOWUP_3DAY', 'PASSWORD_RESET', ...));


commit;

-- 1) DDL 실행 후

-- select * from user_tables where table_name = 'EMAIL_SEND_LOG';
-- select * from user_sequences where sequence_name = 'SEQ_EMAIL_LOG';
-- select * from user_constraints where table_name = 'EMAIL_SEND_LOG';

-- ==========================================
-- 자원예약 노쇼/반납지연 자동 경고 봇
-- reservation 테이블 변경 스크립트
-- ==========================================
 
ALTER TABLE reservation ADD (noshow_alert_at TIMESTAMP);
 
COMMENT ON COLUMN reservation.noshow_alert_at IS
  '노쇼/반납지연 AI 알림 발송 일시 (NULL=미발송, 값 있으면 재발송하지 않음)';