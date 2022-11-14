package pt.isec.pd.client.model;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.ServerAddress;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

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

    public void next() {
        context.next();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public void previous() {
        context.previous();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }

    public Pair<Boolean,String> login(String userName, String password) {
        return context.login(userName,password);
    }

    public void register(String userName,String name,String password){
        context.register(userName,name,password);
    }

    public void edit(ClientAction action, String edit) {
        context.edit(action,edit);
    }

    public void disconnect() {
        context.disconnect();
    }

    public void swapToRegister() {
        context.swapToRegister();
        pcs.firePropertyChange(PROP_STATE,null,context.getState());
    }
}
