# Kairos - AI摄影助手 📸✨

一款专为旅游摄影设计的智能补光相机应用，通过AI智能推荐和自动补光功能，帮助用户在各种光线环境下拍出完美的旅游照片。Kairos使用生成式AI实时推荐姿势、光线和角度，帮助摄影师精准捕捉艺术瞬间。

## 🌟 项目特点

- **智能光线检测**：自动分析环境光线强度，提供最佳补光建议
- **AI姿势推荐**：基于豆包AI服务，智能推荐最适合的拍照姿势
- **实时补光调节**：支持手动和自动补光模式，满足不同拍摄需求
- **直观用户界面**：简洁透明的UI设计，不遮挡拍摄画面
- **多种拍摄模式**：支持站立、坐姿、侧面、躺卧、跳跃、行走等多种姿势
- **相册集成**：无缝集成系统相册，方便照片管理

## 📱 功能模块

### 核心功能
- 🔆 **智能补光**：根据环境光线自动调整补光强度
- 🤖 **AI推荐**：智能分析场景并推荐最佳拍照姿势
- 📷 **实时预览**：高质量相机预览，所见即所得
- 🎨 **透明UI**：全透明界面设计，最大化拍摄视野

### 辅助功能
- 📊 **光线参数显示**：实时显示光线强度和拍摄角度
- 🎯 **姿势示意图**：简笔线稿风格的姿势指导
- ⚙️ **设置中心**：个性化配置补光和推荐参数
- 📁 **相册访问**：快速查看和管理拍摄的照片

## 🚀 安装指南

### 系统要求
- Android 7.0 (API level 24) 或更高版本
- 相机权限
- 存储权限
- 网络权限（用于AI推荐功能）

### 开发环境配置

1. **克隆项目**
   ```bash
   git clone https://github.com/Hedlen/Kairos.git
   cd Kairos
   ```

2. **安装Android Studio**
   - 下载并安装 [Android Studio](https://developer.android.com/studio)
   - 确保安装了 Android SDK 和相关构建工具

3. **配置项目**
   ```bash
   # 打开Android Studio并导入项目
   # 等待Gradle同步完成
   ```

4. **配置API密钥**
   - 在项目根目录创建 `local.properties` 文件
   - 添加豆包AI服务的API配置：
   ```properties
   DOUBAO_API_KEY=your_api_key_here
   DOUBAO_BASE_URL=your_base_url_here
   ```

5. **构建项目**
   ```bash
   ./gradlew build
   ```

### 依赖项说明

主要依赖包括：
- **Jetpack Compose**：现代化UI框架
- **CameraX**：相机功能实现
- **Retrofit**：网络请求处理
- **Hilt**：依赖注入框架
- **Coroutines**：异步编程支持
- **ViewModel & LiveData**：MVVM架构支持

## 📖 使用说明

### 基本操作

1. **启动应用**
   - 打开应用后自动进入相机界面
   - 首次使用需要授予相机和存储权限

2. **智能推荐**
   - 将相机对准人物，AI会自动分析并显示推荐信息
   - 左侧显示光线参数和拍摄建议
   - 右侧显示推荐的拍照姿势示意图

3. **补光调节**
   - 点击右上角的补光按钮
   - 选择自动模式（跟随AI推荐）或手动模式
   - 手动模式下可滑动调节补光强度

4. **拍照操作**
   - 点击底部圆形拍照按钮进行拍摄
   - 拍摄完成后照片自动保存到相册

5. **查看照片**
   - 点击底部相册图标查看已拍摄的照片
   - 支持照片预览和基本管理功能

### 高级功能

- **姿势切换**：根据AI推荐尝试不同的拍照姿势
- **场景适配**：应用会根据不同场景自动调整推荐策略
- **设置调优**：在设置页面可以调整AI推荐的敏感度和补光参数

## 🛠️ 开发贡献

我们欢迎所有形式的贡献！无论是bug修复、功能增强还是文档改进。

### 如何开始贡献

1. **Fork 项目**
   ```bash
   # 在GitHub上fork本项目
   git clone https://github.com/Hedlen/Kairos.git
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **开发和测试**
   - 遵循项目的代码规范
   - 确保新功能有相应的测试
   - 运行现有测试确保没有破坏性变更

4. **提交变更**
   ```bash
   git add .
   git commit -m "feat: 添加新功能描述"
   git push origin feature/your-feature-name
   ```

5. **创建Pull Request**
   - 在GitHub上创建PR
   - 详细描述变更内容和测试情况
   - 等待代码审查和合并

### 开发规范

- **代码风格**：遵循Kotlin官方编码规范
- **提交信息**：使用语义化提交信息格式
- **测试要求**：新功能需要包含单元测试
- **文档更新**：重要变更需要更新相关文档

### 报告问题

如果发现bug或有功能建议，请：
1. 在GitHub Issues中搜索是否已有相关问题
2. 如果没有，创建新的Issue
3. 详细描述问题或建议，包括复现步骤
4. 提供设备信息和应用版本

## 🏗️ 项目架构

```
app/
├── src/main/java/com/example/cat_light/
│   ├── ui/                 # UI层 - Compose界面
│   │   ├── camera/         # 相机相关界面
│   │   ├── settings/       # 设置界面
│   │   └── components/     # 可复用组件
│   ├── data/              # 数据层
│   │   ├── repository/     # 数据仓库
│   │   ├── network/        # 网络请求
│   │   └── local/          # 本地存储
│   ├── domain/            # 业务逻辑层
│   │   ├── usecase/        # 用例
│   │   └── model/          # 数据模型
│   └── di/                # 依赖注入配置
└── src/test/              # 测试代码
```

## 📄 许可证

本项目采用 [GNU AGPL v3 License](LICENSE) 开源许可证。

```
GNU AFFERO GENERAL PUBLIC LICENSE
Version 3, 19 November 2007

Copyright (C) 2024 Kairos Project

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
```

## 🤝 致谢

感谢所有为这个项目做出贡献的开发者和用户！

- 豆包AI服务提供智能推荐支持
- Android CameraX团队提供优秀的相机框架
- Jetpack Compose团队提供现代化UI框架

## 📞 联系我们

如有任何问题或建议，欢迎通过以下方式联系：

- 🐛 Issues: [GitHub Issues](https://github.com/Hedlen/Kairos/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/Hedlen/Kairos/discussions)

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！