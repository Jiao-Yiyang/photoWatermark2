package com.photowatermark;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class SwingPhotoWatermarkApp extends JFrame {
    private DefaultListModel<String> imageListModel;
    private JList<String> imageList;
    private JLabel imagePreview;
    private JTextField watermarkText;
    private JSlider transparencySlider;
    private JComboBox<String> positionComboBox;
    private JButton importButton;
    private JButton exportButton;
    
    // 新增的高级功能控件
    private JComboBox<String> fontFamilyComboBox;
    private JSpinner fontSizeSpinner;
    private JCheckBox boldCheckBox;
    private JCheckBox italicCheckBox;
    private JButton colorButton;
    private Color selectedColor = Color.WHITE;
    private JCheckBox shadowCheckBox;
    private JCheckBox strokeCheckBox;
    
    private List<File> imageFiles;
    private BufferedImage currentImage;
    
    public SwingPhotoWatermarkApp() {
        imageFiles = new ArrayList<>();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Photo Watermark 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 左侧面板 - 图片列表
        JPanel leftPanel = createLeftPanel();
        
        // 中央面板 - 图片预览
        JPanel centerPanel = createCenterPanel();
        
        // 右侧面板 - 水印设置
        JPanel rightPanel = createRightPanel();
        
        // 底部面板 - 操作按钮
        JPanel bottomPanel = createBottomPanel();
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // 设置窗口大小和居中显示
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("图片列表"));
        panel.setPreferredSize(new Dimension(200, 0));
        
        imageListModel = new DefaultListModel<>();
        imageList = new JList<>(imageListModel);
        imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedImage();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(imageList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("图片预览"));
        
        imagePreview = new JLabel("请选择图片", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(400, 400));
        imagePreview.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JScrollPane scrollPane = new JScrollPane(imagePreview);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("水印设置"));
        panel.setPreferredSize(new Dimension(280, 0));
        
        // 水印文本
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(new JLabel("水印文本:"), BorderLayout.NORTH);
        watermarkText = new JTextField("Sample Watermark");
        // 添加文本变化监听器
        watermarkText.addActionListener(e -> updatePreview());
        watermarkText.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePreview(); }
        });
        textPanel.add(watermarkText, BorderLayout.CENTER);
        panel.add(textPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // 字体设置面板
        JPanel fontPanel = new JPanel();
        fontPanel.setLayout(new BoxLayout(fontPanel, BoxLayout.Y_AXIS));
        fontPanel.setBorder(new TitledBorder("字体设置"));
        
        // 字体族
        JPanel fontFamilyPanel = new JPanel(new BorderLayout());
        fontFamilyPanel.add(new JLabel("字体:"), BorderLayout.WEST);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontFamilyComboBox = new JComboBox<>(fontNames);
        fontFamilyComboBox.setSelectedItem("Arial");
        fontFamilyComboBox.addActionListener(e -> updatePreview());
        fontFamilyPanel.add(fontFamilyComboBox, BorderLayout.CENTER);
        fontPanel.add(fontFamilyPanel);
        
        // 字号和样式
        JPanel fontStylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fontStylePanel.add(new JLabel("字号:"));
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(24, 8, 200, 1));
        fontSizeSpinner.addChangeListener(e -> updatePreview());
        fontStylePanel.add(fontSizeSpinner);
        
        boldCheckBox = new JCheckBox("粗体");
        boldCheckBox.addActionListener(e -> updatePreview());
        fontStylePanel.add(boldCheckBox);
        
        italicCheckBox = new JCheckBox("斜体");
        italicCheckBox.addActionListener(e -> updatePreview());
        fontStylePanel.add(italicCheckBox);
        
        fontPanel.add(fontStylePanel);
        panel.add(fontPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // 颜色设置
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.add(new JLabel("颜色:"), BorderLayout.WEST);
        colorButton = new JButton();
        colorButton.setBackground(selectedColor);
        colorButton.setPreferredSize(new Dimension(50, 25));
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择水印颜色", selectedColor);
            if (newColor != null) {
                selectedColor = newColor;
                colorButton.setBackground(selectedColor);
                updatePreview();
            }
        });
        colorPanel.add(colorButton, BorderLayout.CENTER);
        panel.add(colorPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // 透明度设置
        JPanel transparencyPanel = new JPanel(new BorderLayout());
        transparencyPanel.add(new JLabel("透明度 (0%=不透明, 100%=透明):"), BorderLayout.NORTH);
        transparencySlider = new JSlider(0, 100, 50);
        transparencySlider.setMajorTickSpacing(25);
        transparencySlider.setPaintTicks(true);
        transparencySlider.setPaintLabels(true);
        // 添加透明度变化监听器
        transparencySlider.addChangeListener(e -> updatePreview());
        transparencyPanel.add(transparencySlider, BorderLayout.CENTER);
        panel.add(transparencyPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // 位置设置
        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.add(new JLabel("水印位置:"), BorderLayout.NORTH);
        String[] positions = {"左上角", "上中", "右上角", "左中", "中心", "右中", "左下角", "下中", "右下角"};
        positionComboBox = new JComboBox<>(positions);
        positionComboBox.setSelectedIndex(8); // 默认右下角
        // 添加位置变化监听器
        positionComboBox.addActionListener(e -> updatePreview());
        positionPanel.add(positionComboBox, BorderLayout.CENTER);
        panel.add(positionPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // 样式效果
        JPanel effectPanel = new JPanel();
        effectPanel.setLayout(new BoxLayout(effectPanel, BoxLayout.Y_AXIS));
        effectPanel.setBorder(new TitledBorder("样式效果"));
        
        shadowCheckBox = new JCheckBox("阴影效果");
        shadowCheckBox.addActionListener(e -> updatePreview());
        effectPanel.add(shadowCheckBox);
        
        strokeCheckBox = new JCheckBox("描边效果");
        strokeCheckBox.addActionListener(e -> updatePreview());
        effectPanel.add(strokeCheckBox);
        
        panel.add(effectPanel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        importButton = new JButton("导入图片");
        importButton.addActionListener(new ImportActionListener());
        
        exportButton = new JButton("导出图片");
        exportButton.addActionListener(new ExportActionListener());
        exportButton.setEnabled(false);
        
        panel.add(importButton);
        panel.add(exportButton);
        
        return panel;
    }
    
    private void loadSelectedImage() {
        int selectedIndex = imageList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < imageFiles.size()) {
            try {
                File selectedFile = imageFiles.get(selectedIndex);
                currentImage = ImageIO.read(selectedFile);
                
                updatePreview();
                exportButton.setEnabled(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "加载图片失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updatePreview() {
        if (currentImage != null) {
            // 创建带水印的预览图片
            BufferedImage previewImage = addWatermarkForPreview(currentImage);
            
            // 缩放图片以适应预览区域
            ImageIcon icon = new ImageIcon(scaleImage(previewImage, 400, 400));
            imagePreview.setIcon(icon);
            imagePreview.setText("");
        }
    }
    
    private BufferedImage addWatermarkForPreview(BufferedImage originalImage) {
        // 创建预览用的水印图片（与导出功能相同的逻辑）
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = watermarkedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制原始图片
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置水印样式
        String text = watermarkText.getText();
        if (text != null && !text.trim().isEmpty()) {
            // 创建字体
            String fontFamily = (String) fontFamilyComboBox.getSelectedItem();
            int fontSize = (Integer) fontSizeSpinner.getValue();
            int fontStyle = Font.PLAIN;
            if (boldCheckBox.isSelected()) fontStyle |= Font.BOLD;
            if (italicCheckBox.isSelected()) fontStyle |= Font.ITALIC;
            
            Font font = new Font(fontFamily, fontStyle, fontSize);
            g2d.setFont(font);
            
            // 修复透明度逻辑：100% = 完全透明，0% = 完全不透明
            float alpha = (100 - transparencySlider.getValue()) / 100.0f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            // 计算文本尺寸
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            
            // 根据选择的位置计算坐标
            int x, y;
            String position = (String) positionComboBox.getSelectedItem();
            
            switch (position) {
                case "左上角":
                    x = 10;
                    y = textHeight;
                    break;
                case "上中":
                    x = (originalImage.getWidth() - textWidth) / 2;
                    y = textHeight;
                    break;
                case "右上角":
                    x = originalImage.getWidth() - textWidth - 10;
                    y = textHeight;
                    break;
                case "左中":
                    x = 10;
                    y = (originalImage.getHeight() + textHeight) / 2;
                    break;
                case "中心":
                    x = (originalImage.getWidth() - textWidth) / 2;
                    y = (originalImage.getHeight() + textHeight) / 2;
                    break;
                case "右中":
                    x = originalImage.getWidth() - textWidth - 10;
                    y = (originalImage.getHeight() + textHeight) / 2;
                    break;
                case "左下角":
                    x = 10;
                    y = originalImage.getHeight() - 10;
                    break;
                case "下中":
                    x = (originalImage.getWidth() - textWidth) / 2;
                    y = originalImage.getHeight() - 10;
                    break;
                case "右下角":
                default:
                    x = originalImage.getWidth() - textWidth - 10;
                    y = originalImage.getHeight() - 10;
                    break;
            }
            
            // 绘制阴影效果
            if (shadowCheckBox.isSelected()) {
                g2d.setColor(Color.BLACK);
                g2d.drawString(text, x + 2, y + 2);
            }
            
            // 绘制描边效果
             if (strokeCheckBox.isSelected()) {
                 g2d.setColor(Color.BLACK);
                 g2d.setStroke(new BasicStroke(2));
                 // 创建文本轮廓
                 Font originalFont = g2d.getFont();
                 FontRenderContext frc = g2d.getFontRenderContext();
                 TextLayout textLayout = new TextLayout(text, originalFont, frc);
                 Shape outline = textLayout.getOutline(AffineTransform.getTranslateInstance(x, y));
                 g2d.draw(outline);
             }
            
            // 绘制主要文本
            g2d.setColor(selectedColor);
            g2d.drawString(text, x, y);
        }
        
        g2d.dispose();
        return watermarkedImage;
    }
    
    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    private class ImportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "bmp", "gif"));
            
            int result = fileChooser.showOpenDialog(SwingPhotoWatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    imageFiles.add(file);
                    imageListModel.addElement(file.getName());
                }
            }
        }
    }
    
    private class ExportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentImage == null) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择一张图片", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JPEG 图片", "jpg"));
            
            int result = fileChooser.showSaveDialog(SwingPhotoWatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".jpg")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".jpg");
                }
                
                try {
                    BufferedImage watermarkedImage = addWatermark(currentImage);
                    ImageIO.write(watermarkedImage, "jpg", outputFile);
                    JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "图片导出成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private BufferedImage addWatermark(BufferedImage originalImage) {
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = watermarkedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 绘制原始图片
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置水印样式
        String text = watermarkText.getText();
        if (text != null && !text.trim().isEmpty()) {
            // 创建字体
            String fontFamily = (String) fontFamilyComboBox.getSelectedItem();
            int fontSize = (Integer) fontSizeSpinner.getValue();
            int fontStyle = Font.PLAIN;
            if (boldCheckBox.isSelected()) fontStyle |= Font.BOLD;
            if (italicCheckBox.isSelected()) fontStyle |= Font.ITALIC;
            
            Font font = new Font(fontFamily, fontStyle, fontSize);
            g2d.setFont(font);
            
            // 修复透明度逻辑：100% = 完全透明，0% = 完全不透明
            float alpha = (100 - transparencySlider.getValue()) / 100.0f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            // 计算文本尺寸
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            
            // 根据选择的位置计算坐标
            int x, y;
            String position = (String) positionComboBox.getSelectedItem();
            
            switch (position) {
                case "左上角":
                    x = 10;
                    y = textHeight + 10;
                    break;
                case "上中":
                    x = (originalImage.getWidth() - textWidth) / 2;
                    y = textHeight + 10;
                    break;
                case "右上角":
                    x = originalImage.getWidth() - textWidth - 10;
                    y = textHeight + 10;
                    break;
                case "左中":
                    x = 10;
                    y = (originalImage.getHeight() + textHeight) / 2;
                    break;
                case "中心":
                    x = (originalImage.getWidth() - textWidth) / 2;
                    y = (originalImage.getHeight() + textHeight) / 2;
                    break;
                case "右中":
                    x = originalImage.getWidth() - textWidth - 10;
                    y = (originalImage.getHeight() + textHeight) / 2;
                    break;
                case "左下角":
                    x = 10;
                    y = originalImage.getHeight() - 10;
                    break;
                case "下中":
                    x = (originalImage.getWidth() - textWidth) / 2;
                    y = originalImage.getHeight() - 10;
                    break;
                case "右下角":
                default:
                    x = originalImage.getWidth() - textWidth - 10;
                    y = originalImage.getHeight() - 10;
                    break;
            }
            
            // 绘制阴影效果
            if (shadowCheckBox.isSelected()) {
                g2d.setColor(Color.BLACK);
                g2d.drawString(text, x + 2, y + 2);
            }
            
            // 绘制描边效果
            if (strokeCheckBox.isSelected()) {
                g2d.setColor(Color.BLACK);
                g2d.setStroke(new BasicStroke(2));
                // 创建文本轮廓
                Font originalFont = g2d.getFont();
                FontRenderContext frc = g2d.getFontRenderContext();
                TextLayout textLayout = new TextLayout(text, originalFont, frc);
                Shape outline = textLayout.getOutline(AffineTransform.getTranslateInstance(x, y));
                g2d.draw(outline);
            }
            
            // 绘制主要文本
            g2d.setColor(selectedColor);
            g2d.drawString(text, x, y);
        }
        
        g2d.dispose();
        return watermarkedImage;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 使用系统默认外观 - 移除有问题的方法调用
                // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                // 如果设置系统外观失败，使用默认外观
                e.printStackTrace();
            }
            
            new SwingPhotoWatermarkApp().setVisible(true);
        });
    }
}