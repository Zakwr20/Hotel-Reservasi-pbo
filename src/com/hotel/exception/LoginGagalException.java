package com.hotel.exception;

/** Dilempar ketika username/password salah. */
public class LoginGagalException extends HotelException {

    private static final long serialVersionUID = 1L;

    public LoginGagalException(String pesan) {
        super(pesan);
    }
}
