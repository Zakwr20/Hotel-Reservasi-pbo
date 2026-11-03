package com.hotel.dao;

import com.hotel.exception.ValidasiException;
import com.hotel.model.Admin;
import com.hotel.model.Pengguna;
import com.hotel.model.Resepsionis;
import com.hotel.util.PasswordUtil;

import java.nio.file.Path;
import java.util.List;

public class PenggunaDao extends FileDao<Pengguna> {

    public PenggunaDao(Path folder) {
        super(folder, "pengguna.csv");
    }

    @Override
    protected String header() {
        return "peran;username;password_sha256;nama_lengkap";
    }

    @Override
    protected Pengguna parse(String[] k) throws ValidasiException {
        return switch (k[0]) {
            case Admin.PERAN -> new Admin(k[1], k[2], k[3]);
            case Resepsionis.PERAN -> new Resepsionis(k[1], k[2], k[3]);
            default -> throw new ValidasiException("peran", "Peran tidak dikenal: " + k[0]);
        };
    }

    @Override
    protected List<Pengguna> dataAwal() throws ValidasiException {
        return List.of(
                new Admin("admin", PasswordUtil.hash("admin123"), "Administrator Hotel"),
                new Resepsionis("resepsionis", PasswordUtil.hash("resep123"), "Resepsionis Shift Pagi"));
    }
}
