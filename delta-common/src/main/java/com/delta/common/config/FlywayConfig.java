package com.delta.common.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway数据库版本管理配置
 * 自动执行版本化SQL迁移脚本，确保开发/测试/生产环境数据库结构一致
 *
 * @author 刘建国
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    /** 基准版本，低于此版本的已存在数据库结构不会触发迁移 */
    @Value("${spring.flyway.baseline-version:1.0}")
    private String baselineVersion;

    /** 数据库迁移脚本位置 */
    @Value("${spring.flyway.locations:classpath:db/migration}")
    private String locations;

    /**
     * 配置Flyway实例
     *
     * @param dataSource 数据源
     * @return Flyway实例
     */
    @Bean
    public Flyway flyway(DataSource dataSource) {
        log.info("【Flyway】初始化数据库版本管理，基准版本: {}, 迁移脚本路径: {}", baselineVersion, locations);
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .baselineVersion(baselineVersion)
                .locations(locations)
                .table("flyway_schema_history")
                .cleanDisabled(true)
                .validateOnMigrate(true)
                .outOfOrder(false)
                .load();

        int pending = flyway.info().pending().length;
        if (pending > 0) {
            log.info("【Flyway】发现 {} 个待执行的迁移脚本，开始执行...", pending);
            int applied = flyway.migrate().migrationsExecuted;
            log.info("【Flyway】成功执行 {} 个迁移脚本", applied);
        } else {
            log.info("【Flyway】数据库已是最新版本，无需迁移");
        }
        return flyway;
    }
}