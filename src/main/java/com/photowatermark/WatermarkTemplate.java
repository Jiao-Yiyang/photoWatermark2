package com.photowatermark;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

/**
 * 水印模板类，用于保存和加载水印设置
 */
public class WatermarkTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String templateName;
    private String watermarkText;
    private String fontFamily;
    private int fontSize;
    private boolean bold;
    private boolean italic;
    private Color textColor;
    private int transparency;
    private String position;
    private boolean hasShadow;
    private boolean hasStroke;
    private int rotationAngle;
    private String outputFormat;
    private int jpegQuality;
    private boolean useCustomPosition;
    private Point watermarkOffset;
    // 相对位置信息（0.0-1.0之间的比例）
    private double relativeX;
    private double relativeY;
    
    public WatermarkTemplate() {
        // 默认构造函数
    }
    
    public WatermarkTemplate(String templateName) {
        this.templateName = templateName;
    }
    
    // Getters and Setters
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public String getWatermarkText() {
        return watermarkText;
    }
    
    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }
    
    public String getFontFamily() {
        return fontFamily;
    }
    
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }
    
    public int getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    public boolean isBold() {
        return bold;
    }
    
    public void setBold(boolean bold) {
        this.bold = bold;
    }
    
    public boolean isItalic() {
        return italic;
    }
    
    public void setItalic(boolean italic) {
        this.italic = italic;
    }
    
    public Color getTextColor() {
        return textColor;
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }
    
    public int getTransparency() {
        return transparency;
    }
    
    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public boolean isHasShadow() {
        return hasShadow;
    }
    
    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }
    
    public boolean isHasStroke() {
        return hasStroke;
    }
    
    public void setHasStroke(boolean hasStroke) {
        this.hasStroke = hasStroke;
    }
    
    public int getRotationAngle() {
        return rotationAngle;
    }
    
    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    public int getJpegQuality() {
        return jpegQuality;
    }
    
    public void setJpegQuality(int jpegQuality) {
        this.jpegQuality = jpegQuality;
    }
    
    public boolean isUseCustomPosition() {
        return useCustomPosition;
    }
    
    public void setUseCustomPosition(boolean useCustomPosition) {
        this.useCustomPosition = useCustomPosition;
    }
    
    public Point getWatermarkOffset() {
        return watermarkOffset;
    }
    
    public void setWatermarkOffset(Point watermarkOffset) {
        this.watermarkOffset = watermarkOffset;
    }
    
    public double getRelativeX() {
        return relativeX;
    }
    
    public void setRelativeX(double relativeX) {
        this.relativeX = relativeX;
    }
    
    public double getRelativeY() {
        return relativeY;
    }
    
    public void setRelativeY(double relativeY) {
        this.relativeY = relativeY;
    }
    
    @Override
    public String toString() {
        return templateName != null ? templateName : "未命名模板";
    }
}