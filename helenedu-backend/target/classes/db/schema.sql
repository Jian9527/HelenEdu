-- 创建数据库
CREATE DATABASE IF NOT EXISTS helen_edu DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE helen_edu;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(64) UNIQUE COMMENT '微信openid',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    role TINYINT NOT NULL COMMENT '角色: 1-学生 2-教师 3-管理员',
    avatar_url VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 班级表
CREATE TABLE edu_class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '班级名称',
    grade VARCHAR(50) COMMENT '年级',
    teacher_id BIGINT COMMENT '班主任ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-解散 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '班级表';

-- 班级学生关联表
CREATE TABLE edu_class_student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    UNIQUE KEY uk_class_student (class_id, student_id)
) COMMENT '班级学生关联表';

-- 班级教师关联表
CREATE TABLE edu_class_teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    subject VARCHAR(50) COMMENT '科目',
    UNIQUE KEY uk_class_teacher (class_id, teacher_id)
) COMMENT '班级教师关联表';

-- 作业表
CREATE TABLE edu_homework (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '作业标题',
    content TEXT COMMENT '作业内容/要求',
    class_id BIGINT NOT NULL COMMENT '所属班级',
    teacher_id BIGINT NOT NULL COMMENT '布置教师',
    subject VARCHAR(50) COMMENT '科目',
    deadline DATETIME COMMENT '截止时间',
    attachment_urls JSON COMMENT '附件URL列表',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-已截止',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '作业表';

-- 作业提交表
CREATE TABLE edu_homework_submit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    content TEXT COMMENT '提交内容',
    attachment_urls JSON COMMENT '附件URL列表',
    score DECIMAL(5,2) COMMENT '分数',
    comment TEXT COMMENT '教师评语',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-已提交 1-已批改 2-已退回',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    review_time DATETIME COMMENT '批改时间',
    UNIQUE KEY uk_hw_student (homework_id, student_id)
) COMMENT '作业提交表';

-- 预习资料表
CREATE TABLE edu_preview_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '资料标题',
    description TEXT COMMENT '资料描述',
    class_id BIGINT NOT NULL COMMENT '所属班级',
    teacher_id BIGINT NOT NULL COMMENT '发布教师',
    subject VARCHAR(50) COMMENT '科目',
    file_urls JSON COMMENT '文件URL列表',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-发布',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '预习资料表';

-- 插入初始管理员账户（openid 可为空，后续通过微信登录绑定）
INSERT INTO sys_user (name, phone, role, status) VALUES ('系统管理员', '13800000000', 3, 1);
INSERT INTO sys_user (name, phone, role, status) VALUES ('张老师', '13800000001', 2, 1);
INSERT INTO sys_user (name, phone, role, status) VALUES ('李同学', '13800000002', 1, 1);
