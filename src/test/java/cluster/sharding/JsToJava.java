package cluster.sharding;

import java.io.*;

public class JsToJava {
    public static void main(String[] args) throws IOException {
        String filename = "monitor2.html";
        InputStream inputStream = JsToJava.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new FileNotFoundException(String.format("Filename '%s'", filename));
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.printf("line(html, \"%s\");%n", line);
        }
    }
}
