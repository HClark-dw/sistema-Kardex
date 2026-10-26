package com.kardex.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kardex.model.User;
import com.kardex.ui.panels.*;
import com.kardex.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private final User currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContentPanel = new JPanel(cardLayout);

    private final List<JButton> navButtons = new ArrayList<>();

    // Panels
    private DashboardPanel dashboardPanel;
    private InventoryPanel inventoryPanel;
    private PurchasesPanel purchasesPanel;
    private SalesPanel salesPanel;
    private KardexPanel kardexPanel;
    private ReportsPanel reportsPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Sistema de Gestión de Inventarios y Kardex Valorado - v1.0.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);

        // Crear Paneles
        dashboardPanel = new DashboardPanel();
        inventoryPanel = new InventoryPanel();
        purchasesPanel = new PurchasesPanel(currentUser);
        salesPanel = new SalesPanel(currentUser);
        kardexPanel = new KardexPanel();
        reportsPanel = new ReportsPanel();

        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(inventoryPanel, "INVENTORY");
        mainContentPanel.add(purchasesPanel, "PURCHASES");
        mainContentPanel.add(salesPanel, "SALES");
        mainContentPanel.add(kardexPanel, "KARDEX");
        mainContentPanel.add(reportsPanel, "REPORTS");

        root.add(mainContentPanel, BorderLayout.CENTER);
        add(root);

        // Iniciar en Dashboard
        selectNavIndex(0);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(new Color(24, 32, 47));

        // 1. Logo y Título
        JPanel header = new JPanel(new BorderLayout(8, 4));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(22, 18, 22, 18));

        JLabel lblLogo = new JLabel("📊", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);

        JLabel lblAppName = new JLabel("KARDEX PRO");
        lblAppName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAppName.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Inventario Valorado");
        lblSubtitle.setFont(UITheme.FONT_SMALL);
        lblSubtitle.setForeground(new Color(156, 163, 175));

        titleBox.add(lblAppName);
        titleBox.add(lblSubtitle);

        header.add(lblLogo, BorderLayout.WEST);
        header.add(titleBox, BorderLayout.CENTER);
        sidebar.add(header, BorderLayout.NORTH);

        // 2. Botones de Navegación
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        String[] navLabels = {
            "📊  Dashboard",
            "📦  Inventario",
            "📥  Registro Compras",
            "🛒  Registro Ventas",
            "📑  Kardex Valorado",
            "📈  Reportes"
        };

        String[] panelKeys = {
            "DASHBOARD",
            "INVENTORY",
            "PURCHASES",
            "SALES",
            "KARDEX",
            "REPORTS"
        };

        for (int i = 0; i < navLabels.length; i++) {
            final int index = i;
            final String cardKey = panelKeys[i];
            JButton btn = createNavButton(navLabels[i]);
            btn.addActionListener(e -> {
                selectNavIndex(index);
                cardLayout.show(mainContentPanel, cardKey);
                // Ganchos de actualización al cambiar de pestaña
                if ("DASHBOARD".equals(cardKey)) {
                    dashboardPanel.reloadMetrics();
                } else if ("INVENTORY".equals(cardKey)) {
                    inventoryPanel.loadProducts();
                } else if ("PURCHASES".equals(cardKey)) {
                    purchasesPanel.loadSuppliersAndProducts();
                    purchasesPanel.loadHistory();
                } else if ("SALES".equals(cardKey)) {
                    salesPanel.loadCustomersAndProducts();
                    salesPanel.loadHistory();
                } else if ("KARDEX".equals(cardKey)) {
                    kardexPanel.loadProducts();
                } else if ("REPORTS".equals(cardKey)) {
                    reportsPanel.generateReport();
                }
            });
            navButtons.add(btn);
            navPanel.add(btn);
            navPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        sidebar.add(navPanel, BorderLayout.CENTER);

        // 3. Pie con Usuario, Tema y Logout
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 14, 18, 14));

        // Badge Usuario
        JPanel userBadge = new JPanel(new GridLayout(2, 1));
        userBadge.setOpaque(false);
        JLabel lblUser = new JLabel("👤 " + (currentUser != null ? currentUser.getNombreCompleto() : "Administrador"));
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("Rol: " + (currentUser != null ? currentUser.getRol() : "ADMINISTRADOR"));
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(new Color(56, 189, 248));

        userBadge.add(lblUser);
        userBadge.add(lblRole);

        // Botón Tema
        JButton btnTheme = new JButton("🌓 Alternar Tema (Claro/Oscuro)");
        btnTheme.setFont(UITheme.FONT_SMALL);
        btnTheme.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btnTheme.setFocusPainted(false);
        btnTheme.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTheme.addActionListener(e -> UITheme.toggleTheme(this));

        // Botón Logout
        JButton btnLogout = new JButton("🚪 Cerrar Sesión");
        btnLogout.setFont(UITheme.FONT_BOLD);
        btnLogout.setForeground(new Color(248, 113, 113));
        btnLogout.setBackground(new Color(36, 44, 60));
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        footer.add(userBadge);
        footer.add(Box.createRigidArea(new Dimension(0, 10)));
        footer.add(btnTheme);
        footer.add(Box.createRigidArea(new Dimension(0, 8)));
        footer.add(btnLogout);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(new Color(209, 213, 219));
        btn.setBackground(new Color(24, 32, 47));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 8;");
        return btn;
    }

    private void selectNavIndex(int index) {
        for (int i = 0; i < navButtons.size(); i++) {
            JButton b = navButtons.get(i);
            if (i == index) {
                b.setBackground(UITheme.PRIMARY);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(new Color(24, 32, 47));
                b.setForeground(new Color(209, 213, 219));
            }
        }
    }
}
