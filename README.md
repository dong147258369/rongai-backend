# rongai-backend

融AI 数字驾驶舱与融IEP个别化教育平台后端服务工程。

## 技术栈
- **核心框架**：Spring Boot 3.3.4
- **开发语言**：Java 21
- **持久层框架**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0（数据库名: `rongai_dmp`）
- **构建工具**：Maven 3.9+

## 核心接口说明 (RESTful API v1)
- 场景与数字驾驶舱：`/api/v1/cockpit/**`
- IEP 个案档案管理：`/api/v1/iep/cases/**`
- 目标审议决议：`/api/v1/iep/cases/{caseId}/goals/review`
- 会议建议写回：`/api/v1/iep/cases/{caseId}/meetings/advice/apply`
- 成稿签署生效：`/api/v1/iep/cases/{caseId}/document/effective`
- 多团队会议记录：`/api/v1/iep/meetings/**`

## 运行方式
```bash
# 编译打包
mvn clean package -DskipTests

# 启动服务 (默认端口 8090)
java -jar target/rongai-backend-1.0.0.jar
```
