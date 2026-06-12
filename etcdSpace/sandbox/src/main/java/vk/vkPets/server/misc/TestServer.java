package vk.vkPets.server.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    static final int TEST_PORT = 7777;

    private static final Logger serverLogger = LoggerFactory.getLogger("RMI-server-" + TestClient.getHostAddress());


    public static void openServerSocket() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(TEST_PORT)) {
                serverLogger.info("Server listening on port " + TEST_PORT);

                while (true) {
                    Socket clientSocket = server.accept();
                    serverLogger.info("Client {} connected", clientSocket.getInetAddress());
                    new Thread(new ClientHandler(clientSocket)).start();
                }
            } catch (Exception e) {
                serverLogger.info("Server stopped");
            }
        }).start();
    }


    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final String clientAddress;

        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            clientAddress = clientSocket.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            try (
//                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true))
            {
                out.println("Pong from " + InetAddress.getLocalHost().getHostAddress());
//                String line;
//                while ((line = in.readLine()) != null) {
//                    logger.info("Client says: {}", line);
//
//                    if ("bye".equalsIgnoreCase(line)) {
//                        out.println("Goodbye!");
//                        break;
//                    }
//
//                    out.println("Echo: " + line);
//                }
            } catch (IOException e) {
//                serverLogger.info("Client disconnected");
            } finally {
                serverLogger.info("Client socket {} closed", clientAddress);
                try { clientSocket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
