import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening at port: " +port);

        while(true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> {
                try {
                    handleClient(clientSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            })
            .start();
        }
    }

    public static void handleClient(Socket clientSocket) throws IOException {
        try(
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        ) {
            String requestLine = in.readLine();
            System.out.println("Request: " +requestLine);

            if(requestLine == null || requestLine.isEmpty()) return;

            String[] parts = requestLine.split(" ");
            if(parts.length < 2) return;

            String method = parts[0];
            String rawPath = parts[1];
            String pathOnly = rawPath.split("\\?")[0];

            Map<String, String> queryPrams = parseQueryParams(rawPath);
            Map<String, String> headers = parseHeaders(in);

            String responseBody;
            String status = "HTTP/1.1 200 OK";
            String contentType = "text/plain";

            if(method.equals("POST") && pathOnly.equals("echo")) {
                int contentLength = headers.get("Content-Length") != null ? Integer.parseInt(headers.get("Content-Length")) : 0;
                String requestBody = parseRequestBody(in, contentLength);
                responseBody = "You Posted: " + requestBody;
            } else {
                switch (pathOnly) {
                    case "/":
                        responseBody = "Welcome to my Simple HTTP Server!";
                        break;
                    case "/hello":
                        String name = queryPrams.getOrDefault("name", "Stranger");
                        responseBody = "Hello " + name;
                        break;
                    case "/agent":
                        String userAgent = headers.getOrDefault("User-Agent", "unknown");
                        responseBody = "Your user agent is: " + userAgent;
                        break;
                    case "/json":
                        responseBody = "{\"message\":\"Hello, JSON!\",\"time\":\"" + LocalDateTime.now() + "\"}";
                        contentType = "application/json";
                        break;
                    default:
                        responseBody = "404 NOT FOUND";
                        status = "HTTP/1.1 404 NOT FOUND";
                }
            }

            String httpResponse = "Http/1.1 " + status + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +
                    "\r\n" + responseBody;

            out.write(httpResponse);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }
    }

    private static String parseRequestBody(BufferedReader in, int contentLength) throws IOException {
        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        return new String(body);
    }

    private static Map<String, String> parseHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(":");
            if(idx != -1){
                String headerName = line.substring(0, idx).trim();
                String headerValue = line.substring(idx + 1).trim();
                headers.put(headerName, headerValue);
            }
        }

        return headers;
    }

    private static Map<String, String> parseQueryParams(String rawPath) {
        try {
            Map<String, String> map = new HashMap<>();
            if(rawPath.contains("?")) {
                String queryString = rawPath.split("\\?", 2)[1];
                for(String param: queryString.split("&")) {
                    String[] kv = param.split("=");
                    if(kv.length == 2) {
                        map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                    }
                }
            }
            return map;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }
}
