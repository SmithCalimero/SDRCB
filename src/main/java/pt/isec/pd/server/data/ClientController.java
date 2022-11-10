package pt.isec.pd.server.data;

import pt.isec.pd.server.threads.ClientPingHandler;

import java.io.IOException;
import java.net.ServerSocket;

public class ClientController {
    //TODO: Finish class 
    private ServerSocket serverSocket;
    private final ClientPingHandler pingHandler;
    private int numConnections;

    public ClientController(int pingPort) {
        pingHandler = new ClientPingHandler(pingPort);

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        pingHandler.start();
    }

    public int getServerPort() { return serverSocket.getLocalPort(); }

}
