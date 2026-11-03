package com.hotel.app;

import com.hotel.dao.DataContext;
import com.hotel.exception.DataAksesException;
import com.hotel.view.LoginFrame;
import com.hotel.view.UiUtil;

import javax.swing.SwingUtilities;
import java.nio.file.Paths;

/**
 * Titik masuk aplikasi. Data disimpan di folder "data" (di dalam folder project saat dijalankan dari Eclipse).
 * Folder bisa diganti dengan VM argument: -Dhotel.data=folder_lain
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiUtil.pasangLookAndFeel();
            DataContext ctx = new DataContext(Paths.get(System.getProperty("hotel.data", "data")));
            try {
                ctx.muatSemua();
            } catch (DataAksesException e) {
                UiUtil.error(null, "Data tidak dapat dimuat:\n" + e.getMessage());
                System.exit(1);
            }
            new LoginFrame(ctx).setVisible(true);
        });
    }
}
