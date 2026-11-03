package com.hotel.model;

import com.hotel.exception.ValidasiException;

/**
 * PEWARISAN: Kamar Deluxe — jika paket termasuk sarapan, biaya sarapan
 * (per orang sesuai kapasitas, per malam) ditambahkan ke tarif kamar.
 */
public class KamarDeluxe extends Kamar {

    public static final double BIAYA_SARAPAN_PER_ORANG = 50_000;

    private boolean termasukSarapan;

    public KamarDeluxe(String nomor, int lantai, double hargaPerMalam, int kapasitas,
                       StatusKamar status, boolean termasukSarapan) throws ValidasiException {
        super(nomor, lantai, hargaPerMalam, kapasitas, status);
        this.termasukSarapan = termasukSarapan;
    }

    @Override
    public JenisKamar getJenis() {
        return JenisKamar.DELUXE;
    }

    @Override
    public double hitungBiaya(int malam) {
        validasiMalam(malam);
        double biaya = getHargaPerMalam() * malam;
        if (termasukSarapan) {
            biaya += BIAYA_SARAPAN_PER_ORANG * getKapasitas() * malam;
        }
        return biaya;
    }

    @Override
    public String getFasilitas() {
        return "AC, Smart TV, Wi-Fi, minibar, bathtub" + (termasukSarapan ? ", sarapan termasuk" : "");
    }

    @Override
    protected String getAtributKhusus() {
        return String.valueOf(termasukSarapan);
    }

    public boolean isTermasukSarapan() {
        return termasukSarapan;
    }

    public void setTermasukSarapan(boolean termasukSarapan) {
        this.termasukSarapan = termasukSarapan;
    }
}
