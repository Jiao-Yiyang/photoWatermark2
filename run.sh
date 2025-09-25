#!/bin/bash

# 设置JAVA_HOME为x86_64版本的Java
export JAVA_HOME=$(/usr/libexec/java_home -v 17 -a x86_64)

# 获取classpath
CLASSPATH=$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)

# 使用Rosetta 2运行JavaFX应用程序
arch -x86_64 java -cp "target/classes:$CLASSPATH" \
    --module-path "$CLASSPATH" \
    --add-modules javafx.controls,javafx.fxml \
    --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED \
    --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED \
    com.photowatermark.SimplePhotoWatermarkApp