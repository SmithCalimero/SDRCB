package pt.isec.pd.client.model;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.shared_data.Show;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.List;

public class ModelManager {
    public static final String PROP_STATE = "state";
    public static final String PROP_DATA  = "data";
    Context context;
    PropertyChangeSupport pcs;


    public ModelManager(ServerAddress udpConn) {
        pcs = new PropertyChangeSupport(this);
        this.context = new Context(udpConn,pcs);
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    public State getState() {
        return context.getState();
    }

    public void login(String userName, String password) {
        context.login(userName,password);
    }

    public void register(String userName,String name,String password){
        context.register(userName,name,password);
    }
    public void consultShows(HashMap<String,String> filters) {
        context.consultShows(filters);
    }
    public List<Seat> getSeatsAndPrices() {
        return context.getSeatsAndPrices();
    }
    public void edit(ClientAction action, String edit) {
        context.edit(action,edit);
    }

    public void disconnect() {
        context.disconnect();
    }

    //Change States
    public void next() {
        context.next();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void previous() {
        context.previous();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void swapToRegister() {
        context.swapToRegister();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void editTransition() {
        context.editTransition();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void showsTransition() {
        context.showsTransition();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void seatsTransition(Integer idShow) {
        context.seatsTransition(idShow);
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void insertShowsTransition() {
        context.insertShowsTransition();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void insertShows(String filePath) {
        context.insertShows(filePath);
    }

    public void deleteShow(int idShow) {
        context.deleteShow(idShow);
    }

    public void showVisible(int idShow) {
        context.showVisible(idShow);
    }

    public void selectShowsTransition() {
        context.selectShowsTransition();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void submitReservation(List<Seat> seats) {
        context.submitReservation(seats);
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void consultsPaymentsAwaitingTransition() {
        context.consultsPaymentsAwaitingTransition();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void consultsPaymentsAwaiting() {
        context.consultsPaymentsAwaiting();
    }

    public void payReservationTransition(int resId) {
        context.payReservationTransition(resId);
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public Object getResponse() {
        return context.getResponse();
    }
}
