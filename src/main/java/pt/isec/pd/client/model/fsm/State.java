package pt.isec.pd.client.model.fsm;

import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.fsm.states.*;

public enum State {
    LOGIN, REGISTER, EDIT_USER, MENU_CLIENT,SHOWS;

    IState createState(Context context, Client data) {
        return switch (this) {
            case LOGIN -> new Login(context,data);
            case REGISTER -> new Register(context,data);
            case EDIT_USER -> new EditUser(context,data);
            case MENU_CLIENT -> new MenuClient(context,data);
            case SHOWS -> new Shows(context,data);
        };
    }
}
