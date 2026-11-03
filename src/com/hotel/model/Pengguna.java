package com.hotel.model;

import com.hotel.exception.ValidasiException;
import com.hotel.util.PasswordUtil;

import java.util.Set;

/**
 * ABSTRAKSI + PEWARISAN (hierarki kedua): pengguna aplikasi.
 * Turunan: Admin dan Resepsionis, berbeda pada peran dan daftar hak aksesnya.
 */
public abstract class Pengguna implements Persistable {

    private String username;
    private String passwordHash;
    private String namaLengkap;

    protected Pengguna(String username, String passwordHash, String namaLengkap) throws ValidasiException {
        setUsername(username);
        setPasswordHash(passwordHash);
        setNamaLengkap(namaLengkap);
    }

    public abstract String getPeran();

    protected abstract Set<HakAkses> getDaftarHak();

    public boolean punyaAkses(HakAkses hak) {
        return getDaftarHak().contains(hak);
    }

    /** Overload: true jika memiliki SEMUA hak yang disebutkan. */
    public boolean punyaAkses(HakAkses... daftar) {
        for (HakAkses h : daftar) {
            if (!punyaAkses(h)) {
                return false;
            }
        }
        return true;
    }

    public boolean cekPassword(String plain) {
        return plain != null && passwordHash.equals(PasswordUtil.hash(plain));
    }

    public void gantiPassword(String plainBaru) throws ValidasiException {
        if (plainBaru == null || plainBaru.length() < 6) {
            throw new ValidasiException("password", "Password minimal 6 karakter.");
        }
        this.passwordHash = PasswordUtil.hash(plainBaru);
    }

    @Override
    public String getId() {
        return username;
    }

    public String getUsername() {
        return username;
    }

    public final void setUsername(String username) throws ValidasiException {
        if (username == null || !username.matches("[a-z0-9_]{3,20}")) {
            throw new ValidasiException("username", "Username 3–20 karakter: huruf kecil, angka, atau garis bawah.");
        }
        this.username = username;
    }

    public final void setPasswordHash(String passwordHash) throws ValidasiException {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidasiException("password", "Password wajib ada.");
        }
        this.passwordHash = passwordHash;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public final void setNamaLengkap(String namaLengkap) throws ValidasiException {
        if (namaLengkap == null || namaLengkap.isBlank()) {
            throw new ValidasiException("namaLengkap", "Nama lengkap wajib diisi.");
        }
        this.namaLengkap = namaLengkap.trim();
    }

    @Override
    public String[] toRecord() {
        return new String[] {getPeran(), username, passwordHash, namaLengkap};
    }

    @Override
    public String toString() {
        return namaLengkap + " (" + getPeran() + ")";
    }
}
