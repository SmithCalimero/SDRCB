package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.Type;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.shared_data.Show;

import java.util.HashMap;
import java.util.List;

public class InsertShows extends StateAdapter {
    public InsertShows(Context context, Client data) {
        super(context, data);
    }

    @Override
    public void showsTransition() {
        if (data.getType() == Type.ADMIN) {
            changeState(State.MENU_ADMIN);
        } else {
            changeState(State.MENU_CLIENT);
        }
    }

    @Override
    public void insertShowsTransition() {
        changeState(State.MENU_ADMIN);
    }

    @Override
    public Show insertShows(String filePath) {
        return data.insertShows(filePath);
    }

    @Override
    public State getState() {
        return State.INSERT_SHOWS;
    }
}
