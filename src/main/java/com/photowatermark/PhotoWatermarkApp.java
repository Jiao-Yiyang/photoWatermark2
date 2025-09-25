package com.photowatermark;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Photo Watermark 2 主应用程序类
 * 
 * @author PhotoWatermark Team
 * @version 1.0.0
 */
public class PhotoWatermarkApp extends Application {
    
    private static final String APP_TITLE = "Photo Watermark 2";
    private static final int MIN_WIDTH = 1000;
    private static final int MIN_HEIGHT = 700;
    
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PhotoWatermarkApp.class.getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), MIN_WIDTH, MIN_HEIGHT);
        
        // 设置应用程序图标
        try {
            // SVG图标在JavaFX中可能有兼容性问题，暂时跳过
            // Image icon = new Image(getClass().getResourceAsStream("/images/app-icon.svg"));
            // stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Warning: Could not load application icon: " + e.getMessage());
        }
        
        // 加载CSS样式
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        
        // 设置窗口居中显示
        stage.centerOnScreen();
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}