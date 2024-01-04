package no.kristiania.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);

        Socket clientSocket = serverSocket.accept();


        String requestLine = HttpClient.readLine(clientSocket);

        System.out.println(requestLine);

        String headerLine;
        while (!(headerLine = HttpClient.readLine(clientSocket)).isBlank()) {
            System.out.println(headerLine);
        }

        String messageBody = "Hello World";

        String responseMessage =
                "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " +  messageBody.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                messageBody;
        clientSocket.getOutputStream().write(responseMessage.getBytes());
    }
}
