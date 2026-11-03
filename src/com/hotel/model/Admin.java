package com.hotel.model;

import com.hotel.exception.ValidasiException;

import java.util.EnumSet;
import java.util.Set;

/** Admin: memiliki semua hak akses. */
public class Admin extends Pengguna {

    public static final String PERAN = "ADMIN";

    public Admin(String username, String passwordHash, String namaLengkap) throws ValidasiException {
        super(username, passwordHash, namaLengkap);
    }

    @Override
    public String getPeran() {
        return PERAN;
    }

    @Override
    protected Set<HakAkses> getDaftarHak() {
        return EnumSet.allOf(HakAkses.class);
    }
}
