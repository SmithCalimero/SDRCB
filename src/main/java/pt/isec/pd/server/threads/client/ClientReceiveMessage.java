package pt.isec.pd.server.threads.client;

import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.server.data.HeartBeatController;
import pt.isec.pd.server.data.Server;
import pt.isec.pd.server.data.database.DBHandler;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientReceiveMessage extends Thread {
    private final Log LOG = Log.getLogger(Server.class);
    private final HeartBeatController controller;
    private final DBHandler dbHandler;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private boolean requestAccepted = false;
    private final ClientManagement clientManagement;
    private final List<ClientData> queue = new ArrayList<>();
    private final QueueUpdate queueUpdate;

    public ClientReceiveMessage(Socket socket, DBHandler dbHandler, ClientManagement clientManagement, HeartBeatController controller) {
        this.controller = controller;
        this.dbHandler = dbHandler;
        this.clientManagement = clientManagement;
        queueUpdate = new QueueUpdate(controller,queue,this);
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        queueUpdate.start();
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                // Verifications for the clients actions
                ClientData clientData = (ClientData) ois.readObject();
                if (!controller.isUpdating() && queue.isEmpty()) {
                    handleClientRequest(clientData);
                } else {
                    queue.add(clientData);
                    LOG.log("The client requested a request but the server is currently updating");
                    LOG.log("The request was added to the queue!");
                }
            } catch (ClassNotFoundException e) {
                LOG.log("Unable to read client data: " + e);
            } catch (IOException e) {
                LOG.log("Client left");
                clientManagement.subConnection();
               break;
            }
        }
    }

    public void handleClientRequest(ClientData clientData) {
        LOG.log("Execution " + clientData.getAction());
        try {
            String sqlCommand = switch(clientData.getAction()) {
                case REGISTER -> dbHandler.register(clientData,oos,ois);
                case LOGIN -> dbHandler.login(clientData,oos,ois);
                case EDIT_NAME,EDIT_USERNAME,EDIT_PASSWORD -> dbHandler.editClientData(clientData,oos,ois);
                case CONSULT_PAYMENTS_AWAITING -> dbHandler.consultPaymentsAwaiting(clientData,oos,ois);
                case CONSULT_PAYED_RESERVATIONS -> dbHandler.consultPayedReservations(clientData,oos,ois);
                case CONSULT_SHOWS_VISIBLE -> dbHandler.consultShows(clientData,oos,ois);
                case CONSULT_SHOWS_ALL -> dbHandler.consultShowsAdmin(clientData,oos,ois);
                case SELECT_SHOWS -> dbHandler.selectShows(clientData,oos,ois);
                case VIEW_SEATS_PRICES -> {
                    clientManagement.addClientViewingSeats(this);
                    yield dbHandler.viewSeatsAndPrices(clientData,oos,ois);
                }
                case STOPPED_VIEWING_SEATS -> clientManagement.isViewingSeats(this);
                case VISIBLE_SHOW -> dbHandler.showVisible(clientData,oos,ois);
                case SUBMIT_RESERVATION -> {
                    requestAccepted = dbHandler.submitReservation(clientData,oos,ois);
                    yield "";
                }
                case DELETE_UNPAID_RESERVATION -> dbHandler.deleteUnpaidReservation(clientData,oos,ois);
                case PAY_RESERVATION -> dbHandler.payReservation(clientData,oos,ois);
                case INSERT_SHOWS -> dbHandler.insertShows(clientData,oos,ois);
                case DELETE_SHOW -> dbHandler.deleteShow(clientData,oos,ois);
                case DISCONNECTED -> dbHandler.disconnect(clientData,oos,ois);

                default -> throw new IllegalArgumentException("Unexpected action value");
            };

            if (requestAccepted) {
                clientManagement.notifyClients();
                requestAccepted = false;
            }

            //If db was updated, init the process of updating other servers db
            if (!sqlCommand.isEmpty()) {
                controller.updateDataBase(sqlCommand);
            }
        }  catch (ClassNotFoundException | SQLException | IOException e) {
            LOG.log("Unable to read client data: " + e);
        }
    }

    public synchronized ObjectOutputStream getOos() {
        return oos;
    }

    public synchronized ObjectInputStream getOis() {
        return ois;
    }
}
