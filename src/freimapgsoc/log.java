package freimapgsoc;

import java.io.*;

public class log {

    public log() {
    }

    public static void append(String message) {

        String path = "/Users/stefanopilla/Desktop/freimap/log.txt";

        try {

            File file = new File(path);
            if (file.exists()) {
                PrintStream output = new PrintStream(file);
                output.append(message);
            } else  {
                FileOutputStream fileo = new FileOutputStream(path);
                PrintStream output = new PrintStream(fileo);
                output.append(message);
            }

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}


