package com.hotel.view;

import com.hotel.controller.DashboardController;
import com.hotel.model.Reservasi;
import com.hotel.model.RingkasanDashboard;
import com.hotel.util.FormatUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;

/** Halaman ringkasan: 6 kartu statistik + tabel agenda check-in/check-out hari ini. */
public class DashboardPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final DashboardController controller;
    private final JLabel labelTanggal = new JLabel();
    private final JLabel nilaiTotalKamar = new JLabel();
    private final JLabel nilaiTersedia = new JLabel();
    private final JLabel nilaiMenginap = new JLabel();
    private final JLabel nilaiCheckIn = new JLabel();
    private final JLabel nilaiCheckOut = new JLabel();
    private final JLabel nilaiPendapatan = new JLabel();
    private final BaseTableModel<Reservasi> modelAgenda;

    public DashboardPanel(DashboardController controller, String namaPengguna) {
        super(new BorderLayout(0, 14));
        this.controller = controller;
        UiUtil.beriPadding(this, 16);

        JPanel atas = new JPanel();
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        JLabel judul = UiUtil.judul("Selamat datang, " + namaPengguna);
        judul.setAlignmentX(Component.LEFT_ALIGNMENT);
        labelTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(judul);
        atas.add(Box.createVerticalStrut(4));
        atas.add(labelTanggal);
        atas.add(Box.createVerticalStrut(14));

        JPanel kartu = new JPanel(new GridLayout(2, 3, 12, 12));
        kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(buatKartu("Total kamar", nilaiTotalKamar, UiUtil.WARNA_UTAMA));
        kartu.add(buatKartu("Kamar kosong hari ini", nilaiTersedia, new Color(0x2E7D32)));
        kartu.add(buatKartu("Tamu sedang menginap", nilaiMenginap, UiUtil.WARNA_AKSEN));
        kartu.add(buatKartu("Jadwal check-in hari ini", nilaiCheckIn, new Color(0xEF6C00)));
        kartu.add(buatKartu("Harus check-out hari ini", nilaiCheckOut, new Color(0xC62828)));
        kartu.add(buatKartu("Pendapatan bulan ini", nilaiPendapatan, new Color(0x6A1B9A)));
        atas.add(kartu);
        add(atas, BorderLayout.NORTH);

        modelAgenda = new BaseTableModel<>(
                new String[] {"Agenda", "ID", "Tamu", "Kamar", "Check-in", "Check-out", "Total"},
                new Class<?>[] {String.class, String.class, String.class, String.class, LocalDate.class, LocalDate.class, Double.class}) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Object nilaiKolom(Reservasi r, int c) {
                return switch (c) {
                    case 0 -> r.isAktif() && r.getStatus().name().equals("DIPESAN") ? "Akan check-in" : "Harus check-out";
                    case 1 -> r.getId();
                    case 2 -> r.getTamu().getNama();
                    case 3 -> r.getKamar().getNomor();
                    case 4 -> r.getCheckIn();
                    case 5 -> r.getCheckOut();
                    default -> r.hitungTotal();
                };
            }
        };
        JTable tabel = new JTable(modelAgenda);
        tabel.setRowHeight(26);
        tabel.setFillsViewportHeight(true);
        tabel.setDefaultRenderer(Double.class, UiUtil.rendererRupiah());
        tabel.setDefaultRenderer(LocalDate.class, UiUtil.rendererTanggal());

        JPanel panelAgenda = new JPanel(new BorderLayout(0, 6));
        JLabel judulAgenda = new JLabel("Agenda hari ini");
        judulAgenda.setFont(judulAgenda.getFont().deriveFont(Font.BOLD, 14f));
        panelAgenda.add(judulAgenda, BorderLayout.NORTH);
        panelAgenda.add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(panelAgenda, BorderLayout.CENTER);
    }

    private static JPanel buatKartu(String judul, JLabel nilai, Color warna) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, warna),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel labelJudul = new JLabel(judul);
        labelJudul.setForeground(new Color(0x5F6B7A));
        nilai.setFont(nilai.getFont().deriveFont(Font.BOLD, 24f));
        nilai.setForeground(warna);
        p.add(labelJudul, BorderLayout.NORTH);
        p.add(nilai, BorderLayout.CENTER);
        return p;
    }

    @Override
    public void muatUlang() {
        RingkasanDashboard r = controller.getRingkasan();
        labelTanggal.setText(LocalDate.now().format(FormatUtil.TANGGAL_PANJANG));
        nilaiTotalKamar.setText(String.valueOf(r.totalKamar()));
        nilaiTersedia.setText(String.valueOf(r.kamarTersediaHariIni()));
        nilaiMenginap.setText(String.valueOf(r.tamuMenginap()));
        nilaiCheckIn.setText(String.valueOf(r.checkInHariIni()));
        nilaiCheckOut.setText(String.valueOf(r.checkOutHariIni()));
        nilaiPendapatan.setText(FormatUtil.rupiah(r.pendapatanBulanIni()));
        modelAgenda.setData(controller.getAgendaHariIni());
    }
}
