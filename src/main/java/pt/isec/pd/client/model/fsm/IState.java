package pt.isec.pd.client.model.fsm;

public interface IState {
    void next();
    void previous();

    boolean login();
    void register();
    void edit();
    State getState();
}
