package ru.netology;

public class Main {
    public static void main(String[] args) {
        int port = Server.DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new Thread(new Server(port)).start();
    }
}
