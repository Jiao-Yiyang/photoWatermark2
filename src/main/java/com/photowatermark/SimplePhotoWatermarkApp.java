package com.photowatermark;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * 简化版Photo Watermark 2应用程序
 * 不使用FXML，直接用代码构建UI
 */
public class SimplePhotoWatermarkApp extends Application {
    
    private static final String APP_TITLE = "Photo Watermark 2 - 简化版";
    private static final int MIN_WIDTH = 1000;
    private static final int MIN_HEIGHT = 700;
    
    private ListView<String> imageListView;
    private ImageView previewImageView;
    private TextField watermarkTextField;
    private Slider transparencySlider;
    private Label transparencyLabel;
    private ComboBox<String> outputFormatCombo;
    private TextField outputFolderField;
    
    @Override
    public void start(Stage stage) {
        BorderPane root = createMainLayout();
        Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
        
        // 应用基本样式
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();
        stage.show();
    }
    
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        
        // 左侧面板 - 图片列表
        VBox leftPanel = createLeftPanel();
        leftPanel.setPrefWidth(250);
        leftPanel.getStyleClass().add("left-panel");
        
        // 中央面板 - 图片预览
        VBox centerPanel = createCenterPanel();
        centerPanel.getStyleClass().add("center-panel");
        
        // 右侧面板 - 水印设置
        VBox rightPanel = createRightPanel();
        rightPanel.setPrefWidth(300);
        rightPanel.getStyleClass().add("right-panel");
        
        // 底部面板 - 操作按钮
        HBox bottomPanel = createBottomPanel();
        bottomPanel.getStyleClass().add("action-panel");
        
        root.setLeft(leftPanel);
        root.setCenter(centerPanel);
        root.setRight(rightPanel);
        root.setBottom(bottomPanel);
        
        return root;
    }
    
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        Label title = new Label("图片列表");
        title.getStyleClass().add("panel-title");
        
        HBox buttonBox = new HBox(5);
        Button importButton = new Button("导入图片");
        Button clearButton = new Button("清空列表");
        
        importButton.getStyleClass().add("import-button");
        clearButton.getStyleClass().add("clear-button");
        
        importButton.setOnAction(e -> handleImportImage());
        clearButton.setOnAction(e -> imageListView.getItems().clear());
        
        buttonBox.getChildren().addAll(importButton, clearButton);
        
        imageListView = new ListView<>();
        imageListView.getStyleClass().add("image-list");
        VBox.setVgrow(imageListView, Priority.ALWAYS);
        
        panel.getChildren().addAll(title, buttonBox, imageListView);
        return panel;
    }
    
    private VBox createCenterPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        
        Label title = new Label("图片预览");
        title.getStyleClass().add("panel-title");
        
        previewImageView = new ImageView();
        previewImageView.setFitWidth(400);
        previewImageView.setFitHeight(300);
        previewImageView.setPreserveRatio(true);
        previewImageView.getStyleClass().add("preview-image");
        
        StackPane previewContainer = new StackPane(previewImageView);
        previewContainer.getStyleClass().add("preview-container");
        previewContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(previewContainer, Priority.ALWAYS);
        
        panel.getChildren().addAll(title, previewContainer);
        return panel;
    }
    
    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        
        Label title = new Label("水印设置");
        title.getStyleClass().add("panel-title");
        
        // 水印类型选择
        VBox typeGroup = new VBox(5);
        Label typeLabel = new Label("水印类型");
        typeLabel.getStyleClass().add("setting-label");
        
        ToggleGroup typeToggleGroup = new ToggleGroup();
        RadioButton textRadio = new RadioButton("文本水印");
        RadioButton imageRadio = new RadioButton("图片水印");
        textRadio.setToggleGroup(typeToggleGroup);
        imageRadio.setToggleGroup(typeToggleGroup);
        textRadio.setSelected(true);
        
        typeGroup.getChildren().addAll(typeLabel, textRadio, imageRadio);
        
        // 文本水印设置
        VBox textGroup = new VBox(5);
        Label textLabel = new Label("水印文本");
        textLabel.getStyleClass().add("setting-label");
        
        watermarkTextField = new TextField("Sample Watermark");
        watermarkTextField.getStyleClass().add("watermark-text-field");
        
        textGroup.getChildren().addAll(textLabel, watermarkTextField);
        
        // 透明度设置
        VBox transparencyGroup = new VBox(5);
        transparencyLabel = new Label("透明度: 50%");
        transparencyLabel.getStyleClass().add("setting-label");
        
        transparencySlider = new Slider(0, 100, 50);
        transparencySlider.getStyleClass().add("transparency-slider");
        transparencySlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            transparencyLabel.setText("透明度: " + Math.round(newVal.doubleValue()) + "%"));
        
        transparencyGroup.getChildren().addAll(transparencyLabel, transparencySlider);
        
        // 导出设置
        VBox exportGroup = new VBox(5);
        Label exportLabel = new Label("导出设置");
        exportLabel.getStyleClass().add("setting-label");
        
        outputFormatCombo = new ComboBox<>();
        outputFormatCombo.getItems().addAll("JPEG", "PNG");
        outputFormatCombo.setValue("JPEG");
        outputFormatCombo.getStyleClass().add("output-format-combo");
        
        HBox folderBox = new HBox(5);
        outputFolderField = new TextField(System.getProperty("user.home") + "/Desktop");
        outputFolderField.getStyleClass().add("output-folder-field");
        Button browseButton = new Button("浏览");
        browseButton.getStyleClass().add("browse-button");
        
        HBox.setHgrow(outputFolderField, Priority.ALWAYS);
        folderBox.getChildren().addAll(outputFolderField, browseButton);
        
        exportGroup.getChildren().addAll(exportLabel, new Label("格式:"), outputFormatCombo, 
                                       new Label("输出文件夹:"), folderBox);
        
        panel.getChildren().addAll(title, typeGroup, textGroup, transparencyGroup, exportGroup);
        return panel;
    }
    
    private HBox createBottomPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_RIGHT);
        
        Button previewButton = new Button("预览水印");
        Button exportButton = new Button("导出当前");
        Button exportAllButton = new Button("批量导出");
        
        previewButton.getStyleClass().add("preview-button");
        exportButton.getStyleClass().add("export-button");
        exportAllButton.getStyleClass().add("export-button");
        
        previewButton.setOnAction(e -> showAlert("预览", "预览功能", "预览功能将在后续版本中实现"));
        exportButton.setOnAction(e -> showAlert("导出", "导出功能", "导出功能将在后续版本中实现"));
        exportAllButton.setOnAction(e -> showAlert("批量导出", "批量导出功能", "批量导出功能将在后续版本中实现"));
        
        panel.getChildren().addAll(previewButton, exportButton, exportAllButton);
        return panel;
    }
    
    private void handleImportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片文件");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("图片文件", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif"),
            new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("PNG", "*.png"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            for (File file : files) {
                imageListView.getItems().add(file.getName());
                
                // 如果是第一张图片，显示预览
                if (imageListView.getItems().size() == 1) {
                    try {
                        Image image = new Image(file.toURI().toString());
                        previewImageView.setImage(image);
                    } catch (Exception e) {
                        showAlert("错误", "图片加载失败", "无法加载图片: " + file.getName());
                    }
                }
            }
        }
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}