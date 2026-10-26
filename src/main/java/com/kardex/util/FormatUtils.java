package com.kardex.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtils {

    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat QUANTITY_FORMAT;
    private static final DecimalFormat UNIT_COST_FORMAT;

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter SQL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        CURRENCY_FORMAT = new DecimalFormat("$ #,##0.00", symbols);
        QUANTITY_FORMAT = new DecimalFormat("#,##0.00", symbols);
        UNIT_COST_FORMAT = new DecimalFormat("#,##0.0000", symbols);
    }

    public static String formatMoney(BigDecimal amount) {
        if (amount == null) return "$ 0.00";
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatQuantity(BigDecimal qty) {
        if (qty == null) return "0.00";
        return QUANTITY_FORMAT.format(qty);
    }

    public static String formatUnitCost(BigDecimal cost) {
        if (cost == null) return "0.0000";
        return UNIT_COST_FORMAT.format(cost);
    }

    public static String formatDateTime(LocalDateTime ldt) {
        if (ldt == null) return "-";
        return ldt.format(DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDate ld) {
        if (ld == null) return "-";
        return ld.format(DATE_FORMATTER);
    }

    public static BigDecimal parseBigDecimal(String str, BigDecimal def) {
        if (str == null || str.trim().isEmpty()) return def;
        try {
            String clean = str.replace("$", "").replace(",", "").trim();
            return new BigDecimal(clean);
        } catch (Exception e) {
            return def;
        }
    }
}
