package com.photowatermark.util;

import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 图片处理工具类
 * 提供图片文件验证、加载、格式转换等功能
 */
public class ImageUtils {
    
    // 支持的图片格式
    public static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "bmp", "gif", "tiff", "tif"
    );
    
    // 支持的导出格式
    public static final List<String> EXPORT_FORMATS = Arrays.asList(
        "jpg", "jpeg", "png"
    );
    
    /**
     * 检查文件是否为支持的图片格式
     */
    public static boolean isImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return false;
        }
        
        String extension = fileName.substring(lastDotIndex + 1);
        return SUPPORTED_EXTENSIONS.contains(extension);
    }
    
    /**
     * 从文件加载JavaFX Image对象
     */
    public static Image loadImage(File file) throws IOException {
        if (!isImageFile(file)) {
            throw new IOException("不支持的图片格式: " + file.getName());
        }
        
        try {
            return new Image(file.toURI().toString());
        } catch (Exception e) {
            throw new IOException("无法加载图片: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将JavaFX Image转换为BufferedImage
     */
    public static BufferedImage toBufferedImage(Image fxImage) {
        return SwingFXUtils.fromFXImage(fxImage, null);
    }
    
    /**
     * 将BufferedImage转换为JavaFX Image
     */
    public static Image toFXImage(BufferedImage bufferedImage) {
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    
    /**
     * 保存BufferedImage到文件
     */
    public static void saveImage(BufferedImage image, File file, String format) throws IOException {
        if (!EXPORT_FORMATS.contains(format.toLowerCase())) {
            throw new IOException("不支持的导出格式: " + format);
        }
        
        // 确保目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // 对于JPEG格式，需要处理透明度
        if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
            BufferedImage rgbImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            rgbImage.getGraphics().drawImage(image, 0, 0, null);
            image = rgbImage;
        }
        
        ImageIO.write(image, format, file);
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 生成输出文件名
     */
    public static String generateOutputFileName(String originalName, String suffix) {
        int lastDotIndex = originalName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return originalName + suffix;
        }
        
        String nameWithoutExt = originalName.substring(0, lastDotIndex);
        String extension = originalName.substring(lastDotIndex);
        return nameWithoutExt + suffix + extension;
    }
}