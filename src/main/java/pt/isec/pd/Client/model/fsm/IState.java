package pt.isec.pd.Client.model.fsm;

public interface IState {
    void next();
    void previous();

    void login();
    void register();
    void edit();
    State getState();
}
