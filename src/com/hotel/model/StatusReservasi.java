package com.hotel.model;

public enum StatusReservasi {
    DIPESAN("Dipesan"),
    CHECK_IN("Check-in"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusReservasi(String label) {
        this.label = label;
    }

    /** Reservasi aktif = masih "memakai" kamar pada rentang tanggalnya. */
    public boolean isAktif() {
        return this == DIPESAN || this == CHECK_IN;
    }

    @Override
    public String toString() {
        return label;
    }
}
