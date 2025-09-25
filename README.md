# Photo Watermark 2

一个功能强大的 macOS 桌面图片水印应用程序。

## 项目简介

Photo Watermark 2 是一个基于 JavaFX 开发的 macOS 桌面应用程序，专门用于为图片添加水印。该应用程序提供了直观的用户界面和丰富的水印定制选项，支持批量处理和多种输出格式。

## 主要功能

### 基础功能
- ✅ **文件处理**
  - 支持单张图片拖拽导入
  - 支持文件选择器导入
  - 支持批量导入多张图片
  - 支持导入整个文件夹
  - 显示已导入图片的缩略图列表

- ✅ **支持格式**
  - 输入格式：JPEG, PNG, BMP, TIFF
  - PNG 透明通道支持
  - 输出格式：JPEG, PNG

- ✅ **文本水印**
  - 自定义文本内容
  - 透明度调节 (0-100%)
  - 九宫格位置预设

- ✅ **导出功能**
  - 指定输出文件夹
  - 防止覆盖原图
  - 文件命名规则选项（保留原名、添加前缀/后缀）

### 高级功能
- 🔄 **增强文本水印**
  - 字体选择（系统字体）
  - 字号、粗体、斜体设置
  - 颜色选择器
  - 阴影和描边效果

- 🔄 **图片水印**
  - 支持 Logo 图片水印
  - PNG 透明通道支持
  - 缩放和透明度调节

- 🔄 **高级布局**
  - 实时预览
  - 手动拖拽定位
  - 任意角度旋转

- 🔄 **配置管理**
  - 水印模板保存
  - 模板加载和管理
  - 自动保存上次设置

- 🔄 **导出增强**
  - JPEG 质量调节
  - 图片尺寸调整

## 系统要求

- **操作系统**: macOS 10.14+ (专为 macOS 设计)
- **Java**: JDK 17 或更高版本 (兼容 JDK 25)
- **内存**: 最少 512MB RAM
- **存储**: 最少 100MB 可用空间

## 安装和运行

### 开发环境运行
1. 确保已安装 JDK 17+ (兼容 JDK 25)
2. 克隆项目到本地
3. 在项目根目录执行：
   ```bash
   mvn clean javafx:run
   ```

### 构建可执行文件
```bash
mvn clean package
```

生成的 JAR 文件位于 `target/` 目录下。

## 项目结构

```
photo-watermark2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/photowatermark/
│   │   │       ├── PhotoWatermarkApp.java      # 主应用程序类
│   │   │       ├── controller/                 # 控制器类
│   │   │       ├── model/                      # 数据模型类
│   │   │       ├── service/                    # 业务逻辑服务
│   │   │       └── util/                       # 工具类
│   │   └── resources/
│   │       ├── fxml/                          # FXML 界面文件
│   │       ├── css/                           # 样式文件
│   │       └── images/                        # 应用程序图标等
│   └── test/                                  # 测试代码
├── docs/                                      # 文档
├── pom.xml                                    # Maven 配置
└── README.md                                  # 项目说明
```

## 开发进度

- [x] 项目初始化和基础配置
- [ ] 基础 UI 框架搭建
- [ ] 文件导入功能
- [ ] 图片显示和预览
- [ ] 基础文本水印
- [ ] 水印位置控制
- [ ] 基础导出功能
- [ ] 批量处理
- [ ] 高级文本功能
- [ ] 模板管理

## 技术栈

- **UI 框架**: JavaFX 17
- **构建工具**: Maven
- **图像处理**: Java BufferedImage + Apache Commons Imaging
- **数据序列化**: Jackson JSON
- **测试框架**: JUnit 5

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
- 创建 Issue
- 发送邮件至项目维护者

---

**注意**: 本项目正在积极开发中，功能和 API 可能会发生变化。