package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;
import pt.isec.pd.shared_data.Reserve;

import java.util.List;

public class ConsultPaymentsAwaiting extends StateAdapter {
    public ConsultPaymentsAwaiting(Context context, Client data) {
        super(context, data);
    }

    @Override
    public void consultsPaymentsAwaitingTransition() {
        changeState(State.MENU_CLIENT);
    }

    @Override
    public List<Reserve> consultsPaymentsAwaiting() {
        return data.consultsPaymentsAwaiting();
    }

    @Override
    public State getState() {
        return State.CONSULT_PAYMENTS_AWAITING;
    }
}
