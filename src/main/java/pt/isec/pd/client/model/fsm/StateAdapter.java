package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.states.Shows;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.Show;

import java.util.HashMap;
import java.util.List;

public abstract class StateAdapter implements IState {
    protected Context context;
    protected Client data;

    protected StateAdapter(Context context, Client data) {
        this.context = context;
        this.data = data;
    }

    protected void changeState(State newState) {
        context.changeState(newState.createState(context,data));
    }

    @Override
    public void next() {
        context.next();
    }

    @Override
    public Pair<Boolean,String> login(String userName, String password) {
        return context.login(userName,password);
    }

    @Override
    public String register(String userName,String name,String password) {
        return context.register(userName,name,password);
    }

    @Override
    public void edit(ClientAction action, String edit) {
        context.edit(action,edit);
    }

    @Override
    public void previous() {
        context.previous();
    }

    @Override
    public void disconnect() {
        context.disconnect();
    }

    @Override
    public void swapToRegister() {
        context.swapToRegister();
    }

    @Override
    public void editTransition() {
        context.editTransition();
    }

    @Override
    public void showsTransition() {
        context.showsTransition();
    }

    @Override
    public List<Show> consultShows(HashMap<String, String> filters) {
        return context.consultShows(filters);
    }

    @Override
    public List<Seat> getSeatsAndPrices() {
        return context.getSeatsAndPrices();
    }

    @Override
    public void seatsTransition(Integer idSwow) {
        context.seatsTransition(idSwow);
    }
}
