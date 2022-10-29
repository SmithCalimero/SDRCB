package pt.isec.pa.Client.model.fsm.states;

import pt.isec.pa.Client.model.fsm.Context;
import pt.isec.pa.Client.model.fsm.State;
import pt.isec.pa.Client.model.fsm.StateAdapter;
import pt.isec.pa.Client.model.data.Client;

public class Register extends StateAdapter {
    public Register(Context context, Client data) {
        super(context,data);
    }

    @Override
    public void next() {
        changeState(State.EDIT_USER);
    }

    @Override
    public void previous() {
        changeState(State.LOGIN);
    }

    @Override
    public void register() {
        data.register();
    }

    @Override
    public State getState() {
        return State.REGISTER;
    }
}

