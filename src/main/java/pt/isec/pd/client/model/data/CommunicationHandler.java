package pt.isec.pd.client.model.data;

import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommunicationHandler extends Thread{
    private final Log LOG = Log.getLogger(Client.class);
    private final ServerAddress pingAddr;
    private Socket socket;

    public CommunicationHandler(ServerAddress pingAddr) {
        this.pingAddr = pingAddr;
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

            DatagramPacket dpReceive = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
            ds.receive(dpReceive);
            LOG.log("DatagramPacket received from the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            return Utils.deserializeObject(dpReceive.getData());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized boolean establishingTcpConn(List<ServerAddress> serversAddr) {
        for (ServerAddress address : serversAddr) {
            if (tryConnection(address)) {
                LOG.log("Connected to " + address.getIp() + ":" + address.getPort());
                return true;
            }
        }
        LOG.log("The client was not able to connect to any server");
        return false;
    }

    private boolean tryConnection(ServerAddress address) {
        try {
            socket = new Socket(address.getIp(), address.getPort());
            setSocket(socket);
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    private void setSocket(Socket socket) { this.socket = socket; }

    public synchronized void writeToSocket(Socket socket, ClientAction action, Object object) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        //ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        HashMap<ClientAction,Object> sendObject = new HashMap<>();
        sendObject.put(action,object);
        oos.writeObject(sendObject);
    }
}
