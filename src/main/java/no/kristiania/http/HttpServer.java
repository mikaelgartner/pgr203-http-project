package no.kristiania.http;

import no.kristiania.person.Person;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final ServerSocket serverSocket;
    private final HashMap<String, HttpController> controllers = new HashMap<>();
    private List<Person> people = new ArrayList<>();

    public HttpServer(int serverPort) throws IOException {
        serverSocket = new ServerSocket(serverPort);

        new Thread(this::handleClients).start();
    }

    private void handleClients() {
        try {
            while (true) {
                handleClient();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleClient() throws IOException, SQLException {
        Socket clientSocket = serverSocket.accept();

        HttpMessage httpMessage = new HttpMessage(clientSocket);
        String[] requestLine = httpMessage.startLine.split(" ");
        String requestTarget = requestLine[1];

        int questionPos = requestTarget.indexOf('?');
        String fileTarget;
        String query = null;
        if (questionPos != -1) {
            fileTarget = requestTarget.substring(0, questionPos);
            query = requestTarget.substring(questionPos+1);
        } else {
            fileTarget = requestTarget;
        }

        if (controllers.containsKey(fileTarget)) {
            HttpMessage response = controllers.get(fileTarget).handle(httpMessage);
            response.write(clientSocket);
            return;
        }


        if (fileTarget.equals("/hello")) {
            String yourName = "world";
            if (query != null) {
                Map<String, String> queryMap = HttpMessage.parseRequestParameters(query);
                yourName = queryMap.get("lastName") + ", " + queryMap.get("firstName");
            }
            String responseText = "<p>Hello " + yourName + "</p>";

            writeOkResponse(clientSocket, responseText, " text/html");
        } else if (fileTarget.equals("/api/newPerson")) {
            Map<String, String> queryMap = HttpMessage.parseRequestParameters(httpMessage.messageBody);
            Person person = new Person();
            person.setLastName(queryMap.get("lastName"));
            people.add(person);
            writeOkResponse(clientSocket, "it is done", "text/html");
        }  else {
            InputStream fileResource = getClass().getResourceAsStream(fileTarget);
            if (fileResource != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                fileResource.transferTo(buffer);
                String responseText = buffer.toString();

                String contentType = "text/plain";
                if (requestTarget.endsWith(".html")) {
                    contentType = "text/html";
                }
                writeOkResponse(clientSocket, responseText, contentType);
                return;
            }


            String responseText = "File not found: " + requestTarget;

            String response = "HTTP/1.1 404 Not found\r\n" +
                    "Content-Length: " + responseText.length() + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseText;
            clientSocket.getOutputStream().write(response.getBytes());
        }
    }

    private static void writeOkResponse(Socket clientSocket, String responseText, String contentType) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + responseText.length() + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" +
                responseText;
        clientSocket.getOutputStream().write(response.getBytes());
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(1962);
        createDataSource();
        logger.info("Starting http://localhost:{}/index.html", httpServer.getPort());
    }

    private static DataSource createDataSource() throws IOException {
        Properties properties = new Properties();
        try (FileReader reader = new FileReader("pgr203.properties")) {
            properties.load(reader);
        }

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(properties.getProperty(
                "dataSource.url",
                "jdbc:postgresql://localhost:5432/person_db"
        ));
        dataSource.setUser(properties.getProperty("dataSource.user","person_dbuser"));
        dataSource.setPassword(properties.getProperty("dataSource.password"));
        Flyway.configure().dataSource(dataSource).load().migrate();
        return dataSource;
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public List<Person> getPeople() {
        return people;
    }

    public void addController(String path, HttpController controller) {
        controllers.put(path, controller);
    }
}
