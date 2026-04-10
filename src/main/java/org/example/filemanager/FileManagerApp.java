package org.example.filemanager;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 个人文件分类管理系统 - 主窗口
 * 包含动态菜单栏、工具栏、分类树、文件列表、状态栏
 */
public class FileManagerApp extends JFrame {
    
    // ==================== 组件声明 ====================
    
    // 菜单栏
    private JMenuBar menuBar;
    
    // 菜单
    private JMenu fileMenu, editMenu, viewMenu, toolsMenu, helpMenu;
    
    // 工具栏
    private JToolBar toolBar;
    private JButton importBtn, newCategoryBtn, deleteBtn, renameBtn, recycleBinBtn;
    private JTextField searchField;
    private JButton searchBtn;
    
    // 分类树
    private JTree categoryTree;
    private DefaultTreeModel treeModel;
    
    // 文件列表
    private JTable fileTable;
    private DefaultTableModel tableModel;
    
    // 状态栏
    private JLabel statusLabel, fileCountLabel, storageLabel;
    
    // 进度条
    private JProgressBar progressBar;
    
    // ==================== 数据 ====================
    
    private List<Models.Category> categories;
    private List<Models.FileInfo> currentFiles;
    private Integer currentCategoryId = null; // null 表示显示全部
    private boolean showDeleted = false; // 是否显示回收站
    
    // 列名
    private final String[] COLUMN_NAMES = {"文件名", "路径", "大小", "分类", "导入时间"};
    
    // 配色
    private static final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private static final Color ACCENT_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(231, 76, 60);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;
    
    // ==================== 构造函数 ====================
    
    public FileManagerApp() {
        // 设置窗口属性
        setTitle("📁 个人文件分类管理系统");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 500));
        
        // 初始化数据库
        DatabaseManager.initializeDatabase();
        
        // 初始化UI
        initComponents();
        
        // 加载数据
        refreshAll();
        
        // 设置可见
        setVisible(true);
    }
    
    // ==================== 初始化组件 ====================
    
    private void initComponents() {
        // 设置整体布局
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        
        // 创建菜单栏
        createMenuBar();
        
        // 创建工具栏
        createToolBar();
        
        // 创建主面板（左右布局）
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 左侧：分类树
        JPanel leftPanel = createCategoryPanel();
        splitPane.setLeftComponent(leftPanel);
        
        // 右侧：文件列表
        JPanel rightPanel = createFileListPanel();
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // 底部：状态栏
        createStatusBar();
    }
    
    // ==================== 创建动态菜单栏 ====================
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_COLOR));
        
        // 文件菜单
        createFileMenu();
        
        // 编辑菜单
        createEditMenu();
        
        // 视图菜单
        createViewMenu();
        
        // 工具菜单
        createToolsMenu();
        
        // 帮助菜单
        createHelpMenu();
        
        setJMenuBar(menuBar);
    }
    
    /**
     * 文件菜单
     */
    private void createFileMenu() {
        fileMenu = createMenu("文件(F)", KeyEvent.VK_F);
        
        // 导入文件
        JMenuItem importItem = createMenuItem("导入文件/文件夹...", KeyEvent.VK_I, 
            KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        importItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        importItem.addActionListener(e -> importFiles());
        fileMenu.add(importItem);
        
        // 新建分类
        JMenuItem newCatItem = createMenuItem("新建分类...", KeyEvent.VK_N,
            KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newCatItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newCatItem.addActionListener(e -> showNewCategoryDialog());
        fileMenu.add(newCatItem);
        
        fileMenu.addSeparator();
        
        // 重命名
        JMenuItem renameItem = createMenuItem("重命名选中文件", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        renameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        renameItem.addActionListener(e -> renameSelectedFile());
        fileMenu.add(renameItem);
        
        // 移动到
        JMenuItem moveItem = createMenuItem("移动到...", KeyEvent.VK_M,
            KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        moveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        moveItem.addActionListener(e -> moveSelectedFile());
        fileMenu.add(moveItem);
        
        // 删除
        JMenuItem deleteItem = createMenuItem("删除选中文件", KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(e -> deleteSelectedFiles());
        fileMenu.add(deleteItem);
        
        fileMenu.addSeparator();
        
        // 退出
        JMenuItem exitItem = createMenuItem("退出", KeyEvent.VK_X, 
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
    }
    
    /**
     * 编辑菜单
     */
    private void createEditMenu() {
        editMenu = createMenu("编辑(E)", KeyEvent.VK_E);
        
        // 搜索
        JMenuItem searchItem = createMenuItem("搜索文件...", KeyEvent.VK_F,
            KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        searchItem.addActionListener(e -> searchField.requestFocus());
        editMenu.add(searchItem);
        
        // 刷新
        JMenuItem refreshItem = createMenuItem("刷新", KeyEvent.VK_R, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        refreshItem.addActionListener(e -> refreshAll());
        editMenu.add(refreshItem);
        
        editMenu.addSeparator();
        
        // 全选
        JMenuItem selectAllItem = createMenuItem("全选", KeyEvent.VK_A,
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        selectAllItem.addActionListener(e -> fileTable.selectAll());
        editMenu.add(selectAllItem);
        
        // 反向选择
        JMenuItem invertItem = createMenuItem("反向选择", KeyEvent.VK_I, null);
        invertItem.addActionListener(e -> invertSelection());
        editMenu.add(invertItem);
        
        menuBar.add(editMenu);
    }
    
    /**
     * 视图菜单
     */
    private void createViewMenu() {
        viewMenu = createMenu("视图(V)", KeyEvent.VK_V);
        
        // 全部文件
        JMenuItem allFilesItem = createMenuItem("全部文件", KeyEvent.VK_1,
            KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
        allFilesItem.addActionListener(e -> showAllFiles());
        viewMenu.add(allFilesItem);
        
        // 按分类显示 - 动态子菜单
        JMenu categorySubMenu = new JMenu("按分类显示");
        categorySubMenu.setMnemonic(KeyEvent.VK_R);
        updateCategorySubMenu(categorySubMenu);
        viewMenu.add(categorySubMenu);
        
        viewMenu.addSeparator();
        
        // 显示/隐藏工具栏
        JCheckBoxMenuItem toolBarItem = new JCheckBoxMenuItem("显示工具栏");
        toolBarItem.setSelected(true);
        toolBarItem.addActionListener(e -> toolBar.setVisible(toolBarItem.isSelected()));
        viewMenu.add(toolBarItem);
        
        // 显示/隐藏状态栏
        JCheckBoxMenuItem statusBarItem = new JCheckBoxMenuItem("显示状态栏");
        statusBarItem.setSelected(true);
        statusBarItem.addActionListener(e -> {
            Component[] comps = getContentPane().getComponents();
            for (Component comp : comps) {
                if (comp instanceof JPanel) {
                    ((JPanel) comp).setVisible(statusBarItem.isSelected());
                }
            }
        });
        viewMenu.add(statusBarItem);
        
        menuBar.add(viewMenu);
    }
    
    /**
     * 工具菜单
     */
    private void createToolsMenu() {
        toolsMenu = createMenu("工具(T)", KeyEvent.VK_T);
        
        // 回收站
        JMenuItem recycleItem = createMenuItem("回收站", KeyEvent.VK_R,
            KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        recycleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        recycleItem.addActionListener(e -> showRecycleBin());
        toolsMenu.add(recycleItem);
        
        // 导入历史
        JMenuItem historyItem = createMenuItem("导入历史", KeyEvent.VK_H, null);
        historyItem.addActionListener(e -> showImportHistory());
        toolsMenu.add(historyItem);
        
        toolsMenu.addSeparator();
        
        // 清空数据库
        JMenuItem clearItem = createMenuItem("清空数据库", KeyEvent.VK_C, null);
        clearItem.setForeground(WARNING_COLOR);
        clearItem.addActionListener(e -> clearDatabase());
        toolsMenu.add(clearItem);
        
        // 重新初始化数据库
        JMenuItem initItem = createMenuItem("重新初始化数据库", KeyEvent.VK_I, null);
        initItem.addActionListener(e -> reinitializeDatabase());
        toolsMenu.add(initItem);
        
        menuBar.add(toolsMenu);
    }
    
    /**
     * 帮助菜单
     */
    private void createHelpMenu() {
        helpMenu = createMenu("帮助(H)", KeyEvent.VK_H);
        
        // 用户指南
        JMenuItem guideItem = createMenuItem("用户指南", KeyEvent.VK_G, null);
        guideItem.addActionListener(e -> showUserGuide());
        helpMenu.add(guideItem);
        
        helpMenu.addSeparator();
        
        // 关于
        JMenuItem aboutItem = createMenuItem("关于", KeyEvent.VK_A, null);
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        
        menuBar.add(helpMenu);
    }
    
    /**
     * 创建菜单
     */
    private JMenu createMenu(String text, int mnemonic) {
        JMenu menu = new JMenu(text);
        menu.setMnemonic(mnemonic);
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("微软雅黑", Font.BOLD, 13));
        menu.setOpaque(true);
        menu.setBackground(PRIMARY_COLOR);
        return menu;
    }
    
    /**
     * 创建菜单项
     */
    private JMenuItem createMenuItem(String text, int mnemonic, KeyStroke accelerator) {
        JMenuItem item = new JMenuItem(text);
        item.setMnemonic(mnemonic);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        item.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        item.setForeground(Color.WHITE);
        item.setBackground(PRIMARY_COLOR);
        item.setOpaque(true);
        return item;
    }
    
    /**
     * 更新分类子菜单
     */
    private void updateCategorySubMenu(JMenu subMenu) {
        subMenu.removeAll();
        categories = DatabaseManager.getAllCategories();
        
        // 添加所有顶级分类
        for (Models.Category cat : categories) {
            if (cat.getParentId() == null) {
                JMenuItem item = new JMenuItem(cat.getName() + " (" + cat.getFileCount() + ")");
                int categoryId = cat.getId();
                item.addActionListener(e -> selectCategory(categoryId));
                subMenu.add(item);
                
                // 添加子分类
                for (Models.Category subCat : categories) {
                    if (subCat.getParentId() != null && subCat.getParentId() == cat.getId()) {
                        JMenuItem subItem = new JMenuItem("  ├─ " + subCat.getName() + " (" + subCat.getFileCount() + ")");
                        int subCategoryId = subCat.getId();
                        subItem.addActionListener(e -> selectCategory(subCategoryId));
                        subMenu.add(subItem);
                    }
                }
            }
        }
        
        // 添加未分类
        JMenuItem uncategorizedItem = new JMenuItem("  └─ 未分类");
        uncategorizedItem.addActionListener(e -> selectCategory(-1));
        subMenu.add(uncategorizedItem);
    }
    
    // ==================== 创建工具栏 ====================
    
    private void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(PRIMARY_COLOR);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 导入按钮
        importBtn = createToolButton("导入", e -> importFiles());
        toolBar.add(importBtn);
        
        // 新建分类按钮
        newCategoryBtn = createToolButton("新建分类", e -> showNewCategoryDialog());
        toolBar.add(newCategoryBtn);
        
        // 删除按钮
        deleteBtn = createToolButton("删除", e -> deleteSelectedFiles());
        toolBar.add(deleteBtn);
        
        // 重命名按钮
        renameBtn = createToolButton("重命名", e -> renameSelectedFile());
        toolBar.add(renameBtn);
        
        // 回收站按钮
        recycleBinBtn = createToolButton("回收站", e -> showRecycleBin());
        toolBar.add(recycleBinBtn);
        
        toolBar.addSeparator();
        
        // 搜索框
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(150, 30));
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        searchField.addActionListener(e -> searchFiles());
        toolBar.add(searchField);
        
        // 搜索按钮
        searchBtn = createToolButton("搜索", e -> searchFiles());
        toolBar.add(searchBtn);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    /**
     * 创建工具栏按钮
     */
    private JButton createToolButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(listener);
        
        // 鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });
        
        return button;
    }
    
    // ==================== 创建分类树面板 ====================
    
    private JPanel createCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // 标题
        JLabel titleLabel = new JLabel("📁 分类目录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建分类树
        categoryTree = new JTree();
        categoryTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryTree.setRowHeight(25);
        categoryTree.setForeground(new Color(44, 62, 80)); // 深色文字
        categoryTree.setBackground(CARD_COLOR);
        categoryTree.setOpaque(true);
        
        // 设置树节点渲染器，确保文字颜色正确
        categoryTree.setCellRenderer(new javax.swing.tree.DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (selected) {
                    setForeground(Color.WHITE);
                } else {
                    setForeground(new Color(44, 62, 80));
                }
                return comp;
            }
        });
        
        categoryTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getPath();
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node != null && node.getUserObject() instanceof Models.Category) {
                        Models.Category cat = (Models.Category) node.getUserObject();
                        selectCategory(cat.getId());
                    }
                }
            }
        });
        
        // 鼠标右键菜单
        categoryTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showCategoryTreePopup(e);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showCategoryTreePopup(e);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(categoryTree);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 显示分类树右键菜单
     */
    private void showCategoryTreePopup(MouseEvent e) {
        TreePath path = categoryTree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            categoryTree.setSelectionPath(path);
            JPopupMenu popup = new JPopupMenu();
            popup.setBackground(CARD_COLOR);
            
            JMenuItem addItem = new JMenuItem("添加子分类");
            addItem.setBackground(CARD_COLOR);
            addItem.setForeground(PRIMARY_COLOR);
            addItem.addActionListener(ev -> {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Models.Category cat = (Models.Category) node.getUserObject();
                showNewCategoryDialog(cat.getId());
            });
            popup.add(addItem);
            
            JMenuItem renameItem = new JMenuItem("重命名");
            renameItem.setBackground(CARD_COLOR);
            renameItem.setForeground(PRIMARY_COLOR);
            renameItem.addActionListener(ev -> {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Models.Category cat = (Models.Category) node.getUserObject();
                renameCategory(cat);
            });
            popup.add(renameItem);
            
            JMenuItem deleteItem = new JMenuItem("删除");
            deleteItem.setBackground(CARD_COLOR);
            deleteItem.setForeground(WARNING_COLOR);
            deleteItem.addActionListener(ev -> {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Models.Category cat = (Models.Category) node.getUserObject();
                deleteCategory(cat);
            });
            popup.add(deleteItem);
            
            popup.show(categoryTree, e.getX(), e.getY());
        }
    }
    
    // ==================== 创建文件列表面板 ====================
    
    private JPanel createFileListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // 标题
        JLabel titleLabel = new JLabel(showDeleted ? "🗑️ 回收站" : "📋 文件列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建表格
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        fileTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                // 斑马纹效果
                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }
                return comp;
            }
        };
        
        // 设置表格样式
        fileTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fileTable.setRowHeight(25);
        fileTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileTable.setGridColor(Color.LIGHT_GRAY);
        fileTable.setShowGrid(true);
        fileTable.setIntercellSpacing(new Dimension(1, 1));
        
        // 表头样式
        JTableHeader header = fileTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 12));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_COLOR));
        header.setPreferredSize(new Dimension(0, 35));
        
        // 自定义表头渲染器
        header.setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                label.setBackground(PRIMARY_COLOR);
                label.setForeground(Color.WHITE);
                label.setFont(new Font("微软雅黑", Font.BOLD, 12));
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });
        
        // 设置列宽
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 文件名
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(300); // 路径
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 大小
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 分类
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(120); // 时间
        
        // 鼠标双击打开文件
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedFile();
                }
            }
        });
        
        // 表格右键菜单
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showFileTablePopup(e);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showFileTablePopup(e);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(fileTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 进度条
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 显示文件表格右键菜单
     */
    private void showFileTablePopup(MouseEvent e) {
        int row = fileTable.rowAtPoint(e.getPoint());
        if (row >= 0 && !fileTable.isRowSelected(row)) {
            fileTable.setRowSelectionInterval(row, row);
        }
        
        if (fileTable.getSelectedRowCount() > 0) {
            JPopupMenu popup = new JPopupMenu();
            popup.setBackground(CARD_COLOR);
            
            if (!showDeleted) {
                JMenuItem openItem = new JMenuItem("打开文件");
                openItem.setBackground(CARD_COLOR);
                openItem.setForeground(PRIMARY_COLOR);
                openItem.addActionListener(ev -> openSelectedFile());
                popup.add(openItem);
                
                JMenuItem renameItem = new JMenuItem("重命名");
                renameItem.setBackground(CARD_COLOR);
                renameItem.setForeground(PRIMARY_COLOR);
                renameItem.addActionListener(ev -> renameSelectedFile());
                popup.add(renameItem);
                
                JMenuItem moveItem = new JMenuItem("移动到...");
                moveItem.setBackground(CARD_COLOR);
                moveItem.setForeground(PRIMARY_COLOR);
                moveItem.addActionListener(ev -> moveSelectedFile());
                popup.add(moveItem);
                
                popup.addSeparator();
                
                JMenuItem deleteItem = new JMenuItem("删除");
                deleteItem.setBackground(CARD_COLOR);
                deleteItem.setForeground(WARNING_COLOR);
                deleteItem.addActionListener(ev -> deleteSelectedFiles());
                popup.add(deleteItem);
            } else {
                JMenuItem restoreItem = new JMenuItem("恢复文件");
                restoreItem.setBackground(CARD_COLOR);
                restoreItem.setForeground(PRIMARY_COLOR);
                restoreItem.addActionListener(ev -> restoreSelectedFiles());
                popup.add(restoreItem);
                
                JMenuItem permDeleteItem = new JMenuItem("彻底删除");
                permDeleteItem.setBackground(CARD_COLOR);
                permDeleteItem.setForeground(WARNING_COLOR);
                permDeleteItem.addActionListener(ev -> permanentlyDeleteSelectedFiles());
                popup.add(permDeleteItem);
            }
            
            popup.show(fileTable, e.getX(), e.getY());
        }
    }
    
    // ==================== 创建状态栏 ====================
    
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(PRIMARY_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // 左侧状态
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(statusLabel);
        
        fileCountLabel = new JLabel("文件数: 0");
        fileCountLabel.setForeground(Color.WHITE);
        fileCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(fileCountLabel);
        
        storageLabel = new JLabel("存储: 0 B");
        storageLabel.setForeground(Color.WHITE);
        storageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(storageLabel);
        
        statusPanel.add(leftPanel, BorderLayout.WEST);
        
        // 右侧分类信息
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JLabel categoryLabel = new JLabel("分类: 全部");
        categoryLabel.setForeground(Color.WHITE);
        categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(categoryLabel);
        
        statusPanel.add(rightPanel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    // ==================== 数据加载和刷新 ====================
    
    private void refreshAll() {
        refreshCategoryTree();
        refreshFileList();
        updateStatusBar();
        updateViewMenu();
    }
    
    private void refreshCategoryTree() {
        categories = DatabaseManager.getAllCategories();
        
        // 构建树结构
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("全部文件");
        
        // 添加全部文件节点（注意：Category.toString()会自动添加文件数量）
        int totalCount = categories.stream().mapToInt(c -> c.getFileCount()).sum();
        Models.Category allFilesCat = new Models.Category(0, "全部文件", null);
        allFilesCat.setFileCount(totalCount);
        DefaultMutableTreeNode allFilesNode = new DefaultMutableTreeNode(allFilesCat);
        root.add(allFilesNode);
        
        // 按层级构建分类树
        Map<Integer, DefaultMutableTreeNode> nodeMap = new HashMap<>();
        
        // 添加顶级分类
        for (Models.Category cat : categories) {
            if (cat.getParentId() == null) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(cat);
                nodeMap.put(cat.getId(), node);
                root.add(node);
            }
        }
        
        // 添加子分类
        for (Models.Category cat : categories) {
            if (cat.getParentId() != null && nodeMap.containsKey(cat.getParentId())) {
                DefaultMutableTreeNode parentNode = nodeMap.get(cat.getParentId());
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(cat);
                nodeMap.put(cat.getId(), node);
                parentNode.add(node);
            }
        }
        
        treeModel = new DefaultTreeModel(root);
        categoryTree.setModel(treeModel);
        
        // 展开所有节点
        for (int i = 0; i < categoryTree.getRowCount(); i++) {
            categoryTree.expandRow(i);
        }
    }
    
    private void refreshFileList() {
        currentFiles = showDeleted ? 
            DatabaseManager.getDeletedFiles() : 
            (currentCategoryId == null ? 
                DatabaseManager.getAllFiles() : 
                DatabaseManager.getFilesByCategory(currentCategoryId));
        
        // 更新表格
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (Models.FileInfo file : currentFiles) {
            Object[] row = {
                file.getFileName(),
                file.getFilePath(),
                file.getFormattedSize(),
                file.getCategoryName() != null ? file.getCategoryName() : "未分类",
                file.getCreatedAt() != null ? sdf.format(file.getCreatedAt()) : ""
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStatusBar() {
        int fileCount = DatabaseManager.getTotalFileCount();
        long storageSize = DatabaseManager.getTotalStorageSize();
        
        fileCountLabel.setText("文件数: " + fileCount);
        storageLabel.setText("存储: " + FileService.formatSize(storageSize));
        
        String status = showDeleted ? "回收站模式" : (currentCategoryId == null ? "全部文件" : getCategoryName(currentCategoryId));
        statusLabel.setText(status + " - 就绪");
    }
    
    private void updateViewMenu() {
        // 更新视图菜单中的分类子菜单
        for (Component comp : viewMenu.getMenuComponents()) {
            if (comp instanceof JMenu) {
                JMenu menu = (JMenu) comp;
                if (menu.getText().startsWith("按分类")) {
                    updateCategorySubMenu(menu);
                    break;
                }
            }
        }
    }
    
    private String getCategoryName(int categoryId) {
        for (Models.Category cat : categories) {
            if (cat.getId() == categoryId) {
                return cat.getName();
            }
        }
        return "未知";
    }
    
    // ==================== 功能方法 ====================
    
    /**
     * 导入文件
     */
    private void importFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择要导入的文件或文件夹");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            
            if (selectedFiles.length > 0) {
                // 弹出分类选择对话框
                Integer categoryId = showCategorySelectDialog();
                
                // 后台导入
                new SwingWorker<Void, Void>() {
                    int successCount;
                    int totalCount;
                    
                    @Override
                    protected Void doInBackground() throws Exception {
                        totalCount = selectedFiles.length;
                        successCount = FileService.importFiles(selectedFiles, categoryId, 
                            (current, total, fileName) -> {
                                setProgress((int) ((current * 100.0) / total));
                                statusLabel.setText("正在导入: " + fileName);
                            });
                        return null;
                    }
                    
                    @Override
                    protected void done() {
                        JOptionPane.showMessageDialog(FileManagerApp.this,
                            "导入完成！成功导入 " + successCount + " 个文件。",
                            "导入成功", JOptionPane.INFORMATION_MESSAGE);
                        refreshAll();
                    }
                }.execute();
            }
        }
    }
    
    /**
     * 显示分类选择对话框
     */
    private Integer showCategorySelectDialog() {
        String[] options = new String[categories.size() + 1];
        options[0] = "未分类";
        for (int i = 0; i < categories.size(); i++) {
            options[i + 1] = categories.get(i).getName();
        }
        
        int choice = JOptionPane.showOptionDialog(this,
            "请选择文件导入的分类：",
            "选择分类",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) {
            return null;
        } else if (choice > 0) {
            return categories.get(choice - 1).getId();
        }
        return null;
    }
    
    /**
     * 新建分类对话框
     */
    private void showNewCategoryDialog() {
        showNewCategoryDialog(null);
    }
    
    private void showNewCategoryDialog(Integer parentId) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("分类名称："));
        JTextField nameField = new JTextField(20);
        panel.add(nameField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "新建分类", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                int id = DatabaseManager.addCategory(name, parentId);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "分类创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this, "分类创建失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * 重命名分类
     */
    private void renameCategory(Models.Category category) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("新名称："));
        JTextField nameField = new JTextField(category.getName(), 20);
        panel.add(nameField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "重命名分类", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                if (DatabaseManager.updateCategoryName(category.getId(), name)) {
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this, "重命名失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * 删除分类
     */
    private void deleteCategory(Models.Category category) {
        int result = JOptionPane.showConfirmDialog(this,
            "确定要删除分类 \"" + category.getName() + "\" 吗？\n该分类下的文件将变为未分类状态。",
            "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            if (DatabaseManager.deleteCategory(category.getId())) {
                JOptionPane.showMessageDialog(this, "分类已删除！", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            }
        }
    }
    
    /**
     * 选择分类
     */
    private void selectCategory(int categoryId) {
        this.currentCategoryId = categoryId == 0 ? null : categoryId;
        this.showDeleted = false;
        refreshFileList();
        updateStatusBar();
    }
    
    /**
     * 显示全部文件
     */
    private void showAllFiles() {
        this.currentCategoryId = null;
        this.showDeleted = false;
        refreshFileList();
        updateStatusBar();
    }
    
    /**
     * 重命名选中文件
     */
    private void renameSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Models.FileInfo file = currentFiles.get(selectedRow);
        
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("新文件名："));
        JTextField nameField = new JTextField(file.getFileName(), 30);
        panel.add(nameField);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "重命名文件", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            if (!newName.isEmpty() && !newName.equals(file.getFileName())) {
                if (!FileService.isValidFileName(newName)) {
                    JOptionPane.showMessageDialog(this, "文件名包含非法字符！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (FileService.renameFile(file.getId(), file.getFilePath(), newName)) {
                    JOptionPane.showMessageDialog(this, "文件重命名成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                } else {
                    JOptionPane.showMessageDialog(this, "文件重命名失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    /**
     * 移动文件到分类
     */
    private void moveSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Models.FileInfo file = currentFiles.get(selectedRow);
        
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.add(new JLabel("移动到分类："));
        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.addItem("未分类");
        for (Models.Category cat : categories) {
            categoryCombo.addItem(cat.getName());
        }
        if (file.getCategoryName() != null) {
            categoryCombo.setSelectedItem(file.getCategoryName());
        }
        panel.add(categoryCombo);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "移动文件", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String selectedCategory = (String) categoryCombo.getSelectedItem();
            Integer newCategoryId = null;
            
            for (Models.Category cat : categories) {
                if (cat.getName().equals(selectedCategory)) {
                    newCategoryId = cat.getId();
                    break;
                }
            }
            
            if (FileService.moveFileToCategory(file.getId(), newCategoryId)) {
                JOptionPane.showMessageDialog(this, "文件移动成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "文件移动失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 删除选中文件
     */
    private void deleteSelectedFiles() {
        int[] selectedRows = fileTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            "确定要将选中的 " + selectedRows.length + " 个文件移至回收站吗？",
            "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            List<Integer> fileIds = new ArrayList<>();
            for (int row : selectedRows) {
                fileIds.add(currentFiles.get(row).getId());
            }
            
            int successCount = FileService.batchSoftDelete(fileIds);
            JOptionPane.showMessageDialog(this, 
                "已删除 " + successCount + " 个文件！", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        }
    }
    
    /**
     * 打开选中文件
     */
    private void openSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow < 0) return;
        
        Models.FileInfo file = currentFiles.get(selectedRow);
        File f = new File(file.getFilePath());
        
        if (f.exists()) {
            try {
                Desktop.getDesktop().open(f);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "无法打开文件：" + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "文件不存在！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 反向选择
     */
    private void invertSelection() {
        ListSelectionModel selectionModel = fileTable.getSelectionModel();
        int min = fileTable.getRowCount();
        int max = -1;
        
        for (int i = 0; i < fileTable.getRowCount(); i++) {
            if (fileTable.isRowSelected(i)) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
        }
        
        selectionModel.clearSelection();
        for (int i = 0; i < fileTable.getRowCount(); i++) {
            if (i < min || i > max) {
                selectionModel.addSelectionInterval(i, i);
            }
        }
    }
    
    /**
     * 搜索文件
     */
    private void searchFiles() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refreshFileList();
            return;
        }
        
        currentFiles = DatabaseManager.searchFiles(keyword);
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        for (Models.FileInfo file : currentFiles) {
            Object[] row = {
                file.getFileName(),
                file.getFilePath(),
                file.getFormattedSize(),
                file.getCategoryName() != null ? file.getCategoryName() : "未分类",
                file.getCreatedAt() != null ? sdf.format(file.getCreatedAt()) : ""
            };
            tableModel.addRow(row);
        }
        
        statusLabel.setText("搜索结果: " + currentFiles.size() + " 个文件");
    }
    
    /**
     * 显示回收站
     */
    private void showRecycleBin() {
        this.showDeleted = true;
        this.currentCategoryId = null;
        refreshFileList();
        updateStatusBar();
    }
    
    /**
     * 恢复选中文件
     */
    private void restoreSelectedFiles() {
        int[] selectedRows = fileTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        List<Integer> fileIds = new ArrayList<>();
        for (int row : selectedRows) {
            fileIds.add(currentFiles.get(row).getId());
        }
        
        int successCount = FileService.batchRestore(fileIds);
        JOptionPane.showMessageDialog(this, 
            "已恢复 " + successCount + " 个文件！", "成功", JOptionPane.INFORMATION_MESSAGE);
        refreshAll();
    }
    
    /**
     * 彻底删除选中文件
     */
    private void permanentlyDeleteSelectedFiles() {
        int[] selectedRows = fileTable.getSelectedRows();
        if (selectedRows.length == 0) return;
        
        int result = JOptionPane.showConfirmDialog(this,
            "⚠️ 警告：彻底删除后文件将无法恢复！\n确定要彻底删除选中的 " + selectedRows.length + " 个文件吗？",
            "确认彻底删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            List<Models.FileInfo> files = new ArrayList<>();
            for (int row : selectedRows) {
                files.add(currentFiles.get(row));
            }
            
            int successCount = FileService.batchPermanentDelete(files);
            JOptionPane.showMessageDialog(this, 
                "已彻底删除 " + successCount + " 个文件！", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        }
    }
    
    /**
     * 显示导入历史
     */
    private void showImportHistory() {
        List<Models.ImportRecord> records = DatabaseManager.getImportRecords();
        
        StringBuilder sb = new StringBuilder("导入历史记录：\n\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Models.ImportRecord record : records) {
            sb.append("• ").append(record.getFilePath())
              .append("\n  时间: ").append(sdf.format(record.getImportedAt()))
              .append("\n\n");
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        textArea.setLineWrap(true);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "导入历史", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 清空数据库
     */
    private void clearDatabase() {
        int result = JOptionPane.showConfirmDialog(this,
            "⚠️ 警告：清空数据库将删除所有文件记录！\n实际文件不会被删除。\n\n确定要清空数据库吗？",
            "确认清空", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            DatabaseManager.clearDatabase();
            JOptionPane.showMessageDialog(this, "数据库已清空！", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        }
    }
    
    /**
     * 重新初始化数据库
     */
    private void reinitializeDatabase() {
        int result = JOptionPane.showConfirmDialog(this,
            "⚠️ 警告：这将清空所有数据并重新创建默认分类！\n\n确定要继续吗？",
            "确认重新初始化", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            DatabaseManager.clearDatabase();
            DatabaseManager.initializeDatabase();
            JOptionPane.showMessageDialog(this, "数据库已重新初始化！", "成功", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        }
    }
    
    /**
     * 显示用户指南
     */
    private void showUserGuide() {
        String guide = """
            📖 个人文件分类管理系统 - 用户指南
            
            【基本操作】
            • 导入文件：点击"导入"按钮或使用 Ctrl+I
            • 新建分类：点击"新建分类"按钮或使用 Ctrl+N
            • 重命名：选中文件后按 F2 或使用菜单
            • 删除文件：选中后按 Delete 键
            
            【分类管理】
            • 在左侧分类树中点击分类名称查看该分类下的文件
            • 右键点击分类可以进行添加子分类、重命名、删除操作
            
            【回收站】
            • 删除的文件会移至回收站
            • 在回收站中可以恢复或彻底删除文件
            
            【快捷键】
            • Ctrl+I - 导入文件
            • Ctrl+N - 新建分类
            • F2 - 重命名
            • Ctrl+M - 移动文件
            • Delete - 删除文件
            • Ctrl+F - 搜索
            • F5 - 刷新
            • Ctrl+R - 打开回收站
            """;
        
        JOptionPane.showMessageDialog(this, guide, "用户指南", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示关于
     */
    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "📁 个人文件分类管理系统\n\n" +
            "版本: 1.0\n" +
            "作者: 文件管理开发团队\n" +
            "描述: 简洁易用的文件分类管理工具\n\n" +
            "支持功能:\n" +
            "• 分类管理\n" +
            "• 文件导入/重命名/移动\n" +
            "• 软删除与回收站\n" +
            "• 文件搜索\n",
            "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // ==================== 主方法 ====================
    
    public static void main(String[] args) {
        // 设置外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 强制覆盖菜单颜色
        UIManager.put("MenuBar.background", PRIMARY_COLOR);
        UIManager.put("MenuBar.foreground", Color.WHITE);
        UIManager.put("Menu.background", PRIMARY_COLOR);
        UIManager.put("Menu.foreground", Color.WHITE);
        UIManager.put("MenuItem.background", PRIMARY_COLOR);
        UIManager.put("MenuItem.foreground", Color.WHITE);
        
        // 强制覆盖表格表头颜色
        UIManager.put("TableHeader.background", PRIMARY_COLOR);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        
        // 强制覆盖弹出菜单颜色
        UIManager.put("PopupMenu.background", CARD_COLOR);
        UIManager.put("PopupMenu.foreground", PRIMARY_COLOR);
        UIManager.put("Menu.background", CARD_COLOR);
        UIManager.put("Menu.foreground", PRIMARY_COLOR);
        UIManager.put("MenuItem.background", CARD_COLOR);
        UIManager.put("MenuItem.foreground", PRIMARY_COLOR);
        
        // 启动应用
        SwingUtilities.invokeLater(() -> new FileManagerApp());
    }
}
