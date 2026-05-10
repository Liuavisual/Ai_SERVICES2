## 项目启动

在 Trae 终端中启动项目，确保中文日志不乱码：

### 第一步：切换编码（解决中文乱码）

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; $OutputEncoding = [System.Text.Encoding]::UTF8
```

### 第二步：启动项目

```powershell
cd delta-admin
mvn spring-boot:run
```

或者一条命令搞定：

```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; $OutputEncoding = [System.Text.Encoding]::UTF8; Set-Location delta-admin; mvn spring-boot:run
```

### 停止项目

按 `Ctrl+C` 即可停止。

### 数据库

- 用户名：`root`
- 密码：`123456`
- 数据库：`delta_ai_customer_service`

### 登录账号

- 用户名：`admin`
- 密码：`123456`
