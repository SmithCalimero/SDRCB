package pt.isec.pd.client.model.fsm;

import pt.isec.pd.client.model.data.Client;

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
    public boolean login() {
        return context.login();
    }

    @Override
    public void register() {
        context.register();
    }

    @Override
    public void edit() {
        context.edit();
    }

    @Override
    public void previous() {
        context.previous();
    }
}
