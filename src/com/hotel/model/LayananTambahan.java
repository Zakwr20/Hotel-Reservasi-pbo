package com.hotel.model;

import com.hotel.util.FormatUtil;

/** Layanan tambahan yang bisa dipilih saat reservasi (ditampilkan sebagai checkbox). */
public enum LayananTambahan {
    SARAPAN("Sarapan", 75_000, true, true),
    LAUNDRY("Laundry", 50_000, true, false),
    ANTAR_JEMPUT("Antar-jemput bandara", 200_000, false, false),
    SPA("Paket spa", 350_000, false, true);

    private final String label;
    private final double harga;
    private final boolean perMalam;
    private final boolean perOrang;

    LayananTambahan(String label, double harga, boolean perMalam, boolean perOrang) {
        this.label = label;
        this.harga = harga;
        this.perMalam = perMalam;
        this.perOrang = perOrang;
    }

    public double hitungBiaya(int malam, int jumlahTamu) {
        double biaya = harga;
        if (perMalam) {
            biaya *= malam;
        }
        if (perOrang) {
            biaya *= jumlahTamu;
        }
        return biaya;
    }

    public double getHarga() {
        return harga;
    }

    public String getKeterangan() {
        return label + " (" + FormatUtil.rupiah(harga)
                + (perOrang ? "/orang" : "")
                + (perMalam ? "/malam" : "") + ")";
    }

    @Override
    public String toString() {
        return label;
    }
}
