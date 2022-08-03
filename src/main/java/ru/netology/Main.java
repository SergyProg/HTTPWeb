package ru.netology;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final int DEFAULT_PORT = 23445; //9999;
        final String RESOURCE_DIR = "public";
        final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/default-get.html");

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        for (String validMethod : server.validMethods) {
            for (String validPath : validPaths) {
                server.addHandler(validMethod, validPath, (request, responseStream) -> {
                    try {
                        ClientHandler.returnResponseOK(request, responseStream, RESOURCE_DIR);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        server.listen();
    }
}