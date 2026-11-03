package com.hotel.exception;

/** Dilempar ketika kamar sudah dipesan pada rentang tanggal yang sama atau sedang diperbaiki. */
public class KamarTidakTersediaException extends HotelException {

    private static final long serialVersionUID = 1L;

    public KamarTidakTersediaException(String pesan) {
        super(pesan);
    }
}
