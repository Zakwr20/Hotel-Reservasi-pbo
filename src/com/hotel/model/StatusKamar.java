package com.hotel.model;

public enum StatusKamar {
    TERSEDIA("Tersedia"),
    TERISI("Terisi"),
    PERBAIKAN("Perbaikan");

    private final String label;

    StatusKamar(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
