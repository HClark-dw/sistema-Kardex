package com.kardex;

import com.kardex.config.DatabaseConfig;
import com.kardex.ui.LoginFrame;
import com.kardex.util.UITheme;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

         System.out.println("HASH GENERADO: " + com.kardex.util.PasswordHasher.hash("123456"));
        // Establecer Look and Feel moderno FlatLaf
        UITheme.setup();

        // Lanzar interfaz en el Event Dispatch Thread de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Inicializar pool de conexiones en segundo plano
                new Thread(() -> {
                    try {
                        DatabaseConfig.initDataSource();
                    } catch (Exception e) {
                        System.err.println("Aviso: No se pudo conectar a MySQL al inicio. Se podrá configurar en la pantalla de Login: " + e.getMessage());
                    }
                }).start();

                LoginFrame login = new LoginFrame();
                login.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al iniciar la aplicación: " + ex.getMessage(), "Error Crítico", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
