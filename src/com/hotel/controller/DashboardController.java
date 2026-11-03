package com.hotel.controller;

import com.hotel.dao.KamarDao;
import com.hotel.dao.ReservasiDao;
import com.hotel.model.Kamar;
import com.hotel.model.Reservasi;
import com.hotel.model.RingkasanDashboard;
import com.hotel.model.StatusReservasi;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Menyusun statistik ringkas dan agenda hari ini untuk halaman dashboard. */
public class DashboardController {

    private final KamarDao kamarDao;
    private final ReservasiDao reservasiDao;

    public DashboardController(KamarDao kamarDao, ReservasiDao reservasiDao) {
        this.kamarDao = kamarDao;
        this.reservasiDao = reservasiDao;
    }

    public RingkasanDashboard getRingkasan() {
        LocalDate hariIni = LocalDate.now();
        LocalDate besok = hariIni.plusDays(1);
        List<Kamar> semuaKamar = kamarDao.findAll();
        List<Reservasi> semuaReservasi = reservasiDao.findAll();

        int tersedia = (int) semuaKamar.stream()
                .filter(Kamar::isBisaDipesan)
                .filter(k -> semuaReservasi.stream()
                        .noneMatch(r -> r.getKamar().equals(k) && r.bentrokDengan(hariIni, besok)))
                .count();
        int menginap = hitung(semuaReservasi, StatusReservasi.CHECK_IN);
        int checkInHariIni = (int) semuaReservasi.stream()
                .filter(r -> r.getStatus() == StatusReservasi.DIPESAN && r.getCheckIn().equals(hariIni))
                .count();
        int checkOutHariIni = (int) semuaReservasi.stream()
                .filter(r -> r.getStatus() == StatusReservasi.CHECK_IN && !r.getCheckOut().isAfter(hariIni))
                .count();
        YearMonth bulanIni = YearMonth.from(hariIni);
        double pendapatan = semuaReservasi.stream()
                .filter(r -> r.getStatus() == StatusReservasi.SELESAI || r.getStatus() == StatusReservasi.CHECK_IN)
                .filter(r -> YearMonth.from(r.getCheckOut()).equals(bulanIni))
                .mapToDouble(Reservasi::hitungTotal)
                .sum();

        return new RingkasanDashboard(semuaKamar.size(), tersedia, menginap, checkInHariIni, checkOutHariIni, pendapatan);
    }

    /** Tamu yang dijadwalkan check-in hari ini dan tamu yang harus check-out (hari ini atau terlambat). */
    public List<Reservasi> getAgendaHariIni() {
        LocalDate hariIni = LocalDate.now();
        return reservasiDao.findAll().stream()
                .filter(r -> (r.getStatus() == StatusReservasi.DIPESAN && r.getCheckIn().equals(hariIni))
                        || (r.getStatus() == StatusReservasi.CHECK_IN && !r.getCheckOut().isAfter(hariIni)))
                .sorted(Comparator.comparing(Reservasi::getId))
                .collect(Collectors.toList());
    }

    private static int hitung(List<Reservasi> daftar, StatusReservasi status) {
        return (int) daftar.stream().filter(r -> r.getStatus() == status).count();
    }
}
