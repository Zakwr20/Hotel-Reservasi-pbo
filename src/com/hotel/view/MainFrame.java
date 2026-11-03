package com.hotel.view;

import com.hotel.controller.AuthController;
import com.hotel.controller.DashboardController;
import com.hotel.controller.KamarController;
import com.hotel.controller.ReservasiController;
import com.hotel.controller.TamuController;
import com.hotel.dao.DataContext;
import com.hotel.model.Pengguna;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

/**
 * Tampilan utama: menu bar + sidebar navigasi (konsisten di semua halaman) + area konten CardLayout.
 * Controller dibuat di sini lalu diberikan ke tiap panel (View tidak mengakses DAO langsung).
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final DataContext ctx;
    private final AuthController auth;
    private final CardLayout kartu = new CardLayout();
    private final JPanel konten = new JPanel(kartu);
    private final Map<String, Refreshable> halaman = new LinkedHashMap<>();
    private final Map<String, JToggleButton> tombolNav = new HashMap<>();
    private final ButtonGroup grupNav = new ButtonGroup();
    private final JPanel panelNav = new JPanel(new GridLayout(0, 1, 0, 6));
    private final JMenu menuNavigasi = new JMenu("Navigasi");

    public MainFrame(DataContext ctx, AuthController auth) {
        super("Sistem Reservasi Hotel — " + auth.getPenggunaAktif().getNamaLengkap());
        this.ctx = ctx;
        this.auth = auth;
        Pengguna p = auth.getPenggunaAktif();

        KamarController kamarCtrl = new KamarController(ctx.getKamarDao(), ctx.getReservasiDao(), auth);
        TamuController tamuCtrl = new TamuController(ctx.getTamuDao(), ctx.getReservasiDao(), auth);
        ReservasiController resCtrl = new ReservasiController(ctx.getReservasiDao(), ctx.getKamarDao(), ctx.getTamuDao(), auth);
        DashboardController dashCtrl = new DashboardController(ctx.getKamarDao(), ctx.getReservasiDao());

        setJMenuBar(buatMenuBar());
        daftarkan("dashboard", "Dashboard", new DashboardPanel(dashCtrl, p.getNamaLengkap()), KeyEvent.VK_1);
        daftarkan("reservasi", "Reservasi", new ReservasiPanel(resCtrl, auth), KeyEvent.VK_2);
        daftarkan("tamu", "Tamu", new TamuPanel(tamuCtrl, auth), KeyEvent.VK_3);
        daftarkan("kamar", "Kamar", new KamarPanel(kamarCtrl, auth), KeyEvent.VK_4);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buatSidebar(), BorderLayout.WEST);
        getContentPane().add(konten, BorderLayout.CENTER);
        getContentPane().add(buatStatusBar(p), BorderLayout.SOUTH);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                keluar();
            }
        });
        setSize(1180, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        tampilkan("dashboard");
    }

    /** Menampilkan halaman tertentu dan memuat ulang datanya. */
    public void tampilkan(String nama) {
        kartu.show(konten, nama);
        halaman.get(nama).muatUlang();
        tombolNav.get(nama).setSelected(true);
    }

    private void daftarkan(String nama, String label, JPanel panel, int tombolAngka) {
        halaman.put(nama, (Refreshable) panel);
        konten.add(panel, nama);

        JToggleButton tombol = new JToggleButton(label);
        tombol.setHorizontalAlignment(JToggleButton.LEFT);
        tombol.setFocusPainted(false);
        tombol.addActionListener(e -> tampilkan(nama));
        grupNav.add(tombol);
        tombolNav.put(nama, tombol);
        panelNav.add(tombol);

        JMenuItem item = new JMenuItem(label);
        item.setAccelerator(KeyStroke.getKeyStroke(tombolAngka, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        item.addActionListener(e -> tampilkan(nama));
        menuNavigasi.add(item);
    }

    private JMenuBar buatMenuBar() {
        int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenuBar bar = new JMenuBar();

        JMenu menuBerkas = new JMenu("Berkas");
        JMenuItem itemLogout = new JMenuItem("Logout");
        itemLogout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ctrl));
        itemLogout.addActionListener(e -> logout());
        JMenuItem itemKeluar = new JMenuItem("Keluar");
        itemKeluar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ctrl));
        itemKeluar.addActionListener(e -> keluar());
        menuBerkas.add(itemLogout);
        menuBerkas.addSeparator();
        menuBerkas.add(itemKeluar);

        JMenu menuBantuan = new JMenu("Bantuan");
        JMenuItem itemTentang = new JMenuItem("Tentang aplikasi");
        itemTentang.addActionListener(e -> tampilkanTentang());
        menuBantuan.add(itemTentang);

        bar.add(menuBerkas);
        bar.add(menuNavigasi);
        bar.add(menuBantuan);
        return bar;
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 18));
        sidebar.setBackground(UiUtil.WARNA_UTAMA);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        sidebar.setPreferredSize(new Dimension(190, 0));

        JLabel logo = new JLabel("<html>Hotel<br>Mulawarman</html>");
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 20f));
        sidebar.add(logo, BorderLayout.NORTH);

        panelNav.setOpaque(false);
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.add(panelNav, BorderLayout.NORTH);
        sidebar.add(pembungkus, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buatStatusBar(Pengguna p) {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD0D7E1)),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        status.add(new JLabel("Login sebagai " + p.getNamaLengkap() + " · peran " + p.getPeran()), BorderLayout.WEST);
        status.add(new JLabel("Data: " + ctx.getFolder().toAbsolutePath()), BorderLayout.EAST);
        return status;
    }

    private void tampilkanTentang() {
        UiUtil.info(this, "Sistem Reservasi Hotel\n"
                + "Tugas Proyek Pemrograman Berorientasi Objek\n\n"
                + "Arsitektur: Model – View – Controller (Java Swing)\n"
                + "Penyimpanan: file CSV di folder data/\n\n"
                + "Pintasan: Ctrl+1 Dashboard · Ctrl+2 Reservasi · Ctrl+3 Tamu · Ctrl+4 Kamar");
    }

    private void logout() {
        if (!UiUtil.konfirmasi(this, "Keluar dari akun " + auth.getPenggunaAktif().getUsername() + "?")) {
            return;
        }
        auth.logout();
        dispose();
        new LoginFrame(ctx).setVisible(true);
    }

    private void keluar() {
        if (UiUtil.konfirmasi(this, "Tutup aplikasi?")) {
            dispose();
            System.exit(0);
        }
    }
}
