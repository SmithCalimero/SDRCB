package pt.isec.pd.client.model.data;

import javafx.util.Pair;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.shared_data.Show;
import pt.isec.pd.shared_data.Triple;

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

        ch = new CommunicationHandler(pingAddr);
        ch.start();
    }

    public Pair<Boolean,String> login(String userName,String password) {
        try {
            ch.writeToSocket(ClientAction.LOGIN,new Pair<>(userName,password));

            Integer id = (Integer) ch.readFromSocket();

            //<isLogin,isAdmin?,ErrorMessage?>
            Triple<Boolean,Boolean,String> response = (Triple<Boolean,Boolean,String>) ch.readFromSocket();

            //Assign the mode;
            if (response.getFirst()) {
                synchronized (ch.getClientData()) {
                    ch.getClientData().setId(id);
                }
                type = response.getSecond() ? Type.ADMIN : Type.NORMAl_MODE;
            }

            //<isLogin,ErrorMessage?>
            return new Pair<>(response.getFirst(),response.getThird());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String register(String userName,String name,String password) {
        try {
            ch.writeToSocket(ClientAction.REGISTER,new Triple<>(userName,name,password));
            return (String) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        return type;
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

    public boolean submitReservation(List<Seat> seats) {
        try {
            ch.writeToSocket(ClientAction.SUBMIT_RESERVATION,new Pair<>(seats.get(0).getShowId(),seats));
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
