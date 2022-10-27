package pt.isec.pa.Client.model;

import pt.isec.pa.Client.model.data.ServerAddress;
import pt.isec.pa.Client.model.fsm.Context;
import pt.isec.pa.Client.model.fsm.State;

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

    public void login() {
        context.login();
    }

    public void register() {
        context.register();
    }

    public void edit() {
        context.edit();
    }
}
