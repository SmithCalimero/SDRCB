package pt.isec.pd.client.model.data;

import pt.isec.pd.shared_data.ServerAddress;

import java.net.Socket;

public class Client extends Thread {
    private final CommunicationHandler ch;
    //private Socket socket;

    public Client(ServerAddress pingAddr) {
        ch = new CommunicationHandler(pingAddr);
        ch.start();
    }
    public void login() {
    }

    public void register() {
    }

    public void edit() {
    }
}
