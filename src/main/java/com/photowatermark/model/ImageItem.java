package com.photowatermark.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import java.io.File;

/**
 * 图片项模型类
 * 用于存储图片文件信息和处理状态
 */
public class ImageItem {
    
    private final File file;
    private final StringProperty fileName;
    private final StringProperty filePath;
    private final LongProperty fileSize;
    private final StringProperty status;
    private final BooleanProperty processed;
    
    private Image image;
    private Image thumbnail;
    
    public ImageItem(File file) {
        this.file = file;
        this.fileName = new SimpleStringProperty(file.getName());
        this.filePath = new SimpleStringProperty(file.getAbsolutePath());
        this.fileSize = new SimpleLongProperty(file.length());
        this.status = new SimpleStringProperty("待处理");
        this.processed = new SimpleBooleanProperty(false);
    }
    
    // Getters and Setters
    public File getFile() {
        return file;
    }
    
    public String getFileName() {
        return fileName.get();
    }
    
    public StringProperty fileNameProperty() {
        return fileName;
    }
    
    public String getFilePath() {
        return filePath.get();
    }
    
    public StringProperty filePathProperty() {
        return filePath;
    }
    
    public long getFileSize() {
        return fileSize.get();
    }
    
    public LongProperty fileSizeProperty() {
        return fileSize;
    }
    
    public String getStatus() {
        return status.get();
    }
    
    public void setStatus(String status) {
        this.status.set(status);
    }
    
    public StringProperty statusProperty() {
        return status;
    }
    
    public boolean isProcessed() {
        return processed.get();
    }
    
    public void setProcessed(boolean processed) {
        this.processed.set(processed);
    }
    
    public BooleanProperty processedProperty() {
        return processed;
    }
    
    public Image getImage() {
        return image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }
    
    public Image getThumbnail() {
        return thumbnail;
    }
    
    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        long size = getFileSize();
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return getFileName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ImageItem imageItem = (ImageItem) obj;
        return file.equals(imageItem.file);
    }
    
    @Override
    public int hashCode() {
        return file.hashCode();
    }
}