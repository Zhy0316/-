# 项目结构

## 根目录

```
/
├── Archive Management/     # 后端 Spring Boot 项目
├── bysj/                   # 前端 Vue 3 项目
├── 任务清单.txt             # 功能开发进度跟踪
└── .kiro/steering/         # AI 辅助规则文件
```

## 后端结构（Archive Management/src/main/java/com/example/archivemanagement/）

```
├── config/
│   ├── AuthInterceptor.java      # 角色权限拦截器（白名单 + roleKey 校验）
│   ├── JwtAuthFilter.java        # JWT Token 解析过滤器
│   ├── WebConfig.java            # CORS + 静态资源映射（/uploads/**）
│   ├── WebMvcConfig.java         # 注册拦截器
│   ├── MybatisPlusConfig.java    # 分页插件配置
│   └── DataInitializer.java      # 启动时初始化角色和测试数据
├── controller/                   # REST 控制器，路径前缀 /api
├── service/                      # 业务逻辑层
│   └── impl/                     # Service 实现类
├── mapper/                       # MyBatis Plus Mapper 接口
├── entity/                       # 数据库实体类（对应表结构）
├── dto/                          # 请求/响应数据传输对象
└── util/
    ├── JwtUtil.java              # Token 生成与解析
    └── FileUploadUtil.java       # 文件上传工具（按类型分目录）
```

### 实体类与表对应关系

| 实体类 | 数据库表 | 说明 |
|--------|---------|------|
| SysUser | sys_user | 用户基本信息 |
| SysRole | sys_role | 角色（admin/student/tutor/hr）|
| SysUserRole | sys_user_role | 用户角色关联 |
| InfoStudent | info_student | 学生档案 |
| InfoTutor | info_tutor | 导师信息 |
| InfoEnterprise | info_enterprise | 企业信息及认证 |
| RecordAcademic | record_academic | 学业成绩 |
| RecordAward | record_award | 获奖记录 |
| RecordDiary | record_diary | 成长日记 |
| RecordPortfolio | record_portfolio | 作品集 |
| RecordPractice | record_practice | 实践记录 |
| RecordResearch | record_research | 科研项目 |
| EvalComment | eval_comment | 导师评语 |
| EvalGrowthScore | eval_growth_score | 成长综合评分 |
| BusRecruitment | bus_recruitment | 招聘信息 |
| BusJobApplication | bus_job_application | 求职申请 |
| StudentTutorRelation | student_tutor_relation | 学生导师绑定关系 |

## 前端结构（bysj/src/）

```
├── main.js                 # 应用入口，注册 Element Plus、Pinia、Router
├── App.vue
├── router/
│   └── index.js            # 路由配置 + 导航守卫（角色校验）
├── stores/
│   └── user.js             # Pinia store：token、userInfo、roleKey
├── services/               # Axios API 封装（每模块一个文件）
│   ├── api.js              # Axios 实例（拦截器、baseURL=localhost:8083/api）
│   ├── auth.js             # 登录/注册/修改密码
│   ├── student.js          # 学生档案
│   ├── academic.js         # 学业成绩
│   ├── award.js            # 获奖记录
│   ├── diary.js            # 成长日记
│   ├── portfolio.js        # 作品集
│   ├── practice.js         # 实践记录
│   ├── research.js         # 科研项目
│   ├── comment.js          # 导师评语
│   ├── enterprise.js       # 企业信息
│   ├── recruitment.js      # 招聘求职
│   ├── stats.js            # 数据统计
│   ├── export.js           # 报表导出
│   └── admin.js            # 管理员接口
├── views/
│   ├── Login.vue           # 多角色登录
│   ├── Register.vue        # 分角色注册（学生/导师/企业HR）
│   ├── student/            # 学生端（StudentDashboard/Profile/Academic/Record/Portfolio/GrowthScore/Recruitment）
│   ├── tutor/              # 导师端（TutorDashboard/Home/Students/StudentDetail/Pending/CommentList）
│   ├── hr/                 # 企业HR端（HrDashboard/HrProfile/RecruitmentManage/StudentBrowse/ApplicationList）
│   └── admin/              # 管理员端（AdminDashboard/UserManage/EnterpriseAudit/GrowthRanking/ReportExport）
└── components/             # 公共组件（当前仅 HelloWorld.vue，图表为各页面内联实现）
```

## 开发约定

- 后端接口统一前缀 `/api`，按资源分组（`/api/student`、`/api/academic` 等）
- 管理员专属接口使用 `/api/admin` 前缀，由 AuthInterceptor 统一校验 admin 角色
- 前端每个业务模块对应 `services/` 下一个独立文件，通过默认导出的 api 实例调用
- 文件上传响应统一返回 `{ filePath: "..." }`，路径可通过 `/uploads/...` 访问
- 数据库 SQL 初始化文件：`Archive Management/bysj.sql`
