package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.client.model.data.Client;

public class Login extends StateAdapter {
    public Login(Context context, Client data) {
        super(context,data);
    }

    @Override
    public void next() {
        changeState(State.REGISTER);
    }

    @Override
    public boolean login() {
        return data.login();
    }

    @Override
    public State getState() {
        return State.LOGIN;
    }
}
