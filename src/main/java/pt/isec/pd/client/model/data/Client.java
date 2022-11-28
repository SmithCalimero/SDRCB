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
    private Type type;
    private final CommunicationHandler ch;
    private UpdateSeatsView updateSeatsView;
    private PropertyChangeSupport pcs;

    public Client(ServerAddress pingAddr, PropertyChangeSupport pcs) {
        this.pcs = pcs;
        ch = new CommunicationHandler(pingAddr,pcs);
        ch.start();
    }

    public void login(String userName,String password) {
        try {
            ch.writeToSocket(ClientAction.LOGIN,new Pair<>(userName,password));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(String userName,String name,String password) {
        try {
            ch.writeToSocket(ClientAction.REGISTER,new Triple<>(userName,name,password));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void edit(ClientAction action,String edit) {
        try {
            ch.writeToSocket(action,edit);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            ch.writeToSocket(ClientAction.DISCONNECTED,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void viewSeatsAndPrices(int showId) {
        updateSeatsView = new UpdateSeatsView(this,ch,showId,pcs);
        updateSeatsView.start();
    }

    public synchronized List<Seat> getSeatsAndPrices() { return updateSeatsView.getSeatsList(); }

    public List<Show> consultShows(ClientAction action,HashMap<String,String> filters) {
        try {
            ch.writeToSocket(action,filters);
            return (List<Show>) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String insertShows(String filePath) {
        try {
            ch.writeToSocket(ClientAction.INSERT_SHOWS,filePath);
            return (String) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Pair<Boolean,String> deleteShow(int idShow) {
        try {
            ch.writeToSocket(ClientAction.DELETE_SHOW,idShow);
            return (Pair<Boolean,String>) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Type getType() {
        return ch.getClientData().isAdmin() ? Type.ADMIN : Type.NORMAl_MODE;
    }

    public void notifyServer() {
         updateSeatsView.close();
    }

    public String showVisible(int idShow) {
        try {
            ch.writeToSocket(ClientAction.VISIBLE_SHOW,idShow);
            return (String) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void submitReservation(List<Seat> seats) {
        try {
            ch.writeToSocket(ClientAction.SUBMIT_RESERVATION,new Pair<>(seats.get(0).getShowId(),seats));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Reserve> consultsPaymentsAwaiting() {
        try {
            ch.writeToSocket(ClientAction.CONSULT_PAYMENTS_AWAITING,null);
            return (List<Reserve>) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getResponse() {
        return ch.getResponse();
    }
}
