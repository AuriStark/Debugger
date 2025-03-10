package scraper.plugins.core.debugger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import scraper.plugins.core.debugger.Utils.Request;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked") // API conventions
public class DebuggerWebsocketServer extends WebSocketServer {
    private static final System.Logger l = System.getLogger("Debugger");

    WebSocket debugger = null;
    final ReentrantLock lock = new ReentrantLock();
    final DebuggerHookAddon actions;

    ObjectMapper m = new ObjectMapper();

    public DebuggerWebsocketServer(DebuggerHookAddon actions, int port, String bindingIp) {
        super(new InetSocketAddress(bindingIp, port));
        this.actions = actions;
        this.setReuseAddr(true);
        this.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                l.log(System.Logger.Level.WARNING, "Shutting down system");
                this.stop();
                l.log(System.Logger.Level.WARNING, "Graceful shutdown");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    // returns a socket, if a client is connected
    public Optional<WebSocket> get() {
        return Optional.ofNullable(debugger);
    }

    // only a single debugger allowed to be connected at the same time
    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        ServerHandshakeBuilder builder = super.onWebsocketHandshakeReceivedAsServer( conn, draft, request );
        try {
            lock.lock();
            if(debugger != null) throw new InvalidDataException(409, "A debugger has already connected");
        } finally { lock.unlock(); }

        return builder;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        try {
            lock.lock();
            debugger = conn;
        } finally { lock.unlock(); }
        l.log(System.Logger.Level.INFO, "Debugger connected");
        debugger.send(wrap("ready", Map.of(
                "value", (DebuggerHookAddon.state.getReady())
        )));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        try {
            lock.lock();
            debugger = null;
        } finally {
            lock.unlock();
        }
        l.log(System.Logger.Level.WARNING, "Debugger disconnected");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        l.log(System.Logger.Level.ERROR, "Web Socket error", ex);
        /**
         * Put the debugger on a unready state
         * Will force the process to wait until the debugger reconnect
         * and send a ready signal
         * */
        DebuggerHookAddon.state.setReady(false);
    }


    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            // Cast the input Message from the WebSocket as a Request Object
            Request request = m.readValue(message, Request.class);

            String cmd = request.command;
            Map<String, Object> data = (Map<String, Object>) request.data;

            Method cmdMethod = actions.getClass().getMethod(cmd, Map.class);
            cmdMethod.invoke(actions, data);
        } catch (Exception e) {
            e.getCause().printStackTrace();
            l.log(System.Logger.Level.ERROR, "Not a valid command: " + message.substring(0,Math.min(message.length(),100)) + e);
        }
    }

    private String wrap(String type, Object data) {
        try {
            return m.writeValueAsString(Map.of("type", type, "data", data));
        } catch (JsonProcessingException e) {
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public void onStart() {

    }

}
