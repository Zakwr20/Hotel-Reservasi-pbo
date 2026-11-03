package com.hotel.view;

import com.hotel.controller.AuthController;
import com.hotel.controller.TamuController;
import com.hotel.exception.HotelException;
import com.hotel.model.HakAkses;
import com.hotel.model.Tamu;

import java.util.List;

public class TamuPanel extends AbstractCrudPanel<Tamu> {

    private static final long serialVersionUID = 1L;

    private final TamuController controller;

    public TamuPanel(TamuController controller, AuthController auth) {
        super("Data Tamu", new BaseTableModel<Tamu>(
                new String[] {"ID", "Nama", "No. Identitas", "Telepon", "Email", "Jenis Kelamin", "Member"},
                new Class<?>[] {String.class, String.class, String.class, String.class, String.class, String.class, Boolean.class}) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Object nilaiKolom(Tamu t, int c) {
                return switch (c) {
                    case 0 -> t.getId();
                    case 1 -> t.getNama();
                    case 2 -> t.getNoIdentitas();
                    case 3 -> t.getTelepon();
                    case 4 -> t.getEmail().isEmpty() ? "-" : t.getEmail();
                    case 5 -> t.getJenisKelamin().toString();
                    default -> t.isMember();
                };
            }
        });
        this.controller = controller;
        fieldCari.setToolTipText("Cari berdasarkan ID, nama, no. identitas, telepon, atau email");
        aturHakAkses(auth.boleh(HakAkses.KELOLA_TAMU), auth.boleh(HakAkses.KELOLA_TAMU), auth.boleh(HakAkses.HAPUS_DATA));
        tabel.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(170);
        tabel.getColumnModel().getColumn(4).setPreferredWidth(180);
    }

    @Override
    protected List<Tamu> ambilData(String kataKunci) {
        return controller.cari(kataKunci);
    }

    @Override
    protected void tambah() {
        if (new TamuDialog(jendelaInduk(), controller, null).tampilkan()) {
            muatUlang();
        }
    }

    @Override
    protected void ubah(Tamu item) {
        if (new TamuDialog(jendelaInduk(), controller, item).tampilkan()) {
            muatUlang();
        }
    }

    @Override
    protected void hapus(Tamu item) throws HotelException {
        controller.hapus(item.getId());
    }

    @Override
    protected String deskripsi(Tamu item) {
        return "tamu " + item.getNama() + " (" + item.getId() + ")";
    }
}
