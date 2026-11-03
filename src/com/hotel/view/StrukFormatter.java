package com.hotel.view;

import com.hotel.model.Kamar;
import com.hotel.model.LayananTambahan;
import com.hotel.model.Reservasi;
import com.hotel.model.Tamu;
import com.hotel.util.FormatUtil;

/** Menyusun teks rincian biaya reservasi (dipakai di form estimasi, detail, dan struk check-out). */
public final class StrukFormatter {

    private static final String GARIS = "-".repeat(46);

    private StrukFormatter() {
    }

    public static String rincian(Reservasi r) {
        Kamar k = r.getKamar();
        int malam = r.getJumlahMalam();
        StringBuilder sb = new StringBuilder();
        baris(sb, "Kamar " + k.getNomor() + " (" + k.getJenis() + ")", "");
        baris(sb, "  " + malam + " malam x " + FormatUtil.rupiah(k.getHargaPerMalam()), FormatUtil.rupiah(r.hitungBiayaKamar()));
        if (r.getJumlahTamu() > k.getKapasitas()) {
            baris(sb, "  (termasuk extra bed)", "");
        }
        for (LayananTambahan l : r.getLayanan()) {
            baris(sb, "  + " + l, FormatUtil.rupiah(l.hitungBiaya(malam, r.getJumlahTamu())));
        }
        if (r.hitungDiskon() > 0) {
            baris(sb, "  Diskon member " + Math.round(Tamu.DISKON_MEMBER * 100) + "%", "-" + FormatUtil.rupiah(r.hitungDiskon()));
        }
        sb.append(GARIS).append('\n');
        baris(sb, "TOTAL", FormatUtil.rupiah(r.hitungTotal()));
        return sb.toString();
    }

    public static String struk(Reservasi r) {
        return "Reservasi : " + r.getId() + "\n"
                + "Tamu      : " + r.getTamu().getNama() + "\n"
                + "Menginap  : " + FormatUtil.tanggal(r.getCheckIn()) + " s.d. " + FormatUtil.tanggal(r.getCheckOut())
                + " (" + r.getJumlahTamu() + " tamu)\n"
                + "Status    : " + r.getStatus() + "\n"
                + GARIS + "\n"
                + rincian(r);
    }

    private static void baris(StringBuilder sb, String kiri, String kanan) {
        sb.append(String.format("%-31s%15s%n", kiri, kanan));
    }
}
