package com.kardex.ui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.kardex.util.UITheme;

import javax.swing.*;
import java.awt.*;

public class MetricCard extends JPanel {

    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtext;
    private final JLabel lblIcon;

    public MetricCard(String title, String initialValue, String subtext, String iconSymbol, Color accentColor) {
        setLayout(new BorderLayout(10, 8));
        setBackground(Color.WHITE);
        putClientProperty(FlatClientProperties.STYLE, "arc: 14;");
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // Panel Izquierdo: Textos
        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 4));
        textPanel.setOpaque(false);

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(new Color(110, 118, 130));

        lblValue = new JLabel(initialValue);
        lblValue.setFont(UITheme.FONT_BIG_NUMBER);
        lblValue.setForeground(accentColor);

        lblSubtext = new JLabel(subtext);
        lblSubtext.setFont(UITheme.FONT_SMALL);
        lblSubtext.setForeground(new Color(140, 148, 160));

        textPanel.add(lblTitle);
        textPanel.add(lblValue);
        textPanel.add(lblSubtext);

        // Icono / Badge a la derecha
        lblIcon = new JLabel(iconSymbol, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        lblIcon.setPreferredSize(new Dimension(46, 46));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 28));
        lblIcon.setForeground(accentColor);
        lblIcon.putClientProperty(FlatClientProperties.STYLE, "arc: 999;");

        JPanel iconContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        iconContainer.setOpaque(false);
        iconContainer.add(lblIcon);

        add(textPanel, BorderLayout.CENTER);
        add(iconContainer, BorderLayout.EAST);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtext(String subtext) {
        lblSubtext.setText(subtext);
    }
}
