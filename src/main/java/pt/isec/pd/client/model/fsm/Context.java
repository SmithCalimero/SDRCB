package pt.isec.pd.client.model.fsm;

import pt.isec.pd.client.model.data.Client;
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

    public void login() {
        state.login();
    }

    public void register() {
        state.register();
    }

    public void edit() {
        state.edit();
    }

    public void previous() {
        state.previous();
    }

}
