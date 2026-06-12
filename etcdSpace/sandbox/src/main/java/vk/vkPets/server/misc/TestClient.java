package vk.vkPets.server.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.*;

public class TestClient {
    private static final int TEST_PORT = 7777;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Logger clientLogger =  LoggerFactory.getLogger("RMI-client-" + getHostAddress());

    public static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "???";
        }
    }

    public static void testCall(NodeData nodeData) {
        try {
            runWithTimeout(() -> testCallImpl(nodeData), 10);
        } catch (Exception e) {
            clientLogger.error(e.toString(), e);
        }
    }

    private static void testCallImpl(NodeData nodeData) {
        try (Socket socket = new Socket(nodeData.host(), TEST_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            clientLogger.info("Sending hello to {}", nodeData.host());
//            out.println("hello");
            String response = in.readLine();
            clientLogger.info("Server responded: {}", response);
        } catch (Exception e) {
            clientLogger.error(e.toString(), e);
        }
    }

    private static void runWithTimeout(Runnable task, long timeout) throws Exception {
        Future<?> future = executor.submit(task);
        try {
            future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // interrupt the stuck method
            throw new TimeoutException("Method timed out");
        }
    }
}
