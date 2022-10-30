package pt.isec.pd.client.model.data;

import pt.isec.pd.utils.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {
    private final ServerAddress pingAddr;
    private final Log LOG = Log.getLogger(Client.class);

    public Client(ServerAddress pingAddr) {
        this.pingAddr = pingAddr;
        this.start();
    }

    @Override
    public void run() {
        ArrayList<ServerAddress> serversAddr = sendPing();
        establishingTcpConn(serversAddr);
    }

    public ArrayList<ServerAddress> sendPing() {
        try {
            DatagramSocket ds = new DatagramSocket();
            LOG.log("DatagramSocket created on the port: " + ds.getLocalPort());

            //Serialization
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oss = new ObjectOutputStream(baos);
            oss.writeObject("ping");
            byte[] infoBytes = baos.toByteArray();

            DatagramPacket dpSend = new DatagramPacket(infoBytes,infoBytes.length, InetAddress.getByName(pingAddr.getIp()), pingAddr.getPort());
            ds.send(dpSend);
            LOG.log("DatagramPacket sent to the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            DatagramPacket dpReceive = new DatagramPacket(new byte[256],256);
            ds.receive(dpReceive);

            //Deserialization
            ByteArrayInputStream bais = new ByteArrayInputStream(dpReceive.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (ArrayList<ServerAddress>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void establishingTcpConn(ArrayList<ServerAddress> serversAddr) {
        for (ServerAddress address : serversAddr) {

        }
    }

    public void login() {

    }

    public void register() {
    }

    public void edit() {
    }
}
