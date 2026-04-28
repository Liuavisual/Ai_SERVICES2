#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Delta AI Customer Service - 数据库测试数据管理脚本

功能：
  1. 数据还原：还原faq_items和keywords表的脱敏数据
  2. 测试数据生成：导入完整业务测试数据
  3. 脱敏处理：对指定表执行脱敏操作，生成生产环境安全数据
  4. 数据备份：执行关键操作前自动创建备份
  5. 日志记录：详细记录所有操作过程

使用方法：
  python db_manager.py restore          # 还原脱敏数据
  python db_manager.py seed             # 导入测试数据
  python db_manager.py desensitize      # 执行脱敏处理
  python db_manager.py backup           # 手动创建备份
  python db_manager.py full-setup       # 完整初始化（还原+导入）
  python db_manager.py go-production    # 生产发布（备份+脱敏+导出）

配置：
  通过 config.ini 或环境变量配置数据库连接信息
"""

import os
import sys
import json
import logging
import subprocess
import hashlib
import shutil
import re
from datetime import datetime
from pathlib import Path
from configparser import ConfigParser

SCRIPT_DIR = Path(__file__).parent.resolve()
PROJECT_DIR = SCRIPT_DIR.parent
CONFIG_FILE = SCRIPT_DIR / "config.ini"
LOG_DIR = SCRIPT_DIR / "logs"
BACKUP_DIR = SCRIPT_DIR / "backups"
OUTPUT_DIR = SCRIPT_DIR / "output"

LOG_DIR.mkdir(exist_ok=True)
BACKUP_DIR.mkdir(exist_ok=True)
OUTPUT_DIR.mkdir(exist_ok=True)

DESENSITIZE_TABLES = ["faq_items", "keywords"]
SENSITIVE_TABLES = ["sys_user", "companions", "platform_configs", "ai_config"]


def setup_logging():
    log_file = LOG_DIR / f"db_manager_{datetime.now().strftime('%Y%m%d')}.log"
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
        handlers=[
            logging.FileHandler(log_file, encoding="utf-8"),
            logging.StreamHandler(sys.stdout),
        ],
    )
    return logging.getLogger("db_manager")


logger = setup_logging()


def load_config():
    config = ConfigParser()
    if CONFIG_FILE.exists():
        config.read(CONFIG_FILE, encoding="utf-8")
    else:
        config["database"] = {
            "host": os.getenv("DB_HOST", "localhost"),
            "port": os.getenv("DB_PORT", "3306"),
            "user": os.getenv("DB_USERNAME", "root"),
            "password": os.getenv("DB_PASSWORD", "123456"),
            "database": os.getenv("DB_NAME", "delta_ai_customer_service"),
            "charset": "utf8mb4",
        }
        with open(CONFIG_FILE, "w", encoding="utf-8") as f:
            config.write(f)
        logger.info(f"已生成默认配置文件: {CONFIG_FILE}")

    db = config["database"]
    return {
        "host": db.get("host", "localhost"),
        "port": db.getint("port", 3306),
        "user": db.get("user", "root"),
        "password": db.get("password", "123456"),
        "database": db.get("database", "delta_ai_customer_service"),
        "charset": db.get("charset", "utf8mb4"),
    }


def run_mysql(cfg, sql, capture_output=False):
    cmd = [
        "mysql",
        f"-h{cfg['host']}",
        f"-P{cfg['port']}",
        f"-u{cfg['user']}",
        f"-p{cfg['password']}",
        f"--default-character-set={cfg['charset']}",
        "-N",
        "-B",
        cfg["database"],
        "-e",
        sql,
    ]
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            encoding="utf-8",
            timeout=300,
        )
        if result.returncode != 0:
            logger.error(f"MySQL执行失败: {result.stderr.strip()}")
            return None
        return result.stdout.strip() if capture_output else True
    except FileNotFoundError:
        logger.error("未找到mysql命令，请确保MySQL客户端已安装并在PATH中")
        sys.exit(1)
    except subprocess.TimeoutExpired:
        logger.error("MySQL命令执行超时")
        return None


def run_mysql_file(cfg, sql_file):
    cmd = [
        "mysql",
        f"-h{cfg['host']}",
        f"-P{cfg['port']}",
        f"-u{cfg['user']}",
        f"-p{cfg['password']}",
        f"--default-character-set={cfg['charset']}",
        cfg["database"],
        "-e",
        f"source {sql_file}",
    ]
    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            encoding="utf-8",
            timeout=600,
        )
        if result.returncode != 0:
            logger.error(f"SQL文件执行失败: {result.stderr.strip()}")
            return False
        return True
    except FileNotFoundError:
        logger.error("未找到mysql命令，请确保MySQL客户端已安装并在PATH中")
        sys.exit(1)
    except subprocess.TimeoutExpired:
        logger.error("SQL文件执行超时")
        return False


def run_mysqldump(cfg, output_file, tables=None):
    cmd = [
        "mysqldump",
        f"-h{cfg['host']}",
        f"-P{cfg['port']}",
        f"-u{cfg['user']}",
        f"-p{cfg['password']}",
        f"--default-character-set={cfg['charset']}",
        "--single-transaction",
        "--routines",
        "--triggers",
        cfg["database"],
    ]
    if tables:
        cmd.extend(tables)

    try:
        with open(output_file, "w", encoding="utf-8") as f:
            result = subprocess.run(
                cmd,
                stdout=f,
                stderr=subprocess.PIPE,
                text=True,
                encoding="utf-8",
                timeout=600,
            )
        if result.returncode != 0:
            logger.error(f"备份失败: {result.stderr.strip()}")
            return False
        return True
    except FileNotFoundError:
        logger.error("未找到mysqldump命令，请确保MySQL客户端已安装并在PATH中")
        sys.exit(1)
    except subprocess.TimeoutExpired:
        logger.error("备份超时")
        return False


def get_row_count(cfg, table):
    result = run_mysql(cfg, f"SELECT COUNT(*) FROM `{table}`;", capture_output=True)
    if result:
        return int(result.split("\n")[0])
    return -1


def backup_database(cfg, tables=None, label="manual"):
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    suffix = "_".join(tables) if tables else "full"
    backup_file = BACKUP_DIR / f"backup_{label}_{suffix}_{timestamp}.sql"

    logger.info(f"正在创建数据库备份: {backup_file}")
    start_time = datetime.now()

    success = run_mysqldump(cfg, str(backup_file), tables)

    elapsed = (datetime.now() - start_time).total_seconds()

    if success and backup_file.exists():
        file_size = backup_file.stat().st_size / 1024
        logger.info(f"备份完成 | 文件: {backup_file.name} | 大小: {file_size:.1f}KB | 耗时: {elapsed:.1f}s")
        return backup_file
    else:
        logger.error("备份失败")
        return None


def cmd_restore(cfg):
    logger.info("=" * 60)
    logger.info("开始执行: 数据还原（faq_items + keywords）")
    logger.info("=" * 60)

    backup_database(cfg, DESENSITIZE_TABLES, label="pre_restore")

    test_data_file = SCRIPT_DIR / "test_data.sql"
    if not test_data_file.exists():
        logger.error(f"测试数据文件不存在: {test_data_file}")
        return False

    for table in DESENSITIZE_TABLES:
        count_before = get_row_count(cfg, table)
        logger.info(f"表 {table} 当前数据量: {count_before}")

    logger.info("正在清除脱敏数据并导入还原数据...")

    restore_sql = f"""
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `faq_items`;
DELETE FROM `keywords`;

-- 重新导入还原数据由test_data.sql中的INSERT语句完成
SET FOREIGN_KEY_CHECKS = 1;
"""
    temp_file = SCRIPT_DIR / "_temp_restore.sql"
    with open(temp_file, "w", encoding="utf-8") as f:
        f.write(restore_sql)

    if not run_mysql_file(cfg, str(temp_file)):
        temp_file.unlink()
        return False
    temp_file.unlink()

    faq_inserts = []
    keyword_inserts = []
    in_faq = False
    in_keyword = False

    with open(test_data_file, "r", encoding="utf-8") as f:
        for line in f:
            stripped = line.strip()
            if "INSERT INTO `faq_items`" in stripped:
                in_faq = True
                faq_inserts.append(line)
                if stripped.endswith(";"):
                    in_faq = False
                continue
            if "INSERT INTO `keywords`" in stripped:
                in_keyword = True
                keyword_inserts.append(line)
                if stripped.endswith(";"):
                    in_keyword = False
                continue
            if in_faq:
                faq_inserts.append(line)
                if stripped.endswith(";"):
                    in_faq = False
            if in_keyword:
                keyword_inserts.append(line)
                if stripped.endswith(";"):
                    in_keyword = False

    restore_insert_file = SCRIPT_DIR / "_temp_restore_inserts.sql"
    with open(restore_insert_file, "w", encoding="utf-8") as f:
        f.write("SET NAMES utf8mb4;\n")
        f.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")
        f.writelines(faq_inserts)
        f.write("\n\n")
        f.writelines(keyword_inserts)
        f.write("\n\nSET FOREIGN_KEY_CHECKS = 1;\n")

    success = run_mysql_file(cfg, str(restore_insert_file))
    restore_insert_file.unlink()

    if success:
        for table in DESENSITIZE_TABLES:
            count_after = get_row_count(cfg, table)
            logger.info(f"表 {table} 还原后数据量: {count_after}")
        logger.info("数据还原完成 ✓")
    else:
        logger.error("数据还原失败 ✗")

    return success


def cmd_seed(cfg):
    logger.info("=" * 60)
    logger.info("开始执行: 导入测试数据")
    logger.info("=" * 60)

    test_data_file = SCRIPT_DIR / "test_data.sql"
    if not test_data_file.exists():
        logger.error(f"测试数据文件不存在: {test_data_file}")
        return False

    backup_database(cfg, label="pre_seed")

    all_tables = [
        "sys_user", "users", "customer_profile", "messages", "keywords",
        "replies", "ai_config", "platform_configs", "pending_messages",
        "cs_user_customer", "companion_levels", "companions",
        "companion_schedules", "club_config", "club_level_prices",
        "game_config", "service_item", "service_price_rule",
        "activity_package", "orders", "customer_order_record",
        "work_orders", "work_order_records", "work_order_attachments",
        "service_tracks", "faq_items",
    ]

    logger.info("检查现有数据...")
    existing_data = False
    for table in all_tables:
        count = get_row_count(cfg, table)
        if count > 0:
            logger.warning(f"表 {table} 已有 {count} 条数据")
            existing_data = True

    if existing_data:
        logger.warning("检测到已有数据，将使用 INSERT IGNORE 避免主键冲突")
        logger.info("正在转换INSERT为INSERT IGNORE...")
        converted_file = SCRIPT_DIR / "_temp_seed_converted.sql"
        with open(test_data_file, "r", encoding="utf-8") as rf:
            content = rf.read()
        content = re.sub(r"INSERT INTO", "INSERT IGNORE INTO", content)
        with open(converted_file, "w", encoding="utf-8") as wf:
            wf.write(content)
        target_file = converted_file
    else:
        target_file = test_data_file

    logger.info(f"正在导入测试数据: {target_file}")
    start_time = datetime.now()

    success = run_mysql_file(cfg, str(target_file))

    elapsed = (datetime.now() - start_time).total_seconds()

    if existing_data and target_file != test_data_file:
        target_file.unlink()

    if success:
        logger.info(f"测试数据导入完成 | 耗时: {elapsed:.1f}s")
        logger.info("各表数据量统计:")
        for table in all_tables:
            count = get_row_count(cfg, table)
            logger.info(f"  {table:30s} : {count} 条")
    else:
        logger.error("测试数据导入失败 ✗")

    return success


def cmd_desensitize(cfg):
    logger.info("=" * 60)
    logger.info("开始执行: 数据脱敏处理")
    logger.info("=" * 60)

    backup_database(cfg, label="pre_desensitize")

    desensitize_sql = generate_desensitize_sql(cfg)

    temp_file = SCRIPT_DIR / "_temp_desensitize.sql"
    with open(temp_file, "w", encoding="utf-8") as f:
        f.write(desensitize_sql)

    logger.info("正在执行脱敏SQL...")
    start_time = datetime.now()

    success = run_mysql_file(cfg, str(temp_file))
    temp_file.unlink()

    elapsed = (datetime.now() - start_time).total_seconds()

    if success:
        logger.info(f"脱敏处理完成 | 耗时: {elapsed:.1f}s")
        logger.info("脱敏范围:")
        logger.info("  - faq_items: 问题分类/问题内容/答案内容 已脱敏")
        logger.info("  - keywords: 关键词内容 已脱敏")
        logger.info("  - sys_user: 手机号/邮箱 已脱敏")
        logger.info("  - companions: 手机号/微信号 已脱敏")
        logger.info("  - platform_configs: 配置密钥 已脱敏")
        logger.info("  - ai_config: 敏感配置值 已脱敏")
    else:
        logger.error("脱敏处理失败 ✗")

    return success


def generate_desensitize_sql(cfg):
    sql_lines = [
        "SET NAMES utf8mb4;",
        "SET FOREIGN_KEY_CHECKS = 0;",
        "",
        "-- ============================================================",
        "-- 自动生成的脱敏SQL",
        f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
        "-- ============================================================",
        "",
        "-- 1. faq_items 脱敏：问题分类、问题内容、答案内容",
        "UPDATE `faq_items` SET",
        "  `category` = CONCAT('分类_', `id`),",
        "  `question` = CONCAT('问题_', `id`, '_', LEFT(MD5(`question`), 8)),",
        "  `answer` = CONCAT('答案_', `id`, '_', LEFT(MD5(`answer`), 8));",
        "",
        "-- 2. keywords 脱敏：关键词内容",
        "UPDATE `keywords` SET",
        "  `keyword` = CONCAT('关键词_', `id`, '_', LEFT(MD5(`keyword`), 6));",
        "",
        "-- 3. sys_user 脱敏：手机号、邮箱",
        "UPDATE `sys_user` SET",
        "  `phone` = CONCAT(LEFT(`phone`, 3), '****', RIGHT(`phone`, 4)),",
        "  `email` = CONCAT(LEFT(`email`, 1), '***', SUBSTRING(`email`, LOCATE('@', `email`)));",
        "",
        "-- 4. companions 脱敏：手机号、微信号",
        "UPDATE `companions` SET",
        "  `phone` = CONCAT(LEFT(`phone`, 3), '****', RIGHT(`phone`, 4)),",
        "  `wechat` = CONCAT(LEFT(`wechat`, 2), '****');",
        "",
        "-- 5. platform_configs 脱敏：配置中的敏感信息",
        "UPDATE `platform_configs` SET",
        "  `config` = JSON_SET(",
        "    `config`,",
        "    '$.appSecret', '****masked****',",
        "    '$.secret', '****masked****',",
        "    '$.token', '****masked****',",
        "    '$.aesKey', '****masked****',",
        "    '$.botToken', '****masked****',",
        "    '$.appSecret', '****masked****'",
        "  );",
        "",
        "-- 6. ai_config 脱敏：敏感配置值",
        "UPDATE `ai_config` SET",
        "  `config_value` = CASE",
        "    WHEN `config_key` IN ('system_prompt') THEN CONCAT(LEFT(`config_value`, 10), '****', RIGHT(`config_value`, 10))",
        "    ELSE `config_value`",
        "  END",
        "WHERE `config_key` IN ('system_prompt');",
        "",
        "SET FOREIGN_KEY_CHECKS = 1;",
    ]
    return "\n".join(sql_lines)


def cmd_backup(cfg):
    logger.info("=" * 60)
    logger.info("开始执行: 手动备份")
    logger.info("=" * 60)

    result = backup_database(cfg, label="manual")
    return result is not None


def cmd_full_setup(cfg):
    logger.info("=" * 60)
    logger.info("开始执行: 完整初始化（还原 + 导入测试数据）")
    logger.info("=" * 60)

    backup_database(cfg, label="pre_full_setup")

    logger.info("步骤 1/2: 导入测试数据（含还原数据）...")
    if not cmd_seed(cfg):
        logger.error("完整初始化失败：测试数据导入失败")
        return False

    logger.info("步骤 2/2: 验证数据完整性...")
    all_tables = [
        "sys_user", "users", "customer_profile", "messages", "keywords",
        "replies", "ai_config", "platform_configs", "pending_messages",
        "cs_user_customer", "companion_levels", "companions",
        "companion_schedules", "club_config", "club_level_prices",
        "game_config", "service_item", "service_price_rule",
        "activity_package", "orders", "customer_order_record",
        "work_orders", "work_order_records", "work_order_attachments",
        "service_tracks", "faq_items",
    ]

    all_ok = True
    for table in all_tables:
        count = get_row_count(cfg, table)
        status = "✓" if count > 0 else "✗"
        logger.info(f"  {status} {table:30s} : {count} 条")
        if count <= 0:
            all_ok = False

    if all_ok:
        logger.info("完整初始化成功 ✓ 所有表均有数据")
    else:
        logger.warning("部分表无数据，请检查 ✗")

    return all_ok


def cmd_go_production(cfg):
    logger.info("=" * 60)
    logger.info("开始执行: 生产发布流程")
    logger.info("=" * 60)

    logger.info("步骤 1/4: 创建完整备份...")
    backup_file = backup_database(cfg, label="pre_production")
    if not backup_file:
        logger.error("备份失败，终止生产发布流程")
        return False

    logger.info("步骤 2/4: 执行脱敏处理...")
    if not cmd_desensitize(cfg):
        logger.error("脱敏失败，终止生产发布流程")
        return False

    logger.info("步骤 3/4: 导出脱敏后数据...")
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    output_file = OUTPUT_DIR / f"production_data_desensitized_{timestamp}.sql"

    if run_mysqldump(cfg, str(output_file)):
        file_size = output_file.stat().st_size / 1024
        logger.info(f"脱敏数据已导出: {output_file.name} ({file_size:.1f}KB)")
    else:
        logger.error("脱敏数据导出失败")
        return False

    logger.info("步骤 4/4: 生成脱敏报告...")
    report = generate_report(cfg, backup_file, output_file)
    report_file = OUTPUT_DIR / f"desensitize_report_{timestamp}.json"
    with open(report_file, "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
    logger.info(f"脱敏报告已生成: {report_file.name}")

    logger.info("=" * 60)
    logger.info("生产发布流程完成 ✓")
    logger.info(f"备份文件: {backup_file}")
    logger.info(f"脱敏数据: {output_file}")
    logger.info(f"脱敏报告: {report_file}")
    logger.info("=" * 60)

    return True


def generate_report(cfg, backup_file, output_file):
    all_tables = [
        "sys_user", "users", "customer_profile", "messages", "keywords",
        "replies", "ai_config", "platform_configs", "pending_messages",
        "cs_user_customer", "companion_levels", "companions",
        "companion_schedules", "club_config", "club_level_prices",
        "game_config", "service_item", "service_price_rule",
        "activity_package", "orders", "customer_order_record",
        "work_orders", "work_order_records", "work_order_attachments",
        "service_tracks", "faq_items",
    ]

    table_stats = {}
    for table in all_tables:
        count = get_row_count(cfg, table)
        table_stats[table] = count

    report = {
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "operation": "production_desensitize",
        "database": cfg["database"],
        "backup_file": str(backup_file),
        "output_file": str(output_file),
        "desensitized_tables": DESENSITIZE_TABLES + SENSITIVE_TABLES,
        "table_statistics": table_stats,
        "total_records": sum(table_stats.values()),
        "desensitize_rules": {
            "faq_items": "问题分类/问题内容/答案内容 → 哈希摘要",
            "keywords": "关键词内容 → 哈希摘要",
            "sys_user": "手机号 → 中间4位掩码, 邮箱 → 用户名掩码",
            "companions": "手机号 → 中间4位掩码, 微信号 → 保留前2位+掩码",
            "platform_configs": "appSecret/secret/token/aesKey → 掩码替换",
            "ai_config": "system_prompt → 首尾保留+中间掩码",
        },
    }

    if backup_file and backup_file.exists():
        report["backup_file_md5"] = hashlib.md5(
            backup_file.read_bytes()
        ).hexdigest()
    if output_file and output_file.exists():
        report["output_file_md5"] = hashlib.md5(
            output_file.read_bytes()
        ).hexdigest()

    return report


def print_usage():
    usage = """
╔══════════════════════════════════════════════════════════════╗
║       Delta AI Customer Service - 数据库管理脚本            ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  用法: python db_manager.py <命令>                           ║
║                                                              ║
║  命令:                                                       ║
║    restore          还原faq_items和keywords的脱敏数据        ║
║    seed             导入完整测试数据                          ║
║    desensitize      执行脱敏处理                              ║
║    backup           手动创建数据库备份                        ║
║    full-setup       完整初始化（还原+导入）                   ║
║    go-production    生产发布（备份+脱敏+导出+报告）           ║
║                                                              ║
║  配置文件: scripts/config.ini                                 ║
║  日志目录: scripts/logs/                                      ║
║  备份目录: scripts/backups/                                   ║
║  输出目录: scripts/output/                                    ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
"""
    print(usage)


def main():
    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)

    command = sys.argv[1].lower()
    cfg = load_config()

    logger.info(f"数据库: {cfg['host']}:{cfg['port']}/{cfg['database']}")
    logger.info(f"执行命令: {command}")

    test_conn = run_mysql(cfg, "SELECT 1;", capture_output=True)
    if test_conn is None:
        logger.error("数据库连接失败，请检查配置")
        sys.exit(1)
    logger.info("数据库连接成功 ✓")

    commands = {
        "restore": cmd_restore,
        "seed": cmd_seed,
        "desensitize": cmd_desensitize,
        "backup": cmd_backup,
        "full-setup": cmd_full_setup,
        "go-production": cmd_go_production,
    }

    if command not in commands:
        logger.error(f"未知命令: {command}")
        print_usage()
        sys.exit(1)

    success = commands[command](cfg)
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
