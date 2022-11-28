package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.shared_data.Show;

import java.util.HashMap;
import java.util.List;

public class SelectShows extends StateAdapter {
    public SelectShows(Context context, Client data) {
        super(context,data);
    }

    @Override
    public List<Show> consultShows(HashMap<String, String> filters) {
        return data.consultShows(ClientAction.SELECT_SHOWS,null);
    }

    @Override
    public void seatsTransition(Integer idShow) {
        changeState(State.SEATS_PRICES);
        data.viewSeatsAndPrices(idShow);
    }

    @Override
    public void payReservationTransition(int resId) {
        context.changeState(new PayReservation(context,data,resId));
    }

    @Override
    public void selectShowsTransition() {
        changeState(State.MENU_CLIENT);
    }

    @Override
    public State getState() {
        return State.SELECT_SHOWS;
    }
}
