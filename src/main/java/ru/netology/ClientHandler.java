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

    private String readHeader() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String ln = null;
        while (true) {
            try {
                ln = reader.readLine();
            } catch (IOException ex) {
                break;
            }
            if (ln == null || ln.isEmpty()) {
                break;
            }
            builder.append(ln + System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    private String getURIFromHeader(String header) {
        int from = header.indexOf(" ") + 1;
        int to = header.indexOf(" ", from);
        String uri = header.substring(from, to);
        int paramIndex = uri.indexOf("?");
        if (paramIndex != -1) {
            uri = uri.substring(0, paramIndex);
        }
        return uri;
    }

    private String getHeaderForAnswer(String kod, String contentType, String length) {
        return responseHeaderTemplate.replaceAll("&responseKod", kod)
                .replaceAll("&responseContentType", contentType)
                .replaceAll("&responseLength", length);
    }

    @Override
    public void run() {
        try {
            BufferedOutputStream outBuffer = new BufferedOutputStream(outputStream);
            String header = readHeader();
            if (!header.isEmpty()) {
                System.out.println(header);
                final String path = getURIFromHeader(header);
                if (!server.validPaths.contains(path)) {
                    outBuffer.write(getHeaderForAnswer(KOD_404_NOT_FOUND, "", "0").getBytes());
                    outBuffer.flush();
                    return;
                }

                final Path filePath = Path.of(server.RESOURCE_DIR + path);
                final String mimeType = Files.probeContentType(filePath);

                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    outBuffer.write(getHeaderForAnswer(KOD_200_OK, mimeType, Integer.toString(content.length)).getBytes());
                    outBuffer.write(content);
                    outBuffer.flush();
                    return;
                }

                final Long length = Files.size(filePath);
                outBuffer.write(getHeaderForAnswer(KOD_200_OK, mimeType, Long.toString(length)).getBytes());
                Files.copy(filePath, outBuffer);
//                Files.copy(filePath, System.out);
                outBuffer.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
