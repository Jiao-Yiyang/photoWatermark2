package com.photowatermark;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
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
    private JButton batchExportButton;
    private JButton deleteButton; // 添加删除按钮作为成员变量
    
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
    private JLabel qualityLabel;
    private JTextField fileNamePatternField;
    private JComboBox<String> fileNamePatternComboBox;
    private JComboBox<String> namingRuleComboBox;
    private JTextField prefixField;
    private JTextField suffixField;
    private JLabel outputFolderLabel;
    private File selectedOutputFolder;
    
    // 旋转角度控制组件
    private JSlider rotationSlider;
    private JTextField rotationTextField;
    
    // 图片水印相关变量
    private JComboBox<String> watermarkTypeComboBox;
    private JButton selectImageButton;
    private JLabel imageWatermarkLabel;
    private BufferedImage watermarkImage;
    private JSlider imageScaleSlider;
    private JTextField imageScaleTextField;
    private JSlider imageTransparencySlider;
    private JPanel textWatermarkPanel;
    private JPanel imageWatermarkPanel;

    private List<File> imageFiles;
    private BufferedImage currentImage;
    private List<Boolean> imageSelectionStates; // 记录每个图片的选中状态
    private List<BufferedImage> thumbnailCache; // 缩略图缓存
    
    public SwingPhotoWatermarkApp() {
        imageFiles = new ArrayList<>();
        imageSelectionStates = new ArrayList<>();
        thumbnailCache = new ArrayList<>();
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
        JScrollPane rightScrollPane = new JScrollPane(rightPanel);
        rightScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rightScrollPane.setPreferredSize(new Dimension(350, 0));
        
        // 底部面板 - 操作按钮
        JPanel bottomPanel = createBottomPanel();
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightScrollPane, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // 设置窗口大小和居中显示
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("图片列表"));
        panel.setPreferredSize(new Dimension(250, 0)); // 增加宽度以容纳缩略图
        
        // 创建带复选框和缩略图的图片列表
        imageListModel = new DefaultListModel<>();
        imageList = new JList<String>(imageListModel);
        
        imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        imageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedImage();
            }
        });
        
        // 添加鼠标点击监听器来处理复选框点击
        imageList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = imageList.locationToIndex(e.getPoint());
                if (index >= 0 && index < imageSelectionStates.size()) {
                    Rectangle cellBounds = imageList.getCellBounds(index, index);
                    if (cellBounds != null) {
                        // 扩大点击区域：整个左侧区域（复选框+缩略图区域）都可以点击
                        if (e.getX() >= cellBounds.x && e.getX() <= cellBounds.x + 80) {
                            // 点击在左侧区域内，切换选中状态
                            imageSelectionStates.set(index, !imageSelectionStates.get(index));
                            imageList.repaint();
                            updateBatchExportButton();
                            updateDeleteButton();
                        }
                    }
                }
            }
        });
        
        // 自定义单元格渲染器，显示缩略图和复选框
        imageList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                
                JPanel cellPanel = new JPanel(new BorderLayout());
                cellPanel.setOpaque(true);
                
                // 设置背景色
                if (isSelected) {
                    cellPanel.setBackground(list.getSelectionBackground());
                } else {
                    cellPanel.setBackground(list.getBackground());
                }
                
                // 左侧：复选框和缩略图
                JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
                leftPanel.setOpaque(false);
                
                // 复选框
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(index < imageSelectionStates.size() && imageSelectionStates.get(index));
                checkBox.setOpaque(false);
                leftPanel.add(checkBox);
                
                // 缩略图
                if (index < thumbnailCache.size() && thumbnailCache.get(index) != null) {
                    BufferedImage thumbnail = thumbnailCache.get(index);
                    ImageIcon thumbnailIcon = new ImageIcon(thumbnail);
                    JLabel thumbnailLabel = new JLabel(thumbnailIcon);
                    leftPanel.add(thumbnailLabel);
                }
                
                cellPanel.add(leftPanel, BorderLayout.WEST);
                
                // 右侧：文件名
                JLabel nameLabel = new JLabel(value.toString());
                nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                if (isSelected) {
                    nameLabel.setForeground(list.getSelectionForeground());
                } else {
                    nameLabel.setForeground(list.getForeground());
                }
                cellPanel.add(nameLabel, BorderLayout.CENTER);
                
                cellPanel.setPreferredSize(new Dimension(240, 50)); // 设置固定高度
                return cellPanel;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(imageList);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加全选/取消全选按钮和删除按钮
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // 垂直排列
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // 添加内边距
        
        JButton selectAllButton = new JButton("全选");
        selectAllButton.addActionListener(e -> {
            for (int i = 0; i < imageSelectionStates.size(); i++) {
                imageSelectionStates.set(i, true);
            }
            imageList.repaint();
            updateBatchExportButton();
            updateDeleteButton();
        });
        
        JButton deselectAllButton = new JButton("取消全选");
        deselectAllButton.addActionListener(e -> {
            for (int i = 0; i < imageSelectionStates.size(); i++) {
                imageSelectionStates.set(i, false);
            }
            imageList.repaint();
            updateBatchExportButton();
            updateDeleteButton();
        });
        
        deleteButton = new JButton("删除选中");
        deleteButton.addActionListener(e -> deleteSelectedImages());
        deleteButton.setEnabled(false); // 初始状态为禁用
        
        buttonPanel.add(selectAllButton);
        buttonPanel.add(deselectAllButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("图片预览"));
        
        imagePreview = new JLabel("请选择图片", SwingConstants.CENTER);
        // 移除固定尺寸限制，让图片可以显示原始大小
        imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreview.setVerticalAlignment(SwingConstants.CENTER);
        imagePreview.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JScrollPane scrollPane = new JScrollPane(imagePreview);
        scrollPane.setPreferredSize(new Dimension(400, 400)); // 设置滚动面板的首选尺寸
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("水印设置"));
        
        // 水印类型选择
        JPanel typePanel = new JPanel(new BorderLayout());
        typePanel.setBorder(new TitledBorder("水印类型"));
        watermarkTypeComboBox = new JComboBox<>(new String[]{"文字水印", "图片水印"});
        watermarkTypeComboBox.setSelectedIndex(0); // 默认选择文字水印
        watermarkTypeComboBox.addActionListener(e -> switchWatermarkType());
        typePanel.add(watermarkTypeComboBox, BorderLayout.CENTER);
        panel.add(typePanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // 创建文字水印面板
        textWatermarkPanel = createTextWatermarkPanel();
        panel.add(textWatermarkPanel);
        
        // 创建图片水印面板
        imageWatermarkPanel = createImageWatermarkPanel();
        imageWatermarkPanel.setVisible(false); // 初始隐藏
        panel.add(imageWatermarkPanel);
        
        return panel;
    }
    
    private JPanel createTextWatermarkPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
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
        mainPanel.add(textPanel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
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
        mainPanel.add(fontPanel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 颜色设置
        JPanel colorPanel = new JPanel(new BorderLayout());
        colorPanel.add(new JLabel("颜色:"), BorderLayout.WEST);
        colorButton = new JButton();
        colorButton.setBackground(selectedColor);
        colorButton.setOpaque(true);
        colorButton.setBorderPainted(false);
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
        mainPanel.add(colorPanel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
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
        mainPanel.add(transparencyPanel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 位置设置和样式效果并排布局
        JPanel positionStylePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // 位置设置
        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.setBorder(new TitledBorder("水印位置"));
        String[] positions = {"左上角", "上中", "右上角", "左中", "中心", "右中", "左下角", "下中", "右下角"};
        positionComboBox = new JComboBox<>(positions);
        positionComboBox.setSelectedIndex(8); // 默认右下角
        positionComboBox.setPreferredSize(new Dimension(120, 25)); // 限制宽度
        // 添加位置变化监听器
        positionComboBox.addActionListener(e -> updatePreview());
        positionPanel.add(positionComboBox, BorderLayout.CENTER);
        
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
        
        positionStylePanel.add(positionPanel);
        positionStylePanel.add(effectPanel);
        mainPanel.add(positionStylePanel);
        
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 旋转角度设置
        JPanel rotationPanel = new JPanel(new BorderLayout());
        rotationPanel.setBorder(new TitledBorder("旋转角度"));
        
        // 滑块和输入框的容器
        JPanel rotationControlPanel = new JPanel(new BorderLayout());
        
        // 滑块
        rotationSlider = new JSlider(-180, 180, 0);
        rotationSlider.setMajorTickSpacing(45);
        rotationSlider.setMinorTickSpacing(15);
        rotationSlider.setPaintTicks(true);
        rotationSlider.setPaintLabels(true);
        rotationSlider.addChangeListener(e -> {
            int value = rotationSlider.getValue();
            rotationTextField.setText(String.valueOf(value));
            updatePreview();
        });
        
        // 输入框和标签
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("角度:"));
        rotationTextField = new JTextField("0", 5);
        rotationTextField.addActionListener(e -> {
            try {
                int value = Integer.parseInt(rotationTextField.getText());
                // 限制角度范围
                value = Math.max(-180, Math.min(180, value));
                rotationSlider.setValue(value);
                rotationTextField.setText(String.valueOf(value));
                updatePreview();
            } catch (NumberFormatException ex) {
                // 如果输入无效，恢复为滑块的值
                rotationTextField.setText(String.valueOf(rotationSlider.getValue()));
            }
        });
        inputPanel.add(rotationTextField);
        inputPanel.add(new JLabel("°"));
        
        rotationControlPanel.add(rotationSlider, BorderLayout.CENTER);
        rotationControlPanel.add(inputPanel, BorderLayout.SOUTH);
        rotationPanel.add(rotationControlPanel, BorderLayout.CENTER);
        mainPanel.add(rotationPanel);
        
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
        qualityLabel = new JLabel("JPEG质量:");
        qualityPanel.add(qualityLabel);
        jpegQualitySlider = new JSlider(0, 100, 85);
        jpegQualitySlider.setMajorTickSpacing(25);
        jpegQualitySlider.setMinorTickSpacing(5);
        jpegQualitySlider.setPaintTicks(true);
        jpegQualitySlider.setPaintLabels(true);
        jpegQualitySlider.setPreferredSize(new Dimension(200, 50));
        qualityPanel.add(jpegQualitySlider);
        outputPanel.add(qualityPanel);
        
        mainPanel.add(outputPanel);
        
        // 导出选项
        JPanel exportOptionsPanel = new JPanel();
        exportOptionsPanel.setLayout(new BoxLayout(exportOptionsPanel, BoxLayout.Y_AXIS));
        exportOptionsPanel.setBorder(new TitledBorder("导出选项"));
        
        // 文件命名规则
        JPanel namingPanel = new JPanel(new BorderLayout());
        namingPanel.add(new JLabel("文件命名规则:"), BorderLayout.NORTH);
        
        // 命名规则选择
        JPanel ruleSelectionPanel = new JPanel(new BorderLayout());
        String[] namingRules = {
            "保留原文件名",
            "添加自定义前缀",
            "添加自定义后缀",
            "自定义模式"
        };
        namingRuleComboBox = new JComboBox<>(namingRules);
        namingRuleComboBox.setSelectedIndex(2); // 默认选择"添加自定义后缀"
        ruleSelectionPanel.add(namingRuleComboBox, BorderLayout.CENTER);
        
        // 创建输入面板容器
        JPanel inputContainer = new JPanel(new CardLayout());
        
        // 1. 保留原文件名 - 空面板
        JPanel originalNamePanel = new JPanel();
        originalNamePanel.add(new JLabel("将保持原始文件名"));
        
        // 2. 自定义前缀面板
        JPanel prefixPanel = new JPanel(new BorderLayout());
        prefixPanel.add(new JLabel("前缀:"), BorderLayout.WEST);
        prefixField = new JTextField("wm_", 10);
        prefixPanel.add(prefixField, BorderLayout.CENTER);
        
        // 3. 自定义后缀面板
        JPanel suffixPanel = new JPanel(new BorderLayout());
        suffixPanel.add(new JLabel("后缀:"), BorderLayout.WEST);
        suffixField = new JTextField("_watermarked", 10);
        suffixPanel.add(suffixField, BorderLayout.CENTER);
        
        // 4. 自定义模式面板（简化版本，直接显示输入框）
        JPanel customModePanel = new JPanel(new BorderLayout());
        customModePanel.add(new JLabel("自定义模式:"), BorderLayout.WEST);
        fileNamePatternField = new JTextField("{name}_watermarked", 15);
        fileNamePatternField.setToolTipText("可用变量: {name}=原文件名, {index}=序号, {format}=格式");
        customModePanel.add(fileNamePatternField, BorderLayout.CENTER);
        
        // 将所有面板添加到CardLayout容器
        inputContainer.add(originalNamePanel, "保留原文件名");
        inputContainer.add(prefixPanel, "添加自定义前缀");
        inputContainer.add(suffixPanel, "添加自定义后缀");
        inputContainer.add(customModePanel, "自定义模式");
        
        // 添加命名规则选择监听器
        namingRuleComboBox.addActionListener(e -> {
            String selectedRule = (String) namingRuleComboBox.getSelectedItem();
            CardLayout cardLayout = (CardLayout) inputContainer.getLayout();
            cardLayout.show(inputContainer, selectedRule);
        });
        
        // 初始化显示默认选择的面板
        CardLayout cardLayout = (CardLayout) inputContainer.getLayout();
        cardLayout.show(inputContainer, "添加自定义后缀");
        
        // 组装面板
        JPanel namingContentPanel = new JPanel(new BorderLayout());
        namingContentPanel.add(ruleSelectionPanel, BorderLayout.NORTH);
        namingContentPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        
        JPanel inputWrapperPanel = new JPanel(new BorderLayout());
        inputWrapperPanel.add(inputContainer, BorderLayout.CENTER);
        namingContentPanel.add(inputWrapperPanel, BorderLayout.SOUTH);
        
        namingPanel.add(namingContentPanel, BorderLayout.CENTER);
        exportOptionsPanel.add(namingPanel);
        
        exportOptionsPanel.add(Box.createVerticalStrut(5));
        
        // 输出文件夹选择
        JPanel folderPanel = new JPanel(new BorderLayout());
        folderPanel.add(new JLabel("输出文件夹:"), BorderLayout.NORTH);
        
        JPanel folderSelectPanel = new JPanel(new BorderLayout());
        outputFolderLabel = new JLabel("未选择");
        outputFolderLabel.setBorder(BorderFactory.createEtchedBorder());
        outputFolderLabel.setPreferredSize(new Dimension(200, 25));
        folderSelectPanel.add(outputFolderLabel, BorderLayout.CENTER);
        
        JButton selectFolderButton = new JButton("选择");
        selectFolderButton.addActionListener(e -> selectOutputFolder());
        folderSelectPanel.add(selectFolderButton, BorderLayout.EAST);
        
        folderPanel.add(folderSelectPanel, BorderLayout.CENTER);
        exportOptionsPanel.add(folderPanel);
        
        mainPanel.add(exportOptionsPanel);
        
        return mainPanel;
    }
    
    private void switchWatermarkType() {
        String selectedType = (String) watermarkTypeComboBox.getSelectedItem();
        if ("文字水印".equals(selectedType)) {
            textWatermarkPanel.setVisible(true);
            imageWatermarkPanel.setVisible(false);
        } else {
            textWatermarkPanel.setVisible(false);
            imageWatermarkPanel.setVisible(true);
        }
        updatePreview();
    }
    
    private JPanel createImageWatermarkPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // 图片选择面板
        JPanel imageSelectPanel = new JPanel(new BorderLayout());
        imageSelectPanel.setBorder(BorderFactory.createTitledBorder("图片选择"));
        
        selectImageButton = new JButton("选择图片");
        selectImageButton.addActionListener(e -> selectWatermarkImage());
        imageSelectPanel.add(selectImageButton, BorderLayout.WEST);
        
        imageWatermarkLabel = new JLabel("未选择图片");
        imageWatermarkLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageSelectPanel.add(imageWatermarkLabel, BorderLayout.CENTER);
        
        mainPanel.add(imageSelectPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 缩放控制面板
        JPanel scalePanel = new JPanel(new BorderLayout());
        scalePanel.setBorder(BorderFactory.createTitledBorder("缩放比例"));
        
        imageScaleSlider = new JSlider(10, 200, 100);
        imageScaleSlider.setMajorTickSpacing(50);
        imageScaleSlider.setMinorTickSpacing(10);
        imageScaleSlider.setPaintTicks(true);
        imageScaleSlider.setPaintLabels(true);
        imageScaleSlider.addChangeListener(e -> {
            int value = imageScaleSlider.getValue();
            imageScaleTextField.setText(String.valueOf(value));
            updatePreview();
        });
        
        imageScaleTextField = new JTextField("100", 5);
        imageScaleTextField.addActionListener(e -> {
            try {
                int value = Integer.parseInt(imageScaleTextField.getText());
                value = Math.max(10, Math.min(200, value));
                imageScaleSlider.setValue(value);
                imageScaleTextField.setText(String.valueOf(value));
                updatePreview();
            } catch (NumberFormatException ex) {
                imageScaleTextField.setText(String.valueOf(imageScaleSlider.getValue()));
            }
        });
        
        JPanel scaleInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scaleInputPanel.add(new JLabel("比例:"));
        scaleInputPanel.add(imageScaleTextField);
        scaleInputPanel.add(new JLabel("%"));
        
        scalePanel.add(scaleInputPanel, BorderLayout.NORTH);
        scalePanel.add(imageScaleSlider, BorderLayout.CENTER);
        
        mainPanel.add(scalePanel);
        mainPanel.add(Box.createVerticalStrut(10));
        
        // 透明度控制面板
        JPanel transparencyPanel = new JPanel(new BorderLayout());
        transparencyPanel.setBorder(BorderFactory.createTitledBorder("透明度"));
        
        imageTransparencySlider = new JSlider(0, 100, 100);
        imageTransparencySlider.setMajorTickSpacing(25);
        imageTransparencySlider.setMinorTickSpacing(5);
        imageTransparencySlider.setPaintTicks(true);
        imageTransparencySlider.setPaintLabels(true);
        imageTransparencySlider.addChangeListener(e -> updatePreview());
        
        transparencyPanel.add(imageTransparencySlider, BorderLayout.CENTER);
        
        mainPanel.add(transparencyPanel);
        
        return mainPanel;
    }
    
    private void selectWatermarkImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件 (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                watermarkImage = ImageIO.read(selectedFile);
                imageWatermarkLabel.setText(selectedFile.getName());
                updatePreview();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "无法加载图片: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        // 导入按钮
        importButton = new JButton("导入图片");
        importButton.addActionListener(new ImportActionListener());
        panel.add(importButton);
        
        // 设置拖拽支持
        setupDragAndDrop();
        
        // 导出按钮
        exportButton = new JButton("导出当前图片");
        exportButton.setEnabled(false);
        exportButton.addActionListener(new ExportActionListener());
        panel.add(exportButton);
        
        // 批量导出按钮
        batchExportButton = new JButton("批量导出");
        batchExportButton.setEnabled(false);
        batchExportButton.addActionListener(new ExportActionListener());
        panel.add(batchExportButton);
        
        return panel;
    }
    
    // 生成缩略图的方法
    private BufferedImage generateThumbnail(BufferedImage originalImage) {
        int thumbnailSize = 40; // 缩略图大小
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // 计算缩放比例，保持宽高比
        double scale = Math.min((double) thumbnailSize / originalWidth, (double) thumbnailSize / originalHeight);
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        // 创建缩略图
        BufferedImage thumbnail = new BufferedImage(thumbnailSize, thumbnailSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, thumbnailSize, thumbnailSize);
        
        // 居中绘制缩放后的图片
        int x = (thumbnailSize - scaledWidth) / 2;
        int y = (thumbnailSize - scaledHeight) / 2;
        g2d.drawImage(originalImage, x, y, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        return thumbnail;
    }
    
    // 设置拖拽支持
    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    dtde.rejectDrag();
                }
            }
            
            @Override
            public void dragOver(DropTargetDragEvent dtde) {}
            
            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}
            
            @Override
            public void dragExit(DropTargetEvent dte) {}
            
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        
                        List<File> imageFiles = new ArrayList<>();
                        for (File file : droppedFiles) {
                            if (file.isDirectory()) {
                                // 递归添加目录中的图片文件
                                addImagesFromDirectory(file, imageFiles);
                            } else if (isImageFile(file)) {
                                imageFiles.add(file);
                            }
                        }
                        
                        if (!imageFiles.isEmpty()) {
                            ImportActionListener importListener = new ImportActionListener();
                            importListener.addImagesToList(imageFiles.toArray(new File[0]));
                        }
                        
                        dtde.dropComplete(true);
                    } else {
                        dtde.dropComplete(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.dropComplete(false);
                }
            }
        });
    }
    
    // 递归添加目录中的图片文件
    private void addImagesFromDirectory(File directory, List<File> imageFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addImagesFromDirectory(file, imageFiles);
                } else if (isImageFile(file)) {
                    imageFiles.add(file);
                }
            }
        }
    }
    
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") || 
               name.endsWith(".bmp");
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
                JOptionPane.showMessageDialog(this, "无法加载图片: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                currentImage = null;
                imagePreview.setIcon(null);
                imagePreview.setText("无法加载图片");
                exportButton.setEnabled(false);
            }
        } else {
            currentImage = null;
            imagePreview.setIcon(null);
            imagePreview.setText("请选择图片");
            exportButton.setEnabled(false);
        }
    }
    
    private void updatePreview() {
        if (currentImage != null) {
            BufferedImage previewImage = addWatermarkForPreview(currentImage);
            // 强制缩放图片到固定尺寸以适应预览区域
            BufferedImage scaledImage = scaleImage(previewImage, 400, 400);
            imagePreview.setIcon(new ImageIcon(scaledImage));
            imagePreview.setText("");
            
            // 设置固定的首选尺寸
            imagePreview.setPreferredSize(new Dimension(400, 400));
            imagePreview.revalidate(); // 重新验证布局
        }
    }
    
    private BufferedImage addWatermarkForPreview(BufferedImage originalImage) {
        // 创建图片副本用于预览
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = watermarkedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 检查水印类型
        String watermarkType = (String) watermarkTypeComboBox.getSelectedItem();
        
        if ("文字水印".equals(watermarkType)) {
            // 文字水印处理
            String text = watermarkText.getText();
            if (text != null && !text.trim().isEmpty()) {
                addTextWatermark(g2d, originalImage, text);
            }
        } else if ("图片水印".equals(watermarkType) && watermarkImage != null) {
            // 图片水印处理
            addImageWatermark(g2d, originalImage);
        }
        
        g2d.dispose();
        return watermarkedImage;
    }
    
    private void addTextWatermark(Graphics2D g2d, BufferedImage originalImage, String text) {
        // 获取字体设置
        String fontFamily = (String) fontFamilyComboBox.getSelectedItem();
        int fontSize = (Integer) fontSizeSpinner.getValue();
        int fontStyle = Font.PLAIN;
        if (boldCheckBox.isSelected()) fontStyle |= Font.BOLD;
        if (italicCheckBox.isSelected()) fontStyle |= Font.ITALIC;
        
        Font font = new Font(fontFamily, fontStyle, fontSize);
        g2d.setFont(font);
        
        // 计算文本位置
        FontRenderContext frc = g2d.getFontRenderContext();
        TextLayout textLayout = new TextLayout(text, font, frc);
        Rectangle2D textBounds = textLayout.getBounds();
        
        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();
        int textWidth = (int) textBounds.getWidth();
        int textHeight = (int) textBounds.getHeight();
        
        int x, y;
        String position = (String) positionComboBox.getSelectedItem();
        
        switch (position) {
            case "左上角":
                x = 10;
                y = textHeight + 10;
                break;
            case "上中":
                x = (imageWidth - textWidth) / 2;
                y = textHeight + 10;
                break;
            case "右上角":
                x = imageWidth - textWidth - 10;
                y = textHeight + 10;
                break;
            case "左中":
                x = 10;
                y = (imageHeight + textHeight) / 2;
                break;
            case "中心":
                x = (imageWidth - textWidth) / 2;
                    y = (imageHeight + textHeight) / 2;
                    break;
                case "右中":
                    x = imageWidth - textWidth - 10;
                    y = (imageHeight + textHeight) / 2;
                    break;
                case "左下角":
                    x = 10;
                    y = imageHeight - 10;
                    break;
                case "下中":
                    x = (imageWidth - textWidth) / 2;
                    y = imageHeight - 10;
                    break;
                case "右下角":
                default:
                    x = imageWidth - textWidth - 10;
                    y = imageHeight - 10;
                    break;
            }
            
            // 设置透明度
            float alpha = 1.0f - (transparencySlider.getValue() / 100.0f);
            
            // 获取旋转角度
            int rotationAngle = rotationSlider.getValue();
            
            // 如果有旋转角度，应用旋转变换
            if (rotationAngle != 0) {
                // 保存当前的变换状态
                AffineTransform originalTransform = g2d.getTransform();
                
                // 计算旋转中心点（文本的中心）
                double centerX = x + textWidth / 2.0;
                double centerY = y - textHeight / 2.0;
                
                // 应用旋转变换
                g2d.rotate(Math.toRadians(rotationAngle), centerX, centerY);
                
                // 绘制阴影效果
                if (shadowCheckBox.isSelected()) {
                    g2d.setColor(new Color(0, 0, 0, alpha * 0.5f));
                    g2d.drawString(text, x + 2, y + 2);
                }
                
                // 绘制描边效果
                if (strokeCheckBox.isSelected()) {
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.setStroke(new BasicStroke(2));
                    
                    // 创建文本轮廓
                    AffineTransform textTransform = AffineTransform.getTranslateInstance(x, y);
                    Shape textShape = textLayout.getOutline(textTransform);
                    g2d.draw(textShape);
                }
                
                // 绘制主文本
                g2d.setColor(new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (int)(alpha * 255)));
                g2d.drawString(text, x, y);
                
                // 恢复原始变换状态
                g2d.setTransform(originalTransform);
            } else {
                // 没有旋转时的原始绘制逻辑
                // 绘制阴影效果
                if (shadowCheckBox.isSelected()) {
                    g2d.setColor(new Color(0, 0, 0, alpha * 0.5f));
                    g2d.drawString(text, x + 2, y + 2);
                }
                
                // 绘制描边效果
                if (strokeCheckBox.isSelected()) {
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.setStroke(new BasicStroke(2));
                    
                    // 创建文本轮廓
                    AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
                    Shape textShape = textLayout.getOutline(transform);
                    g2d.draw(textShape);
                }
                
                // 绘制主文本
                g2d.setColor(new Color(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), (int)(alpha * 255)));
                g2d.drawString(text, x, y);
            }
        }
    }
    
    private void addImageWatermark(Graphics2D g2d, BufferedImage originalImage) {
        if (watermarkImage == null) return;
        
        // 获取缩放比例
        float scale = imageScaleSlider.getValue() / 100.0f;
        int scaledWidth = (int) (watermarkImage.getWidth() * scale);
        int scaledHeight = (int) (watermarkImage.getHeight() * scale);
        
        // 计算位置（使用与文字水印相同的位置逻辑）
        int imageWidth = originalImage.getWidth();
        int imageHeight = originalImage.getHeight();
        
        int x, y;
        String position = (String) positionComboBox.getSelectedItem();
        
        switch (position) {
            case "左上角":
                x = 10;
                y = 10;
                break;
            case "上中":
                x = (imageWidth - scaledWidth) / 2;
                y = 10;
                break;
            case "右上角":
                x = imageWidth - scaledWidth - 10;
                y = 10;
                break;
            case "左中":
                x = 10;
                y = (imageHeight - scaledHeight) / 2;
                break;
            case "中心":
                x = (imageWidth - scaledWidth) / 2;
                y = (imageHeight - scaledHeight) / 2;
                break;
            case "右中":
                x = imageWidth - scaledWidth - 10;
                y = (imageHeight - scaledHeight) / 2;
                break;
            case "左下角":
                x = 10;
                y = imageHeight - scaledHeight - 10;
                break;
            case "下中":
                x = (imageWidth - scaledWidth) / 2;
                y = imageHeight - scaledHeight - 10;
                break;
            case "右下角":
            default:
                x = imageWidth - scaledWidth - 10;
                y = imageHeight - scaledHeight - 10;
                break;
        }
        
        // 设置透明度
        float alpha = imageTransparencySlider.getValue() / 100.0f;
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // 绘制缩放后的水印图片
        g2d.drawImage(watermarkImage, x, y, scaledWidth, scaledHeight, null);
        
        // 恢复原始合成模式
        g2d.setComposite(originalComposite);
    }
    
    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // 计算缩放比例
        double scaleX = (double) maxWidth / originalWidth;
        double scaleY = (double) maxHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY);
        
        // 如果图片已经小于最大尺寸，不需要缩放
        if (scale >= 1.0) {
            return original;
        }
        
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        return scaledImage;
    }
    
    private class ImportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JPopupMenu popupMenu = new JPopupMenu();
            
            JMenuItem importFiles = new JMenuItem("导入图片文件");
            importFiles.addActionListener(ev -> importImageFiles());
            popupMenu.add(importFiles);
            
            JMenuItem importFolder = new JMenuItem("导入文件夹");
            importFolder.addActionListener(ev -> importFromFolder());
            popupMenu.add(importFolder);
            
            // 显示弹出菜单
            Component source = (Component) e.getSource();
            popupMenu.show(source, 0, source.getHeight());
        }
        
        private void importImageFiles() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif", "bmp"));
            
            if (fileChooser.showOpenDialog(SwingPhotoWatermarkApp.this) == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                addImagesToList(selectedFiles);
            }
        }
        
        private void importFromFolder() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            
            if (fileChooser.showOpenDialog(SwingPhotoWatermarkApp.this) == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                importImagesFromFolder(selectedFolder);
            }
        }
        
        private void importImagesFromFolder(File folder) {
            List<File> imageFilesList = new ArrayList<>();
            addImagesFromDirectory(folder, imageFilesList);
            
            if (imageFilesList.isEmpty()) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, 
                    "在选择的文件夹中没有找到支持的图片文件", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                addImagesToList(imageFilesList.toArray(new File[0]));
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, 
                    "成功导入 " + imageFilesList.size() + " 张图片", "导入成功", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        private void addImagesToList(File[] files) {
            List<String> duplicateFiles = new ArrayList<>();
            List<File> newFiles = new ArrayList<>();
            
            for (File file : files) {
                if (isImageFile(file)) {
                    // 检查是否已存在同名同后缀的文件
                    boolean isDuplicate = false;
                    for (File existingFile : imageFiles) {
                        if (existingFile.getName().equals(file.getName())) {
                            duplicateFiles.add(file.getName());
                            isDuplicate = true;
                            break;
                        }
                    }
                    
                    if (!isDuplicate) {
                        newFiles.add(file);
                    }
                }
            }
            
            // 如果有重复文件，显示提示
            if (!duplicateFiles.isEmpty()) {
                StringBuilder message = new StringBuilder("以下文件已存在，将跳过导入：\n");
                for (String fileName : duplicateFiles) {
                    message.append("• ").append(fileName).append("\n");
                }
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, 
                    message.toString(), "重复文件提示", JOptionPane.WARNING_MESSAGE);
            }
            
            // 添加新文件
            for (File file : newFiles) {
                imageFiles.add(file);
                imageListModel.addElement(file.getName());
                imageSelectionStates.add(false); // 默认不选中
                
                // 生成并缓存缩略图
                try {
                    BufferedImage originalImage = ImageIO.read(file);
                    BufferedImage thumbnail = generateThumbnail(originalImage);
                    thumbnailCache.add(thumbnail);
                } catch (IOException e) {
                    // 如果无法生成缩略图，添加null占位
                    thumbnailCache.add(null);
                }
            }
            
            // 如果之前没有选中任何图片，自动选中第一张
            if (imageList.getSelectedIndex() == -1 && !imageFiles.isEmpty()) {
                imageList.setSelectedIndex(0);
            }
            
            updateBatchExportButton();
            updateDeleteButton();
        }
    }
    
    private void updateBatchExportButton() {
        boolean hasSelectedImages = false;
        for (Boolean selected : imageSelectionStates) {
            if (selected) {
                hasSelectedImages = true;
                break;
            }
        }
        batchExportButton.setEnabled(hasSelectedImages);
    }
    
    private void updateDeleteButton() {
        boolean hasSelectedImages = false;
        for (Boolean selected : imageSelectionStates) {
            if (selected) {
                hasSelectedImages = true;
                break;
            }
        }
        deleteButton.setEnabled(hasSelectedImages);
    }
    
    private void deleteSelectedImages() {
        // 获取选中的图片索引（从后往前删除以避免索引变化问题）
        List<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < imageSelectionStates.size(); i++) {
            if (imageSelectionStates.get(i)) {
                selectedIndices.add(i);
            }
        }
        
        if (selectedIndices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的图片", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 确认删除
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要删除选中的 " + selectedIndices.size() + " 张图片吗？", 
            "确认删除", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // 从后往前删除，避免索引变化
            for (int i = selectedIndices.size() - 1; i >= 0; i--) {
                int index = selectedIndices.get(i);
                imageFiles.remove(index);
                imageSelectionStates.remove(index);
                thumbnailCache.remove(index);
                imageListModel.remove(index);
            }
            
            // 更新预览
            if (imageFiles.isEmpty()) {
                currentImage = null;
                updatePreview();
            } else {
                // 如果当前选中的图片被删除了，选择第一张图片
                imageList.setSelectedIndex(0);
                loadSelectedImage();
            }
            
            updateBatchExportButton();
            updateDeleteButton();
        }
    }
    
    private class ExportActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == batchExportButton) {
                batchExport();
            } else {
                // 检查是否有选中的图片
                int selectedIndex = imageList.getSelectedIndex();
                if (selectedIndex == -1) {
                    JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择一张图片", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // 单张图片导出
                if (currentImage == null) {
                    JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择一张图片", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                exportSingleImage();
            }
        }
        
        private void exportSingleImage() {
            // 检查是否设置了输出文件夹
            if (selectedOutputFolder == null) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择输出文件夹", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 获取选择的输出格式
            String selectedFormat = (String) outputFormatComboBox.getSelectedItem();
            
            try {
                // 获取当前选中的图片文件
                int selectedIndex = imageList.getSelectedIndex();
                File currentImageFile = imageFiles.get(selectedIndex);
                
                // 生成输出文件名（使用索引1，因为是单张导出）
                String outputFileName = generateOutputFileName(currentImageFile, 1, selectedFormat);
                File outputFile = new File(selectedOutputFolder, outputFileName);
                
                // 检查文件是否存在，如果存在则询问用户
                if (outputFile.exists()) {
                    int choice = JOptionPane.showConfirmDialog(
                        SwingPhotoWatermarkApp.this,
                        "文件 " + outputFile.getName() + " 已存在，是否覆盖？",
                        "文件已存在",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    
                    if (choice == JOptionPane.NO_OPTION) {
                        // 用户选择不覆盖，生成唯一文件名
                        outputFile = generateUniqueFileNameWithOriginalFormat(outputFile, currentImageFile);
                    } else if (choice == JOptionPane.CANCEL_OPTION) {
                        // 用户取消操作
                        return;
                    }
                    // YES_OPTION 继续覆盖
                }
                
                exportImageWithWatermark(currentImage, outputFile, selectedFormat);
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, 
                    "图片导出成功!\n保存位置: " + outputFile.getAbsolutePath(), 
                    "成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "导出失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        private void batchExport() {
            // 检查是否设置了输出文件夹
            if (selectedOutputFolder == null) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择输出文件夹", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 获取选择的输出格式
            String selectedFormat = (String) outputFormatComboBox.getSelectedItem();
            
            // 收集选中的图片
            List<Integer> selectedIndices = new ArrayList<>();
            for (int i = 0; i < imageSelectionStates.size(); i++) {
                if (imageSelectionStates.get(i)) {
                    selectedIndices.add(i);
                }
            }
            
            if (selectedIndices.isEmpty()) {
                JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, "请先选择要导出的图片", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 批量导出
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();
            
            for (int i = 0; i < selectedIndices.size(); i++) {
                int imageIndex = selectedIndices.get(i);
                File imageFile = imageFiles.get(imageIndex);
                
                try {
                    BufferedImage image = ImageIO.read(imageFile);
                    String outputFileName = generateOutputFileName(imageFile, i + 1, selectedFormat);
                    File outputFile = new File(selectedOutputFolder, outputFileName);
                    
                    // 使用防覆盖机制生成唯一文件名
                    outputFile = generateUniqueFileName(outputFile);
                    
                    exportImageWithWatermark(image, outputFile, selectedFormat);
                    successCount++;
                } catch (IOException ex) {
                    failCount++;
                    errorMessages.append("文件 ").append(imageFile.getName()).append(": ").append(ex.getMessage()).append("\n");
                }
            }
            
            // 显示结果
            String message = "批量导出完成!\n成功: " + successCount + " 张\n失败: " + failCount + " 张";
            if (failCount > 0) {
                message += "\n\n错误详情:\n" + errorMessages.toString();
            }
            
            JOptionPane.showMessageDialog(SwingPhotoWatermarkApp.this, message, 
                failCount > 0 ? "部分成功" : "全部成功", 
                failCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
        }
        
        private File generateUniqueFileName(File originalFile) {
            if (!originalFile.exists()) {
                return originalFile;
            }
            
            String originalPath = originalFile.getAbsolutePath();
            String directory = originalFile.getParent();
            String fileName = originalFile.getName();
            
            // 分离文件名和扩展名
            String nameWithoutExt;
            String extension;
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                nameWithoutExt = fileName.substring(0, lastDotIndex);
                extension = fileName.substring(lastDotIndex);
            } else {
                nameWithoutExt = fileName;
                extension = "";
            }
            
            // 尝试添加数字后缀
            int counter = 1;
            File newFile;
            do {
                String newFileName = nameWithoutExt + "_" + counter + extension;
                newFile = new File(directory, newFileName);
                counter++;
            } while (newFile.exists());
            
            return newFile;
        }
        
        private File generateUniqueFileNameWithOriginalFormat(File outputFile, File originalFile) {
            String outputPath = outputFile.getAbsolutePath();
            String outputName = outputFile.getName();
            String outputDir = outputFile.getParent();
            
            // 获取原始文件的扩展名
            String originalExt = "";
            String originalName = originalFile.getName();
            int lastDot = originalName.lastIndexOf('.');
            if (lastDot > 0) {
                originalExt = originalName.substring(lastDot + 1).toLowerCase();
            }
            
            // 获取输出文件的扩展名
            String outputExt = "";
            int outputLastDot = outputName.lastIndexOf('.');
            if (outputLastDot > 0) {
                outputExt = outputName.substring(outputLastDot + 1).toLowerCase();
            }
            
            // 如果原始格式和输出格式不同，且存在同名文件，则在文件名中加入原始格式
            if (!originalExt.equals(outputExt) && outputFile.exists()) {
                String nameWithoutExt = outputName.substring(0, outputLastDot);
                String newFileName = nameWithoutExt + "_" + originalExt + "." + outputExt;
                File newFile = new File(outputDir, newFileName);
                
                // 如果加了原始格式后缀的文件名仍然存在，则使用数字后缀
                if (newFile.exists()) {
                    return generateUniqueFileName(newFile);
                } else {
                    return newFile;
                }
            } else {
                // 如果格式相同或没有冲突，使用普通的数字后缀方式
                return generateUniqueFileName(outputFile);
            }
        }
        
        private String generateOutputFileName(File originalFile, int index, String format) {
            String originalName = originalFile.getName();
            String nameWithoutExt = originalName;
            
            // 移除原始扩展名
            int lastDotIndex = originalName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                nameWithoutExt = originalName.substring(0, lastDotIndex);
            }
            
            String selectedRule = (String) namingRuleComboBox.getSelectedItem();
            String newName = nameWithoutExt;
            
            switch (selectedRule) {
                case "保留原文件名":
                    // 保持原文件名不变
                    break;
                case "添加自定义前缀":
                    String prefix = prefixField.getText().trim();
                    if (!prefix.isEmpty()) {
                        newName = prefix + nameWithoutExt;
                    }
                    break;
                case "添加自定义后缀":
                    String suffix = suffixField.getText().trim();
                    if (!suffix.isEmpty()) {
                        newName = nameWithoutExt + suffix;
                    }
                    break;
                case "自定义模式":
                    String pattern = fileNamePatternField.getText().trim();
                    if (!pattern.isEmpty()) {
                        newName = pattern.replace("{name}", nameWithoutExt)
                                         .replace("{index}", String.valueOf(index))
                                         .replace("{format}", format.toLowerCase());
                    }
                    break;
            }
            
            // 添加新的扩展名
            return newName + "." + format.toLowerCase();
        }
        
        private void exportImageWithWatermark(BufferedImage image, File outputFile, String format) throws IOException {
            BufferedImage watermarkedImage = addWatermark(image);
            
            if ("JPEG".equalsIgnoreCase(format)) {
                // JPEG格式，使用质量设置
                javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("JPEG").next();
                javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(jpegQualitySlider.getValue() / 100.0f);
                
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(outputFile);
                     javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
                    writer.setOutput(ios);
                    writer.write(null, new javax.imageio.IIOImage(watermarkedImage, null, null), param);
                }
                writer.dispose();
            } else {
                // PNG或其他格式
                ImageIO.write(watermarkedImage, format, outputFile);
            }
        }
    }
    
    private BufferedImage addWatermark(BufferedImage originalImage) {
        // 创建图片副本
        BufferedImage watermarkedImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = watermarkedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, null);
        
        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 根据水印类型添加水印
        String selectedType = (String) watermarkTypeComboBox.getSelectedItem();
        if ("文字水印".equals(selectedType)) {
            String text = watermarkText.getText();
            if (text != null && !text.trim().isEmpty()) {
                addTextWatermark(g2d, originalImage, text);
            }
        } else if ("图片水印".equals(selectedType)) {
            if (watermarkImage != null) {
                addImageWatermark(g2d, originalImage);
            }
        }
        
        g2d.dispose();
        return watermarkedImage;
    }
    
    private void updateQualitySliderVisibility() {
        boolean isJPEG = "JPEG".equals(outputFormatComboBox.getSelectedItem());
        qualityLabel.setVisible(isJPEG);
        jpegQualitySlider.setVisible(isJPEG);
    }
    
    private void selectOutputFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择输出文件夹");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedOutputFolder = fileChooser.getSelectedFile();
            outputFolderLabel.setText(selectedOutputFolder.getAbsolutePath());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SwingPhotoWatermarkApp().setVisible(true);
        });
    }
}