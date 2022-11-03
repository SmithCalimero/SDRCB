package pt.isec.pd.server.threads;

import pt.isec.pd.server.data.Server;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.ArrayList;
import java.util.List;

/* UDP: Receives the ping from a client and sends a list of servers to later initialize a tcp connection*/
public class ClientPingHandler extends Thread{
    private final Log LOG = Log.getLogger(Server.class);
    private final int port;
    public ClientPingHandler(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try(DatagramSocket ds = new DatagramSocket(port)) {
            LOG.log("DatagramSocket created on the port: " + port);

            while(true) {
                DatagramPacket dp = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
                ds.receive(dp);
                LOG.log("Ping has been received from " + dp.getAddress().getHostAddress() + ":" + dp.getPort());

                //TODO: send a list of servers organized by their tcp connection
                List<ServerAddress> list = new ArrayList<>();

                byte[] listBytes = Utils.serializeObject(list);
                dp.setData(listBytes,0,listBytes.length);
                dp.setLength(listBytes.length);
                ds.send(dp);
                LOG.log("The list of servers was sent to the client");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
