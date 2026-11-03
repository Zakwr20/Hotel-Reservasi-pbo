package com.hotel.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitas sederhana untuk menulis/membaca satu baris CSV dengan pemisah titik koma.
 * Field yang mengandung pemisah atau tanda kutip akan diberi kutip ganda.
 */
public final class CsvUtil {

    public static final char PEMISAH = ';';

    private CsvUtil() {
    }

    public static String gabung(String[] kolom) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kolom.length; i++) {
            if (i > 0) {
                sb.append(PEMISAH);
            }
            sb.append(escape(kolom[i] == null ? "" : kolom[i]));
        }
        return sb.toString();
    }

    private static String escape(String s) {
        String bersih = s.replace('\n', ' ').replace('\r', ' ');
        boolean perluKutip = bersih.indexOf(PEMISAH) >= 0 || bersih.indexOf('"') >= 0;
        if (!perluKutip) {
            return bersih;
        }
        return "\"" + bersih.replace("\"", "\"\"") + "\"";
    }

    public static String[] pecah(String baris) {
        List<String> hasil = new ArrayList<>();
        StringBuilder sekarang = new StringBuilder();
        boolean dalamKutip = false;
        for (int i = 0; i < baris.length(); i++) {
            char c = baris.charAt(i);
            if (dalamKutip) {
                if (c == '"') {
                    if (i + 1 < baris.length() && baris.charAt(i + 1) == '"') {
                        sekarang.append('"');
                        i++;
                    } else {
                        dalamKutip = false;
                    }
                } else {
                    sekarang.append(c);
                }
            } else if (c == '"') {
                dalamKutip = true;
            } else if (c == PEMISAH) {
                hasil.add(sekarang.toString());
                sekarang.setLength(0);
            } else {
                sekarang.append(c);
            }
        }
        hasil.add(sekarang.toString());
        return hasil.toArray(new String[0]);
    }
}
