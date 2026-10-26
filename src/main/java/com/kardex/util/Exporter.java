package com.kardex.util;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Exporter {

    /**
     * Exporta el contenido de una JTable a un archivo CSV delimitado por comas
     * compatible con Microsoft Excel, LibreOffice Calc y Google Sheets.
     */
    public static void exportTableToCSV(JTable table, File file) throws Exception {
        TableModel model = table.getModel();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Escribir BOM UTF-8 para que Excel lo abra con tildes y caracteres especiales correctamente
            writer.write('\ufeff');

            // Encabezados
            int colCount = model.getColumnCount();
            for (int i = 0; i < colCount; i++) {
                writer.print(escapeCSV(model.getColumnName(i)));
                if (i < colCount - 1) writer.print(",");
            }
            writer.println();

            // Filas
            int rowCount = model.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    Object val = model.getValueAt(r, c);
                    writer.print(escapeCSV(val != null ? val.toString() : ""));
                    if (c < colCount - 1) writer.print(",");
                }
                writer.println();
            }
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "\"\"";
        String s = value.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
