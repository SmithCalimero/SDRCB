package pt.isec.pd.client.model.data;

import javafx.application.Platform;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Exceptions.NoServerFound;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class CommunicationHandler extends Thread{
    private final Log LOG = Log.getLogger(Client.class);
    private final ServerAddress pingAddr;
    private Socket socket;
    private DatagramSocket ds;
    private Client client;

    public CommunicationHandler(ServerAddress pingAddr,Client client) {
        try {
            ds = new DatagramSocket();
            ds.setSoTimeout(Constants.TIMEOUT);
            LOG.log("DatagramSocket created on the port: " + ds.getLocalPort());
        } catch (SocketException e) { throw new RuntimeException(e); }

        this.pingAddr = pingAddr;
        this.client = client;
    }

    @Override
    public void run() {
        sendPing();
    }

    public void sendPing() {
        try {
            DatagramPacket dpSend = new DatagramPacket(new byte[0],0, InetAddress.getByName(pingAddr.getIp()), pingAddr.getPort());
            ds.send(dpSend);
            LOG.log("DatagramPacket sent to the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            DatagramPacket dpReceive = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
            ds.receive(dpReceive);
            LOG.log("DatagramPacket received from the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            //try tcp connections
            List<ServerAddress> serverAddr = Utils.deserializeObject(dpReceive.getData());
            if(!establishingTcpConn(serverAddr)) throw new NoServerFound();

        } catch (IOException | ClassNotFoundException | NoServerFound e) {
            LOG.log("No tcp connection found or the udp connection was not establish: shutting down application : "+  pingAddr.getIp() + ":" + pingAddr.getPort());
            Platform.exit();
            System.exit(0);
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

    public synchronized void writeToSocket(ClientAction action, Object object) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            ClientData clientData = new ClientData();
            clientData.setAction(action);

            oos.writeObject(clientData);
            oos.writeObject(object);
        } catch (SocketException e) {
            sendPing();
            writeToSocket(action,object);
        }
    }

    public synchronized Object readFromSocket() throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            try {
                return ois.readObject();
            } catch (NullPointerException | ClassNotFoundException ignored) {}

        } catch (SocketException e) {
            sendPing();
            readFromSocket();
        }
        return null;
    }
}
