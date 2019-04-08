package am.davsoft.logmanager.sniffer;

import sun.misc.Signal;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Shahbazyan
 * @since Apr 08, 2019
 */
public class Sniffer {
    private static boolean TERMINATE = false;

    private static final String URL_REGEX = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private static final String[] loadingChars = {
            "[*     ]",
            "[**    ]",
            "[***   ]",
            "[ ***  ]",
            "[  *** ]",
            "[   ***]",
            "[    **]",
            "[     *]",
            "[      ]"
    };

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        if (args.length != 1) {
            System.err.println(" Error: Please specify the log file path.");
            System.exit(1);
        }
        try (InputStream is = Files.newInputStream(Paths.get("./log.txt"), StandardOpenOption.READ)) {
            System.out.println("The Sniffer process is running... ");
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader lineReader = new BufferedReader(reader);
            // Process all lines.
            // Signal handler method
            Signal.handle(new Signal("INT"), signal -> {
                System.out.println("Received signal: " + signal);
                System.out.println("Shutting down...");
                TERMINATE = true;
            });
            int i = 0;
            String line;
            while (!TERMINATE) {
                line = lineReader.readLine();
                if (line != null) {
                    processLine(line);
                }
                if (i == loadingChars.length) {
                    i = 0;
                }
                System.out.print("\r Press <Ctrl+C> for termination." + loadingChars[i++]);
            }
            lineReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processLine(String line) throws IOException {
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String url = matcher.group(0);
            Socket socket = new Socket("localhost", 59090);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(url);
        }
    }
}
