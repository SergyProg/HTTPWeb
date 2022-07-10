package ru.netology;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    private Server server = null;
    private Socket clientSocket = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static final String responseHeaderTemplate = "HTTP/1.1 &responseKod\r\n" +
            "Content-Type: &responseContentType\r\n" +
            "Content-Length: &responseLength\r\n" +
            "Connection: close\r\n" +
            "\r\n";
    private static final String KOD_404_NOT_FOUND = "404 Not Found";
    private static final String KOD_400_BAD_REQUEST = "400 Bad Request";
    private static final String KOD_200_OK = "200 OK";

    public ClientHandler(Socket socket, Server server) {
        try {
            this.server = server;
            this.clientSocket = socket;
            this.inputStream = clientSocket.getInputStream();
            this.outputStream = clientSocket.getOutputStream();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static String getHeaderForAnswer(String kod, String contentType, String length) {
        return responseHeaderTemplate.replaceAll("&responseKod", kod)
                .replaceAll("&responseContentType", contentType)
                .replaceAll("&responseLength", length);
    }

    static void returnResponseOK(Request request, BufferedOutputStream outBuffer) throws IOException {
        final Path filePath = Path.of(".", Server.RESOURCE_DIR, request.getUri());
        final String mimeType = Files.probeContentType(filePath);

        if(mimeType.contains("html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            outBuffer.write(getHeaderForAnswer(KOD_200_OK, mimeType, Integer.toString(content.length)).getBytes());
            outBuffer.write(content);
        }

        final Long length = Files.size(filePath);
        outBuffer.write(getHeaderForAnswer(KOD_200_OK, mimeType, Long.toString(length)).getBytes());
        Files.copy(filePath, outBuffer);
        outBuffer.flush();
    }

    static void returnResponseError(String errorCode, BufferedOutputStream outBuffer) throws IOException {
        outBuffer.write(getHeaderForAnswer(errorCode, "", "0").getBytes());
        outBuffer.flush();
    }

    @Override
    public void run() {
        try {
            BufferedOutputStream outBuffer = new BufferedOutputStream(outputStream);
            BufferedReader inBuffer = new BufferedReader(new InputStreamReader(inputStream));
            Request request = new Request(inBuffer);
            System.out.println(request.getQueryParam("login"));
            System.out.println(request.getQueryParams());
            if(request.isEmptyRequest()) {return;}
            if (request.isBadRequest()) {
                returnResponseError(KOD_400_BAD_REQUEST, outBuffer);
                return;
            }
            Handler handler = server.getHandlers().get(request.getMethod())
                    .get(request.getUri());
            if (handler == null) {
                returnResponseError(KOD_404_NOT_FOUND, outBuffer);
                return;
            }

            handler.handle(request, outBuffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
