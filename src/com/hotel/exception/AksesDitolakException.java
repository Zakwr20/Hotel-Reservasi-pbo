package com.hotel.exception;

/** Dilempar ketika peran pengguna (mis. Resepsionis) tidak berhak melakukan suatu operasi. */
public class AksesDitolakException extends HotelException {

    private static final long serialVersionUID = 1L;

    public AksesDitolakException(String pesan) {
        super(pesan);
    }
}
