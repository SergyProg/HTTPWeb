package ru.netology;

public class Main {
    public static void main(String[] args) {
        final int DEFAULT_PORT = 23445; //9999;

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        new Thread(new Server(port)).start();
    }
}
