package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.Type;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;

public class MenuClient extends StateAdapter {
    public MenuClient(Context context, Client data) {
        super(context, data);
    }

    @Override
    public void editTransition() {
        if (data.getType() == Type.NORMAl_MODE) {
            changeState(State.EDIT_USER);
        }
    }

    @Override
    public void showsTransition() {
        if (data.getType() == Type.NORMAl_MODE) {
            changeState(State.SHOWS);
        }
    }

    @Override
    public void selectShowsTransition() {
        changeState(State.SELECT_SHOWS);
    }

    @Override
    public State getState() {
        return State.MENU_CLIENT;
    }
}
