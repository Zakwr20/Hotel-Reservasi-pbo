package com.hotel.exception;

/** Dilempar ketika data tidak boleh dihapus/diubah karena masih dipakai data lain (mis. kamar yang punya reservasi). */
public class DataTerpakaiException extends HotelException {

    private static final long serialVersionUID = 1L;

    public DataTerpakaiException(String pesan) {
        super(pesan);
    }
}
