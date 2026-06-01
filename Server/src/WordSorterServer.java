import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class WordSorterServer {
    private static final int DEFAULT_PORT = 10000;

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)) {

                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    String requestLine = in.readLine();
                    if (requestLine != null && requestLine.startsWith("POST")) {
                        String line;
                        int contentLength = 0;
                        while ((line = in.readLine()) != null && !line.isEmpty()) {
                            if (line.toLowerCase().startsWith("content-length:")) {
                                contentLength = Integer.parseInt(line.substring(16).trim());
                            }
                        }
                        char[] body = new char[contentLength];
                        in.read(body, 0, contentLength);
                        String textBlock = new String(body);

                        String sortedWords = processText(textBlock);

                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: text/plain; charset=utf-8");
                        out.println("Content-Length: " + sortedWords.getBytes("UTF-8").length);
                        out.println();
                        out.print(sortedWords);
                        out.flush();
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static String processText(String text) {
        String cleanedText = text.toLowerCase()
            .replaceAll("[^а-яёa-z\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
        Set<String> uniqueWords = new TreeSet<>(Arrays.asList(
            cleanedText.split("\\s+")
        ));
        return uniqueWords.stream()
            .filter(word -> !word.isEmpty())
            .collect(Collectors.joining("\n"));
    }
}
