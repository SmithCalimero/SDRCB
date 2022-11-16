package pt.isec.pd.server.threads.client;

import pt.isec.pd.server.data.HeartBeatList;
import pt.isec.pd.server.data.Server;
import pt.isec.pd.server.data.database.DataBaseHandler;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientManagement extends Thread {
    private final Log LOG = Log.getLogger(Server.class);
    private ServerSocket serverSocket;
    private ClientPingHandler pingHandler;
    private boolean isConnected = true;             // validate if the user is connected
    private DataBaseHandler dbHandler;
    private Integer numConnections = 0;
    private List<ClientReceiveMessage> clientsThread = new ArrayList<>();
    private ArrayList<ClientReceiveMessage> viewingSeats = new ArrayList<>();

    public ClientManagement(int pingPort, DataBaseHandler dataBaseHandler, HeartBeatList hbList) {
        try {
            this.serverSocket = new ServerSocket(0);
            this.pingHandler = new ClientPingHandler(pingPort, hbList);
            this.dbHandler = dataBaseHandler;
            this.clientsThread = new ArrayList<>();
            this.viewingSeats = new ArrayList<>();
            hbList.add(new HeartBeatEvent(serverSocket.getLocalPort(), true, dbHandler.getCurrentVersion(), 0));
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
                incConnection();
                LOG.log("New connection established: " + numConnections);

                // Creates a thread for that client
                ClientReceiveMessage clientRM = new ClientReceiveMessage(clientSocket, dbHandler, this);
                clientRM.start();

                clientsThread.add(clientRM);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConnected() {
        isConnected = false;
    }

    public void startPingHandler() {
        pingHandler.start();
    }

    public int getServerPort() {
        return serverSocket.getLocalPort();
    }

    public int getNumConnections() {
        return numConnections;
    }

    // =========================== WORK IN PROGRESS ===========================
    // Only adds the clients with the current action = VIEW_SEATS_PRICES
    public void addClientViewingSeats(ClientReceiveMessage client) {
        viewingSeats.add(client);
    }

    // If the client is already executing another action, he is removed from the list
    public void isViewingSeats(ClientReceiveMessage client) {
        // If a client from the list executes another action, he gets removed from the list and they
        // get notified again
        viewingSeats.remove(client);

        ObjectOutputStream oos = client.getOos();
        try {
            oos.writeObject(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        notifyClients();
    }

    // Notifies clients to resend the action, so they can get the new list
    public void notifyClients() {
        for (var v : viewingSeats) {
            try {
                ObjectOutputStream oos = v.getOos();
                oos.writeObject(true);
            } catch (IOException e) {
                LOG.log("Unable to warn client");
            }
        }
    }

    public synchronized void incConnection() {
        numConnections++;
    }

    public synchronized void subConnection() {
        numConnections--;
    }
}
