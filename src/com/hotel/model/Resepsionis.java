package com.hotel.model;

import com.hotel.exception.ValidasiException;

import java.util.EnumSet;
import java.util.Set;

/** Resepsionis: boleh mengelola tamu & reservasi, tetapi tidak boleh mengubah kamar atau menghapus data. */
public class Resepsionis extends Pengguna {

    public static final String PERAN = "RESEPSIONIS";

    public Resepsionis(String username, String passwordHash, String namaLengkap) throws ValidasiException {
        super(username, passwordHash, namaLengkap);
    }

    @Override
    public String getPeran() {
        return PERAN;
    }

    @Override
    protected Set<HakAkses> getDaftarHak() {
        return EnumSet.of(HakAkses.KELOLA_TAMU, HakAkses.KELOLA_RESERVASI, HakAkses.LIHAT_LAPORAN);
    }
}
