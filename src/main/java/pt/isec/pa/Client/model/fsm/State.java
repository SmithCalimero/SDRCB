package pt.isec.pa.Client.model.fsm;

import pt.isec.pa.Client.model.data.Client;
import pt.isec.pa.Client.model.fsm.states.Login;
import pt.isec.pa.Client.model.fsm.states.Register;
import pt.isec.pa.Client.model.fsm.states.EditUser;

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
