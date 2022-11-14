package pt.isec.pd.server.threads.client;

import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.server.data.HeartBeatList;
import pt.isec.pd.server.data.Server;
import pt.isec.pd.server.data.database.DataBaseHandler;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ClientManagement extends Thread {
    private final Log LOG = Log.getLogger(Server.class);
    private ServerSocket serverSocket;
    private ClientPingHandler pingHandler;
    private boolean isConnected = true;             // validate if the user is connected
    private DataBaseHandler dbHandler;
    private Integer numConnections = 0;
    private List<Thread> clientsThread = new ArrayList<>();

    public ClientManagement(int pingPort, DataBaseHandler dataBaseHandler,HeartBeatList hbList) {
        try {
            this.serverSocket = new ServerSocket(0);
            this.pingHandler = new ClientPingHandler(pingPort,hbList);
            this.dbHandler = dataBaseHandler;
            hbList.add(new HeartBeatEvent(serverSocket.getLocalPort(),true,dbHandler.getCurrentVersion(),0));
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    //Handles the connection of the clients and instantiates a new Thread for the client
    @Override
    public void run() {
        try {
            while (isConnected) {
                Socket clientSocket = serverSocket.accept();
                synchronized (numConnections){
                    numConnections++;
                }
                LOG.log("New connection established: " + numConnections);

                // Creates a thread for that client
                ClientReceiveMessage clientRM = new ClientReceiveMessage(clientSocket,dbHandler,numConnections);
                clientRM.start();

                clientsThread.add(clientRM);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConnected() { isConnected = false; }

    public void startPingHandler() {
        pingHandler.start();
    }

    public int getServerPort() { return serverSocket.getLocalPort(); }

    public int getNumConnections() { return numConnections; }
}
