package com.hotel.model;

public enum JenisKelamin {
    LAKI_LAKI("Laki-laki"),
    PEREMPUAN("Perempuan");

    private final String label;

    JenisKelamin(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
