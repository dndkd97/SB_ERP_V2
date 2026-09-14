
# <회사>
```sql
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
```
## 샘플 데이터
```markdown
| com_id | industry_grp_code | industry_code | com_name | com_ceo | biz_no | com_tel | com_logo |
|---|---|---|---|---|---|---|---|
| 1 | J | 62010 | (주)스프링브리즈 | 김정수 | 111-11-11111 | 02-1234-5678 | /images/company/springbreeze_logo.png |
| 2 | C | 26121 | (주)한빛전자 | 이한빛 | 222-22-22222 | 031-222-3333 | /images/company/hanbit_logo.png |
| 3 | H | 52913 | (주)미래물류 | 박미래 | 333-33-33333 | 02-333-4444 | /images/company/mirae_logo.png |
```

# <부서>
```sql
CREATE TABLE department (
  dept_id     NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  parent_id   NUMBER,
  dept_name   VARCHAR2(100),
  dept_code   VARCHAR2(100),
  depth       NUMBER,
  sort_order  NUMBER,
  emp_id      NUMBER,
  dept_status varchar2(100),
  created_at  DATE DEFAULT SYSDATE,
  updated_at  DATE DEFAULT SYSDATE,
  CONSTRAINT pk_department PRIMARY KEY (dept_id),
  CONSTRAINT uq_department_com_code UNIQUE (com_id, dept_code),
  [CHECK (dept_status IN ('ACTIVE','PENDING_DELETE','DELETED'))]
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
 
-- FK 컬럼 인덱스 (com_id는 uq_department_com_code 복합 UNIQUE 인덱스로 커버되므로 parent_id만 추가)
CREATE INDEX fk_department_department1_idx ON department (parent_id);
```
## 샘플 데이터
```markdown
| dept_id | com_id | parent_id | dept_name | dept_code | depth | sort_order | emp_id | dept_status |
|---|---|---|---|---|---|---|---|---|
| 1 | 1 | NULL | 본사 | HQ | 1 | 1 | NULL | ACTIVE |
| 2 | 1 | 1 | 시스템운영부서 | SYSOP | 2 | 1 | NULL | ACTIVE|
| 3 | 1 | 2 | QA팀 | QA | 3 | 1 | NULL | ACTIVE |
```

------------------------------------------------------------

# <사원>
```sql
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

-- 트리거(시퀀스)
CREATE OR REPLACE TRIGGER trg_employee_pk
BEFORE INSERT ON employee
FOR EACH ROW
BEGIN
    IF :NEW.emp_id IS NULL THEN
       :NEW.emp_id := seq_employee.NEXTVAL;
    END IF;
END;
/

-- 트리거(날짜 업데이트)
CREATE OR REPLACE TRIGGER trg_employee_updated_at
BEFORE UPDATE ON employee
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYSDATE;
END;
/

-- fk 인덱스
CREATE INDEX fk_emp_pos_idx  ON employee (pos_id);
CREATE INDEX fk_emp_dept_idx ON employee (dept_id);
```

## 샘플 데이터
```markdown
| emp_id | emp_no | emp_name | emp_pass | emp_email | emp_mobile | emp_status | hire_date | com_id | pos_id | dept_id |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | 'EMP-001' | '김이사' | '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjSQA0VwGPcOt5EbHqHOFDVLfjRJeG' | 'kim.ceo@company.com' | '010-1111-1001' | '재직' | '2020.01.01' | 1 | 1 | 1 |
| 2 | 'EMP-002' | '박부장' | '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjSQA0VwGPcOt5EbHqHOFDVLfjRJeG' | 'park.bj@company.com' | '010-1111-1002' | '재직' | '2020.01.03' | 1 | 2 | 2 |
| 3 | 'EMP-003' | '정과장' | '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjSQA0VwGPcOt5EbHqHOFDVLfjRJeG' | 'jung.kj@company.com' | '010-1111-1003' | '재직' | '2020.02.01' | 1 | 3 | 2 |
| 4 | 'EMP-004' | '노대리' | '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjSQA0VwGPcOt5EbHqHOFDVLfjRJeG' | 'no.dae@company.com' | '010-1111-1004' | '재직' | '2022.04.06' | 1 | 4 | 3 |
| 5 | 'EMP-005' | '유사원' | '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjSQA0VwGPcOt5EbHqHOFDVLfjRJeG' | 'yu.saw@company.com' | '010-1111-1005' | '재직' | '2025.12.15' | 1 | 5 | 3 |
```

# <직급>
```sql
CREATE TABLE emp_position (
    pos_id    NUMBER(10)   NOT NULL,
    pos_code  VARCHAR2(20) NOT NULL,
    pos_name  VARCHAR2(20) NOT NULL,
    pos_order NUMBER(10)   NOT NULL, -- 직급 정렬 순서 (이사=1 ... 사원=5)
    com_id    NUMBER(10)   NOT NULL,
    CONSTRAINT pk_pos PRIMARY KEY (pos_id),
    CONSTRAINT uq_pos_com_code UNIQUE (com_id, pos_code),   -- 회사별 직급코드
    CONSTRAINT fk_pos_com FOREIGN KEY (com_id) REFERENCES company (com_id)
);

CREATE SEQUENCE seq_position START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- PK 자동 채번: pos_id 가 NULL 로 들어오면 시퀀스 값으로 채움
CREATE OR REPLACE TRIGGER trg_emp_position_pk
BEFORE INSERT ON emp_position
FOR EACH ROW
BEGIN
    IF :NEW.pos_id IS NULL THEN
        :NEW.pos_id := seq_position.NEXTVAL;
    END IF;
END;
/
```

## 샘플 데이터
```markdown
| pos_id | pos_code | pos_name | pos_order | com_id |
|---|---|---|---|---|
| 1 | 'POS-01' | '이사' | 1 | 1 |
| 2 | 'POS-02' | '부장' | 2 | 1 |
| 3 | 'POS-03' | '과장' | 3 | 1 |
| 4 | 'POS-04' | '대리' | 4 | 1 |
| 5 | 'POS-05' | '사원' | 5 | 1 |
```

# <권한>
```sql
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
```

## 샘플 데이터
```markdown
| aut_id  | com_id | aut_name |
|---|---|---|
| 1 | 1 | 'ROOT' | 
| 2 | 1 | 'ROLE_ADMIN' | 
| 3 | 1 | 'ROLE_MANAGER' |
| 4 | 1 | 'ROLE_MEMBER' | 
```

# <권한-사원>
```sql
CREATE TABLE emp_auth (
  emp_aut_id  NUMBER NOT NULL,
  emp_id      NUMBER NOT NULL,
  aut_id      NUMBER NOT NULL,
  CONSTRAINT pk_emp_auth PRIMARY KEY (emp_aut_id),
  CONSTRAINT uq_emp_auth UNIQUE (emp_id, aut_id),
  CONSTRAINT fk_emp_auth_emp FOREIGN KEY (emp_id) REFERENCES employee (emp_id),
  CONSTRAINT fk_emp_auth_auth FOREIGN KEY (aut_id) REFERENCES authority (aut_id)
);

CREATE SEQUENCE semp_auth START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- 트리거
CREATE OR REPLACE TRIGGER trg_emp_auth_bi
BEFORE INSERT ON emp_auth
FOR EACH ROW
BEGIN
  IF :NEW.emp_aut_id IS NULL THEN
    SELECT seq_emp_auth.NEXTVAL INTO :NEW.emp_aut_id FROM dual;
  END IF;
END;
/
```

## 샘플 데이터
```markdown
| emp_aut_id | emp_id | aut_id |
|---|---|---|
| 1 | 1 | 1 | 
| 2 | 2 | 2 | 
| 3 | 3 | 3 | 
| 4 | 4 | 3 | 
| 5 | 5 | 3 | 
```

------------------------------------------------------------

# <결재 양식>
```sql
CREATE TABLE appr_form (
  for_id      NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  for_code    VARCHAR2(50) NOT NULL,
  for_title   VARCHAR2(50) NOT NULL,
  for_content CLOB NOT NULL,
  for_status  NUMBER(1) NOT NULL,
  is_deleted  number(1) default 0 not null, -- 삭제시 데이터 삭제를 하지 않기위함 / 추가
  for_version number default 1 not null,    -- 양식 수정 및 추가시 기존 양식 데이터 불일치를 위해 버전을 올려 새로 insert / 추가
  created_at  DATE DEFAULT SYSDATE NOT NULL,
  updated_at  DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_appr_form PRIMARY KEY (for_id,for_version),  -- 문서 작성때 데이터 불일치 방어를 위해 두개 교차검증 / 복합키 추가
  CONSTRAINT uq_appr_form_com_code UNIQUE (com_id, for_code),
  CONSTRAINT fk_appr_form_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT chk_appr_form_status CHECK (for_status IN (0,1)),
  constraint chk_appr_form_delete check (is_deleted in (0,1)) -- 추가
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
 
-- com_id는 uq_appr_form_com_code로 커버됨
```

## 샘플 데이터
```markdown
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (101, 'FORM_VAC_01', '연차신청서', '연차 신청을 위한 표준 양식입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (101, 'FORM_EXP_01', '지출결의서', '비용 정산을 위한 지출결의서 양식입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (101, 'FORM_BIZ_01', '기안서', '일반 업무 기안을 위한 표준 양식입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (101, 'FORM_WRK_01', '연장근무신청서', '연장 및 휴일 근무 신청 양식입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (101, 'FORM_BTR_01', '출장보고서', '국내외 출장 결과 보고 양식입니다.', 1, 0, 1);

INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (102, 'FORM_VAC_01', '휴가신청서', '회사B 전용 휴가 신청서입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (102, 'FORM_EXP_01', '경비청구서', '회사B 비용 처리용 경비청구서입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (102, 'FORM_BIZ_01', '업무기안서', '회사B 일반 기안 양식입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (102, 'FORM_PUR_01', '구매품의서', '물품 및 자산 구매를 위한 품의서입니다.', 1, 0, 1);
INSERT INTO appr_form (com_id, for_code, for_title, for_content, for_status, is_deleted, for_version)
VALUES (102, 'FORM_GFT_01', '경조사비신청서', '임직원 경조사 지원금 신청 양식입니다.', 1, 0, 1);
```



# <결재 문서>
```sql
CREATE TABLE appr_doc (
  doc_id       NUMBER NOT NULL,
  emp_id       NUMBER NOT NULL,
  for_id       NUMBER NOT NULL,
  com_id       NUMBER NOT NULL,
  doc_title    VARCHAR2(100) NOT NULL,
  doc_content  CLOB NOT NULL,
  doc_status   VARCHAR2(20) DEFAULT 'ING' NOT NULL,
  created_at   DATE DEFAULT SYSDATE NOT NULL,
  updated_at   DATE DEFAULT SYSDATE NOT NULL,
  is_important NUMBER(1) DEFAULT 0 NOT NULL, -- 0: 일반결재(3단계), 1: 중요문서(추가 결재라인)
  doc_revision number default 1 not null,  -- 문서 수정중 결재를 완료했을때 데이터 꼬임 방지용 / 추가
  for_version number default 1 not null, -- 문서 작성중 위와 동일하게 방어용 / 추가
  CONSTRAINT pk_appr_doc PRIMARY KEY (doc_id),
  CONSTRAINT ck_appr_doc_important CHECK (is_important IN (0,1)),
  CONSTRAINT ck_appr_doc_status CHECK (doc_status IN ('ING','APP','REJ','CAN')),
  CONSTRAINT fk_appr_doc_appr_form1
    FOREIGN KEY (for_id) REFERENCES appr_form (for_id),
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
```

## 샘플 데이터
```markdown
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (1, 1, 101, '홍길동 연차 신청의 건', '7월 10일 개인 사정으로 인한 연차 신청합니다.', 'ING', 0, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (2, 2, 101, '6월 마케팅 비품 지출결의', '소모품 및 사무용품 구매 비용 청구 건.', 'APP', 0, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (3, 3, 101, '[중요] 신규 프로젝트 추진 기안', '2026년 하반기 신사업 추진을 위한 기안서 발의.', 'ING', 1, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (1, 4, 101, '주말 장비 점검에 따른 연장근무 신청', '7월 4일 서버 점검을 위한 연장 근무.', 'REJ', 0, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (2, 5, 101, '부산 출장 결과 보고', '부산 지사 파트너십 미팅 완료 보고의 건.', 'APP', 0, 1, 1);

INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (4, 6, 102, '김철수 하계 휴가 신청서', '8월 여름 휴가 사용 요청의 건입니다.', 'ING', 0, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (5, 7, 102, '탕비실 다과 구매 경비 청구', '조직 활성화 및 탕비실 물품 구매 비용.', 'APP', 0, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (4, 8, 102, '[중요] 신규 사옥 이전에 관한 기안', '본사 이전에 따른 임대차 계약 체결 기안.', 'WAI', 1, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (5, 9, 102, '개발 서버 교체용 워크스테이션 구매품의', '노후화된 개발 서버 교체를 위한 품의.', 'CAN', 0, 1, 1);
INSERT INTO appr_doc (emp_id, for_id, com_id, doc_title, doc_content, doc_status, is_important, doc_revision, for_version)
VALUES (4, 10, 102, '이영희 대리 결혼 축의금 신청', '임직원 경조사 가이드라인에 따른 지원금 신청.', 'APP', 0, 1, 1)
```

# <결재 라인>
```sql
CREATE TABLE appr_line (
  lin_id       NUMBER NOT NULL,
  doc_id       NUMBER NOT NULL,
  emp_id       NUMBER NOT NULL,
  lin_order    NUMBER NOT NULL,
  lin_status   VARCHAR2(20) NOT NULL,
  lin_approved DATE NULL, -- 승인 시점에 UPDATE로 값 채움
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
    SELECT appr_line_seq.NEXTVAL INTO :NEW.lin_id FROM dual; -- (수정) appr_lin_seq -> appr_line_seq
  END IF;
END;
/
 
-- doc_id는 uq_appr_line_doc_order로 커버되며, emp_id만 별도 인덱스 필요
CREATE INDEX fk_appr_line_employee1_idx ON appr_line (emp_id);
```

## 샘플 데이터
```markdown
-- 1번 문서 (진행중, 3단계 결재선 생성)
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (1, 2, 1, 'APP', SYSDATE - 1);
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (1, 3, 2, 'WAI', NULL);
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (1, 5, 3, 'NOT', NULL);

-- 2번 문서 (승인완료, 2단계 결재선 모두 승인)
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (2, 3, 1, 'APP', SYSDATE - 2);
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (2, 5, 2, 'APP', SYSDATE - 1);

-- 3번 문서 (중요문서 진행중, 3단계 결재선)
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (3, 2, 1, 'APP', SYSDATE);
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (3, 3, 2, 'WAI', NULL);
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (3, 5, 3, 'NOT', NULL);

-- 4번 문서 (반려됨, 1단계에서 바로 반려)
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (4, 2, 1, 'REJ', SYSDATE);

-- 6번 문서 (회사B 진행중, 1단계 대기)
INSERT INTO appr_line (doc_id, emp_id, lin_order, lin_status, lin_approved) VALUES (6, 5, 1, 'WAI', NULL);
```

-------------------------------------------

# <프로젝트>
```sql
CREATE TABLE project (
  pro_id            NUMBER NOT NULL,
  emp_id            NUMBER NOT NULL,
  com_id            NUMBER NOT NULL,
  pro_name          VARCHAR2(100) NOT NULL,
  pro_desc          VARCHAR2(1000),
  pro_status        VARCHAR2(20) DEFAULT 'TODO' NOT NULL,
  start_date        DATE NOT NULL,        -- 예정 시작일
  end_date          DATE NOT NULL,        -- 예정 종료일 (필수 입력으로 변경)
  actual_start_date DATE,                  -- 실제 착수일 (DOING 전환 시 자동 기록)
  actual_end_date   DATE,                  -- 실제 완료일 (DONE 전환 시 자동 기록)
  created_at        DATE DEFAULT SYSDATE,
  updated_at        DATE DEFAULT SYSDATE,
  CONSTRAINT pk_project PRIMARY KEY (pro_id),
  CONSTRAINT ck_project_status CHECK (pro_status IN ('TODO','DOING','DONE')),
  CONSTRAINT ck_project_dates CHECK (end_date >= start_date),   -- 종료일이 시작일보다 빠를 수 없도록 방어
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
```

## 샘플 데이터
```markdown
프로젝트번호|회사이름|      프로젝트명      |       프로젝트설명       |생성자  |상태   |참여인원수|예정시작일  |예정종료일  |실제착수일  |실제완료일  |등록일
    1      |'네이버'|     'ERP개발'       |    'ERP 시스템 구축'     |'최다영'|'TODO' |    3    |'2026-06-13'|'2026-06-14'|   null    |   null    |'2026-06-12'
    2      |'네이버'|   '쇼핑몰 리뉴얼'    |    'UI 및 기능개선'      |'김민지'|'DOING'|    4    |'2026-06-15'|'2026-06-16'|'2026-06-15'|   null    |'2026-06-12'
    3      |'카카오'|'데이터 분석 플랫폼'  |  '데이터 대시보드 개발'   |'이서준'|'DONE' |    2    |'2026-05-01'|'2026-06-10'|'2026-05-01'|'2026-06-09'|'2026-04-28'
```

# <프로젝트 멤버>
```sql
CREATE TABLE project_member (
  pm_id          NUMBER NOT NULL,
  project_pro_id NUMBER NOT NULL,
  emp_id         NUMBER NOT NULL,
  member_role    VARCHAR2(50) NOT NULL, -- (수정) role -> member_role, ROLE은 Oracle 예약어
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

-- project_pro_id는 uq_project_member로 커버되므로 emp_id만 추가
CREATE INDEX fk_project_member_employee1_idx ON project_member (emp_id);
```

## 샘플 데이터
```markdown
참여번호|소속프로젝트번호|참여자  |역할  |참여일
   1   |      1       |'최다영'|'PM' |'2026-06-12'
   2   |      2       |'김민지'|'PM' |'2026-06-12'
   3   |      3       |'이서준'|'PM' |'2026-04-28'
```

# <태스크> 
```sql
CREATE TABLE task (
  task_id           NUMBER NOT NULL,
  pro_id            NUMBER NOT NULL,
  pm_id             NUMBER NOT NULL,
  com_id            NUMBER NOT NULL,
  task_name         VARCHAR2(100),
  task_desc         VARCHAR2(1000),
  task_status       VARCHAR2(20) DEFAULT 'TODO' NOT NULL,
  task_start_date   DATE NOT NULL,        -- 예정 시작일 (등록 시 입력)
  task_end_date     DATE NOT NULL,        -- 예정 마감일 (등록 시 입력)
  actual_start_date DATE,                  -- 실제 시작일 (DOING 전환 시 자동 기록)
  actual_end_date   DATE,                  -- 실제 완료일 (DONE 전환 시 자동 기록)
  created_at        DATE DEFAULT SYSDATE NOT NULL,
  updated_at        DATE DEFAULT SYSDATE NOT NULL,
  CONSTRAINT pk_task PRIMARY KEY (task_id),
  CONSTRAINT ck_task_status CHECK (task_status IN ('TODO','DOING','DONE')),
  CONSTRAINT ck_task_dates CHECK (task_end_date >= task_start_date),   -- 방어 제약 추가
  CONSTRAINT fk_task_project1 FOREIGN KEY (pro_id) REFERENCES project (pro_id),
  CONSTRAINT fk_task_company1 FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_task_project_member FOREIGN KEY (pm_id) REFERENCES project_member (pm_id)
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
```

## 샘플 데이터
```markdown
태스크번호|프로젝트번호|pm_id|     태스크명      |        태스크설명         |상태   |예정시작일  |예정종료일  |실제시작일  |실제완료일  
   1     |     1     |  1  |  '요구사항 분석'   | 'ERP 시스템 요구사항 정의' |'TODO' |'2026-06-13'|'2026-06-14'|   null    |   null    
   2     |     2     |  2  |'화면 디자인 시안'  |  '쇼핑몰 UI 개선 작업'    |'DOING'|'2026-06-15'|'2026-06-16'|'2026-06-15'|   null    
   3     |     3     |  3  |'대시보드 개발 완료'|'데이터 시각화 대시보드 구축'|'DONE' |'2026-05-01'|'2026-06-10'|'2026-05-01'|'2026-06-10'
```

--------------------------------------------------

# <전사공지>

```sql
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
 
-- 참고: 첨부파일이 여러 개 필요해지면 notice_file(file_id, bno, file_name, file_path...)
--       테이블로 분리하는 걸 권장 (지금은 1건만 저장 가능한 구조)
```

---------------------------------------------------------------------------------

# <자원관리>
```sql
CREATE TABLE resource (
  res_id      NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  res_code    VARCHAR2(50) NOT NULL,
  res_name    VARCHAR2(100) NOT NULL,
  res_type    VARCHAR2(20) NOT NULL,
  quantity    NUMBER NOT NULL,
  remark      VARCHAR2(255),
  created_at  DATE DEFAULT SYSDATE,
  updated_at  DATE DEFAULT SYSDATE,
  CONSTRAINT pk_resource PRIMARY KEY (res_id),
  CONSTRAINT uq_resource_com_code UNIQUE (com_id, res_code),
  CONSTRAINT ck_resource_type CHECK (res_type IN ('ROOM','EQUIPMENT')),
  CONSTRAINT ck_resource_qty CHECK (quantity >= 0),
  CONSTRAINT fk_resource_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id)
);
 
CREATE SEQUENCE seq_resource START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
 
CREATE OR REPLACE TRIGGER trg_resource_bi
BEFORE INSERT ON resource
FOR EACH ROW
BEGIN
  IF :NEW.res_id IS NULL THEN
    SELECT seq_resource.NEXTVAL INTO :NEW.res_id FROM dual;
  END IF;
END;
/
 
CREATE OR REPLACE TRIGGER trg_resource_bu
BEFORE UPDATE ON resource
FOR EACH ROW
BEGIN
  :NEW.updated_at := SYSDATE;
END;
/
 
-- com_id는 uq_resource_com_code로 커버됨
```

# <자원예약>
```sql
CREATE TABLE reservation (
  rev_id      NUMBER NOT NULL,
  res_id      NUMBER NOT NULL,
  com_id      NUMBER NOT NULL,
  emp_id      NUMBER NOT NULL,
  quantity    NUMBER NOT NULL,
  status      VARCHAR2(10) DEFAULT 'WAI' NOT NULL,
  req_date    DATE,
  remark      VARCHAR2(255),
  updated_at  DATE DEFAULT SYSDATE,
  CONSTRAINT pk_reservation PRIMARY KEY (rev_id),
  CONSTRAINT ck_reservation_status CHECK (status IN ('WAI','APP','REJ')),
  CONSTRAINT ck_reservation_qty CHECK (quantity > 0),
  CONSTRAINT fk_reservation_employee1
    FOREIGN KEY (emp_id) REFERENCES employee (emp_id),
  CONSTRAINT fk_reservation_company1
    FOREIGN KEY (com_id) REFERENCES company (com_id),
  CONSTRAINT fk_reservation_resource1
    FOREIGN KEY (res_id) REFERENCES resource (res_id)
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
CREATE INDEX fk_reservation_company1_idx  ON reservation (com_id);
CREATE INDEX fk_reservation_resource1_idx ON reservation (res_id);
```