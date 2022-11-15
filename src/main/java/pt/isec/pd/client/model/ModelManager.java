package pt.isec.pd.client.model;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
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
        this.context = new Context(udpConn);
        pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    public State getState() {
        return context.getState();
    }

    public Pair<Boolean,String> login(String userName, String password) {
        return context.login(userName,password);
    }

    public String register(String userName,String name,String password){
        return context.register(userName,name,password);
    }

    public List<Show> consultShows(HashMap<String,String> filters) {
        return context.consultShows(filters);
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
}
