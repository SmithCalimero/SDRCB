package pt.isec.pd.client.model.fsm.states;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.data.Type;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.shared_data.Show;

import java.util.HashMap;
import java.util.List;

public class ManageShows extends StateAdapter {
    public ManageShows(Context context, Client data) {
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
    public String insertShows(String filePath) {
        return data.insertShows(filePath);
    }

    @Override
    public List<Show> consultShows(HashMap<String, String> filters) {
        return data.consultShows(ClientAction.CONSULT_SHOWS_ALL,null);
    }

    @Override
    public Pair<Boolean, String> deleteShow(int idShow) {
        return data.deleteShow(idShow);
    }

    @Override
    public String showVisible(int idShow) {
        return data.showVisible(idShow);
    }

    @Override
    public State getState() {
        return State.MANAGE_SHOWS;
    }
}
