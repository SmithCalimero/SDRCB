package pt.isec.pd.Client.model.fsm.states;

import pt.isec.pd.Client.model.fsm.Context;
import pt.isec.pd.Client.model.fsm.State;
import pt.isec.pd.Client.model.fsm.StateAdapter;
import pt.isec.pd.Client.model.data.Client;

public class Login extends StateAdapter {
    public Login(Context context, Client data) {
        super(context,data);
    }

    @Override
    public void next() {
        changeState(State.REGISTER);
    }

    @Override
    public void login() {
        data.login();
    }

    @Override
    public State getState() {
        return State.LOGIN;
    }
}
