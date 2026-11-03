package com.hotel.model;

import com.hotel.exception.ValidasiException;

/**
 * Factory pattern: satu tempat untuk membuat objek turunan Kamar sesuai jenisnya.
 * Dipakai oleh KamarDao (saat membaca file) dan KamarController (saat input dari form).
 */
public final class KamarFactory {

    private KamarFactory() {
    }

    /**
     * @param atributKhusus Standard: tipe kasur; Deluxe: "true"/"false" (termasuk sarapan);
     *                      Suite: jumlah ruang
     */
    public static Kamar buat(JenisKamar jenis, String nomor, int lantai, double harga, int kapasitas,
                             StatusKamar status, String atributKhusus) throws ValidasiException {
        if (jenis == null) {
            throw new ValidasiException("jenis", "Jenis kamar wajib dipilih.");
        }
        return switch (jenis) {
            case STANDARD -> new KamarStandard(nomor, lantai, harga, kapasitas, status, atributKhusus);
            case DELUXE -> new KamarDeluxe(nomor, lantai, harga, kapasitas, status, Boolean.parseBoolean(atributKhusus));
            case SUITE -> new KamarSuite(nomor, lantai, harga, kapasitas, status, parseRuang(atributKhusus));
        };
    }

    private static int parseRuang(String teks) throws ValidasiException {
        try {
            return Integer.parseInt(teks == null ? "" : teks.trim());
        } catch (NumberFormatException e) {
            throw new ValidasiException("jumlahRuang", "Jumlah ruang suite harus berupa angka.");
        }
    }
}
