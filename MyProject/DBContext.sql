/* =======================
   TẠO/CÀI LẠI DB: ASSIGNMENT
   ======================= */
IF DB_ID('ASSIGNMENT') IS NOT NULL
BEGIN
  ALTER DATABASE ASSIGNMENT SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
  DROP DATABASE ASSIGNMENT;
END;
CREATE DATABASE ASSIGNMENT;
GO
USE ASSIGNMENT;
GO

/* =======================
   SCHEMA CHUẨN (ASSIGNMENT)
   ======================= */
CREATE TABLE Department (
  DepartmentID    INT IDENTITY PRIMARY KEY,
  Name            NVARCHAR(100) NOT NULL,
  ManagerUserID   INT NULL
);

CREATE TABLE [User] (
  UserID          INT IDENTITY PRIMARY KEY,
  Username        VARCHAR(50)  NOT NULL UNIQUE,
  PasswordHash    VARCHAR(200) NOT NULL,
  FullName        NVARCHAR(120) NOT NULL,
  Email           VARCHAR(120)  NOT NULL,
  DepartmentID    INT NOT NULL,
  ManagerUserID   INT NULL,
  IsActive        BIT NOT NULL DEFAULT 1,
  CONSTRAINT FK_User_Department FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID),
  CONSTRAINT FK_User_Manager    FOREIGN KEY (ManagerUserID) REFERENCES [User](UserID)
);

CREATE TABLE Role (
  RoleID  INT IDENTITY PRIMARY KEY,
  Code    VARCHAR(50) UNIQUE NOT NULL,
  Name    NVARCHAR(100) NOT NULL
);

CREATE TABLE Feature (
  FeatureID    INT IDENTITY PRIMARY KEY,
  Code         VARCHAR(50) UNIQUE NOT NULL,
  Name         NVARCHAR(100) NOT NULL,
  PathPattern  VARCHAR(200) NOT NULL
);

CREATE TABLE UserRole (
  UserID INT NOT NULL,
  RoleID INT NOT NULL,
  PRIMARY KEY(UserID, RoleID),
  FOREIGN KEY (UserID) REFERENCES [User](UserID),
  FOREIGN KEY (RoleID) REFERENCES Role(RoleID)
);

CREATE TABLE RoleFeature (
  RoleID    INT NOT NULL,
  FeatureID INT NOT NULL,
  PRIMARY KEY(RoleID, FeatureID),
  FOREIGN KEY (RoleID) REFERENCES Role(RoleID),
  FOREIGN KEY (FeatureID) REFERENCES Feature(FeatureID)
);

CREATE TABLE [Request] (
  RequestID     INT IDENTITY PRIMARY KEY,
  Title         NVARCHAR(200) NOT NULL,
  DateFrom      DATE NOT NULL,
  DateTo        DATE NOT NULL,
  Reason        NVARCHAR(1000) NOT NULL,
  CreatedBy     INT NOT NULL,
  Status        VARCHAR(20) NOT NULL,      -- NEW/APPROVED/REJECTED/...
  ProcessedBy   INT NULL,
  ProcessedNote NVARCHAR(1000) NULL,
  CreatedAt     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  UpdatedAt     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT FK_Request_Creator   FOREIGN KEY (CreatedBy)   REFERENCES [User](UserID),
  CONSTRAINT FK_Request_Processor FOREIGN KEY (ProcessedBy) REFERENCES [User](UserID),
  CONSTRAINT CK_Request_Date CHECK (DateFrom <= DateTo)
);

CREATE TABLE RequestHistory (
  HistoryID   INT IDENTITY PRIMARY KEY,
  RequestID   INT NOT NULL,
  ActionBy    INT NOT NULL,
  OldStatus   VARCHAR(20) NULL,
  NewStatus   VARCHAR(20) NOT NULL,
  Note        NVARCHAR(1000) NULL,
  ActionAt    DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  FOREIGN KEY (RequestID) REFERENCES [Request](RequestID),
  FOREIGN KEY (ActionBy)  REFERENCES [User](UserID)
);

CREATE INDEX IX_Request_CreatedBy_Status ON [Request](CreatedBy, Status);
CREATE INDEX IX_Request_DateRange        ON [Request](DateFrom, DateTo);
GO

/* =======================
   DỮ LIỆU MẪU (ASSIGNMENT)
   ======================= */
INSERT INTO Department(Name) VALUES (N'IT'), (N'QA'), (N'Sale');

INSERT INTO Role(Code,Name) VALUES
 ('DIV_LEADER', N'Division Leader'),
 ('TEAM_LEAD',  N'Trưởng nhóm'),
 ('EMP',        N'Nhân viên');

INSERT INTO Feature(Code,Name,PathPattern) VALUES
 ('REQ_MY',      N'Xem đơn của tôi',        '/requestlistmyservlet1'),
 ('REQ_CREATE',  N'Tạo đơn',                '/requestcreateservlet1'),
 ('REQ_MGR',     N'Xem đơn cấp dưới',       '/requestsubordinatesservlet1'),
 ('REQ_APPROVE', N'Duyệt đơn',              '/requestapproveservlet1'),
 ('AGD',         N'Agenda phòng ban',       '/agendaservlet1');

INSERT INTO RoleFeature(RoleID, FeatureID)
SELECT r.RoleID, f.FeatureID
FROM Role r JOIN Feature f ON 1=1
WHERE (r.Code='EMP'        AND f.Code IN ('REQ_MY','REQ_CREATE'))
   OR (r.Code='TEAM_LEAD'  AND f.Code IN ('REQ_MY','REQ_CREATE','REQ_MGR','REQ_APPROVE'))
   OR (r.Code='DIV_LEADER' AND f.Code IN ('REQ_MY','REQ_CREATE','REQ_MGR','REQ_APPROVE','AGD'));

-- Người dùng (mật khẩu: 123456)
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('cara',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Cara (Head IT)' , 'cara@company.com',  (SELECT DepartmentID FROM Department WHERE Name=N'IT'), NULL, 1);
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('alice', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Alice (Team Lead)','alice@company.com', (SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='cara'), 1),
('mike',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Mike (Team Lead)' ,'mike@company.com',  (SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='cara'), 1),
('bob',   CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Bob'             , 'bob@company.com',   (SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='alice'), 1),
('ryder', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Ryder'           , 'ryder@company.com', (SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='alice'), 1),
('laura', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Laura'           , 'laura@company.com', (SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='alice'), 1),
('clause',CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Clause'          , 'clause@company.com',(SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='mike'), 1),
('michel',CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Michel'          , 'michel@company.com',(SELECT DepartmentID FROM Department WHERE Name=N'IT'), (SELECT UserID FROM [User] WHERE Username='mike'), 1);

-- Trưởng phòng IT
UPDATE d SET ManagerUserID = (SELECT UserID FROM [User] WHERE Username='cara')
FROM Department d WHERE d.Name=N'IT';

-- Gán role
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='DIV_LEADER' WHERE u.Username='cara';
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='TEAM_LEAD' WHERE u.Username IN ('alice','mike');
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='EMP'
WHERE u.Username IN ('bob','ryder','laura','clause','michel');



/* =======================
   MAP TRẠNG THÁI (để app cũ dùng số int)
   ======================= */
CREATE TABLE StatusMap(
  Code  VARCHAR(20) PRIMARY KEY,  -- NEW/APPROVED/REJECTED/...
  Id    INT NOT NULL UNIQUE       -- 0/1/2/3...
);
INSERT INTO StatusMap(Code,Id) VALUES
('NEW',0),('APPROVED',1),('REJECTED',2),('CANCELLED',3);



/* =======================
   VIEW TƯƠNG THÍCH VỚI DB CŨ
   (FALL25_Assignment) – không còn bảng vật lý Employee/Division/...
   ======================= */

-- Division (cũ) -> Department (mới)
IF OBJECT_ID('dbo.Division','V') IS NOT NULL DROP VIEW dbo.Division;
GO
CREATE VIEW dbo.Division
AS
SELECT DepartmentID AS did, Name AS dname
FROM Department;
GO

-- Employee (cũ) -> User (mới)
IF OBJECT_ID('dbo.Employee','V') IS NOT NULL DROP VIEW dbo.Employee;
GO
CREATE VIEW dbo.Employee
AS
SELECT 
  u.UserID        AS eid,
  u.FullName      AS ename,
  u.DepartmentID  AS did,
  u.ManagerUserID AS supervisorid
FROM [User] u;
GO

-- Enrollment (cũ) -> dựa vào IsActive
IF OBJECT_ID('dbo.Enrollment','V') IS NOT NULL DROP VIEW dbo.Enrollment;
GO
CREATE VIEW dbo.Enrollment
AS
SELECT 
  u.UserID AS uid,
  u.UserID AS eid,
  u.IsActive AS active
FROM [User] u;
GO

-- Feature_legacy (cũ: fid,url)
IF OBJECT_ID('dbo.Feature_legacy','V') IS NOT NULL DROP VIEW dbo.Feature_legacy;
GO
CREATE VIEW dbo.Feature_legacy
AS
SELECT FeatureID AS fid, PathPattern AS url
FROM Feature;
GO

-- Role_legacy (cũ: rid,rname)
IF OBJECT_ID('dbo.Role_legacy','V') IS NOT NULL DROP VIEW dbo.Role_legacy;
GO
CREATE VIEW dbo.Role_legacy
AS
SELECT RoleID AS rid, Name AS rname
FROM Role;
GO

-- RequestForLeave (cũ) -> Request (mới)
IF OBJECT_ID('dbo.RequestForLeave','V') IS NOT NULL DROP VIEW dbo.RequestForLeave;
GO
CREATE VIEW dbo.RequestForLeave
AS
SELECT
  r.RequestID                AS rid,
  r.CreatedBy                AS created_by,
  CAST(r.CreatedAt AS DATETIME) AS created_time,
  r.DateFrom                 AS [from],
  r.DateTo                   AS [to],
  r.Reason                   AS reason,
  sm.Id                      AS [status]
FROM [Request] r
LEFT JOIN StatusMap sm ON sm.Code = r.Status;
GO


/* =======================
   KIỂM TRA NHANH
   ======================= */
-- Nhân sự (chuẩn ASSIGNMENT)
SELECT u.Username, u.FullName, d.Name AS Department, m.Username AS Manager
FROM [User] u
JOIN Department d ON d.DepartmentID=u.DepartmentID
LEFT JOIN [User] m ON m.UserID=u.ManagerUserID
ORDER BY u.Username;

-- App cũ qua VIEW
SELECT TOP 5 * FROM dbo.Employee;
SELECT TOP 5 * FROM dbo.Division;
SELECT TOP 5 * FROM dbo.RequestForLeave;
SELECT * FROM dbo.Employee;
SELECT * FROM dbo.Feature;
SELECT * FROM dbo.Request;
SELECT * FROM dbo.RequestHistory;
SELECT * FROM dbo.Role;
SELECT * FROM dbo.RoleFeature;
SELECT * FROM dbo.StatusMap;
SELECT * FROM dbo.[User];
SELECT * FROM dbo.UserRole;


/* 1) Bỏ phần chức vụ trong FullName: "Tên (xxx)" -> "Tên" */
UPDATE [User]
SET FullName = LTRIM(RTRIM(
    CASE WHEN CHARINDEX('(', FullName) > 0
         THEN LEFT(FullName, CHARINDEX('(', FullName) - 1)
         ELSE FullName
    END));

/* 2) Set lại quan hệ quản lý đúng mong muốn */
UPDATE u SET ManagerUserID = (SELECT UserID FROM [User] WHERE Username='cara')
FROM [User] u
WHERE u.Username IN ('alice','mike');   -- cấp dưới trực tiếp của Cara

UPDATE u SET ManagerUserID = (SELECT UserID FROM [User] WHERE Username='alice')
FROM [User] u
WHERE u.Username IN ('bob','ryder','laura');   -- dưới Alice

UPDATE u SET ManagerUserID = (SELECT UserID FROM [User] WHERE Username='mike')
FROM [User] u
WHERE u.Username IN ('clause','michel');       -- dưới Mike

/* 3) Kiểm tra lại */
SELECT u.Username, u.FullName, u.DepartmentID, u.ManagerUserID,
       m.Username AS Manager
FROM [User] u
LEFT JOIN [User] m ON m.UserID = u.ManagerUserID
ORDER BY u.Username;

SELECT u.Username, r.Code AS RoleCode, f.Code AS FeatureCode
FROM [User] u
LEFT JOIN UserRole ur ON ur.UserID = u.UserID
LEFT JOIN Role r      ON r.RoleID = ur.RoleID
LEFT JOIN RoleFeature rf ON rf.RoleID = r.RoleID
LEFT JOIN Feature f      ON f.FeatureID = rf.FeatureID
WHERE u.Username IN ('bob','ryder','laura','clause','michel')
ORDER BY u.Username;

SELECT * FROM [User] WHERE Username = 'alice' AND IsActive = 1;

SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='Request';

/* =======================
   THÊM NHÂN SỰ CHO QA & SALE
   ======================= */

-- ===== QA =====
-- Trưởng phòng QA
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('quinn', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Quinn (Head QA)', 'quinn@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), NULL, 1);

-- Hai team lead dưới QA Head
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('tina',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Tina (Team Lead)',  'tina@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='quinn'), 1),
('tony',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Tony (Team Lead)',  'tony@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='quinn'), 1);

-- Mỗi team lead có 3 nhân viên
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('ivy',   CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Ivy',   'ivy@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='tina'), 1),
('jack',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Jack',  'jack@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='tina'), 1),
('kate',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Kate',  'kate@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='tina'), 1),
('liam',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Liam',  'liam@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='tony'), 1),
('maya',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Maya',  'maya@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='tony'), 1),
('nate',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Nate',  'nate@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'QA'), (SELECT UserID FROM [User] WHERE Username='tony'), 1);

-- Gán head cho Department QA
UPDATE d SET ManagerUserID = (SELECT UserID FROM [User] WHERE Username='quinn')
FROM Department d WHERE d.Name=N'QA';

-- Role cho QA
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='DIV_LEADER' WHERE u.Username='quinn';
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='TEAM_LEAD'  WHERE u.Username IN ('tina','tony');
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='EMP'
WHERE u.Username IN ('ivy','jack','kate','liam','maya','nate');



-- ===== SALE =====
-- Trưởng phòng Sale
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('sara', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Sara (Head Sale)', 'sara@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), NULL, 1);

-- Hai team lead dưới Sale Head
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('sally', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Sally (Team Lead)', 'sally@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='sara'), 1),
('steve', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Steve (Team Lead)', 'steve@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='sara'), 1);

-- Mỗi team lead có 3 nhân viên
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive) VALUES
('owen',   CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Owen',   'owen@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='sally'), 1),
('peter',  CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Peter',  'peter@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='sally'), 1),
('rachel', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Rachel', 'rachel@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='sally'), 1),
('tom',    CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Tom',    'tom@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='steve'), 1),
('ursula', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Ursula', 'ursula@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='steve'), 1),
('victor', CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'),2), N'Victor', 'victor@company.com',
 (SELECT DepartmentID FROM Department WHERE Name=N'Sale'), (SELECT UserID FROM [User] WHERE Username='steve'), 1);

-- Gán head cho Department Sale
UPDATE d SET ManagerUserID = (SELECT UserID FROM [User] WHERE Username='sara')
FROM Department d WHERE d.Name=N'Sale';

-- Role cho Sale
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='DIV_LEADER' WHERE u.Username='sara';
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='TEAM_LEAD'  WHERE u.Username IN ('sally','steve');
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID FROM [User] u JOIN Role r ON r.Code='EMP'
WHERE u.Username IN ('owen','peter','rachel','tom','ursula','victor');

SELECT u.Username, d.Name AS Dept, m.Username AS Manager
FROM [User] u
JOIN Department d ON d.DepartmentID=u.DepartmentID
LEFT JOIN [User] m ON m.UserID=u.ManagerUserID
WHERE d.Name IN (N'QA', N'Sale')
ORDER BY d.Name, u.Username;

SELECT u.Username, r.Code AS RoleCode
FROM [User] u
JOIN UserRole ur ON ur.UserID=u.UserID
JOIN Role r ON r.RoleID=ur.RoleID
WHERE u.Username IN ('quinn','tina','tony','ivy','jack','kate','liam','maya','nate',
                      'sara','sally','steve','owen','peter','rachel','tom','ursula','victor')
ORDER BY u.Username;
SELECT * FROM [UserRole]

ALTER TABLE [User]
ADD FailedLoginCount INT NOT NULL DEFAULT 0,
    LastFailedAt     DATETIME2 NULL,
    LockUntil        DATETIME2 NULL;
GO
CREATE TABLE LoginAudit(
  Id INT IDENTITY PRIMARY KEY,
  UserId INT NULL,
  Username VARCHAR(50) NOT NULL,
  Ip VARCHAR(45) NOT NULL,
  Ok BIT NOT NULL,
  Msg NVARCHAR(200) NULL,
  At DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
-- nếu chưa có
ALTER TABLE [User] 
  ADD FailedLoginCount INT NOT NULL DEFAULT 0,
      LockUntil       DATETIME2 NULL;

	SELECT * FROM [User]

	IF OBJECT_ID('dbo.PasswordResetRequest','U') IS NULL
CREATE TABLE dbo.PasswordResetRequest (
  Id            INT IDENTITY PRIMARY KEY,
  UserId        INT       NOT NULL,
  Username      VARCHAR(50) NOT NULL,
  RequestedAt   DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
  Status        VARCHAR(16) NOT NULL DEFAULT 'PENDING', -- PENDING | APPROVED | DENIED | USED | EXPIRED
  ApprovedBy    INT        NULL,
  ApprovedAt    DATETIME2  NULL,
  Token         UNIQUEIDENTIFIER NULL,                  -- phát khi admin approve
  ExpiresAt     DATETIME2  NULL,                        -- hết hạn token
  UsedAt        DATETIME2  NULL
);
CREATE INDEX IX_Reset_Status ON dbo.PasswordResetRequest(Status, RequestedAt DESC);
CREATE INDEX IX_Reset_User   ON dbo.PasswordResetRequest(UserId, Status);

/* (tuỳ chọn) Thêm 1 role/feature cho admin nếu bạn đang dùng RBAC */
-- INSERT INTO Role(Code,Name) VALUES('ADMIN',N'Quản trị'); -- nếu chưa có
-- INSERT INTO UserRole(UserID,RoleID) VALUES(<<adminUserId>>, (SELECT RoleID FROM Role WHERE Code='ADMIN'));

-- FailedLoginCount + LockUntil chỉ tạo nếu CHƯA tồn tại
IF COL_LENGTH('dbo.[User]', 'FailedLoginCount') IS NULL
    ALTER TABLE dbo.[User] ADD FailedLoginCount INT NOT NULL DEFAULT(0);
IF COL_LENGTH('dbo.[User]', 'LockUntil') IS NULL
    ALTER TABLE dbo.[User] ADD LockUntil DATETIME2 NULL;

IF NOT EXISTS (SELECT 1 FROM dbo.Role WHERE Code = 'ADMIN')
BEGIN
    INSERT dbo.Role(Code, [Name]) VALUES ('ADMIN', N'Quản trị hệ thống');
END


DECLARE @pwd NVARCHAR(100) = N'Admin@123';           -- ← đổi nếu muốn
DECLARE @pwdHash VARCHAR(64) =
    CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2);  -- Hex uppercase

/* ===== 1) Role ADMIN ===== */
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE [Code] = 'ADMIN')
BEGIN
    INSERT dbo.[Role]([Code], [Name]) VALUES ('ADMIN', N'Administrator');
END
GO

/* ===== 2) Bổ sung cột bảo mật nếu thiếu ===== */
IF COL_LENGTH('dbo.[User]', 'FailedLoginCount') IS NULL
    ALTER TABLE dbo.[User] ADD FailedLoginCount INT NOT NULL DEFAULT 0;
IF COL_LENGTH('dbo.[User]', 'LockUntil') IS NULL
    ALTER TABLE dbo.[User] ADD LockUntil DATETIME2 NULL;
GO

/* ===== 3) Tạo Department mặc định để tránh DepartmentID = NULL ===== */
IF NOT EXISTS (SELECT 1 FROM dbo.Department WHERE [Name] = N'Administration')
BEGIN
    INSERT dbo.Department([Name], ManagerUserID) VALUES (N'Administration', NULL);
END
GO

/* ===== 4) Tạo user admin nếu chưa có (hash SHA-256 hex UPPER) ===== */
DECLARE @pwd       NVARCHAR(128) = N'Admin@123';
DECLARE @pwdHash   VARCHAR(64)   = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2);
DECLARE @depId     INT           = (SELECT TOP (1) DepartmentID
                                   FROM dbo.Department
                                   WHERE [Name] = N'Administration'
                                   ORDER BY DepartmentID);

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE Username = 'admin')
BEGIN
    INSERT dbo.[User](
        Username, PasswordHash, FullName, Email,
        DepartmentID, ManagerUserID, IsActive,
        FailedLoginCount, LockUntil
    )
    VALUES(
        'admin', @pwdHash, N'System Administrator', 'admin@example.com',
        @depId, NULL, 1,
        0, NULL
    );
END
GO

/* ===== 5) Gán role ADMIN cho admin nếu chưa có ===== */
DECLARE @adminId INT = (SELECT TOP (1) UserID FROM dbo.[User] WHERE Username = 'admin' ORDER BY UserID);
DECLARE @roleId  INT = (SELECT TOP (1) RoleID FROM dbo.[Role] WHERE [Code] = 'ADMIN' ORDER BY RoleID);

IF @adminId IS NOT NULL AND @roleId IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID = @adminId AND RoleID = @roleId)
BEGIN
    INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE [Code] = 'ADMIN')
BEGIN
  INSERT dbo.[Role]([Code],[Name]) VALUES ('ADMIN', N'Quản trị hệ thống');
END
GO


-- Chọn 1 Department bất kỳ (hoặc tự set tên phòng bạn muốn)
DECLARE @depId INT = (SELECT TOP (1) DepartmentID FROM dbo.Department ORDER BY DepartmentID);

-- Hash SHA-256 (hex uppercase) cho mật khẩu mới
DECLARE @pwd     NVARCHAR(128) = N'Admin@123';
DECLARE @pwdHash VARCHAR(64)   = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2);

IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE Username = 'admin')
BEGIN
  INSERT dbo.[User](
    Username, PasswordHash, FullName, Email,
    DepartmentID, ManagerUserID, IsActive,
    FailedLoginCount, LockUntil
  )
  VALUES(
    'admin', @pwdHash, N'System Administrator', 'admin@example.com',
    @depId, NULL, 1,
    0, NULL
  );
END
ELSE
BEGIN
  UPDATE dbo.[User]
  SET PasswordHash      = @pwdHash,
      FailedLoginCount  = 0,         -- reset bộ đếm
      LockUntil         = NULL       -- mở khóa nếu đang khóa
  WHERE Username = 'admin';
END
GO


DECLARE @adminId INT = (SELECT TOP 1 UserID FROM dbo.[User] WHERE Username = 'admin' ORDER BY UserID);
DECLARE @roleId  INT = (SELECT TOP 1 RoleID FROM dbo.[Role] WHERE [Code] = 'ADMIN' ORDER BY RoleID);

IF @adminId IS NOT NULL AND @roleId IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID = @adminId AND RoleID = @roleId)
BEGIN
  INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);
END
GO

SELECT Username, FailedLoginCount, LockUntil
FROM dbo.[User]
WHERE Username = 'admin';

UPDATE dbo.[User]
SET FullName = N'__CHECK_ADMIN__'
WHERE Username = 'admin';

SELECT UserID, Username, IsActive, FailedLoginCount, LockUntil
FROM dbo.[User]
WHERE Username = 'admin';

UPDATE dbo.[User]
SET FailedLoginCount = 0, LockUntil = NULL
WHERE Username = 'admin';

DECLARE @pwd NVARCHAR(128) = N'Admin@123';
UPDATE dbo.[User]
SET PasswordHash = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2)
WHERE Username = 'admin';


-- Đảm bảo Department 'Administration' tồn tại
IF NOT EXISTS (SELECT 1 FROM dbo.Department WHERE [Name]=N'Administration')
    INSERT dbo.Department([Name]) VALUES (N'Administration');

DECLARE @depId INT = (SELECT TOP 1 DepartmentID FROM dbo.Department WHERE [Name]=N'Administration');

-- Tạo admin nếu chưa có
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE Username='admin')
BEGIN
    DECLARE @pwd NVARCHAR(128) = N'Admin@123';
    INSERT dbo.[User](Username,PasswordHash,FullName,Email,DepartmentID,IsActive,FailedLoginCount,LockUntil)
    VALUES(
        'admin',
        CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2),
        N'System Administrator',
        'admin@example.com',
        @depId, 1, 0, NULL
    );
END

-- Bảo đảm có role ADMIN và gán cho admin
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE [Code]='ADMIN')
    INSERT dbo.[Role]([Code],[Name]) VALUES ('ADMIN', N'Quản trị hệ thống');

DECLARE @adminId INT = (SELECT TOP 1 UserID FROM dbo.[User] WHERE Username='admin');
DECLARE @roleId  INT = (SELECT TOP 1 RoleID  FROM dbo.[Role] WHERE [Code]='ADMIN');

IF NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID=@adminId AND RoleID=@roleId)
    INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);



	/* TẠO/ĐẢM BẢO ADMIN – chạy nguyên khối này một lần */

-- 1) Department 'Administration' (nếu chưa có)
IF NOT EXISTS (SELECT 1 FROM dbo.Department WHERE [Name] = N'Administration')
    INSERT dbo.Department([Name]) VALUES (N'Administration');

DECLARE @depId INT =
(
    SELECT TOP (1) DepartmentID
    FROM dbo.Department
    WHERE [Name] = N'Administration'
    ORDER BY DepartmentID
);

-- 2) Tạo user admin nếu chưa có (mật khẩu Admin@123, SHA-256 hex)
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE Username = 'admin')
BEGIN
    DECLARE @pwd NVARCHAR(128) = N'Admin@123';
    INSERT dbo.[User](
        Username, PasswordHash, FullName, Email,
        DepartmentID, ManagerUserID, IsActive,
        FailedLoginCount, LockUntil
    )
    VALUES (
        'admin',
        CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2),
        N'System Administrator',
        'admin@example.com',
        @depId, NULL, 1,
        0, NULL
    );
END

-- 3) Bảo đảm có role ADMIN
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE [Code] = 'ADMIN')
    INSERT dbo.[Role]([Code],[Name]) VALUES ('ADMIN', N'Quản trị hệ thống');

-- 4) Gán role ADMIN cho admin (nếu chưa có)
DECLARE @adminId INT = (SELECT TOP 1 UserID FROM dbo.[User] WHERE Username='admin');
DECLARE @roleId  INT = (SELECT TOP 1 RoleID FROM dbo.[Role] WHERE [Code]='ADMIN');

IF @adminId IS NOT NULL AND @roleId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID=@adminId AND RoleID=@roleId)
    INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);

	UPDATE dbo.[User] SET FailedLoginCount=0, LockUntil=NULL WHERE Username='admin';

/* === ĐẢM BẢO ADMIN TỒN TẠI – KHÔNG DÙNG BIẾN === */

-- 0) Bổ sung cột bảo mật nếu thiếu
IF COL_LENGTH('dbo.[User]', 'FailedLoginCount') IS NULL
    ALTER TABLE dbo.[User] ADD FailedLoginCount INT NOT NULL DEFAULT(0);
IF COL_LENGTH('dbo.[User]', 'LockUntil') IS NULL
    ALTER TABLE dbo.[User] ADD LockUntil DATETIME2 NULL;

-- 1) Tạo phòng ban 'Administration' nếu chưa có
IF NOT EXISTS (SELECT 1 FROM dbo.Department WHERE [Name] = N'Administration')
    INSERT dbo.Department([Name]) VALUES (N'Administration');

-- 2) Tạo user admin (mật khẩu Admin@123, SHA-256 hex) nếu chưa có
IF NOT EXISTS (SELECT 1 FROM dbo.[User] WHERE Username = 'admin')
BEGIN
    DECLARE @pwd NVARCHAR(128) = N'Admin@123';
    INSERT dbo.[User](
        Username, PasswordHash, FullName, Email,
        DepartmentID, ManagerUserID, IsActive,
        FailedLoginCount, LockUntil
    )
    VALUES(
        'admin',
        CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', @pwd), 2),
        N'System Administrator',
        'admin@example.com',
        (SELECT TOP (1) DepartmentID FROM dbo.Department WHERE [Name]=N'Administration' ORDER BY DepartmentID),
        NULL, 1,
        0, NULL
    );
END

-- 3) Tạo role ADMIN nếu chưa có
IF NOT EXISTS (SELECT 1 FROM dbo.[Role] WHERE [Code] = 'ADMIN')
    INSERT dbo.[Role]([Code],[Name]) VALUES ('ADMIN', N'Quản trị hệ thống');

-- 4) Gán role ADMIN cho admin nếu chưa có
IF NOT EXISTS (
    SELECT 1
    FROM dbo.UserRole ur
    WHERE ur.UserID = (SELECT TOP (1) UserID FROM dbo.[User] WHERE Username='admin' ORDER BY UserID)
      AND ur.RoleID = (SELECT TOP (1) RoleID FROM dbo.[Role] WHERE [Code]='ADMIN' ORDER BY RoleID)
)
INSERT dbo.UserRole(UserID, RoleID)
VALUES (
    (SELECT TOP (1) UserID FROM dbo.[User] WHERE Username='admin' ORDER BY UserID),
    (SELECT TOP (1) RoleID FROM dbo.[Role] WHERE [Code]='ADMIN' ORDER BY RoleID)
);
UPDATE dbo.[User]
SET FailedLoginCount = 0, LockUntil = NULL
WHERE Username = 'admin';

-- SHA-256(UTF-8) của "Admin@123":
-- E86F78A8A3CAF0B60D8E74E5942AA6D86DC150CD3C03338AEF25B7D2D7E3ACC7
UPDATE dbo.[User]
SET PasswordHash = 'E86F78A8A3CAF0B60D8E74E5942AA6D86DC150CD3C03338AEF25B7D2D7E3ACC7',
    FailedLoginCount = 0,
    LockUntil = NULL
WHERE Username = 'admin';

DECLARE @adminId INT = (SELECT TOP 1 UserID FROM dbo.[User] WHERE Username='admin');
DECLARE @roleId  INT = (SELECT TOP 1 RoleID  FROM dbo.[Role] WHERE [Code]='ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID=@adminId AND RoleID=@roleId)
    INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);



	-- 1) Tài khoản có tồn tại & đang active?
SELECT UserID, Username, IsActive FROM dbo.[User] WHERE Username = 'alice';  -- thay username anh đã nhập ở /forgot

-- 2) Có bản ghi PENDING chưa?
SELECT TOP 20 * 
FROM dbo.PasswordResetRequest 
ORDER BY Id DESC;

-- 3) Nếu chưa có, INSERT thủ công để kiểm tra pipeline
INSERT dbo.PasswordResetRequest(UserId, Username)  
SELECT UserID, Username FROM dbo.[User] WHERE Username='alice';


DECLARE @adminId INT = (SELECT TOP 1 UserID FROM dbo.[User] WHERE Username='admin');
DECLARE @roleId  INT = (SELECT TOP 1 RoleID  FROM dbo.[Role] WHERE Code='ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID=@adminId AND RoleID=@roleId)
    INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);



	SELECT u.UserID, u.Username, r.Code AS RoleCode
FROM dbo.[User] u
LEFT JOIN dbo.UserRole ur ON ur.UserID=u.UserID
LEFT JOIN dbo.[Role] r ON r.RoleID=ur.RoleID
WHERE u.Username='admin';

-- nếu chưa có:
DECLARE @adminId INT = (SELECT TOP 1 UserID FROM dbo.[User] WHERE Username='admin');
DECLARE @roleId  INT = (SELECT TOP 1 RoleID  FROM dbo.[Role] WHERE Code='ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.UserRole WHERE UserID=@adminId AND RoleID=@roleId)
    INSERT dbo.UserRole(UserID, RoleID) VALUES (@adminId, @roleId);
