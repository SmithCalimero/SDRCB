package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.shared_data.Seat;

import java.util.List;

public class SeatsAndPrices extends StateAdapter {
    public SeatsAndPrices(Context context, Client data) {
        super(context, data);
    }

    @Override
    public List<Seat> getSeatsAndPrices() {
        return data.getSeatsAndPrices();
    }

    @Override
    public void seatsTransition(Integer idSwow) {
        data.notifyServer();
        changeState(State.SHOWS);
    }

    @Override
    public State getState() {
        return State.SEATS_PRICES;
    }
}
