package com.hotel.model;

public enum JenisKamar {
    STANDARD("Standard"),
    DELUXE("Deluxe"),
    SUITE("Suite");

    private final String label;

    JenisKamar(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
