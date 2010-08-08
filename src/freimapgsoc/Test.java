
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Test {

    public Test() throws IOException {
        read();
    }

    public void read() throws MalformedURLException, IOException {

        String olsr = "file:///etc/olsrd.conf";
        System.out.println("Fetching data from: " + olsr);
        System.out.println("This may take a while ... ");
        BufferedReader in = new BufferedReader(new InputStreamReader(new URL(olsr).openStream()));
        String line = in.readLine();
        while (true) {
            line = in.readLine();
            if (line == null) {
                break;
            }
            if (line.contains("LoadPlugin \"olsrd_dot_draw.so.0.3\"")) {
                while(!in.readLine().equals("}")){
                    String line2=in.readLine();
                    if(line2.contains("port")){
                        StringTokenizer st = new StringTokenizer(line2.substring(15, line2.length() - 1), "\"", false);
                        System.out.println(st.nextToken());
                    }
                }

            }

        }
    }

    public static void main(String args[]) throws IOException {
        new Test();
    }
}
