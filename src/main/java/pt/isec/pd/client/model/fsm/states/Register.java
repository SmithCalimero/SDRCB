package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.client.model.data.Client;

public class Register extends StateAdapter {
    public Register(Context context, Client data) {
        super(context,data);
    }

    @Override
    public void next() {
        changeState(State.LOGIN);
    }

    @Override
    public void previous() {
        changeState(State.LOGIN);
    }

    @Override
    public String register(String userName,String name,String password) {
        return data.register(userName,name,password);
    }

    @Override
    public void swapToRegister() {
        changeState(State.LOGIN);
    }

    @Override
    public State getState() {
        return State.REGISTER;
    }
}

