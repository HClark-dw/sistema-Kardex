package com.kardex.util;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class UITheme {

    public static final Color PRIMARY = new Color(24, 119, 242);
    public static final Color PRIMARY_HOVER = new Color(20, 100, 204);
    public static final Color SUCCESS = new Color(46, 125, 50);
    public static final Color DANGER = new Color(198, 40, 40);
    public static final Color WARNING = new Color(239, 108, 0);
    public static final Color INFO = new Color(2, 119, 189);

    public static final Color CARD_BG_LIGHT = new Color(255, 255, 255);
    public static final Color CARD_BORDER_LIGHT = new Color(225, 230, 238);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 22);

    private static boolean isDark = false;

    public static void setup() {
        try {
            // Configurar tema inicial (FlatLightLaf para un aspecto limpio y claro de oficina)
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("Table.rowHeight", 28);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TableHeader.height", 32);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void toggleTheme(JFrame frame) {
        try {
            if (isDark) {
                FlatLightLaf.setup();
                isDark = false;
            } else {
                FlatDarkLaf.setup();
                isDark = true;
            }
            FlatLightLaf.updateUI();
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isDarkMode() {
        return isDark;
    }

    public static void styleCard(JComponent comp) {
        comp.putClientProperty(FlatClientProperties.STYLE, "arc: 12; background: $Card.background;");
        comp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isDark ? new Color(60, 63, 65) : CARD_BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        return btn;
    }
}
