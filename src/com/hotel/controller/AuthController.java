package com.hotel.controller;

import com.hotel.dao.PenggunaDao;
import com.hotel.exception.AksesDitolakException;
import com.hotel.exception.LoginGagalException;
import com.hotel.model.HakAkses;
import com.hotel.model.Pengguna;

import java.util.Arrays;

/** Mengatur login/logout dan pemeriksaan hak akses pengguna yang sedang aktif. */
public class AuthController {

    private final PenggunaDao penggunaDao;
    private Pengguna penggunaAktif;

    public AuthController(PenggunaDao penggunaDao) {
        this.penggunaDao = penggunaDao;
    }

    public Pengguna login(String username, char[] password) throws LoginGagalException {
        try {
            if (username == null || username.isBlank() || password == null || password.length == 0) {
                throw new LoginGagalException("Username dan password wajib diisi.");
            }
            Pengguna p = penggunaDao.findById(username.trim().toLowerCase())
                    .orElseThrow(() -> new LoginGagalException("Username atau password salah."));
            if (!p.cekPassword(new String(password))) {
                throw new LoginGagalException("Username atau password salah.");
            }
            penggunaAktif = p;
            return p;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0'); // hapus password dari memori
            }
        }
    }

    public void logout() {
        penggunaAktif = null;
    }

    public Pengguna getPenggunaAktif() {
        return penggunaAktif;
    }

    public boolean boleh(HakAkses hak) {
        return penggunaAktif != null && penggunaAktif.punyaAkses(hak);
    }

    public void wajibAkses(HakAkses hak) throws AksesDitolakException {
        if (penggunaAktif == null) {
            throw new AksesDitolakException("Silakan login terlebih dahulu.");
        }
        if (!penggunaAktif.punyaAkses(hak)) {
            throw new AksesDitolakException("Peran " + penggunaAktif.getPeran() + " tidak berhak " + hak.getDeskripsi() + ".");
        }
    }
}
