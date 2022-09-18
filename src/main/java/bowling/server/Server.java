package bowling.server;

import bowling.entities.Frame;
import bowling.entities.Game;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private HttpServer httpServer;
    private final ExecutorService executorService;
    private int freeGameId;
    private final Map<Integer, Game> gameCache;
    private static final Logger LOGGER = Logger.getLogger(Server.class.getCanonicalName());

    public static void main(String[] args) throws UnknownHostException {
        LOGGER.log(Level.INFO, "Started application");
        try (Scanner scanner = new Scanner(new File("server_info.txt"))) {
            Server server =
                    new Server(InetAddress.getByName(scanner.nextLine()), 10, Integer.parseInt(scanner.nextLine()));
            server.startServer();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    static {
        try {
            LOGGER.addHandler(new FileHandler("server_logs.txt"));
        } catch (IOException e) {
            String message = "Could not add File handler for processor logging";
            LOGGER.log(Level.INFO, message);
        }
    }

    public Server(InetAddress inetAddress, int threads, int port) {
        freeGameId = 0;
        executorService = Executors.newFixedThreadPool(threads);
        gameCache = new HashMap<>();
        try {
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(inetAddress, port), 0);
            httpServer.createContext("/shutdown", new ShutdownHandler());
            HttpContext context = httpServer.createContext("/bowling");
            context.setHandler(new BowlingHandler());
            httpServer.setExecutor(executorService);
            LOGGER.log(Level.INFO, "Created httpServer");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    public void startServer() {
        httpServer.start();
        LOGGER.log(Level.INFO, "startServer");
    }

    public void shutdown() {
        LOGGER.log(Level.INFO, "Starting server shutdown");
        httpServer.stop(0);
        if (executorService != null)
            executorService.shutdown();
        LOGGER.log(Level.INFO, "Finishing server shutdown");
    }

    private class ShutdownHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(204, -1);
            shutdown();
        }
    }

    private class BowlingHandler implements HttpHandler {
        private static final String HTML_OPENER = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8" lang="en">
                  <title>Bowling</title>
                </head>
                <body>
                <h1>Bowling score keeping</h1>
                                """;
        private static final String HTML_CLOSER = """
                </body>
                </html>
                """;
        private static final String HTML_FORM_OPENER = """
                                <form method="get" action="/bowling">
                                  <label for="score">Score on this throw:</label><br>
                                  <input type="text" id="score" name="score" value="0"><br>
                """;
        private static final String HTML_HIDDEN_INPUT_GAME_OPENER = "<input type=\"hidden\" id=\"gameId\" name=\"gameId\" value=\"";
        private static final String HTML_HIDDEN_INPUT_GAME_CLOSER = "\">";
        private static final String HTML_FORM_CLOSER = """
                  <input type="submit" value="Submit">
                </form>
                """;
        private static final String HTML_PARAGRAPH_OPENER = "<p>";
        private static final String HTML_PARAGRAPH_CLOSER = "</p>";
        private static final String HTML_H2_OPENER = "<h2>";
        private static final String HTML_H2_CLOSER = "</h2>";

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            LOGGER.log(Level.INFO, "Handling request to /bowling");
            var requestQuery = exchange.getRequestURI().getQuery();
            LOGGER.log(Level.INFO, requestQuery);
            if (requestQuery == null || requestQuery.isEmpty() || requestQuery.isBlank()) {
                gameCache.put(freeGameId, new Game());
                exchange.sendResponseHeaders(200, 0);
                var body = exchange.getResponseBody();
                body.write(HTML_OPENER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_FORM_OPENER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_HIDDEN_INPUT_GAME_OPENER.getBytes(StandardCharsets.UTF_8));
                body.write(Integer.toString(freeGameId++).getBytes(StandardCharsets.UTF_8));
                body.write(HTML_HIDDEN_INPUT_GAME_CLOSER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_FORM_CLOSER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_CLOSER.getBytes(StandardCharsets.UTF_8));
                body.flush();
                body.close();
                return;
            }
            var queryMap = formQueryMap(requestQuery);
            if (queryMap.containsKey("score") && queryMap.containsKey("gameId")) {
                LOGGER.log(Level.INFO, "Handling request to /bowling with score & game parameters");
                int gameId = Integer.parseInt(queryMap.get("gameId"));
                int scoreValue = Integer.parseInt(queryMap.get("score"));
                var game = gameCache.get(gameId);
                var resultOfScoring = game.score(scoreValue);
                var gameStringValue = gameToString(game);
                exchange.sendResponseHeaders(200, 0);
                var body = exchange.getResponseBody();
                body.write(HTML_OPENER.getBytes(StandardCharsets.UTF_8));
                if (!resultOfScoring) {
                    body.write(HTML_H2_OPENER.getBytes(StandardCharsets.UTF_8));
                    body.write("Unfortunately this score cannot be accounted for! The value was either illegal, or the game had already finished!"
                            .getBytes(StandardCharsets.UTF_8));
                    body.write(HTML_H2_CLOSER.getBytes(StandardCharsets.UTF_8));
                }
                body.write(gameStringValue.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_FORM_OPENER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_HIDDEN_INPUT_GAME_OPENER.getBytes(StandardCharsets.UTF_8));
                body.write(Integer.toString(gameId).getBytes(StandardCharsets.UTF_8));
                body.write(HTML_HIDDEN_INPUT_GAME_CLOSER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_FORM_CLOSER.getBytes(StandardCharsets.UTF_8));
                body.write(HTML_CLOSER.getBytes(StandardCharsets.UTF_8));
                body.flush();
                body.close();
                LOGGER.log(Level.INFO, "Finishing handling request to /bowling with score & game parameters");
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }

        private String gameToString(Game game) {
            LOGGER.log(Level.INFO, "Turning game to String");
            StringBuilder builder = new StringBuilder();
            int numberOfFrame = 0;
            for (Frame frame : game.getFrames()) {
                LOGGER.log(Level.INFO, "Turning game to String at frame " + numberOfFrame);
                if (frame != null) {
                    numberOfFrame++;
                    builder.append(HTML_H2_OPENER)
                            .append("Frame: ")
                            .append(numberOfFrame)
                            .append(HTML_H2_CLOSER);
                    if (frame.getFirstHit() == 10) {
                        builder.append(HTML_PARAGRAPH_OPENER);
                        builder.append("\\_/STRIKE\\_/ FRAME!!!");
                        builder.append(HTML_PARAGRAPH_CLOSER);
                    } else {
                        builder.append(HTML_PARAGRAPH_OPENER);
                        builder.append("First throw score: ").append(frame.getFirstHit());
                        builder.append(HTML_PARAGRAPH_CLOSER);
                        builder.append(HTML_PARAGRAPH_OPENER);
                        builder.append("Second throw score: ").append(frame.getSecondHit());
                        builder.append(HTML_PARAGRAPH_CLOSER);
                        if (frame.getFirstHit() + frame.getSecondHit() == 10) {
                            builder.append(HTML_PARAGRAPH_OPENER);
                            builder.append("_SPARE_ FRAME!!!");
                            builder.append(HTML_PARAGRAPH_CLOSER);
                        }
                    }
                    if (frame.isFinishedCountingScore()) {
                        builder.append(HTML_PARAGRAPH_OPENER);
                        builder.append("Final score on this frame: ").append(frame.getFinalScore());
                        builder.append(HTML_PARAGRAPH_CLOSER);
                    } else {
                        builder.append(HTML_PARAGRAPH_OPENER);
                        builder.append("Final score on this frame: ").append("TBD");
                        builder.append(HTML_PARAGRAPH_CLOSER);
                    }
                }
            }
            builder.append(HTML_H2_OPENER)
                    .append("Current total score: ")
                    .append(game.getGameScore())
                    .append(HTML_H2_CLOSER);
            if (game.isFinishedGame())
                builder.append(HTML_H2_OPENER)
                        .append("Game is finished!")
                        .append(HTML_H2_CLOSER);
            else
                builder.append(HTML_H2_OPENER)
                        .append("Further throws are possible!")
                        .append(HTML_H2_CLOSER);
            return builder.toString();
        }
    }

    private static Map<String, String> formQueryMap(String query) {
        var pairs = query.split("&");
        var map = new HashMap<String, String>();
        for (String pair : pairs) {
            var splitPair = pair.split("=");
            map.put(splitPair[0], splitPair[1]);
        }
        return map;
    }
}
