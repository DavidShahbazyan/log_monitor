package am.davsoft.logmanager.allinone;

import sun.misc.Signal;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author David Shahbazyan
 * @since Apr 09, 2019
 */
public class Main {
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

    private static final Map<String, Long> hitsMap = new HashMap<>();
    private static int i = 0;

    public static void main(String[] args) {
        clearConsole();
        if (args.length != 1) {
            System.err.println(" Error: Please specify the log file path.");
            System.exit(1);
        }
        try (InputStream is = Files.newInputStream(Paths.get(args[0]), StandardOpenOption.READ)) {
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
            String line;
            while (!TERMINATE) {
                line = lineReader.readLine();
                if (line != null) {
                    processLine(line);
                    rerenderScreen();
                    System.out.println();
                }
                System.out.print("\r Processing... " + loadingChars[i++]);
                if (i == loadingChars.length) {
                    i = 0;
                }
                Thread.sleep(100);
            }
            lineReader.close();
            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processLine(String line) throws IOException {
        Pattern pattern = Pattern.compile(URL_REGEX);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String url = matcher.group(0);
            hitsMap.put(url, hitsMap.computeIfAbsent(url, s -> 0L) + 1);
//            Socket socket = new Socket("localhost", 59090);
//            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
//            writer.println(url);
        }
    }

    private static void rerenderScreen() {
        clearConsole();
        System.out.println("The Monitor is running...\nPress <Ctrl+C> for termination.\n");
        for (Map.Entry<String, Long> entry : hitsMap.entrySet()) {
            System.out.println(String.format(" [%5d] : %s", entry.getValue(), entry.getKey()));
        }
    }

    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
