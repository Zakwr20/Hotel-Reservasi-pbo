package com.hotel.exception;

/**
 * Dilempar oleh setter/konstruktor model ketika input tidak valid.
 * Menyimpan nama field agar View dapat memindahkan fokus ke input yang salah.
 */
public class ValidasiException extends HotelException {

    private static final long serialVersionUID = 1L;

    private final String field;

    public ValidasiException(String field, String pesan) {
        super(pesan);
        this.field = field;
    }

    public ValidasiException(String pesan) {
        this(null, pesan);
    }

    public String getField() {
        return field;
    }
}
