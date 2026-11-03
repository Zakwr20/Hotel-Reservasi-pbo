package com.hotel.view;

import com.hotel.controller.AuthController;
import com.hotel.dao.DataContext;
import com.hotel.exception.LoginGagalException;
import com.hotel.model.Pengguna;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/** Tampilan 1: halaman login. Setelah 3x gagal, tombol login dikunci 10 detik. */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final int MAKS_PERCOBAAN = 3;

    private final DataContext ctx;
    private final AuthController auth;
    private final JTextField fieldUsername = new JTextField(18);
    private final JPasswordField fieldPassword = new JPasswordField(18);
    private final JButton btnLogin = new JButton("Masuk");
    private int gagal;

    public LoginFrame(DataContext ctx) {
        super("Login — Sistem Reservasi Hotel");
        this.ctx = ctx;
        this.auth = new AuthController(ctx.getPenggunaDao());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel latar = new JPanel(new GridBagLayout()); // GridBagLayout menjaga kartu tetap di tengah saat resize
        latar.setBackground(UiUtil.WARNA_UTAMA);

        JPanel kartu = new JPanel(new GridBagLayout());
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        JLabel judul = new JLabel("Hotel Mulawarman", SwingConstants.CENTER);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 24f));
        judul.setForeground(UiUtil.WARNA_UTAMA);
        JLabel subjudul = new JLabel("Sistem Reservasi Hotel", SwingConstants.CENTER);
        subjudul.setForeground(new Color(0x5F6B7A));
        JLabel petunjuk = new JLabel("<html><center>Akun demo: <b>admin</b> / admin123<br>"
                + "<b>resepsionis</b> / resep123</center></html>", SwingConstants.CENTER);
        petunjuk.setForeground(new Color(0x7A8594));
        petunjuk.setFont(petunjuk.getFont().deriveFont(11f));

        c.gridy = 0;
        kartu.add(judul, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 18, 0);
        kartu.add(subjudul, c);
        c.insets = new Insets(4, 0, 4, 0);
        c.gridy = 2;
        kartu.add(new JLabel("Username"), c);
        c.gridy = 3;
        kartu.add(fieldUsername, c);
        c.gridy = 4;
        kartu.add(new JLabel("Password"), c);
        c.gridy = 5;
        kartu.add(fieldPassword, c);
        c.gridy = 6;
        c.insets = new Insets(16, 0, 8, 0);
        btnLogin.setPreferredSize(new Dimension(btnLogin.getPreferredSize().width, 34));
        kartu.add(btnLogin, c);
        c.gridy = 7;
        c.insets = new Insets(6, 0, 0, 0);
        kartu.add(petunjuk, c);

        latar.add(kartu);
        setContentPane(latar);

        btnLogin.addActionListener(e -> prosesLogin());
        fieldPassword.addActionListener(e -> prosesLogin());
        fieldUsername.addActionListener(e -> fieldPassword.requestFocusInWindow());
        getRootPane().setDefaultButton(btnLogin);

        setSize(480, 520);
        setMinimumSize(new Dimension(400, 460));
        setLocationRelativeTo(null);
    }

    private void prosesLogin() {
        if (!btnLogin.isEnabled()) {
            return;
        }
        try {
            Pengguna p = auth.login(fieldUsername.getText(), fieldPassword.getPassword());
            bukaMenuUtama(p);
        } catch (LoginGagalException ex) {
            gagal++;
            fieldPassword.setText("");
            if (gagal >= MAKS_PERCOBAAN) {
                kunciSementara();
                UiUtil.error(this, ex.getMessage() + "\nTerlalu banyak percobaan. Coba lagi dalam 10 detik.");
            } else {
                UiUtil.error(this, ex.getMessage() + "\nSisa percobaan: " + (MAKS_PERCOBAAN - gagal));
                fieldPassword.requestFocusInWindow();
            }
        }
    }

    private void kunciSementara() {
        btnLogin.setEnabled(false);
        Timer timer = new Timer(10_000, e -> {
            btnLogin.setEnabled(true);
            gagal = 0;
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void bukaMenuUtama(Pengguna p) {
        new MainFrame(ctx, auth).setVisible(true);
        dispose();
    }
}
