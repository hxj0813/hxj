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
- **依赖注入**：Hilt

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

# Firebase笔记存储实现指南

本项目实现了使用Firebase作为后端存储反思笔记的功能。以下是主要功能和使用指南。

## 功能特点

- 使用Firebase Authentication进行用户认证
- 使用Firebase Firestore存储笔记数据
- 使用Firebase Storage存储笔记图片
- 支持富文本笔记内容
- 支持图片上传和显示
- 实时数据同步和离线支持

## 架构设计

项目采用Clean Architecture（整洁架构）设计：

1. **表现层**：包含UI组件和ViewModel
2. **域层**：包含用例和业务逻辑
3. **数据层**：包含存储库实现和数据来源

## 实现细节

### 存储库实现

`FirebaseHabitNoteRepository` 接口定义了所有笔记操作，其实现 `FirebaseHabitNoteRepositoryImpl` 使用Firebase提供了这些功能：

- 获取所有笔记
- 获取特定习惯的笔记
- 按心情获取笔记
- 获取顶置笔记
- 保存和删除笔记
- 更新笔记状态
- 上传和删除图片

### 数据模型

`HabitNote` 类是核心数据模型，包含笔记的所有信息：

- 标题和内容
- 富文本数据
- 图片列表
- 标签和心情
- 创建和更新时间

### 图片存储

笔记中的图片使用Firebase Storage存储：

1. 上传图片到Firebase Storage
2. 获取图片的下载URL
3. 创建NoteImage对象并保存到笔记中
4. 在UI中使用Coil加载和显示图片

## 安全规则

应用使用Firebase安全规则保护数据：

- Firestore安全规则确保用户只能访问自己的数据
- Storage安全规则限制用户只能访问自己上传的图片

## 使用指南

### 1. 配置Firebase

1. 在Firebase控制台创建项目
2. 添加Android应用并下载google-services.json
3. 启用身份验证、Firestore和Storage服务
4. 应用安全规则（参见项目中的规则文件）

### 2. 使用笔记功能

- 登录后可以创建、查看和编辑笔记
- 可以添加图片、设置心情和标签
- 笔记数据会自动同步到云端
- 支持离线操作，稍后自动同步

### 3. 从本地迁移数据

如果已有本地数据，可以使用迁移工具将数据上传到Firebase：

```kotlin
// 调用ViewModel中的方法
viewModel.migrateLocalNotesToFirebase()
```

## 常见问题

1. **图片上传失败**
   - 检查网络连接
   - 确认用户已登录
   - 验证应用是否有存储权限

2. **同步问题**
   - Firebase会自动处理同步和冲突
   - 离线时可以正常使用，恢复连接后会自动同步

3. **数据安全**
   - 所有数据都受Firebase安全规则保护
   - 每个用户只能访问自己的数据