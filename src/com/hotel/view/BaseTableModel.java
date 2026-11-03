package com.hotel.view;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel generik: menampung List&lt;T&gt; dan subclass hanya menentukan isi tiap kolom.
 * Dipakai oleh semua JTable di aplikasi.
 */
public abstract class BaseTableModel<T> extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] kolom;
    private final Class<?>[] kelasKolom;
    private List<T> data = new ArrayList<>();

    protected BaseTableModel(String[] kolom, Class<?>[] kelasKolom) {
        this.kolom = kolom;
        this.kelasKolom = kelasKolom;
    }

    protected abstract Object nilaiKolom(T item, int indeksKolom);

    public void setData(List<T> dataBaru) {
        this.data = new ArrayList<>(dataBaru);
        fireTableDataChanged();
    }

    public T getItem(int baris) {
        return data.get(baris);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return kolom.length;
    }

    @Override
    public String getColumnName(int c) {
        return kolom[c];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return kelasKolom[c];
    }

    @Override
    public Object getValueAt(int baris, int c) {
        return nilaiKolom(data.get(baris), c);
    }

    @Override
    public boolean isCellEditable(int baris, int c) {
        return false;
    }
}
