package am.davsoft.logmanager.monitor;

import sun.misc.Signal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author David Shahbazyan
 * @since Apr 08, 2019
 */
public class Monitor {
    private static final Map<String, Long> hitsMap = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket listener = new ServerSocket(59090)) {
            // Signal handler method
            Signal.handle(new Signal("INT"), signal -> {
                System.out.println("Received signal: " + signal);
                System.out.println("Shutting down...");
                System.exit(0);
            });
            while (true) {
                try (Socket socket = listener.accept()) {
                    Scanner in = new Scanner(socket.getInputStream());
                    while (in.hasNextLine()) {
                        String url = in.nextLine();
                        hitsMap.put(url, hitsMap.computeIfAbsent(url, s -> 0L) + 1);
                        rerenderScreen();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
