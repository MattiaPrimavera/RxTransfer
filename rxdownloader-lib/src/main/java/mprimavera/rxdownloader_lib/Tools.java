package mprimavera.rxdownloader_lib;

import java.io.File;
import java.io.IOException;

public class Tools {
    public static File getOrCreateFile(String path) {
        File file = new File(path);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
