package com.kardex.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.kardex.config.DatabaseConfig;
import com.kardex.dao.UserDAO;
import com.kardex.model.User;
import com.kardex.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {

    private final JTextField txtUsername;
    private final JPasswordField txtPassword;
    private final JButton btnLogin;
    private final JLabel lblStatus;
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Sistema Kardex Valorado - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // Tarjeta central de login
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 240), 1, true),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 16;");

        // Icono / Encabezado
        JLabel lblIcon = new JLabel("📊", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("SISTEMA KARDEX");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 41, 59));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Control de Inventarios y Valoración");
        lblSub.setFont(UITheme.FONT_REGULAR);
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Campos
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField("admin");
        txtUsername.setFont(UITheme.FONT_REGULAR);
        txtUsername.putClientProperty("JTextField.placeholderText", "Ingrese su nombre de usuario");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(UITheme.FONT_BOLD);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.putClientProperty("JTextField.placeholderText", "Ingrese su contraseña");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.DANGER);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLogin = UITheme.createPrimaryButton("Ingresar al Sistema");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> attemptLogin());

        // Botón Configuración de Base de Datos
        JButton btnConfigDB = new JButton("⚙ Configurar Conexión MySQL");
        btnConfigDB.setFont(UITheme.FONT_SMALL);
        btnConfigDB.setBorderPainted(false);
        btnConfigDB.setContentAreaFilled(false);
        btnConfigDB.setForeground(new Color(100, 116, 139));
        btnConfigDB.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfigDB.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfigDB.addActionListener(e -> openDBConfigDialog());

        // Enter key to login
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        // Armado del contenedor
        card.add(lblIcon);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(lblSub);
        card.add(Box.createRigidArea(new Dimension(0, 24)));
        card.add(lblUser);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtUsername);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(lblPass);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(txtPassword);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(lblStatus);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(btnLogin);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(btnConfigDB);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.add(card);
        mainPanel.add(outer, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void attemptLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            lblStatus.setText("Por favor ingrese usuario y contraseña");
            return;
        }

        btnLogin.setEnabled(false);
        lblStatus.setForeground(UITheme.PRIMARY);
        lblStatus.setText("Autenticando...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return userDAO.authenticate(user, pass);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                try {
                    User authenticated = get();
                    if (authenticated != null) {
                        lblStatus.setText("");
                        dispose();
                        SwingUtilities.invokeLater(() -> {
                            MainFrame frame = new MainFrame(authenticated);
                            frame.setVisible(true);
                        });
                    } else {
                        lblStatus.setForeground(UITheme.DANGER);
                        lblStatus.setText("Usuario o contraseña incorrectos.");
                    }
                } catch (Exception ex) {
                    lblStatus.setForeground(UITheme.DANGER);
                    lblStatus.setText("No se pudo conectar a MySQL. Revise ajustes de BD.");
                    int res = JOptionPane.showConfirmDialog(
                        LoginFrame.this,
                        "Error al conectar con la base de datos MySQL:\n" + ex.getMessage() +
                        "\n\n¿Desea abrir el configurador de conexión a MySQL?",
                        "Error de Conexión MySQL",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE
                    );
                    if (res == JOptionPane.YES_OPTION) {
                        openDBConfigDialog();
                    }
                }
            }
        };
        worker.execute();
    }

    private void openDBConfigDialog() {
        JDialog dlg = new JDialog(this, "Configuración de Conexión MySQL", true);
        dlg.setLayout(new BorderLayout(12, 12));
        dlg.setSize(440, 340);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        JTextField txtHost = new JTextField(DatabaseConfig.getProperty("db.host", "localhost"));
        JTextField txtPort = new JTextField(DatabaseConfig.getProperty("db.port", "3306"));
        JTextField txtDb = new JTextField(DatabaseConfig.getProperty("db.name", "kardex_db"));
        JTextField txtUser = new JTextField(DatabaseConfig.getProperty("db.user", "root"));
        JPasswordField txtPass = new JPasswordField(DatabaseConfig.getProperty("db.password", ""));

        int r = 0;
        gbc.gridx = 0; gbc.gridy = r; form.add(new JLabel("Host / Servidor:"), gbc);
        gbc.gridx = 1; form.add(txtHost, gbc); r++;

        gbc.gridx = 0; gbc.gridy = r; form.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1; form.add(txtPort, gbc); r++;

        gbc.gridx = 0; gbc.gridy = r; form.add(new JLabel("Base de Datos:"), gbc);
        gbc.gridx = 1; form.add(txtDb, gbc); r++;

        gbc.gridx = 0; gbc.gridy = r; form.add(new JLabel("Usuario MySQL:"), gbc);
        gbc.gridx = 1; form.add(txtUser, gbc); r++;

        gbc.gridx = 0; gbc.gridy = r; form.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; form.add(txtPass, gbc); r++;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        JButton btnTest = new JButton("Probar Conexión");
        JButton btnSave = UITheme.createPrimaryButton("Guardar y Aplicar");

        btnTest.addActionListener(e -> {
            boolean ok = DatabaseConfig.testConnection(
                txtHost.getText().trim(),
                txtPort.getText().trim(),
                txtDb.getText().trim(),
                txtUser.getText().trim(),
                new String(txtPass.getPassword())
            );
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "¡Conexión exitosa a MySQL!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dlg, "No se pudo conectar a MySQL. Verifique que el servicio esté activo y los datos sean correctos.", "Fallo de Conexión", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnSave.addActionListener(e -> {
            try {
                DatabaseConfig.updateAndSaveConfig(
                    txtHost.getText().trim(),
                    txtPort.getText().trim(),
                    txtDb.getText().trim(),
                    txtUser.getText().trim(),
                    new String(txtPass.getPassword())
                );
                JOptionPane.showMessageDialog(dlg, "Configuración guardada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al guardar configuración: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnTest);
        btnPanel.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
