package com.hotel.exception;

/** Dilempar ketika data dengan ID tertentu tidak ada di penyimpanan. */
public class DataTidakDitemukanException extends HotelException {

    private static final long serialVersionUID = 1L;

    public DataTidakDitemukanException(String pesan) {
        super(pesan);
    }
}
