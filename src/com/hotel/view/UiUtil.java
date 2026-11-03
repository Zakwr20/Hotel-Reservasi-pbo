package com.hotel.view;

import com.hotel.util.FormatUtil;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/** Kumpulan helper tampilan agar kode View tidak berulang. */
public final class UiUtil {

    public static final Color WARNA_UTAMA = new Color(0x1F4E79);
    public static final Color WARNA_AKSEN = new Color(0x2E75B6);
    public static final Color WARNA_LATAR = new Color(0xF4F6F9);
    public static final Color WARNA_ERROR = new Color(0xB00020);

    private UiUtil() {
    }

    public static void pasangLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // jika Nimbus tidak ada, pakai tampilan bawaan
        }
        UIManager.put("OptionPane.yesButtonText", "Ya");
        UIManager.put("OptionPane.noButtonText", "Tidak");
        UIManager.put("OptionPane.cancelButtonText", "Batal");
        UIManager.put("OptionPane.okButtonText", "OK");
    }

    // ===== dialog pesan =====

    public static void error(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Terjadi Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    public static void peringatan(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean konfirmasi(Component induk, String pesan) {
        return JOptionPane.showConfirmDialog(induk, pesan, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ===== komponen =====

    public static JLabel judul(String teks) {
        JLabel label = new JLabel(teks);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 20f));
        label.setForeground(WARNA_UTAMA);
        return label;
    }

    public static void beriPadding(JComponent c, int px) {
        c.setBorder(BorderFactory.createEmptyBorder(px, px, px, px));
    }

    /** Spinner tanggal (pengganti date picker, bawaan Swing tanpa library tambahan). */
    public static JSpinner buatSpinnerTanggal(LocalDate awal) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(FormatUtil.keDate(awal), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd-MM-yyyy"));
        return spinner;
    }

    public static LocalDate ambilTanggal(JSpinner spinner) {
        return FormatUtil.keLocalDate((Date) spinner.getValue());
    }

    public static void aturTanggal(JSpinner spinner, LocalDate tgl) {
        spinner.setValue(FormatUtil.keDate(tgl));
    }

    /** DocumentListener versi lambda: dipanggil setiap isi text field berubah. */
    public static DocumentListener saatBerubah(Runnable aksi) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aksi.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aksi.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aksi.run();
            }
        };
    }

    // ===== renderer tabel =====

    public static DefaultTableCellRenderer rendererRupiah() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object nilai) {
                super.setValue(nilai instanceof Number n ? FormatUtil.rupiah(n.doubleValue()) : nilai);
            }
        };
        r.setHorizontalAlignment(JLabel.RIGHT);
        return r;
    }

    public static DefaultTableCellRenderer rendererTanggal() {
        return new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object nilai) {
                super.setValue(nilai instanceof LocalDate d ? FormatUtil.tanggal(d) : nilai);
            }
        };
    }
}
