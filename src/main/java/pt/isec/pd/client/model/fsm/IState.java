package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;

public interface IState {
    void next();
    void previous();
    Pair<Boolean,String> login(String userName, String password);
    String register(String userName,String name,String password);
    void edit(ClientAction action, String edit);
    void editTransition();
    void disconnect();
    void swapToRegister();
    State getState();
}
