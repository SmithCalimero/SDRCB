package pt.isec.pd.client.model.fsm;

public interface IState {
    void next();
    void previous();

    void login();
    void register();
    void edit();
    State getState();
}
