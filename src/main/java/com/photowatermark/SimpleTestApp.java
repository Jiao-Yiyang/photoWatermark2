package com.photowatermark;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 简单的测试应用程序，用于验证JavaFX环境是否正常工作
 */
public class SimpleTestApp extends Application {
    
    @Override
    public void start(Stage stage) {
        Label label = new Label("Hello, JavaFX!");
        VBox root = new VBox(label);
        Scene scene = new Scene(root, 300, 200);
        
        stage.setTitle("JavaFX Test");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}