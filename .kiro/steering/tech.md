# 技术栈

## 后端（Archive Management/）

- Java 17 + Spring Boot 3.2.0
- MyBatis Plus 3.5.15（ORM，含分页插件）
- MySQL 5.7+（数据库名：bysj，端口 3306，用户 root/123456）
- JWT 认证：jjwt 0.12.3（手动拦截器方案，无 Spring Security）
- EasyExcel 3.3.4（Excel 导入导出）
- Lombok（实体类简化）
- 服务端口：8083

## 前端（bysj/）

- Vue 3.2 + Vue CLI 5
- Vue Router 4.2（路由守卫，按角色分组）
- Pinia 2.1（状态管理，持久化到 localStorage）
- Element Plus 2.3（UI 组件库）
- Axios 1.4（HTTP，自动携带 Bearer Token，统一错误处理）
- ECharts 5.4（图表，直接使用原生 API，非 vue-echarts）
- xlsx 0.18（Excel 处理）
- 开发端口：8084，代理 /api 和 /uploads 到 8083

## 常用命令

### 后端
```bash
# 在 Archive Management/ 目录下
./mvnw spring-boot:run          # 启动后端（端口 8083）
./mvnw clean package            # 打包
./mvnw test                     # 运行测试
```

### 前端
```bash
# 在 bysj/ 目录下
npm install                     # 安装依赖
npm run serve                   # 启动开发服务器（端口 8084）
npm run build                   # 生产构建
npm run lint                    # 代码检查
```

## 关键配置

- 后端配置文件：`Archive Management/src/main/resources/application.properties`
- 文件上传限制：100MB，按类型分目录存储（uploads/avatar/、uploads/resume/ 等）
- JWT 密钥：`bysj-archive-management-secret-key-2024-very-long`，有效期 24 小时
- 跨域：WebConfig.java 已配置，允许 localhost:8084
- 启动时自动初始化：四个角色 + admin 账号（admin/admin123）+ 三个测试导师账号
