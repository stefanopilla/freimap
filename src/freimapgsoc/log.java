package freimapgsoc;

import java.io.*;

public class log {

    public log() {
    }

    public static void append(String message) {

        String path = "/Users/Stefano/Desktop/FreimapSte/Freimap/hg/src/Data/log.txt";
            File file = new File(path);
        try {

            if (file.exists()) {
                PrintStream output = new PrintStream(file);
                output.append(message);
            } else  {
                file.createNewFile();
                FileOutputStream fileo = new FileOutputStream(path);
                PrintStream output = new PrintStream(fileo);
                output.append(message);
            }

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}


