package pt.isec.pd.client.model.data;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.threads.CommunicationHandler;
import pt.isec.pd.shared_data.*;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class Client extends Thread {
    private final CommunicationHandler ch;

    public Client(ServerAddress pingAddr, PropertyChangeSupport pcs) {
        ch = new CommunicationHandler(pingAddr,pcs);
        ch.start();
    }

    public void login(String userName,String password) {
        ch.writeToSocket(ClientAction.LOGIN,new Pair<>(userName,password));
    }

    public void register(String userName,String name,String password) {
        ch.writeToSocket(ClientAction.REGISTER,new Triple<>(userName,name,password));
    }

    public void edit(ClientAction action,String edit) {
        ch.writeToSocket(action,edit);
    }

    public void disconnect() {
        try {
            ch.writeToSocket(ClientAction.DISCONNECTED,null);
            //waits the response thread to shut down
            ch.getResponseHandler().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void viewSeatsAndPrices(int showId) {
        ch.getClientData().setShowId(showId);
        ch.writeToSocket(ClientAction.VIEW_SEATS_PRICES,showId);
    }

    public void resetShow() {
        ch.getClientData().setShowId(-1);
    }

    public void consultShows(ClientAction action,HashMap<String,String> filters) {
        ch.writeToSocket(action,filters);
    }

    public void insertShows(String filePath) {
        ch.writeToSocket(ClientAction.INSERT_SHOWS,filePath);
    }

    public void deleteShow(int idShow) {
        ch.writeToSocket(ClientAction.DELETE_SHOW,idShow);
    }

    public Type getType() {
        return ch.getClientData().isAdmin() ? Type.ADMIN : Type.NORMAl_MODE;
    }

    public void showVisible(int idShow) {
        ch.writeToSocket(ClientAction.VISIBLE_SHOW,idShow);
    }

    public void submitReservation(List<Seat> seats) {
        ch.writeToSocket(ClientAction.SUBMIT_RESERVATION,new Pair<>(seats.get(0).getShowId(),seats));
    }

    public void consultsPaymentsAwaiting() {
        ch.writeToSocket(ClientAction.CONSULT_PAYMENTS_AWAITING,null);
    }

    public Object getResponse() {
        return ch.getResponse();
    }
}
