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
import java.util.List;

public class CommunicationHandler extends Thread {
    private final Log LOG = Log.getLogger(Client.class);
    private final ServerAddress pingAddr;
    private Socket socket;
    private final DatagramSocket ds;
    private ClientData clientData;

    public CommunicationHandler(ServerAddress pingAddr) {
        try {
            ds = new DatagramSocket();
            ds.setSoTimeout(Constants.TIMEOUT);
            LOG.log("DatagramSocket created on the port: " + ds.getLocalPort());
        } catch (SocketException e) { throw new RuntimeException(e); }

        this.pingAddr = pingAddr;
        this.clientData = new ClientData();
    }

    @Override
    public void run() {
        sendPing();
    }

    public void sendPing() {
        try {
            //1. send ping to server
            DatagramPacket dpSend = new DatagramPacket(new byte[0],0, InetAddress.getByName(pingAddr.getIp()), pingAddr.getPort());
            ds.send(dpSend);
            LOG.log("DatagramPacket sent to the server : "+  pingAddr.getIp() + ":" + pingAddr.getPort());

            DatagramPacket dpReceive = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
            ds.receive(dpReceive);

            //2. try establishing connection
            List<ServerAddress> serverAddr = Utils.deserializeObject(dpReceive.getData());
            LOG.log("List received from the server : "+  serverAddr.toString());
            establishingTcpConn(serverAddr);

        } catch (IOException | ClassNotFoundException | NoServerFound e) {
            // Udp Time-out or no establish connection
            LOG.log("No tcp connection found or the udp connection was not establish: shutting down application : "+  pingAddr.getIp() + ":" + pingAddr.getPort());
            Platform.exit();
            System.exit(0);
        }
    }

    public synchronized void establishingTcpConn(List<ServerAddress> serversAddr) throws NoServerFound {
        for (ServerAddress address : serversAddr) {
            if (tryConnection(address)) {
                LOG.log("Connected to " + address.getIp() + ":" + address.getPort());
                return;
            }
        }

        LOG.log("The client was not able to connect to any server");
        throw new NoServerFound();
    }

    private boolean tryConnection(ServerAddress address) {
        try {
            socket = new Socket(address.getIp(), address.getPort());
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    public synchronized void writeToSocket(ClientAction action, Object object) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            //clientData = new ClientData();
            clientData.setAction(action);

            oos.writeObject(clientData);
            if (object != null) {
                oos.writeObject(object);
            }
        } catch (SocketException e) {
            sendPing();
            writeToSocket(action,object);
        }
    }

    public synchronized Object readFromSocket() throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            return ois.readObject();
        } catch (SocketException e) {
            sendPing();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ClientAction getClientAction() { return clientData.getAction(); }
}
