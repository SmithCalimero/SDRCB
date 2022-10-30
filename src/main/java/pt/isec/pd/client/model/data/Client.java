package pt.isec.pd.client.model.data;

import pt.isec.pd.server.data.Server;
import pt.isec.pd.utils.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client extends Thread {
    private final ServerAddress udpConn;
    private final Log LOG = Log.getLogger(Client.class);

    public Client(ServerAddress udpConn) {
        this.udpConn = udpConn;
        this.start();
    }

    @Override
    public void run() {
        ArrayList<ServerAddress> serverAddresses = initialConnection(udpConn.getIp(),udpConn.getPort());
        establishingTcpConnection(serverAddresses);
    }

    public ArrayList<ServerAddress> initialConnection(String ipUdp, int portUdp) {
        String info = "conn-waiting";

        try {
            DatagramSocket ds = new DatagramSocket();
            LOG.log("DatagramSocket created on the port: " + ds.getLocalPort());

            //Serialization
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oss = new ObjectOutputStream(baos);
            oss.writeObject(info);
            byte[] infoBytes = baos.toByteArray();

            DatagramPacket dpSend = new DatagramPacket(infoBytes,infoBytes.length, InetAddress.getByName(ipUdp),portUdp);
            ds.send(dpSend);
            LOG.log("DatagramPacket sent to the server : "+  ipUdp + ":" + portUdp);

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

    public void establishingTcpConnection(ArrayList<ServerAddress> serverAddresses) {
        for (ServerAddress address : serverAddresses) {

        }
    }

    public void login() {

    }

    public void register() {
    }

    public void edit() {
    }
}
