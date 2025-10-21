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