package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.shared_data.Show;

import java.util.HashMap;
import java.util.List;

public class Shows extends StateAdapter {
    public Shows(Context context, Client data) {
        super(context, data);
    }
    @Override
    public List<Show> consultShows(HashMap<String,String> filters) {
        return data.consultShows(filters);
    }
    @Override
    public State getState() {
        return State.SHOWS;
    }
}
