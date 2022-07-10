package ru.netology;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        int port = Server.DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        for (String validMethod : server.validMethods) {
            for (String validPath : server.validPaths) {
                server.addHandler(validMethod, validPath, (request, responseStream) -> {
                    try {
                        ClientHandler.returnResponseOK(request, responseStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        server.listen();
    }
}