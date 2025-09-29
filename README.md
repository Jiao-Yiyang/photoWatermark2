# Photo Watermark 2

一个功能强大的 macOS 桌面图片水印应用程序。

## 项目简介

Photo Watermark 2 是一个基于 Java Swing 开发的 macOS 桌面应用程序，专门用于为图片添加水印。该应用程序提供了直观的用户界面和丰富的水印定制选项，支持精确的水印定位和模板管理功能。

## 主要功能

### ✅ 已实现功能

#### 文件处理
- 支持单张图片拖拽导入
- 支持文件选择器导入
- 支持多种图片格式：JPEG, PNG, BMP, TIFF, GIF
- PNG 透明通道完美支持
- 实时图片预览和缩放

#### 文本水印
- 自定义文本内容
- 字体选择（系统字体）
- 字号、粗体、斜体设置
- 颜色选择器
- 透明度调节 (0-100%)
- 九宫格位置预设
- **精确拖拽定位** - 支持鼠标拖拽调整水印位置
- **重复拖拽** - 可多次调整水印位置直到满意

#### 水印模板系统
- **模板保存** - 保存当前水印设置为模板
- **模板加载** - 快速应用已保存的模板
- **相对定位** - 模板支持相对位置，适配不同尺寸图片
- **自动保存** - 自动保存最后使用的设置
- 模板文件管理（.wmt 格式）

#### 导出功能
- 指定输出文件夹
- 防止覆盖原图
- 支持 JPEG 和 PNG 输出格式
- 文件命名规则选项（保留原名、添加前缀/后缀）
- JPEG 质量调节

### 🔄 核心技术特性

#### 精确定位系统
- **缩放适配** - 自动计算缩放比例，确保拖拽位置准确
- **坐标系匹配** - 完美匹配显示区域和实际图片坐标
- **相对位置** - 模板使用相对坐标(0.0-1.0)，适配任意尺寸图片

#### 用户体验优化
- 直观的拖拽操作
- 实时预览效果
- 响应式界面设计
- 错误处理和用户提示

## 快速开始

### 方式一：直接下载使用（推荐）

1. **下载 DMG 安装包**
   - 下载 `dist/Photo Watermark-1.0.0.dmg` 文件
   - 双击 DMG 文件挂载
   - 将 "Photo Watermark" 应用拖拽到 Applications 文件夹
   - 从 Launchpad 或 Applications 文件夹启动应用

2. **使用应用**
   - 拖拽图片到应用窗口或点击"选择图片"
   - 输入水印文字并调整样式
   - 拖拽水印到合适位置
   - 点击"应用水印"保存结果

### 方式二：开发环境运行

#### 系统要求
- **操作系统**: macOS 10.14+ 
- **Java**: JDK 17 或更高版本 (已测试兼容 JDK 25)
- **Maven**: 3.6+ 
- **内存**: 最少 512MB RAM
- **存储**: 最少 100MB 可用空间

#### 运行步骤
1. 确保已安装 JDK 17+ 和 Maven
2. 克隆项目到本地：
   ```bash
   git clone [项目地址]
   cd photoWatermark2
   ```
3. 编译并运行：
   ```bash
   mvn clean compile
   mvn exec:java
   ```

#### 构建可执行文件
```bash
# 构建 JAR 文件
mvn clean package

# 构建 macOS 应用包
jpackage --input target --main-jar photo-watermark2-1.0.0.jar \
         --main-class com.photowatermark.SwingPhotoWatermarkApp \
         --type app-image --dest dist --name "Photo Watermark" \
         --app-version "1.0.0"

# 构建 DMG 安装包
jpackage --input target --main-jar photo-watermark2-1.0.0.jar \
         --main-class com.photowatermark.SwingPhotoWatermarkApp \
         --type dmg --dest dist --name "Photo Watermark" \
         --app-version "1.0.0"
```

## 使用指南

### 基本操作流程

1. **导入图片**
   - 方法一：直接拖拽图片文件到应用窗口
   - 方法二：点击"选择图片"按钮选择文件

2. **设置水印**
   - 在"水印文字"输入框中输入要添加的文字
   - 调整字体、大小、颜色等样式
   - 设置透明度（0-100%）

3. **定位水印**
   - 使用九宫格按钮快速定位到预设位置
   - 或直接在预览区域拖拽水印到理想位置
   - 支持多次调整直到满意

4. **模板管理**
   - 点击"保存模板"保存当前设置
   - 点击"加载模板"应用已保存的模板
   - 模板会自动适配不同尺寸的图片

5. **导出图片**
   - 选择输出文件夹
   - 选择输出格式（JPEG/PNG）
   - 点击"应用水印"完成处理

### 高级功能

#### 精确定位
- 水印支持像素级精确定位
- 拖拽操作会自动计算正确的坐标
- 支持在不同缩放级别下准确定位

#### 模板系统
- 模板文件保存在 `watermark_templates/` 目录
- 使用相对坐标系统，确保跨图片兼容性
- 支持快速切换不同的水印样式

## 项目结构

```
photoWatermark2/
├── src/main/java/com/photowatermark/
│   ├── SwingPhotoWatermarkApp.java    # 主应用程序类
│   ├── WatermarkTemplate.java         # 水印模板数据类
│   └── TemplateManager.java           # 模板管理器
├── dist/
│   ├── Photo Watermark.app/           # macOS 应用包
│   └── Photo Watermark-1.0.0.dmg      # DMG 安装包
├── watermark_templates/               # 水印模板存储目录
├── test-images/                       # 测试图片
├── target/                           # 构建输出目录
├── pom.xml                           # Maven 配置
└── README.md                         # 项目说明
```

## 技术实现

### 核心技术栈
- **UI 框架**: Java Swing
- **构建工具**: Maven 3.9.9
- **图像处理**: Java BufferedImage + Apache Commons Imaging
- **数据序列化**: Jackson JSON
- **打包工具**: jpackage (JDK 内置)

### 关键技术特性

#### 精确拖拽系统
```java
// 缩放比例计算
double scaleX = (double) displayWidth / imageWidth;
double scaleY = (double) displayHeight / imageHeight;
double scale = Math.min(scaleX, scaleY);

// 坐标转换
int actualX = (int) ((mouseX - offsetX) / scale);
int actualY = (int) ((mouseY - offsetY) / scale);
```

#### 相对定位模板
```java
// 保存相对位置 (0.0 - 1.0)
double relativeX = (double) watermarkX / imageWidth;
double relativeY = (double) watermarkY / imageHeight;

// 应用到新图片
int newX = (int) (relativeX * newImageWidth);
int newY = (int) (relativeY * newImageHeight);
```

## 版本历史

### v1.0.0 (当前版本)
- ✅ 完整的水印功能实现
- ✅ 精确拖拽定位系统
- ✅ 水印模板管理
- ✅ macOS 原生应用打包
- ✅ DMG 安装包支持

### 已修复的问题
- 🐛 修复水印拖拽位置不准确的问题
- 🐛 修复无法重复拖拽水印的问题  
- 🐛 修复模板位置信息不准确的问题
- 🐛 优化坐标系统匹配和缩放计算

## 贡献指南

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 创建 Issue 反馈问题
- 提交 Pull Request 贡献代码

---

**Photo Watermark 2** - 让图片水印变得简单而精确！