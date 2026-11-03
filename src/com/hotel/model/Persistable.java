package com.hotel.model;

/**
 * Interface untuk semua objek yang dapat disimpan ke file.
 * Diimplementasikan oleh Kamar, Tamu, Reservasi, dan Pengguna (lebih dari satu kelas).
 */
public interface Persistable {

    /** ID unik yang dipakai sebagai kunci di Map penyimpanan. */
    String getId();

    /** Mengubah objek menjadi kumpulan kolom untuk satu baris file. */
    String[] toRecord();
}
