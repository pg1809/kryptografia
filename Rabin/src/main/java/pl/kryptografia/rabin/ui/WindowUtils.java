package pl.kryptografia.rabin.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 *
 * @author Wojciech Szałapski
 */
public class WindowUtils {

    public static void centerWindow(Window window) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dimension.width - window.getWidth()) / 2;
        int y = (dimension.height - window.getHeight()) / 2;
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        window.setLocation(x, y);
    }
}
