package com.hotel.exception;

/** Membungkus kegagalan baca/tulis file (IOException) agar tidak bocor ke View. */
public class DataAksesException extends HotelException {

    private static final long serialVersionUID = 1L;

    public DataAksesException(String pesan, Throwable penyebab) {
        super(pesan, penyebab);
    }
}
