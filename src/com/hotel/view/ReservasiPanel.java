package com.hotel.view;

import com.hotel.controller.AuthController;
import com.hotel.controller.ReservasiController;
import com.hotel.exception.HotelException;
import com.hotel.model.HakAkses;
import com.hotel.model.Reservasi;
import com.hotel.model.StatusReservasi;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Font;
import java.time.LocalDate;
import java.util.List;

/** Halaman reservasi: CRUD + aksi Check-in, Check-out, Batalkan, dan Rincian biaya. */
public class ReservasiPanel extends AbstractCrudPanel<Reservasi> {

    private static final long serialVersionUID = 1L;

    private final ReservasiController controller;
    private final JComboBox<Object> comboStatus = new JComboBox<>();
    private final JButton btnCheckIn = new JButton("Check-in");
    private final JButton btnCheckOut = new JButton("Check-out");
    private final JButton btnBatalkan = new JButton("Batalkan");
    private final JButton btnRincian = new JButton("Rincian");
    private final boolean bolehKelola;

    /** Interface fungsional kecil agar aksi status bisa ditulis sebagai lambda/method reference. */
    @FunctionalInterface
    private interface AksiStatus {
        Reservasi jalankan(String id) throws HotelException;
    }

    public ReservasiPanel(ReservasiController controller, AuthController auth) {
        super("Reservasi", new BaseTableModel<Reservasi>(
                new String[] {"ID", "Tamu", "Kamar", "Check-in", "Check-out", "Malam", "Jml Tamu", "Status", "Total"},
                new Class<?>[] {String.class, String.class, String.class, LocalDate.class, LocalDate.class,
                    Integer.class, Integer.class, String.class, Double.class}) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Object nilaiKolom(Reservasi r, int c) {
                return switch (c) {
                    case 0 -> r.getId();
                    case 1 -> r.getTamu().getNama();
                    case 2 -> r.getKamar().getNomor() + " (" + r.getKamar().getJenis() + ")";
                    case 3 -> r.getCheckIn();
                    case 4 -> r.getCheckOut();
                    case 5 -> r.getJumlahMalam();
                    case 6 -> r.getJumlahTamu();
                    case 7 -> r.getStatus().toString();
                    default -> r.hitungTotal();
                };
            }
        });
        this.controller = controller;
        this.bolehKelola = auth.boleh(HakAkses.KELOLA_RESERVASI);

        comboStatus.addItem("Semua status");
        for (StatusReservasi s : StatusReservasi.values()) {
            comboStatus.addItem(s);
        }
        panelFilter.add(new JLabel("  Status:"));
        panelFilter.add(comboStatus);
        comboStatus.addActionListener(e -> muatUlang());
        fieldCari.setToolTipText("Cari berdasarkan ID reservasi, nama tamu, atau nomor kamar");

        // tombol aksi status disisipkan sebelum tombol Ubah
        panelAksi.add(btnRincian, 0);
        panelAksi.add(btnBatalkan, 0);
        panelAksi.add(btnCheckOut, 0);
        panelAksi.add(btnCheckIn, 0);
        btnBatalkan.setVisible(bolehKelola);
        btnCheckIn.setVisible(bolehKelola);
        btnCheckOut.setVisible(bolehKelola);

        btnCheckIn.addActionListener(e -> jalankanAksi("Check-in", controller::checkIn));
        btnCheckOut.addActionListener(e -> jalankanAksi("Check-out", controller::checkOut));
        btnBatalkan.addActionListener(e -> jalankanAksi("Pembatalan", controller::batalkan));
        btnRincian.addActionListener(e -> tampilkanRincian(getTerpilih(), "Rincian Reservasi"));

        aturHakAkses(bolehKelola, bolehKelola, auth.boleh(HakAkses.HAPUS_DATA));
        int[] lebar = {60, 150, 120, 95, 95, 50, 65, 85, 110};
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    @Override
    protected List<Reservasi> ambilData(String kataKunci) {
        StatusReservasi status = comboStatus.getSelectedItem() instanceof StatusReservasi s ? s : null;
        return controller.cari(kataKunci, status);
    }

    @Override
    protected void perbaruiTombol() {
        super.perbaruiTombol();
        Reservasi r = getTerpilih();
        StatusReservasi s = r == null ? null : r.getStatus();
        btnUbah.setEnabled(s == StatusReservasi.DIPESAN);
        btnCheckIn.setEnabled(s == StatusReservasi.DIPESAN);
        btnBatalkan.setEnabled(s == StatusReservasi.DIPESAN);
        btnCheckOut.setEnabled(s == StatusReservasi.CHECK_IN);
        btnRincian.setEnabled(r != null);
    }

    @Override
    protected void tambah() {
        if (controller.getDaftarTamu().isEmpty()) {
            UiUtil.info(this, "Belum ada data tamu. Tambahkan tamu terlebih dahulu di menu Tamu.");
            return;
        }
        if (new ReservasiDialog(jendelaInduk(), controller, null).tampilkan()) {
            muatUlang();
        }
    }

    @Override
    protected void ubah(Reservasi item) {
        if (new ReservasiDialog(jendelaInduk(), controller, item).tampilkan()) {
            muatUlang();
        }
    }

    @Override
    protected void hapus(Reservasi item) throws HotelException {
        controller.hapus(item.getId());
    }

    @Override
    protected String deskripsi(Reservasi item) {
        return "reservasi " + item.getId() + " (" + item.getTamu().getNama() + ")";
    }

    private void jalankanAksi(String namaAksi, AksiStatus aksi) {
        Reservasi r = getTerpilih();
        if (r == null) {
            return;
        }
        if (!UiUtil.konfirmasi(this, namaAksi + " untuk " + deskripsi(r) + "?")) {
            return;
        }
        try {
            Reservasi hasil = aksi.jalankan(r.getId());
            muatUlang();
            if (hasil.getStatus() == StatusReservasi.SELESAI) {
                tampilkanRincian(hasil, "Struk Check-out");
            } else {
                UiUtil.info(this, namaAksi + " " + hasil.getId() + " berhasil. Status: " + hasil.getStatus() + ".");
            }
        } catch (HotelException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void tampilkanRincian(Reservasi r, String judul) {
        if (r == null) {
            return;
        }
        JTextArea area = new JTextArea(StrukFormatter.struk(r));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), judul, JOptionPane.PLAIN_MESSAGE);
    }
}
