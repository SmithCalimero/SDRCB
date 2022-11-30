package pt.isec.pd.client.model.fsm.states;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.Context;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.fsm.StateAdapter;

public class PayReservation extends StateAdapter {
    int resId;
    public PayReservation(Context context, Client data,int resId) {
        super(context, data);
        this.resId = resId;
    }

    public PayReservation(Context context, Client data) {
        super(context, data);
    }

    @Override
    public void payReservationTransition(int resId) {
        changeState(State.MENU_CLIENT);
    }

    @Override
    public void payReservation() {
        data.payReservation(resId);
    }

    @Override
    public void next() {
        changeState(State.CONSULT_PAYMENTS);
        data.consultReservesPayed();
    }

    @Override
    public State getState() {
        return State.PAY_RESERVATION;
    }
}
