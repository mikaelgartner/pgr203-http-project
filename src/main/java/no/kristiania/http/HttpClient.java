package no.kristiania.http;

import java.io.IOException;
import java.net.Socket;

public class HttpClient {
    private final int statusCode;

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
    }

    //For å lese en linje bruker vi en "buffer", en midlertidig struktur hvor jeg bygger opp linja underveis
    private String readLine(Socket socket) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int c;
        while ((c = socket.getInputStream().read()) != '\r') {  //Leser ett og ett tegn fra socketen inntil vi kommer til \r
            buffer.append((char)c);
        }

        return buffer.toString();   //Returner så den verdien
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeader(String headerName) {
        return null;
    }
}
