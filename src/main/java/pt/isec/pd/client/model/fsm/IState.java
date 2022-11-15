package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.shared_data.Show;

import java.util.HashMap;
import java.util.List;

public interface IState {
    void next();
    void previous();
    Pair<Boolean,String> login(String userName, String password);
    String register(String userName,String name,String password);
    void edit(ClientAction action, String edit);
    void editTransition();

    void showsTransition();
    List<Show> consultShows(HashMap<String,String> filters);

    void disconnect();
    void swapToRegister();
    State getState();
}
