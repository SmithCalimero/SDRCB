package pt.isec.pd.server.threads.client;

import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.server.data.database.DataBaseHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.text.ParseException;

public class ClientReceiveMessage extends Thread {
    private Socket socket;
    private DataBaseHandler dbHandler;
    private Integer numConnections;

    public ClientReceiveMessage(Socket socket, DataBaseHandler dbHandler,Integer numConnections) {
        this.socket = socket;
        this.dbHandler = dbHandler;
        this.numConnections = numConnections;
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
                    case VIEW_SEATS_PRICES -> dbHandler.viewSeatsAndPrices(clientData,oos,ois);
                    case SUBMIT_RESERVATION -> dbHandler.submitReservation(clientData,oos,ois);
                    case DELETE_UNPAID_RESERVATION -> dbHandler.deleteUnpaidReservation(clientData,oos,ois);
                    case PAY_RESERVATION -> dbHandler.payReservation(clientData,oos,ois);
                    case INSERT_SHOWS -> dbHandler.insertShows(clientData,oos,ois);
                    case DELETE_SHOW -> dbHandler.deleteShow(clientData,oos,ois);
                    case DISCONNECTED -> { dbHandler.disconnect(clientData,oos,ois); numConnections--; }
                    default -> throw new IllegalArgumentException("Unexpected action value");
                }
            } catch (ClassNotFoundException | SQLException e) {
                System.out.println("Unable to read client data: " + e);
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
