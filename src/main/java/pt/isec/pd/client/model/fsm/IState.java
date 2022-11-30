package pt.isec.pd.client.model.fsm;

import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.shared_data.Seat;

import java.util.HashMap;
import java.util.List;

public interface IState {
    void next();
    void previous();
    void login(String userName, String password);
    void register(String userName, String name, String password);
    void edit(ClientAction action, String edit);
    void editTransition();
    void showsTransition();
    void consultShows(HashMap<String,String> filters);
    List<Seat> getSeatsAndPrices();
    void disconnect();
    void registerTransition();
    State getState();
    void seatsTransition(Integer idShow);
    void insertShowsTransition();
    void insertShows(String filePath);
    void deleteShow(int idShow);
    void showVisible(int idShow);
    void selectShowsTransition();
    void submitReservation(List<Seat> seats);
    void consultsPaymentsAwaitingTransition();
    void consultsPaymentsAwaiting();
    void payReservationTransition(int resId);

    void payReservation();

    Object getResponse();
}
