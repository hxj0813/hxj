 # 个人成长管理系统

一个基于Jetpack Compose开发的Android应用，用于个人自我管理和成长跟踪。

## 主要功能

- **目标管理**：设置和跟踪长期和短期目标
- **任务规划**：规划和管理日常任务
- **时间追踪**：记录和分析时间使用情况
- **习惯养成**：培养和维持良好习惯
- **反思笔记**：记录想法和感悟

## 技术栈

- Kotlin
- Jetpack Compose
- Room数据库
- Kotlin协程
- Hilt依赖注入
- Material 3设计

## 架构

项目采用Clean Architecture + MVI架构模式：

- **数据层**：Room数据库、Repository模式
- **领域层**：UseCase模式
- **表现层**：MVI模式、Jetpack Compose UI

## 项目结构

```
app/
├── data/            # 数据层
│   ├── local/       # 本地数据源
│   ├── model/       # 数据模型
│   └── repository/  # 仓库
├── domain/          # 领域层
│   └── usecase/     # 用例
├── presentation/    # 表现层
│   ├── common/      # 公共UI组件
│   ├── goals/       # 目标管理UI
│   ├── tasks/       # 任务规划UI
│   ├── timetracking/# 时间追踪UI
│   ├── habits/      # 习惯养成UI
│   └── reflection/  # 反思笔记UI
└── util/            # 工具类
```

## 开发环境

- Android Studio Hedgehog | 2023.1.1
- Gradle 8.0
- MinSDK 24
- TargetSDK 34

## 安装

1. 克隆仓库
2. 在Android Studio中打开项目
3. 点击Run按钮运行应用

## 许可证

MIT