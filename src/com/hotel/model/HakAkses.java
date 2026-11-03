package com.hotel.model;

public enum HakAkses {
    KELOLA_KAMAR("mengelola data kamar"),
    KELOLA_TAMU("mengelola data tamu"),
    KELOLA_RESERVASI("mengelola reservasi"),
    HAPUS_DATA("menghapus data tamu/reservasi"),
    LIHAT_LAPORAN("melihat laporan");

    private final String deskripsi;

    HakAkses(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
