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
    
    // 导出选项相关组件
    private JComboBox<String> outputFormatComboBox;
    private JSlider jpegQualitySlider;
    private JTextField fileNamePatternField;
    private JLabel outputFolderLabel;
    private File selectedOutputFolder;
    
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
        
        // 输出格式选择
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setBorder(new TitledBorder("输出格式"));
        
        outputFormatComboBox = new JComboBox<>(new String[]{"JPEG", "PNG"});
        outputFormatComboBox.setSelectedItem("JPEG");
        outputFormatComboBox.addActionListener(e -> updateQualitySliderVisibility());
        outputPanel.add(outputFormatComboBox);
        
        // JPEG质量调节
        JPanel qualityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qualityPanel.add(new JLabel("JPEG质量:"));
        jpegQualitySlider = new JSlider(0, 100, 85);
        jpegQualitySlider.setMajorTickSpacing(25);
        jpegQualitySlider.setMinorTickSpacing(5);
        jpegQualitySlider.setPaintTicks(true);
        jpegQualitySlider.setPaintLabels(true);
        qualityPanel.add(jpegQualitySlider);
        outputPanel.add(qualityPanel);
        
        panel.add(outputPanel);
        
        // 导出选项
        JPanel exportOptionsPanel = new JPanel();
        exportOptionsPanel.setLayout(new BoxLayout(exportOptionsPanel, BoxLayout.Y_AXIS));
        exportOptionsPanel.setBorder(new TitledBorder("导出选项"));
        
        // 文件命名规则
        JPanel namingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namingPanel.add(new JLabel("文件名模式:"));
        fileNamePatternField = new JTextField("{name}_watermarked", 15);
        fileNamePatternField.setToolTipText("可用变量: {name}=原文件名, {index}=序号, {timestamp}=时间戳");
        namingPanel.add(fileNamePatternField);
        exportOptionsPanel.add(namingPanel);
        
        // 输出文件夹选择
        JPanel folderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        folderPanel.add(new JLabel("输出文件夹:"));
        outputFolderLabel = new JLabel("未选择");
        outputFolderLabel.setPreferredSize(new Dimension(120, 25));
        outputFolderLabel.setBorder(BorderFactory.createEtchedBorder());
        folderPanel.add(outputFolderLabel);
        
        JButton selectFolderButton = new JButton("选择");
        selectFolderButton.addActionListener(e -> selectOutputFolder());
        folderPanel.add(selectFolderButton);
        exportOptionsPanel.add(folderPanel);
        
        panel.add(exportOptionsPanel);
        
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
            // 显示导入选项对话框
            String[] options = {"选择图片文件", "导入文件夹"};
            int choice = JOptionPane.showOptionDialog(
                SwingPhotoWatermarkApp.this,
                "请选择导入方式：",
                "导入选项",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            
            if (choice == 0) {
                // 选择图片文件
                importImageFiles();
            } else if (choice == 1) {
                // 导入文件夹
                importFromFolder();
            }
        }
        
        private void importImageFiles() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "bmp", "gif", "tiff", "tif"));
            
            int result = fileChooser.showOpenDialog(SwingPhotoWatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                addImagesToList(selectedFiles);
            }
        }
        
        private void importFromFolder() {
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            folderChooser.setDialogTitle("选择包含图片的文件夹");
            
            int result = folderChooser.showOpenDialog(SwingPhotoWatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = folderChooser.getSelectedFile();
                importImagesFromFolder(selectedFolder);
            }
        }
        
        private void importImagesFromFolder(File folder) {
            if (!folder.isDirectory()) return;
            
            List<File> imageFilesInFolder = new ArrayList<>();
            String[] supportedExtensions = {"jpg", "jpeg", "png", "bmp", "gif", "tiff", "tif"};
            
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName().toLowerCase();
                        for (String ext : supportedExtensions) {
                            if (fileName.endsWith("." + ext)) {
                                imageFilesInFolder.add(file);
                                break;
                            }
                        }
                    }
                }
            }
            
            if (imageFilesInFolder.isEmpty()) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, 
                    "所选文件夹中没有找到支持的图片文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                addImagesToList(imageFilesInFolder.toArray(new File[0]));
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, 
                    "成功导入 " + imageFilesInFolder.size() + " 张图片", "导入完成", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        private void addImagesToList(File[] files) {
            for (File file : files) {
                if (!imageFiles.contains(file)) {  // 避免重复添加
                    imageFiles.add(file);
                    imageListModel.addElement(file.getName());
                }
            }
        }
    }
    
    private class ExportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (imageFiles.isEmpty()) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先导入图片", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 检查是否有选中的图片
            int selectedIndex = imageList.getSelectedIndex();
            if (selectedIndex == -1) {
                // 没有选中图片，询问是否批量导出
                int choice = JOptionPane.showConfirmDialog(
                    SwingPhotoWatermarkApp.this,
                    "没有选中图片。是否要批量导出所有图片？",
                    "批量导出",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    batchExport();
                }
                return;
            }
            
            // 单张图片导出
            if (currentImage == null) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择一张图片", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            exportSingleImage();
        }
        
        private void exportSingleImage() {
            // 获取选择的输出格式
            String selectedFormat = (String) outputFormatComboBox.getSelectedItem();
            String formatName = selectedFormat.equals("JPEG") ? "jpg" : "png";
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter(selectedFormat + " 图片", formatName));
            
            int result = fileChooser.showSaveDialog(SwingPhotoWatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith("." + formatName)) {
                    outputFile = new File(outputFile.getAbsolutePath() + "." + formatName);
                }
                
                try {
                    exportImageWithWatermark(currentImage, outputFile, selectedFormat);
                    JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "图片导出成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        private void batchExport() {
            // 检查是否设置了输出文件夹
            if (selectedOutputFolder == null) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择输出文件夹", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String selectedFormat = (String) outputFormatComboBox.getSelectedItem();
            String fileNamePattern = fileNamePatternField.getText().trim();
            if (fileNamePattern.isEmpty()) {
                fileNamePattern = "{name}_watermarked";
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (int i = 0; i < imageFiles.size(); i++) {
                File imageFile = imageFiles.get(i);
                try {
                    BufferedImage image = ImageIO.read(imageFile);
                    if (image != null) {
                        // 生成输出文件名
                        String outputFileName = generateOutputFileName(imageFile, fileNamePattern, i + 1, selectedFormat);
                        File outputFile = new File(selectedOutputFolder, outputFileName);
                        
                        exportImageWithWatermark(image, outputFile, selectedFormat);
                        successCount++;
                    }
                } catch (IOException ex) {
                    failCount++;
                    System.err.println("导出失败: " + imageFile.getName() + " - " + ex.getMessage());
                }
            }
            
            String message = String.format("批量导出完成！\n成功: %d 张\n失败: %d 张", successCount, failCount);
            JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, message, "批量导出结果", JOptionPane.INFORMATION_MESSAGE);
        }
        
        private String generateOutputFileName(File originalFile, String pattern, int index, String format) {
            String originalName = originalFile.getName();
            String nameWithoutExt = originalName.substring(0, originalName.lastIndexOf('.'));
            String extension = format.equals("JPEG") ? "jpg" : "png";
            
            String fileName = pattern
                .replace("{name}", nameWithoutExt)
                .replace("{index}", String.valueOf(index))
                .replace("{format}", format.toLowerCase());
            
            return fileName + "." + extension;
        }
        
        private void exportImageWithWatermark(BufferedImage image, File outputFile, String format) throws IOException {
            BufferedImage watermarkedImage = addWatermark(image);
            
            if (format.equals("PNG")) {
                // PNG格式保持透明通道
                ImageIO.write(watermarkedImage, "png", outputFile);
            } else {
                // JPEG格式，确保没有透明通道
                BufferedImage jpegImage = new BufferedImage(
                    watermarkedImage.getWidth(), 
                    watermarkedImage.getHeight(), 
                    BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g2d = jpegImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, jpegImage.getWidth(), jpegImage.getHeight());
                g2d.drawImage(watermarkedImage, 0, 0, null);
                g2d.dispose();
                
                // 使用JPEG质量设置
                javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                float quality = jpegQualitySlider.getValue() / 100.0f;
                param.setCompressionQuality(quality);
                
                try (javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
                    writer.setOutput(ios);
                    writer.write(null, new javax.imageio.IIOImage(jpegImage, null, null), param);
                    writer.dispose();
                }
            }
        }
    }
    
    private BufferedImage addWatermark(BufferedImage originalImage) {
        // 根据输出格式决定图片类型
        String selectedFormat = (String) outputFormatComboBox.getSelectedItem();
        int imageType = selectedFormat.equals("PNG") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            imageType
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
    
    // 更新JPEG质量滑块的可见性
    private void updateQualitySliderVisibility() {
        boolean isJpeg = "JPEG".equals(outputFormatComboBox.getSelectedItem());
        jpegQualitySlider.setVisible(isJpeg);
        jpegQualitySlider.getParent().revalidate();
        jpegQualitySlider.getParent().repaint();
    }
    
    // 选择输出文件夹
    private void selectOutputFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("选择输出文件夹");
        
        if (selectedOutputFolder != null) {
            folderChooser.setCurrentDirectory(selectedOutputFolder);
        }
        
        int result = folderChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedOutputFolder = folderChooser.getSelectedFile();
            outputFolderLabel.setText("输出文件夹: " + selectedOutputFolder.getName());
        }
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