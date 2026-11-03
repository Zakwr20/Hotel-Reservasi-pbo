package com.hotel.view;

import com.hotel.exception.HotelException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Kerangka umum halaman CRUD: judul, kotak pencarian, tombol Tambah/Ubah/Hapus/Muat ulang, dan JTable.
 * Template method: subclass mengisi ambilData(), tambah(), ubah(), hapus(), deskripsi().
 */
public abstract class AbstractCrudPanel<T> extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    protected final BaseTableModel<T> model;
    protected final JTable tabel;
    protected final JTextField fieldCari = new JTextField(18);
    protected final JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    protected final JPanel panelAksi = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    protected final JButton btnTambah = new JButton("Tambah");
    protected final JButton btnUbah = new JButton("Ubah");
    protected final JButton btnHapus = new JButton("Hapus");
    protected final JButton btnMuatUlang = new JButton("Muat ulang");
    private final JLabel labelJumlah = new JLabel(" ");
    private boolean bolehHapus = true;

    protected AbstractCrudPanel(String judul, BaseTableModel<T> model) {
        super(new BorderLayout(0, 10));
        this.model = model;
        UiUtil.beriPadding(this, 16);

        // ----- bagian atas: judul + toolbar -----
        panelFilter.add(new JLabel("Cari:"));
        panelFilter.add(fieldCari);
        panelAksi.add(btnTambah);
        panelAksi.add(btnUbah);
        panelAksi.add(btnHapus);
        panelAksi.add(btnMuatUlang);

        // dua baris (filter di atas, tombol di bawah) agar tetap rapi saat jendela dikecilkan
        fieldCari.setMinimumSize(fieldCari.getPreferredSize());
        panelAksi.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JPanel toolbar = new JPanel(new BorderLayout(0, 8));
        toolbar.add(panelFilter, BorderLayout.NORTH);
        toolbar.add(panelAksi, BorderLayout.SOUTH);

        JPanel atas = new JPanel();
        atas.setLayout(new BoxLayout(atas, BoxLayout.Y_AXIS));
        JLabel labelJudul = UiUtil.judul(judul);
        labelJudul.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        atas.add(labelJudul);
        atas.add(Box.createVerticalStrut(10));
        atas.add(toolbar);
        add(atas, BorderLayout.NORTH);

        // ----- tabel -----
        tabel = new JTable(model);
        tabel.setAutoCreateRowSorter(true);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(26);
        tabel.setFillsViewportHeight(true);
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.setDefaultRenderer(Double.class, UiUtil.rendererRupiah());
        tabel.setDefaultRenderer(LocalDate.class, UiUtil.rendererTanggal());
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xD0D7E1)));
        add(scroll, BorderLayout.CENTER);
        add(labelJumlah, BorderLayout.SOUTH);

        // ----- event handling: tiap event memanggil method kecil masing-masing -----
        btnTambah.addActionListener(e -> aksiTambah());
        btnUbah.addActionListener(e -> aksiUbah());
        btnHapus.addActionListener(e -> aksiHapus());
        btnMuatUlang.addActionListener(e -> muatUlang());
        fieldCari.getDocument().addDocumentListener(UiUtil.saatBerubah(this::muatUlang));
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                perbaruiTombol();
            }
        });
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && btnUbah.isVisible() && btnUbah.isEnabled()) {
                    aksiUbah();
                }
            }
        });
    }

    // ===== method yang wajib diisi subclass =====

    protected abstract List<T> ambilData(String kataKunci);

    protected abstract void tambah();

    protected abstract void ubah(T item);

    protected abstract void hapus(T item) throws HotelException;

    protected abstract String deskripsi(T item);

    // ===== alur umum =====

    @Override
    public final void muatUlang() {
        model.setData(ambilData(fieldCari.getText().trim()));
        labelJumlah.setText(model.getRowCount() + " data ditampilkan");
        perbaruiTombol();
    }

    /** Mengatur tombol mana yang terlihat berdasarkan hak akses pengguna. */
    protected void aturHakAkses(boolean tambah, boolean ubah, boolean hapus) {
        btnTambah.setVisible(tambah);
        btnUbah.setVisible(ubah);
        btnHapus.setVisible(hapus);
        this.bolehHapus = hapus;
    }

    protected void perbaruiTombol() {
        boolean adaPilihan = getTerpilih() != null;
        btnUbah.setEnabled(adaPilihan);
        btnHapus.setEnabled(adaPilihan && bolehHapus);
    }

    protected T getTerpilih() {
        int baris = tabel.getSelectedRow();
        return baris < 0 ? null : model.getItem(tabel.convertRowIndexToModel(baris));
    }

    protected Window jendelaInduk() {
        return SwingUtilities.getWindowAncestor(this);
    }

    private void aksiTambah() {
        tambah();
    }

    private void aksiUbah() {
        T item = getTerpilih();
        if (item == null) {
            UiUtil.info(this, "Pilih satu baris pada tabel terlebih dahulu.");
            return;
        }
        ubah(item);
    }

    private void aksiHapus() {
        T item = getTerpilih();
        if (item == null) {
            UiUtil.info(this, "Pilih satu baris pada tabel terlebih dahulu.");
            return;
        }
        if (!UiUtil.konfirmasi(this, "Yakin ingin menghapus " + deskripsi(item) + "?\nData yang dihapus tidak dapat dikembalikan.")) {
            return;
        }
        try {
            hapus(item);
            muatUlang();
            UiUtil.info(this, deskripsi(item) + " berhasil dihapus.");
        } catch (HotelException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }
}
