# 归档脚本目录
# 存放历史API测试脚本、健康检查脚本、部署辅助脚本等
#
# 文件清单:
# - health_check.ps1: 自动化健康检查脚本（28个端点检查，含JSON/HTML报告生成）
#   用途: 配合Windows任务计划程序实现定时健康检查
#   迁移原因: 已完成其使命，保留供后续运维参考
#
# - ci_test.ps1: CI/CD全流程自动化测试脚本（构建/部署/测试/报告）
#   用途: 本地CI/CD模拟，构建→启动→AP测试→性能验证全流程
#   迁移原因: .github/workflows/ci.yml已取代其CI/CD功能
#
# - test_all_apis_v2.ps1: API全量回归测试脚本v2
#   用途: 覆盖所有API端点的自动化测试
#   迁移原因: 测试已通过，保留供回归测试参考
#
# - test_all_apis.ps1: API全量测试脚本v1（原始版）
#   用途: 初期API测试
#   迁移原因: 已被v2版本取代
#
# - api_full_test.ps1: API完整测试脚本
#   用途: 完整API功能验证
#   迁移原因: 测试已通过，保留供参考
#
# - generate_sensitive_words.ps1: 敏感词生成脚本
#   用途: 批量生成测试敏感词数据
#   迁移原因: 一次性数据生成工具，已完成使命
#
# - db_manager.py: 数据库管理Python脚本
#   用途: 数据库备份、还原、迁移辅助工具
#   迁移原因: Flyway已接管数据库版本管理
#
# - extract_docx.ps1: DOCX文档内容提取脚本
#   用途: 从.docx文件中提取纯文本内容
#   迁移原因: 一次性使用工具
#
# - extract.js: DOCX提取脚本Node.js版
#   用途: Node.js版本DOCX内容提取
#   迁移原因: 一次性使用工具
#
# - auto-commit.log: 自动提交日志
#   用途: 记录Git自动提交操作历史
#   迁移原因: 历史操作记录，供审计参考