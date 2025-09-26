package com.photowatermark;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 水印模板管理器，负责模板的保存、加载和管理
 */
public class TemplateManager {
    private static final String TEMPLATES_DIR = "watermark_templates";
    private static final String TEMPLATE_EXTENSION = ".wmt"; // Watermark Template
    private static final String LAST_SETTINGS_FILE = "last_settings.wmt";
    
    public TemplateManager() {
        // 确保模板目录存在
        File templatesDir = new File(TEMPLATES_DIR);
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }
    }
    
    /**
     * 保存水印模板
     */
    public boolean saveTemplate(WatermarkTemplate template) {
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            return false;
        }
        
        String fileName = sanitizeFileName(template.getTemplateName()) + TEMPLATE_EXTENSION;
        File file = new File(TEMPLATES_DIR, fileName);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(template);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 加载水印模板
     */
    public WatermarkTemplate loadTemplate(String templateName) {
        String fileName = sanitizeFileName(templateName) + TEMPLATE_EXTENSION;
        File file = new File(TEMPLATES_DIR, fileName);
        
        if (!file.exists()) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (WatermarkTemplate) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取所有模板列表
     */
    public List<String> getTemplateNames() {
        List<String> templateNames = new ArrayList<>();
        File templatesDir = new File(TEMPLATES_DIR);
        
        if (templatesDir.exists() && templatesDir.isDirectory()) {
            File[] files = templatesDir.listFiles((dir, name) -> 
                name.endsWith(TEMPLATE_EXTENSION) && !name.equals(LAST_SETTINGS_FILE));
            
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    // 移除扩展名
                    String templateName = name.substring(0, name.length() - TEMPLATE_EXTENSION.length());
                    templateNames.add(templateName);
                }
            }
        }
        
        return templateNames;
    }
    
    /**
     * 删除模板
     */
    public boolean deleteTemplate(String templateName) {
        String fileName = sanitizeFileName(templateName) + TEMPLATE_EXTENSION;
        File file = new File(TEMPLATES_DIR, fileName);
        
        if (file.exists()) {
            return file.delete();
        }
        
        return false;
    }
    
    /**
     * 保存最后的设置
     */
    public boolean saveLastSettings(WatermarkTemplate template) {
        File file = new File(TEMPLATES_DIR, LAST_SETTINGS_FILE);
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(template);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 加载最后的设置
     */
    public WatermarkTemplate loadLastSettings() {
        File file = new File(TEMPLATES_DIR, LAST_SETTINGS_FILE);
        
        if (!file.exists()) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (WatermarkTemplate) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 清理文件名，移除不安全的字符
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_\\-]", "_");
    }
    
    /**
     * 检查模板是否存在
     */
    public boolean templateExists(String templateName) {
        String fileName = sanitizeFileName(templateName) + TEMPLATE_EXTENSION;
        File file = new File(TEMPLATES_DIR, fileName);
        return file.exists();
    }
}