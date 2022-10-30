package pt.isec.pd.client.model.data;

import pt.isec.pd.sharedData.ServerAddress;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {
    private final CommunicationHandler ch;
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
