package pt.isec.pd.server.threads;

import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.server.data.database.DataBaseHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.ParseException;

public class ClientManagement extends Thread {
    private ServerSocket serverSocket;
    private ClientPingHandler pingHandler;
    private boolean isConnected = true;             // validate if the user is connected
    private DataBaseHandler dbHandler;
    private int numConnections = 0;

    public ClientManagement(int pingPort, DataBaseHandler dataBaseHandler) {
        try {
            this.serverSocket = new ServerSocket(pingPort);
            this.pingHandler = new ClientPingHandler(pingPort);
            this.dbHandler = dataBaseHandler;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Keeps receiving info from clients
            while (isConnected) {
                Socket clientSocket = serverSocket.accept();

                increaseConnections();

                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

                try {
                    // Verifications for the clients actions
                    ClientData clientData = (ClientData) ois.readObject();

                    switch(clientData.getAction()) {
                        case REGISTER -> dbHandler.register(clientData,oos,ois);
                        case LOGIN /*, LOGIN_ADM */ -> dbHandler.login(clientData,oos,ois);
                        case EDIT_NAME,EDIT_USERNAME,EDIT_PASSWORD -> dbHandler.editClientData(clientData,oos,ois);
                        case CONSULT_PAYMENTS_AWAITING -> dbHandler.consultPaymentsAwaiting(clientData,oos,ois);
                        case CONSULT_PAYED_RESERVATIONS -> dbHandler.consultPayedReservations(clientData,oos,ois);
                        case CONSULT_SHOWS -> dbHandler.consultShows(clientData,oos,ois);
                        case SELECT_SHOWS -> dbHandler.selectShows(clientData,oos,ois);
                        case VIEW_SEATS_PRICES -> dbHandler.viewSeatsAndPrices(clientData,oos,ois);
                        case SUBMIT_RESERVATION -> dbHandler.submitReservation(clientData,oos,ois);
                        case DELETE_UNPAID_RESERVATION -> dbHandler.deleteUnpaidReservation(clientData,oos,ois);
                        case PAY_RESERVATION -> dbHandler.payReservation(clientData,oos,ois);
                        case INSERT_SHOWS -> dbHandler.insertShows(clientData,oos,ois);
                        case DELETE_SHOW -> dbHandler.deleteShow(clientData,oos,ois);
                        case DISCONNECTED -> { dbHandler.disconnect(clientData,oos,ois); decreaseConnections(); }
                        default -> throw new IllegalArgumentException("Unexpected action value");
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    System.out.println("Unable to read client data: " + e);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            System.out.println("Unable to create OOS & OIS: " + e);
        }
    }

    public void setConnected() { isConnected = false; }

    private void decreaseConnections() { this.numConnections--; }

    private void increaseConnections() { this.numConnections++; }

    public void startPingHandler() {
        pingHandler.start();
    }

    public int getServerPort() { return serverSocket.getLocalPort(); }
}
