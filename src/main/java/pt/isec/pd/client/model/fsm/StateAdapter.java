package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;

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
}
