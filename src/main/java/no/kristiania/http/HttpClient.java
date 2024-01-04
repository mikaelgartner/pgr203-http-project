package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    private final int statusCode;
    private final Map<String, String> headerFields = new HashMap<>();
    private String messageBody;

    public HttpClient(String host, int port, String requestTarget) throws IOException {
        Socket socket = new Socket(host, port);

        //Her bygger vi opp en request som er kommandoen fra client til server
        String request = "GET " + requestTarget + " HTTP/1.1\r\n" +
                "Host: " + host + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        socket.getOutputStream().write(request.getBytes()); //Vi sender requesten til serveren

        String[] statusLine = readLine(socket).split(" ");  //Leser en linje tilbake fra server og splitter på mellomrom
        this.statusCode = Integer.parseInt(statusLine[1]);  //Tar det som står mellom første og andre mellomrom, tolker det som et tall

        String headerLine;
        while (!(headerLine = readLine(socket)).isBlank()) {
            int colonPos = headerLine.indexOf(':');
            String headerField = headerLine.substring(0, colonPos);
            String headerValue = headerLine.substring(colonPos+1).trim();
            headerFields.put(headerField, headerValue);
        }

        this.messageBody = readBytes(socket, getContentLength());
    }

    private String readBytes(Socket socket, int contentLength) throws IOException {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < contentLength; i++) {
            buffer.append((char)socket.getInputStream().read());

        }

        return buffer.toString();
    }

    //For å lese en linje bruker vi en "buffer", en midlertidig struktur hvor jeg bygger opp linja underveis
    static String readLine(Socket socket) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != '\r') {  //Leser ett og ett tegn fra socketen inntil vi kommer til \r
            buffer.append((char)c);
        }
        int expectedNewline = socket.getInputStream().read();
        assert expectedNewline == '\n';
        return buffer.toString();   //Returner så den verdien
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String headerName) {
        return headerFields.get(headerName);
    }

    public int getContentLength() {
        return Integer.parseInt(getHeader("Content-Length"));
    }

    public String getMessageBody() {
        return messageBody;
    }
}
