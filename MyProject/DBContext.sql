/* ====== TẠO LẠI DB TỪ ĐẦU ====== */
IF DB_ID('ASSIGNMENT') IS NOT NULL
BEGIN
  ALTER DATABASE ASSIGNMENT SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
  DROP DATABASE ASSIGNMENT;
END;
CREATE DATABASE ASSIGNMENT;
GO
USE ASSIGNMENT;
GO

/* ====== SCHEMA ====== */
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
  Status        VARCHAR(20) NOT NULL,
  ProcessedBy   INT NULL,
  ProcessedNote NVARCHAR(1000) NULL,
  CreatedAt     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  UpdatedAt     DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
  CONSTRAINT FK_Request_Creator FOREIGN KEY (CreatedBy) REFERENCES [User](UserID),
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
CREATE INDEX IX_Request_DateRange ON [Request](DateFrom, DateTo);
GO

/* ====== DEPARTMENTS ====== */
INSERT INTO Department(Name) VALUES (N'IT'), (N'QA'), (N'Sale');

/* ====== ROLES & FEATURES ====== */
INSERT INTO Role(Code,Name)
SELECT v.Code, v.Name
FROM (VALUES
 ('DIV_LEADER', N'Division Leader'),
 ('TEAM_LEAD', N'Trưởng nhóm'),
 ('EMP',        N'Nhân viên')
) v(Code,Name)
WHERE NOT EXISTS (SELECT 1 FROM Role r WHERE r.Code=v.Code);

INSERT INTO Feature(Code,Name,PathPattern)
SELECT v.Code, v.Name, v.Path
FROM (VALUES
 ('REQ_MY',      N'Xem đơn của tôi',        '/requestlistmyservlet1'),
 ('REQ_CREATE',  N'Tạo đơn',                '/requestcreateservlet1'),
 ('REQ_MGR',     N'Xem đơn cấp dưới',       '/requestsubordinatesservlet1'),
 ('REQ_APPROVE', N'Duyệt đơn',              '/requestapproveservlet1'),
 ('AGD',         N'Agenda phòng ban',       '/agendaservlet1')
) v(Code,Name,Path)
WHERE NOT EXISTS (SELECT 1 FROM Feature f WHERE f.Code=v.Code);

-- RoleFeature
INSERT INTO RoleFeature(RoleID, FeatureID)
SELECT r.RoleID, f.FeatureID
FROM Role r CROSS JOIN Feature f
WHERE r.Code='EMP' AND f.Code IN ('REQ_MY','REQ_CREATE')
  AND NOT EXISTS (SELECT 1 FROM RoleFeature rf WHERE rf.RoleID=r.RoleID AND rf.FeatureID=f.FeatureID);

INSERT INTO RoleFeature(RoleID, FeatureID)
SELECT r.RoleID, f.FeatureID
FROM Role r CROSS JOIN Feature f
WHERE r.Code='TEAM_LEAD' AND f.Code IN ('REQ_MY','REQ_CREATE','REQ_MGR','REQ_APPROVE')
  AND NOT EXISTS (SELECT 1 FROM RoleFeature rf WHERE rf.RoleID=r.RoleID AND rf.FeatureID=f.FeatureID);

INSERT INTO RoleFeature(RoleID, FeatureID)
SELECT r.RoleID, f.FeatureID
FROM Role r CROSS JOIN Feature f
WHERE r.Code='DIV_LEADER' AND f.Code IN ('REQ_MY','REQ_CREATE','REQ_MGR','REQ_APPROVE','AGD')
  AND NOT EXISTS (SELECT 1 FROM RoleFeature rf WHERE rf.RoleID=r.RoleID AND rf.FeatureID=f.FeatureID);

/* ====== USERS (mật khẩu 123456) ====== */
-- Cara (Head IT)
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'cara',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Cara (Head IT)','cara@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='cara');

-- Alice (Team lead under Cara)
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'alice',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Alice (Team Lead)','alice@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='cara'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='alice');

-- Mike (Team lead under Cara)
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'mike',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Mike (Team Lead)','mike@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='cara'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='mike');

-- Bob / Ryder / Laura under Alice
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'bob',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Bob','bob@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='alice'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='bob');

INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'ryder',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Ryder','ryder@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='alice'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='ryder');

INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'laura',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Laura','laura@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='alice'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='laura');

-- Clause / Michel under Mike
INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'clause',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Clause','clause@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='mike'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='clause');

INSERT INTO [User](Username,PasswordHash,FullName,Email,DepartmentID,ManagerUserID,IsActive)
SELECT 'michel',
       CONVERT(VARCHAR(200), HASHBYTES('SHA2_256','123456'), 2),
       N'Michel','michel@company.com',
       (SELECT DepartmentID FROM Department WHERE Name=N'IT'),
       (SELECT UserID FROM [User] WHERE Username='mike'), 1
WHERE NOT EXISTS (SELECT 1 FROM [User] WHERE Username='michel');

-- Set Cara làm head phòng IT
UPDATE d SET d.ManagerUserID = (SELECT UserID FROM [User] WHERE Username='cara')
FROM Department d
WHERE d.Name = N'IT';

/* ====== GÁN ROLES ====== */
-- Cara: DIV_LEADER
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM [User] u JOIN Role r ON r.Code='DIV_LEADER'
WHERE u.Username='cara'
  AND NOT EXISTS (SELECT 1 FROM UserRole ur WHERE ur.UserID=u.UserID AND ur.RoleID=r.RoleID);

-- Alice & Mike: TEAM_LEAD
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM [User] u JOIN Role r ON r.Code='TEAM_LEAD'
WHERE u.Username IN ('alice','mike')
  AND NOT EXISTS (SELECT 1 FROM UserRole ur WHERE ur.UserID=u.UserID AND ur.RoleID=r.RoleID);

-- Nhân viên: EMP
INSERT INTO UserRole(UserID, RoleID)
SELECT u.UserID, r.RoleID
FROM [User] u JOIN Role r ON r.Code='EMP'
WHERE u.Username IN ('bob','ryder','laura','clause','michel')
  AND NOT EXISTS (SELECT 1 FROM UserRole ur WHERE ur.UserID=u.UserID AND ur.RoleID=r.RoleID);

/* ====== VERIFY ====== */
-- Quan hệ quản lý
SELECT u.Username, m.Username AS Manager, d.Name AS Department
FROM [User] u
LEFT JOIN [User] m ON m.UserID=u.ManagerUserID
JOIN Department d ON d.DepartmentID=u.DepartmentID
ORDER BY u.Username;

-- Quyền
SELECT u.Username, r.Code AS RoleCode, f.Code AS FeatureCode
FROM [User] u
JOIN UserRole ur ON ur.UserID=u.UserID
JOIN Role r ON r.RoleID=ur.RoleID
JOIN RoleFeature rf ON rf.RoleID=r.RoleID
JOIN Feature f ON f.FeatureID=rf.FeatureID
ORDER BY u.Username, RoleCode, FeatureCode;

-- Head IT
SELECT d.Name, u.Username AS Head
FROM Department d LEFT JOIN [User] u ON u.UserID=d.ManagerUserID
WHERE d.Name=N'IT';
