package com.hotel.exception;

/**
 * Kelas induk semua exception aplikasi (custom checked exception).
 * Semua exception turunan dapat ditangkap sekaligus di lapisan View
 * dengan satu blok catch (HotelException e).
 */
public class HotelException extends Exception {

    private static final long serialVersionUID = 1L;

    public HotelException(String pesan) {
        super(pesan);
    }

    public HotelException(String pesan, Throwable penyebab) {
        super(pesan, penyebab);
    }
}
