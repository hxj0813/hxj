# 个人成长管理系统

一个基于Jetpack Compose开发的Android应用，用于个人自我管理和成长跟踪。

## 主要功能

- **习惯养成**：创建、跟踪和维持良好习惯，支持多种频率和类别
- **任务规划**：规划和管理日常任务，提高执行效率
- **目标管理**：设置和跟踪长期和短期目标
- **时间追踪**：记录和分析时间使用情况
- **反思笔记**：记录想法和感悟，支持习惯关联笔记

## 特色亮点

- **直观的习惯跟踪**：可视化进度条和统计数据
- **成就徽章系统**：完成特定里程碑解锁徽章，增强动力
- **数据分析**：习惯养成趋势和完成率分析
- **现代化UI**：基于Material 3设计，提供优雅的用户体验
- **多模块导航**：通过底部导航栏轻松切换功能区域

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose + Material 3
- **架构**：Clean Architecture + MVI
- **状态管理**：ViewModel + StateFlow
- **导航**：Compose Navigation
- **数据持久化**：Room数据库 
- **异步处理**：Kotlin协程
- **依赖注入**：Hilt (预设)

## 应用截图

(项目实现后将添加应用截图)

## 架构

项目采用Clean Architecture + MVI架构模式：

- **数据层**：负责数据存取
  - Room数据库本地存储
  - Repository模式实现数据访问抽象
  - 数据模型定义

- **领域层**：业务逻辑
  - UseCase模式封装业务逻辑
  - 领域实体定义

- **表现层**：用户界面
  - MVI模式（Model-View-Intent）
  - Jetpack Compose UI组件
  - ViewModel状态管理

## 项目结构

```
app/
├── data/            # 数据层
│   ├── local/       # 本地数据源（Room数据库）
│   ├── model/       # 数据模型（实体类）
│   ├── repository/  # 仓库实现
│   └── receiver/    # 广播接收器
├── domain/          # 领域层
│   └── usecase/     # 用例实现
├── di/              # 依赖注入
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── UseCaseModule.kt
├── presentation/    # 表现层
│   ├── common/      # 公共UI组件
│   ├── components/  # 可复用组件
│   ├── goals/       # 目标管理UI
│   ├── habits/      # 习惯养成UI
│   ├── reflection/  # 反思笔记UI
│   ├── tasks/       # 任务规划UI
│   ├── theme/       # 应用主题
│   └── timetracking/# 时间追踪UI
└── util/            # 工具类
    ├── DateTimeUtil.kt
    ├── Extensions.kt
    └── NotificationUtil.kt
```

## 开发环境

- Android Studio Hedgehog | 2023.1.1+
- Gradle 8.0+
- Kotlin 1.9+
- MinSDK 24
- TargetSDK 35

## 功能规划

- **第一阶段**：核心功能实现
  - 习惯养成模块完整实现
  - 基础任务管理功能
  - 数据本地存储

- **第二阶段**：增强功能
  - 目标管理模块完善
  - 时间追踪功能完善
  - 数据统计与分析

- **第三阶段**：高级功能
  - 云同步
  - 通知和提醒系统
  - 社区分享功能

## 安装

1. 克隆仓库：`git clone https://github.com/yourusername/personal-growth-app.git`
2. 在Android Studio中打开项目
3. 点击Run按钮运行应用

## 贡献

欢迎提交Pull Request或Issues来改进项目。

## 许可证

MIT