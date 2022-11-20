package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.shared_data.Show;

import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;

public class Context {
    Client data;
    IState state;

    public Context(ServerAddress udpConn,PropertyChangeSupport pcs) {
        data = new Client(udpConn,pcs);
        state = State.LOGIN.createState(this,data);
    }

    void changeState(IState state) {
        this.state = state;
    }

    public State getState() {
        return state.getState();
    }

    public void next() {
        state.next();
    }

    public Pair<Boolean,String> login(String userName, String password) {
        return state.login(userName,password);
    }

    public String register(String userName,String name,String password) {
        return state.register(userName,name,password);
    }

    public void edit(ClientAction action, String edit) {
        state.edit(action,edit);
    }

    public void previous() {
        state.previous();
    }

    public void disconnect() {
        data.disconnect();
    }

    public void swapToRegister() {state.registerTransition();}

    public void editTransition() {
        state.editTransition();
    }

    public void showsTransition() {
        state.showsTransition();
    }

    public List<Show> consultShows(HashMap<String,String> filters) {
        return state.consultShows(filters);
    }

    public List<Seat> getSeatsAndPrices() {
        return state.getSeatsAndPrices();
    }

    public void seatsTransition(Integer idShow) {
        state.seatsTransition(idShow);
    }

    public void insertShowsTransition() {
        state.insertShowsTransition();
    }

    public String insertShows(String filePath) {
        return state.insertShows(filePath);
    }

    public Pair<Boolean,String> deleteShow(int idShow) {
        return state.deleteShow(idShow);
    }

    public String showVisible(int idShow) {
        return state.showVisible(idShow);
    }
}
