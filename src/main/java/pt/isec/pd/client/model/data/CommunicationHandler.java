package pt.isec.pd.client.model.data;

import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CommunicationHandler extends Thread{
    private final Log LOG = Log.getLogger(Client.class);
    private final ServerAddress pingAddr;
    private final Socket socket;

    public CommunicationHandler(ServerAddress pingAddr,Socket socket) {
        this.pingAddr = pingAddr;
        this.socket = socket;
    }

    @Override
    public void run() {
        List<ServerAddress> serversAddr = sendPing();
        establishingTcpConn(serversAddr);
    }

    public ArrayList<ServerAddress> sendPing() {
        try(DatagramSocket ds = new DatagramSocket()) {
            LOG.log("DatagramSocket created on the port: " + ds.getLocalPort());

            DatagramPacket dpSend = new DatagramPacket(new byte[0],0, InetAddress.getByName(pingAddr.getIp()), pingAddr.getPort());
            ds.send(dpSend);
            LOG.log("DatagramPacket sent to the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            DatagramPacket dpReceive = new DatagramPacket(new byte[256],256);
            ds.receive(dpReceive);
            LOG.log("DatagramPacket received from the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            return Utils.deserializeObject(dpReceive.getData());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized void establishingTcpConn(List<ServerAddress> serversAddr) {
        for (ServerAddress address : serversAddr) {
            //TODO:
        }
    }

}
