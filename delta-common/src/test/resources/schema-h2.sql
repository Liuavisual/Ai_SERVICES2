-- 客户满意度评价表（H2测试用）
-- 记录客户对服务完成的满意度评价
-- @author 刘建国

CREATE TABLE IF NOT EXISTS customer_satisfaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '客户ID',
    service_track_id BIGINT NOT NULL COMMENT '服务追踪ID',
    companion_id BIGINT COMMENT '陪玩师ID',
    rating INT NOT NULL COMMENT '评分(1-5)',
    feedback CLOB COMMENT '反馈内容',
    service_type VARCHAR(50) COMMENT '服务类型',
    tags VARCHAR(500) COMMENT '标签(逗号分隔)',
    is_anonymous INT DEFAULT 0 COMMENT '是否匿名(0-否,1-是)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标识'
);
