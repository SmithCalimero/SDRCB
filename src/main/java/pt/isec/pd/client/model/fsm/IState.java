package pt.isec.pd.client.model.fsm;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Seat;
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
    List<Seat> getSeatsAndPrices();
    void disconnect();
    void registerTransition();
    State getState();
    void seatsTransition(Integer idShow);
    void insertShowsTransition();
    String insertShows(String filePath);
    Pair<Boolean,String> deleteShow(int idShow);
    String showVisible(int idShow);
    void selectShowsTransition();
    boolean submitReservation(List<Seat> seats);
    void consultsPaymentsAwaitingTransition();
    List<Reserve> consultsPaymentsAwaiting();
}
