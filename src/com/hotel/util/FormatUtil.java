package com.hotel.util;

import com.hotel.exception.ValidasiException;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/** Format Rupiah, tanggal, dan parsing angka dari input teks. */
public final class FormatUtil {

    public static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    public static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter TANGGAL_PANJANG = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", INDONESIA);

    private FormatUtil() {
    }

    public static String rupiah(double nilai) {
        NumberFormat nf = NumberFormat.getIntegerInstance(INDONESIA);
        return "Rp " + nf.format(Math.round(nilai));
    }

    public static String tanggal(LocalDate tgl) {
        return tgl == null ? "-" : tgl.format(TANGGAL);
    }

    public static LocalDate keLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date keDate(LocalDate tgl) {
        return Date.from(tgl.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Mengubah teks seperti "350000", "350.000" atau "Rp 350.000" menjadi angka.
     *
     * @throws ValidasiException jika kosong atau bukan angka
     */
    public static double parseAngka(String teks, String field, String namaTampilan) throws ValidasiException {
        if (teks == null || teks.isBlank()) {
            throw new ValidasiException(field, namaTampilan + " wajib diisi.");
        }
        String bersih = teks.trim()
                .replaceFirst("(?i)^rp\\.?", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(',', '.');
        try {
            return Double.parseDouble(bersih);
        } catch (NumberFormatException e) {
            throw new ValidasiException(field, namaTampilan + " harus berupa angka (contoh: 350000).");
        }
    }
}
