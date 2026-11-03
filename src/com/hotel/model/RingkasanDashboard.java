package com.hotel.model;

/** Data ringkas untuk kartu statistik di halaman dashboard. */
public record RingkasanDashboard(
        int totalKamar,
        int kamarTersediaHariIni,
        int tamuMenginap,
        int checkInHariIni,
        int checkOutHariIni,
        double pendapatanBulanIni) {
}
