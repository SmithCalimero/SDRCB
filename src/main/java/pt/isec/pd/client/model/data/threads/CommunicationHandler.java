package pt.isec.pd.client.model.data.threads;

import javafx.application.Platform;
import pt.isec.pd.client.model.data.Client;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.shared_data.ServerAddress;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Exceptions.NoServerFound;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.List;

public class CommunicationHandler extends Thread {
    private final Log LOG = Log.getLogger(Client.class);
    private final ServerAddress pingAddr;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final DatagramSocket ds;
    private ClientData clientData;
    private ResponseHandler responseHandler;
    private PropertyChangeSupport pcs;

    public CommunicationHandler(ServerAddress pingAddr, PropertyChangeSupport pcs) {
        this.pcs = pcs;

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

    public Object getResponse() {
        return responseHandler.getResponse();
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
            LOG.log("List received from the server");
            establishingTcpConn(serverAddr);

        } catch (IOException | NoServerFound e) {
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
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            responseHandler = new ResponseHandler(this,pcs,clientData);
            responseHandler.start();
            return true;
        } catch(IOException e) {
            return false;
        }
    }

    public  void writeToSocket(ClientAction action, Object object) {
        try {
            clientData.setAction(action);
            clientData.setData(object);
            oos.writeUnshared(clientData);
            LOG.log("Request sent: " + clientData.getAction());
        } catch (IOException e) {
            sendPing();
            writeToSocket(action,object);
        }
    }

    public Object readFromSocket() throws IOException {
        try {
            return ois.readObject();
        } catch (SocketException e) {
            sendPing();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public ClientAction getClientAction() { return clientData.getAction(); }
    public ClientData getClientData() {
        return clientData;
    }
    public synchronized ObjectOutputStream getOos() {
        return oos;
    }
    public synchronized ObjectInputStream getOis() {
        return ois;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
