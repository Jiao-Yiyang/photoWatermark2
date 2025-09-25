package com.photowatermark.controller;

import com.photowatermark.model.ImageItem;
import com.photowatermark.util.ImageUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主界面控制器
 * 负责处理主界面的用户交互和业务逻辑
 */
public class MainController implements Initializable {

    // 主布局
    @FXML
    private BorderPane mainPane;
    
    // 左侧面板 - 图片列表
    @FXML
    private VBox leftPanel;
    
    @FXML
    private ListView<ImageItem> imageListView;
    
    @FXML
    private Button importButton;
    
    @FXML
    private Button importFolderButton;
    
    @FXML
    private Button clearAllButton;
    
    // 中央面板 - 图片预览
    @FXML
    private VBox centerPanel;
    
    @FXML
    private ImageView previewImageView;
    
    @FXML
    private Label previewStatusLabel;
    
    // 右侧面板 - 水印设置
    @FXML
    private VBox rightPanel;
    
    // 水印类型选择
    @FXML
    private RadioButton textWatermarkRadio;
    
    @FXML
    private RadioButton imageWatermarkRadio;
    
    private ToggleGroup watermarkTypeGroup;
    
    // 文本水印设置
    @FXML
    private VBox textWatermarkPanel;
    
    @FXML
    private TextField watermarkTextField;
    
    @FXML
    private Slider transparencySlider;
    
    @FXML
    private Label transparencyLabel;
    
    // 位置设置
    @FXML
    private VBox positionPanel;
    
    @FXML
    private Button topLeftButton;
    
    @FXML
    private Button topCenterButton;
    
    @FXML
    private Button topRightButton;
    
    @FXML
    private Button middleLeftButton;
    
    @FXML
    private Button middleCenterButton;
    
    @FXML
    private Button middleRightButton;
    
    @FXML
    private Button bottomLeftButton;
    
    @FXML
    private Button bottomCenterButton;
    
    @FXML
    private Button bottomRightButton;
    
    // 导出设置
    @FXML
    private VBox exportPanel;
    
    @FXML
    private ComboBox<String> outputFormatCombo;
    
    @FXML
    private TextField outputFolderField;
    
    @FXML
    private Button browseOutputButton;
    
    @FXML
    private ComboBox<String> namingRuleCombo;
    
    @FXML
    private TextField prefixField;
    
    @FXML
    private TextField suffixField;
    
    // 操作按钮
    @FXML
    private HBox actionPanel;
    
    @FXML
    private Button previewButton;
    
    @FXML
    private Button exportButton;
    
    @FXML
    private Button exportAllButton;

    // 数据模型
    private ObservableList<ImageItem> imageList;
    private ExecutorService executorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化数据模型
        imageList = FXCollections.observableArrayList();
        imageListView.setItems(imageList);
        executorService = Executors.newFixedThreadPool(4);

        initializeComponents();
        setupEventHandlers();
        setupDragAndDrop();
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        // 初始化水印类型单选按钮组
        watermarkTypeGroup = new ToggleGroup();
        textWatermarkRadio.setToggleGroup(watermarkTypeGroup);
        imageWatermarkRadio.setToggleGroup(watermarkTypeGroup);
        textWatermarkRadio.setSelected(true);
        
        // 初始化输出格式下拉框
        outputFormatCombo.getItems().addAll("PNG", "JPEG");
        outputFormatCombo.setValue("PNG");
        
        // 初始化命名规则下拉框
        namingRuleCombo.getItems().addAll("保留原文件名", "添加前缀", "添加后缀");
        namingRuleCombo.setValue("保留原文件名");
        
        // 初始化透明度滑块
        transparencySlider.setMin(0);
        transparencySlider.setMax(100);
        transparencySlider.setValue(80);
        updateTransparencyLabel();
        
        // 设置预览状态
        previewStatusLabel.setText("请导入图片开始使用");
        
        // 初始化图片列表
        imageListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // 设置ListView的单元格工厂
        imageListView.setCellFactory(listView -> new ListCell<ImageItem>() {
            @Override
            protected void updateItem(ImageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getFileName() + " (" + item.getFormattedFileSize() + ")");
                    setTooltip(new Tooltip(item.getFilePath()));
                }
            }
        });
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 透明度滑块事件
        transparencySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTransparencyLabel();
            // TODO: 更新预览
        });
        
        // 水印文本变化事件
        watermarkTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            // TODO: 更新预览
        });
        
        // 命名规则变化事件
        namingRuleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateNamingRuleFields();
        });
        
        // 图片列表选择事件
        imageListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadImagePreview(newVal);
            }
        });
    }

    private void setupDragAndDrop() {
        // 设置拖拽区域
        leftPanel.setOnDragOver(this::handleDragOver);
        leftPanel.setOnDragDropped(this::handleDragDropped);
        
        // 也可以在整个预览区域支持拖拽
        centerPanel.setOnDragOver(this::handleDragOver);
        centerPanel.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != leftPanel && 
            event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            addImageFiles(files);
            success = true;
        }
        
        event.setDropCompleted(success);
        event.consume();
    }
    
    /**
     * 更新透明度标签
     */
    private void updateTransparencyLabel() {
        transparencyLabel.setText(String.format("%.0f%%", transparencySlider.getValue()));
    }
    
    /**
     * 更新命名规则字段
     */
    private void updateNamingRuleFields() {
        String rule = namingRuleCombo.getValue();
        prefixField.setVisible("添加前缀".equals(rule));
        suffixField.setVisible("添加后缀".equals(rule));
    }
    
    /**
     * 加载图片预览
     */
    private void loadImagePreview(ImageItem imageItem) {
        if (imageItem.getImage() != null) {
            previewImageView.setImage(imageItem.getImage());
            
            Image image = imageItem.getImage();
            String info = String.format("%s\n尺寸: %.0f × %.0f\n大小: %s",
                imageItem.getFileName(),
                image.getWidth(),
                image.getHeight(),
                imageItem.getFormattedFileSize()
            );
            previewStatusLabel.setText(info);
        } else {
            previewStatusLabel.setText("正在加载: " + imageItem.getFileName());
        }
    }
    
    @FXML
    private void handleImportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片文件");
        
        // 设置文件过滤器
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
            "图片文件", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.tiff", "*.tif"
        );
        fileChooser.getExtensionFilters().add(imageFilter);
        
        // 获取当前窗口
        Stage stage = (Stage) importButton.getScene().getWindow();
        
        // 选择多个文件
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            addImageFiles(selectedFiles);
        }
    }

    @FXML
    private void handleImportFolder() {
        // TODO: 实现文件夹导入功能
        System.out.println("导入文件夹");
    }

    @FXML
    private void handleClearAll() {
        imageList.clear();
        previewImageView.setImage(null);
        previewStatusLabel.setText("请导入图片开始使用");
    }

    @FXML
    private void handleBrowseOutput() {
        // TODO: 实现输出文件夹选择
        System.out.println("选择输出文件夹");
    }

    @FXML
    private void handlePreview() {
        // TODO: 实现预览功能
        System.out.println("预览水印效果");
    }

    @FXML
    private void handleExport() {
        // TODO: 实现单张导出功能
        System.out.println("导出当前图片");
    }

    @FXML
    private void handleExportAll() {
        // TODO: 实现批量导出功能
        System.out.println("导出所有图片");
    }

    // 位置按钮事件处理
    @FXML
    private void handleTopLeft() { setWatermarkPosition("TOP_LEFT"); }
    @FXML
    private void handleTopCenter() { setWatermarkPosition("TOP_CENTER"); }
    @FXML
    private void handleTopRight() { setWatermarkPosition("TOP_RIGHT"); }
    @FXML
    private void handleMiddleLeft() { setWatermarkPosition("MIDDLE_LEFT"); }
    @FXML
    private void handleMiddleCenter() { setWatermarkPosition("MIDDLE_CENTER"); }
    @FXML
    private void handleMiddleRight() { setWatermarkPosition("MIDDLE_RIGHT"); }
    @FXML
    private void handleBottomLeft() { setWatermarkPosition("BOTTOM_LEFT"); }
    @FXML
    private void handleBottomCenter() { setWatermarkPosition("BOTTOM_CENTER"); }
    @FXML
    private void handleBottomRight() { setWatermarkPosition("BOTTOM_RIGHT"); }

    /**
     * 设置水印位置
     */
    private void setWatermarkPosition(String position) {
        System.out.println("设置水印位置: " + position);
        // TODO: 实现位置设置逻辑
    }

    private void addImageFiles(List<File> files) {
        for (File file : files) {
            if (ImageUtils.isImageFile(file)) {
                // 检查是否已经存在
                boolean exists = imageList.stream()
                    .anyMatch(item -> item.getFile().equals(file));
                
                if (!exists) {
                    ImageItem imageItem = new ImageItem(file);
                    imageList.add(imageItem);
                    
                    // 异步加载图片
                    loadImageAsync(imageItem);
                }
            }
        }
    }

    private void loadImageAsync(ImageItem imageItem) {
        Task<Image> loadTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return ImageUtils.loadImage(imageItem.getFile());
            }
            
            @Override
            protected void succeeded() {
                Image image = getValue();
                Platform.runLater(() -> {
                    imageItem.setImage(image);
                    imageItem.setStatus("已加载");
                    
                    // 如果这是当前选中的项目，更新预览
                    ImageItem selectedItem = imageListView.getSelectionModel().getSelectedItem();
                    if (selectedItem == imageItem) {
                        loadImagePreview(imageItem);
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    imageItem.setStatus("加载失败");
                    showAlert("错误", "无法加载图片: " + imageItem.getFileName(), 
                             getException().getMessage());
                });
            }
        };
        
        executorService.submit(loadTask);
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}