package com.hotel.model;

import com.hotel.exception.ValidasiException;

import java.util.Arrays;

/** PEWARISAN: Kamar Standard — biaya = harga per malam × jumlah malam. */
public class KamarStandard extends Kamar {

    public static final String[] TIPE_KASUR = {"Single", "Double", "Twin"};

    private String tipeKasur;

    public KamarStandard(String nomor, int lantai, double hargaPerMalam, int kapasitas,
                         StatusKamar status, String tipeKasur) throws ValidasiException {
        super(nomor, lantai, hargaPerMalam, kapasitas, status);
        setTipeKasur(tipeKasur);
    }

    @Override
    public JenisKamar getJenis() {
        return JenisKamar.STANDARD;
    }

    @Override
    public double hitungBiaya(int malam) {
        validasiMalam(malam);
        return getHargaPerMalam() * malam;
    }

    @Override
    public String getFasilitas() {
        return "AC, TV, Wi-Fi, kasur " + tipeKasur;
    }

    @Override
    protected String getAtributKhusus() {
        return tipeKasur;
    }

    public String getTipeKasur() {
        return tipeKasur;
    }

    public final void setTipeKasur(String tipeKasur) throws ValidasiException {
        if (tipeKasur == null || Arrays.stream(TIPE_KASUR).noneMatch(t -> t.equals(tipeKasur))) {
            throw new ValidasiException("tipeKasur", "Tipe kasur harus salah satu dari: " + String.join(", ", TIPE_KASUR) + ".");
        }
        this.tipeKasur = tipeKasur;
    }
}
