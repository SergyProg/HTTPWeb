package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    final static List<String> validMethods = List.of("GET", "POST");
    private final static Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    public final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/default-get.html");
    public static final String RESOURCE_DIR = "public";
    private static ServerSocket serverSocket = null;
    public static final int DEFAULT_PORT = 23445; //9999;

    private static ExecutorService executorService = Executors.newFixedThreadPool(64);

    Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер стартовал, порт: "
                    + serverSocket.getLocalPort() + "\n");
        } catch (IOException ex) {
            System.out.println("Ошибка при старте сервера, порт: " + port);
            ex.printStackTrace();
        }
    }

    public void listen() {
        Socket clientSocket = null;
        try {
            while (true) {
                clientSocket = serverSocket.accept();
                executorService.execute(new ClientHandler(clientSocket, this));
            }
        } catch (IOException ex) {
            System.out.println("Ошибка при установке соединения с сервером.");
            ex.printStackTrace(System.out);
        } finally {
            executorService.shutdown();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (handlers.containsKey(method)) {
            handlers.get(method).put(path, handler);
        } else {
            handlers.put(method, new ConcurrentHashMap<>(Map.of(path, handler)));
        }
    }

    public static Map<String, Map<String, Handler>> getHandlers() {
        return handlers;
    }
}
