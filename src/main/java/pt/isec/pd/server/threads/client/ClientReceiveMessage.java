package pt.isec.pd.server.threads.client;

import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.server.data.Server;
import pt.isec.pd.server.data.database.DataBaseHandler;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.text.ParseException;

public class ClientReceiveMessage extends Thread {
    private final Log LOG = Log.getLogger(Server.class);
    private Socket socket;
    private DataBaseHandler dbHandler;
    private Integer numConnections;
    private boolean requestAccepted = false;
    ClientManagement clientManagement;

    public ClientReceiveMessage(Socket socket, DataBaseHandler dbHandler,Integer numConnections,ClientManagement clientManagement) {
        this.socket = socket;
        this.dbHandler = dbHandler;
        this.numConnections = numConnections;
        this.clientManagement = clientManagement;
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

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
                    case VIEW_SEATS_PRICES -> {
                        clientManagement.addClientViewingSeats(socket);
                        dbHandler.viewSeatsAndPrices(clientData,oos,ois);
                    }
                    case STOPPED_VIEWING_SEATS -> clientManagement.isViewingSeats(socket);
                    case SUBMIT_RESERVATION -> requestAccepted = dbHandler.submitReservation(clientData,oos,ois);
                    case DELETE_UNPAID_RESERVATION -> dbHandler.deleteUnpaidReservation(clientData,oos,ois);
                    case PAY_RESERVATION -> dbHandler.payReservation(clientData,oos,ois);
                    case INSERT_SHOWS -> dbHandler.insertShows(clientData,oos,ois);
                    case DELETE_SHOW -> dbHandler.deleteShow(clientData,oos,ois);
                    case DISCONNECTED -> {
                        dbHandler.disconnect(clientData,oos,ois);
                        synchronized (numConnections){
                            numConnections--;
                        }
                    }
                    default -> throw new IllegalArgumentException("Unexpected action value");
                }

                if (requestAccepted)
                    clientManagement.notifyClients();
            } catch (ClassNotFoundException | SQLException e) {
                LOG.log("Unable to read client data: " + e);
            } catch (ParseException | IOException e) {
                LOG.log("Client left");
                synchronized (numConnections){
                    numConnections--;
                }
               break;
            }
        }
    }
}
