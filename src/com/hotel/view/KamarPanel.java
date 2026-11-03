package com.hotel.view;

import com.hotel.controller.AuthController;
import com.hotel.controller.KamarController;
import com.hotel.exception.HotelException;
import com.hotel.model.HakAkses;
import com.hotel.model.JenisKamar;
import com.hotel.model.Kamar;
import com.hotel.model.StatusKamar;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.util.List;

public class KamarPanel extends AbstractCrudPanel<Kamar> {

    private static final long serialVersionUID = 1L;

    private final KamarController controller;
    private final JComboBox<Object> comboJenis = new JComboBox<>();
    private final JComboBox<Object> comboStatus = new JComboBox<>();

    public KamarPanel(KamarController controller, AuthController auth) {
        super("Data Kamar", new BaseTableModel<Kamar>(
                new String[] {"No. Kamar", "Jenis", "Lantai", "Kapasitas", "Harga / Malam", "Status", "Fasilitas"},
                new Class<?>[] {String.class, String.class, Integer.class, Integer.class, Double.class, String.class, String.class}) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Object nilaiKolom(Kamar k, int c) {
                return switch (c) {
                    case 0 -> k.getNomor();
                    case 1 -> k.getJenis().toString();
                    case 2 -> k.getLantai();
                    case 3 -> k.getKapasitas();
                    case 4 -> k.getHargaPerMalam();
                    case 5 -> k.getStatus().toString();
                    default -> k.getFasilitas();
                };
            }
        });
        this.controller = controller;

        comboJenis.addItem("Semua jenis");
        for (JenisKamar j : JenisKamar.values()) {
            comboJenis.addItem(j);
        }
        comboStatus.addItem("Semua status");
        for (StatusKamar s : StatusKamar.values()) {
            comboStatus.addItem(s);
        }
        panelFilter.add(new JLabel("  Jenis:"));
        panelFilter.add(comboJenis);
        panelFilter.add(new JLabel("  Status:"));
        panelFilter.add(comboStatus);
        comboJenis.addActionListener(e -> muatUlang());
        comboStatus.addActionListener(e -> muatUlang());

        boolean boleh = auth.boleh(HakAkses.KELOLA_KAMAR);
        aturHakAkses(boleh, boleh, boleh);
        int[] lebar = {80, 90, 55, 70, 115, 90, 330};
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    @Override
    protected List<Kamar> ambilData(String kataKunci) {
        JenisKamar jenis = comboJenis.getSelectedItem() instanceof JenisKamar j ? j : null;
        StatusKamar status = comboStatus.getSelectedItem() instanceof StatusKamar s ? s : null;
        return controller.cari(kataKunci, jenis, status);
    }

    @Override
    protected void tambah() {
        if (new KamarDialog(jendelaInduk(), controller, null).tampilkan()) {
            muatUlang();
        }
    }

    @Override
    protected void ubah(Kamar item) {
        if (new KamarDialog(jendelaInduk(), controller, item).tampilkan()) {
            muatUlang();
        }
    }

    @Override
    protected void hapus(Kamar item) throws HotelException {
        controller.hapus(item.getNomor());
    }

    @Override
    protected String deskripsi(Kamar item) {
        return "kamar " + item.getNomor();
    }
}
