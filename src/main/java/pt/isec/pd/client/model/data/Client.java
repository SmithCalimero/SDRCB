package pt.isec.pd.client.model.data;

import javafx.util.Pair;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.shared_data.Triple;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    private final CommunicationHandler ch;
    //private Socket socket;

    public Client(ServerAddress pingAddr) {
        ch = new CommunicationHandler(pingAddr,this);
        ch.start();
    }
    public boolean login() {
        try {
            ch.writeToSocket(ClientAction.LOGIN,new Pair<>("ruben","1234"));
            return (Boolean) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register() {
        try {
            ch.writeToSocket(ClientAction.REGISTER,new Triple<>("ruben","ruben santos","1234"));
            return (Boolean) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void edit() {
    }
}
