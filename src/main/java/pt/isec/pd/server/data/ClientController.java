package pt.isec.pd.server.data;

import java.io.IOException;
import java.net.ServerSocket;

public class ClientController {
    //TODO: Finish class 
    private final ServerSocket serverSocket;
    private int numConnections;

    public ClientController() {
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getServerPort() {
        return serverSocket.getLocalPort();
    }

}
