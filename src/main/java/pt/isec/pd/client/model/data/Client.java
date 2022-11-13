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
    public void login() {
        try {
            //ch.writeToSocket(ClientAction.LOGIN,new Pair<>("eduardo","1234"));
            ch.writeToSocket(ClientAction.LOGIN,new Pair<>("ruben","1234"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register() {
        try {
            //ch.writeToSocket(ClientAction.REGISTER,new Triple<>("eduardo","eduardo bento","1234"));
            ch.writeToSocket(ClientAction.REGISTER,new Triple<>("ruben","ruben santos","1234"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void edit() {
    }
}
