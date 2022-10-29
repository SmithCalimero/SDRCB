package pt.isec.pd.client.model.fsm;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.fsm.states.Login;
import pt.isec.pd.client.model.fsm.states.Register;
import pt.isec.pd.client.model.fsm.states.EditUser;

public enum State {
    LOGIN, REGISTER, EDIT_USER;

    IState createState(Context context, Client data) {
        return switch (this) {
            case LOGIN -> new Login(context,data);
            case REGISTER -> new Register(context,data);
            case EDIT_USER -> new EditUser(context,data);
        };
    }
}
