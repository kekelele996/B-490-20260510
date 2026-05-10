# 🏥 在线心理咨询预约系统

基于 **Spring Boot + Vue 3** 的全栈心理咨询预约平台，提供咨询师排班、用户预约、实时聊天、钱包支付、多角色管理等功能。界面经过精心优化，采用磨砂玻璃质感与高级渐变的现代化视觉设计。

---

## How to Run

```bash
cp .env.example .env
docker compose up
```

> 首次启动会构建镜像并初始化数据库，请耐心等待。

## Services

- Frontend: [http://localhost:3300](http://localhost:3300)
- Backend API: [http://localhost:8080](http://localhost:8080)
- MySQL: `localhost:3306` (`root/password`)

## Verification

1. 打开前端页面，使用 `user1 / 123456` 登录用户端。
2. 在“个人信息 -> 成为心理咨询师”上传资质并提交，确认提示为“已开通咨询师权限”，并可进入 `/counselor`。
3. 在“我的钱包”执行充值，确认余额与交易记录同步更新。
4. 在用户端发起预约，确认咨询师端可看到预约并可修改状态。
5. 在用户端与咨询师端进行聊天，确认消息可实时收发。

自测截图归档模板见：`docs/self-test/README.md`。

---

## 🛠 技术栈

### 前端 (Frontend)

| 类别 | 技术 |
| :--- | :--- |
| 核心框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus（定制 Premium 主题） |
| 状态管理 | Pinia |
| 路由管理 | Vue Router 4 |
| HTTP 通信 | Axios |
| 实时通信 | @stomp/stompjs + SockJS |
| 样式 | SCSS + CSS3 Animations + Glassmorphism |

### 后端 (Backend)

| 类别 | 技术 |
| :--- | :--- |
| 核心框架 | Spring Boot 2.7 |
| ORM 框架 | MyBatis Plus 3.5（IService / ServiceImpl 架构） |
| 安全框架 | Spring Security + JWT（jjwt） |
| 数据库 | MySQL 8.0（Docker 容器） |
| 实时通信 | Spring WebSocket（STOMP 协议） |
| 工具库 | Lombok、Fastjson2、BCrypt |

### 运维 & 部署

| 类别 | 技术 |
| :--- | :--- |
| 容器化 | Docker + Docker Compose |
| 构建工具 | Maven (Backend)、NPM (Frontend) |

---

## 🚀 快速启动 (Docker 一键部署)

> 本项目支持 Docker 一键启动，无需本地安装 Java 或 Node.js 环境。

### 1. 环境准备

确保本地已安装并运行 [Docker Desktop](https://www.docker.com/products/docker-desktop/)。

### 2. 启动项目

在项目根目录下执行：

```bash
docker compose up --build
```

> ⏳ 首次启动需要下载基础镜像和构建依赖，可能需要几分钟。

### 3. 访问系统

| 服务 | 地址 |
| :--- | :--- |
| 前端页面 | [http://localhost:3300](http://localhost:3300) |
| 后端 API | [http://localhost:8080](http://localhost:8080) |
| 数据库 | `localhost:3306`（用户名: `root`，密码: `password`） |

---

## 📦 轻量打包（不自动测试）

在项目根目录执行：

```bash
./scripts/package_release.sh
```

该打包脚本不会执行自动化测试，并会自动剔除以下内容以减小包体积：

- `node_modules` / `venv` / `.venv` / `target` / `__pycache__`
- `.git` 等版本控制目录
- `*.py`、`*.pyc`
- `test` / `tests` / `__tests__`、`*.test.*`、`*.spec.*`、`*test*.sh`

打包产物输出到 `release/` 目录。

---

## 🧪 测试账号

系统预置了不同角色的测试账号，方便快速体验完整流程：

| 角色 | 用户名 | 密码 | 功能权限 |
| :--- | :--- | :--- | :--- |
| **普通用户** | `user1` | `123456` | 浏览咨询师、充值钱包、预约咨询、在线聊天、评价反馈 |
| **咨询师** | `counselor1` | `123456` | 排班管理、接收/确认/完成预约、在线聊天、查看收益、申请提现 |
| **管理员** | `admin` | `admin123` | 用户管理、咨询师资料管理、提现审批、系统数据概览 |

> 💡 你也可以通过登录页面的「注册」按钮创建新用户账号。

---

## 📸 功能模块

### 👤 用户端

- **咨询师浏览**：卡片式布局展示咨询师信息，支持悬停交互动效
- **预约咨询**：日历选择日期 + 时段选择，自动检查排班冲突，扣费并创建预约
- **实时聊天**：基于 WebSocket 的即时通讯，发送消息即时回显，支持历史记录
- **个人中心**：个人资料编辑、头像上传、密码修改
- **钱包系统**：模拟充值、余额查询、预约退费自动退回
- **预约管理**：查看预约状态流转、取消预约、评价咨询师
- **消息通知**：实时推送系统通知（预约状态变更、退费提醒等），支持查看全部通知

### 🩺 咨询师端

- **工作台仪表盘**：预约管理、用户评价一览
- **排班管理**：批量排班工具，自定义多日期多时段，灵活管理工作时间
- **收益管理**：可视化收益卡片，查看好评率和平均评分，支持提现申请
- **在线沟通**：通过操作栏直接联系用户，实时聊天
- **评价回复**：对用户评价进行专业回复

### ⚙️ 管理员端

- **数据概览**：系统关键数据统计
- **用户管理**：用户列表、角色管理
- **咨询师管理**：维护咨询师资料与资质信息
- **提现管理**：审批咨询师提现申请

### 🎨 界面特色

- **磨砂玻璃质感**：顶部导航栏采用 `backdrop-filter` 毛玻璃效果
- **高级渐变配色**：侧边栏和品牌色采用深邃的蓝紫渐变
- **微交互动效**：按钮、卡片拥有顺滑的 Hover 和点击反馈
- **响应式布局**：适配不同屏幕尺寸

---

## 🔄 业务流程

```
用户注册/登录
    │
    ├── 充值钱包余额
    │
    ├── 浏览咨询师 → 选择日期时段 → 预约（自动扣费）
    │       │
    │       ├── 咨询师确认 → 生成会议链接 → 开始咨询
    │       │       │
    │       │       └── 咨询师标记完成 → 用户评价 → 咨询师回复
    │       │
    │       ├── 咨询师拒绝 → 自动退费
    │       │
    │       └── 用户取消 → 自动退费
    │
    └── 在线聊天（咨询前沟通）
```

---

## 📂 项目结构

```
.
├── backend/                # Spring Boot 后端
│   ├── src/main/java/      # Java 源代码
│   │   └── com/counseling/system/
│   │       ├── controller/  # REST API 控制器
│   │       ├── entity/      # 数据实体类
│   │       ├── repository/  # MyBatis Plus Mapper
│   │       ├── service/     # 业务逻辑层
│   │       ├── config/      # 安全、WebSocket 等配置
│   │       └── dto/         # 数据传输对象
│   ├── src/main/resources/  # 配置文件 & SQL 初始化脚本
│   ├── Dockerfile           # 后端镜像构建
│   └── pom.xml              # Maven 依赖管理
│
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── views/           # 页面组件 (auth/user/counselor/admin)
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── router/          # 路由配置
│   │   └── utils/           # 工具函数 (格式化、头像等)
│   ├── Dockerfile           # 前端镜像构建
│   ├── vite.config.js       # Vite 配置 (含后端 API 代理)
│   └── package.json         # NPM 依赖管理
│
├── docker-compose.yml      # Docker Compose 编排文件
├── .env.example            # 环境变量示例
└── README.md               # 本文件
```

---

## 🤝 贡献与反馈

如果你在运行过程中遇到问题，欢迎提交 Issue 或检查 Docker 日志：

```bash
# 查看所有服务日志
docker compose logs

# 查看特定服务日志
docker compose logs backend
docker compose logs frontend
docker compose logs db
```
