package com.kardex.ui.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.kardex.util.FormatUtils;
import com.kardex.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SimpleBarChart extends JPanel {

    private Map<String, BigDecimal[]> data = new LinkedHashMap<>();

    private final Color COLOR_INGRESOS = new Color(46, 125, 50);
    private final Color COLOR_EGRESOS = new Color(211, 47, 47);

    public SimpleBarChart() {
        setBackground(Color.WHITE);
        putClientProperty(FlatClientProperties.STYLE, "arc: 14;");
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1, true),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        setPreferredSize(new Dimension(500, 280));
    }

    public void setData(Map<String, BigDecimal[]> newData) {
        this.data = newData != null ? newData : new LinkedHashMap<>();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // 1. Dibujar Título y Leyenda
        g2.setFont(UITheme.FONT_SUBTITLE);
        g2.setColor(new Color(40, 48, 60));
        g2.drawString("Comparativa de Ingresos vs Egresos", 20, 26);

        // Leyenda
        int legendX = width - 240;
        g2.setFont(UITheme.FONT_SMALL);

        // Dot Ingresos
        g2.setColor(COLOR_INGRESOS);
        g2.fillOval(legendX, 15, 10, 10);
        g2.setColor(new Color(70, 75, 85));
        g2.drawString("Ingresos (Ventas)", legendX + 14, 24);

        // Dot Egresos
        g2.setColor(COLOR_EGRESOS);
        g2.fillOval(legendX + 115, 15, 10, 10);
        g2.setColor(new Color(70, 75, 85));
        g2.drawString("Egresos (Compras)", legendX + 129, 24);

        // 2. Comprobar datos
        if (data == null || data.isEmpty()) {
            g2.setFont(UITheme.FONT_REGULAR);
            g2.setColor(Color.GRAY);
            String emptyMsg = "No hay transacciones registradas para graficar";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(emptyMsg, (width - fm.stringWidth(emptyMsg)) / 2, height / 2);
            g2.dispose();
            return;
        }

        // 3. Determinar valor máximo para escala
        BigDecimal maxVal = BigDecimal.valueOf(100);
        for (BigDecimal[] vals : data.values()) {
            if (vals[0] != null && vals[0].compareTo(maxVal) > 0) maxVal = vals[0];
            if (vals[1] != null && vals[1].compareTo(maxVal) > 0) maxVal = vals[1];
        }
        double maxDouble = Math.ceil(maxVal.doubleValue() * 1.15); // Margen superior del 15%

        // Área de gráfico
        int padLeft = 70;
        int padRight = 20;
        int padTop = 50;
        int padBottom = 40;
        int chartW = width - padLeft - padRight;
        int chartH = height - padTop - padBottom;

        // Líneas guía horizontales (4 niveles)
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 4; i++) {
            int y = padTop + (chartH * i / 4);
            double val = maxDouble * (4 - i) / 4.0;
            g2.setColor(new Color(235, 238, 245));
            g2.drawLine(padLeft, y, width - padRight, y);

            g2.setColor(new Color(130, 138, 150));
            String label = "$" + String.format(Locale.US, "%,.0f", val);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, padLeft - fm.stringWidth(label) - 8, y + 4);
        }

        // 4. Dibujar Barras
        int categoryCount = data.size();
        if (categoryCount == 0) {
            g2.dispose();
            return;
        }

        int groupWidth = chartW / categoryCount;
        int barWidth = Math.max(8, Math.min(26, (groupWidth - 24) / 2));
        int catIndex = 0;

        for (Map.Entry<String, BigDecimal[]> entry : data.entrySet()) {
            String period = entry.getKey();
            BigDecimal[] vals = entry.getValue();
            double vIngresos = vals[0] != null ? vals[0].doubleValue() : 0.0;
            double vEgresos = vals[1] != null ? vals[1].doubleValue() : 0.0;

            int groupCenterX = padLeft + (catIndex * groupWidth) + (groupWidth / 2);
            int x1 = groupCenterX - barWidth - 3;
            int x2 = groupCenterX + 3;

            int h1 = (int) Math.round((vIngresos / maxDouble) * chartH);
            int h2 = (int) Math.round((vEgresos / maxDouble) * chartH);

            int y1 = padTop + chartH - h1;
            int y2 = padTop + chartH - h2;

            // Barra Ingreso
            g2.setColor(COLOR_INGRESOS);
            g2.fillRoundRect(x1, y1, barWidth, h1, 6, 6);

            // Barra Egreso
            g2.setColor(COLOR_EGRESOS);
            g2.fillRoundRect(x2, y2, barWidth, h2, 6, 6);

            // Etiqueta del periodo (e.g., Oct 2026)
            g2.setColor(new Color(80, 85, 95));
            g2.setFont(UITheme.FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(period);
            g2.drawString(period, groupCenterX - (strW / 2), height - 16);

            catIndex++;
        }

        g2.dispose();
    }
}
