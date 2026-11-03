package com.hotel.model;

import com.hotel.exception.ValidasiException;

/**
 * PEWARISAN: Kamar Suite — diskon long stay 15% untuk ≥ 3 malam,
 * lalu dikenakan service charge 10%.
 */
public class KamarSuite extends Kamar {

    public static final double DISKON_LONG_STAY = 0.15;
    public static final int MINIMAL_LONG_STAY = 3;
    public static final double SERVICE_CHARGE = 0.10;

    private int jumlahRuang;

    public KamarSuite(String nomor, int lantai, double hargaPerMalam, int kapasitas,
                      StatusKamar status, int jumlahRuang) throws ValidasiException {
        super(nomor, lantai, hargaPerMalam, kapasitas, status);
        setJumlahRuang(jumlahRuang);
    }

    @Override
    public JenisKamar getJenis() {
        return JenisKamar.SUITE;
    }

    @Override
    public double hitungBiaya(int malam) {
        validasiMalam(malam);
        double subtotal = getHargaPerMalam() * malam;
        if (malam >= MINIMAL_LONG_STAY) {
            subtotal -= subtotal * DISKON_LONG_STAY;
        }
        return subtotal + subtotal * SERVICE_CHARGE;
    }

    @Override
    public String getFasilitas() {
        return jumlahRuang + " ruang, ruang tamu, jacuzzi, butler 24 jam";
    }

    @Override
    protected String getAtributKhusus() {
        return String.valueOf(jumlahRuang);
    }

    public int getJumlahRuang() {
        return jumlahRuang;
    }

    public final void setJumlahRuang(int jumlahRuang) throws ValidasiException {
        if (jumlahRuang < 1 || jumlahRuang > 5) {
            throw new ValidasiException("jumlahRuang", "Jumlah ruang suite harus 1 sampai 5.");
        }
        this.jumlahRuang = jumlahRuang;
    }
}
