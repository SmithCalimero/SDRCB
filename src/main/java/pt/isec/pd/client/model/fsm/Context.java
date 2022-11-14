package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.shared_data.ServerAddress;

public class Context {
    Client data;
    IState state;

    public Context(ServerAddress udpConn) {
        data = new Client(udpConn);
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

    public void register(String userName,String name,String password) {
        state.register(userName,name,password);
    }

    public void edit(ClientAction action, String edit) {
        state.edit(action,edit);
    }

    public void previous() {
        state.previous();
    }
    public void disconnect() {
        state.disconnect();
    }

    public void swapToRegister() {state.swapToRegister();}

}
