package pt.isec.pd.server.data;

import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.server.data.database.DBHandler;
import pt.isec.pd.server.threads.client.ClientReceiveMessage;
import pt.isec.pd.server.threads.heart_beat.HeartBeatLifeTime;
import pt.isec.pd.server.threads.heart_beat.HeartBeatReceiver;
import pt.isec.pd.server.threads.heart_beat.HeartBeatSender;
import pt.isec.pd.shared_data.Commit;
import pt.isec.pd.shared_data.CompareDbVersionHeartBeat;
import pt.isec.pd.shared_data.HeartBeat;
import pt.isec.pd.shared_data.Prepare;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HeartBeatController {
    private final Log LOG = Log.getLogger(HeartBeatController.class);
    private final Server server;
    private final DBHandler dbHandler;

    private HeartBeat hbEvent;
    private final HeartBeatReceiver receiver;
    private final HeartBeatSender sender;
    private final HeartBeatLifeTime lifeTimeChecker;
    private final HeartBeatList hbList;
    private boolean updater = false;
    private boolean updating = false;

    private boolean available = true;

    private MulticastSocket ms;

    public HeartBeatController(HeartBeatList hbList, Server server) {
        joinGroup();

        this.server = server;
        this.dbHandler = server.getDbHandler();
        this.hbList = hbList;
        receiver = new HeartBeatReceiver(this,server.getDbHandler());
        sender = new HeartBeatSender(this);
        lifeTimeChecker = new HeartBeatLifeTime(hbList);
    }

    private void joinGroup() {
        try {
            ms = new MulticastSocket(Constants.PORT_MULTICAST);
            InetAddress ipGroup = InetAddress.getByName(Constants.IP_MULTICAST);
            SocketAddress sa = new InetSocketAddress(ipGroup,Constants.PORT_MULTICAST);
            NetworkInterface ni = NetworkInterface.getByName("en0");
            ms.joinGroup(sa,ni);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.log("Joined the group");
    }

    public void start() {
        receiver.start();
        lifeTimeChecker.start();

        try {
            Thread.sleep(Constants.STARTUP * Constants.TO_SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<HeartBeat> hbDbVersion = new ArrayList<>(List.copyOf(hbList));
        hbDbVersion.sort(new CompareDbVersionHeartBeat());

        if (!hbDbVersion.isEmpty()) {
            try {
                int myVersion = dbHandler.getCurrentVersion();
                HeartBeat hbNew = hbDbVersion.get(hbDbVersion.size() - 1);
                if (myVersion < hbDbVersion.get(hbDbVersion.size() - 1).getDbVersion()) {
                    LOG.log("Updating the server to the most recent version");
                    Socket socket = new Socket("localhost", hbNew.getPortTcp());

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    oos.writeObject(0);
                    oos.writeObject(myVersion);
                    List<String> update = (List<String>) ois.readObject();

                    dbHandler.updateToNewVersion(update);
                }
            }
            catch (SQLException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        sender.start();
    }

    public synchronized boolean isAvailable() {
        return available;
    }

    public synchronized void setAvailable(boolean available) {
        this.available = available;
    }

    public HeartBeat updateHeartBeat() {
        hbEvent = new HeartBeat(server.getServerPort(),isAvailable(), server.getDBVersion(), server.getActiveConnections());
        return hbEvent;
    }

    public HeartBeat getHbEvent() {
        return hbEvent;
    }

    // Only called when a request from client updated the database
    public void updateDataBase(List<String> sqlCommand, ClientData clientData) {
        if (!isUpdating()) {
            try {
                DatagramPacket dp;
                LOG.log("Update Starting...");
                setUpdater(true);

                DatagramSocket ds = new DatagramSocket();
                ds.setSoTimeout(1000);

                // 1. Send the 'prepare' object to the multicast
                Prepare prepare = new Prepare(ds.getLocalPort(),server.getServerPort(),sqlCommand,clientData);
                LOG.log("Action: " + clientData.getAction() + " SqlCommands: " + sqlCommand.size());
                byte[] prepareBytes = Utils.serializeObject(prepare);

                dp = new DatagramPacket(prepareBytes,prepareBytes.length,InetAddress.getByName(Constants.IP_MULTICAST),Constants.PORT_MULTICAST);
                ms.send(dp);
                LOG.log("Prepare");

                // 2. Wait for the servers to send the signal (timeout 1000 ms)
                while (true) {
                    try {
                        DatagramPacket dpReceive = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
                        ds.receive(dpReceive);
                        LOG.log("Confirmation");
                    } catch (SocketTimeoutException e) {
                        LOG.log("End of Confirmation");
                        break;
                    }
                }

                // 3. Send a 'commit' to start the updating
                Commit commit = new Commit(prepare.getNextVersion());
                byte[] commitBytes = Utils.serializeObject(commit);
                dp = new DatagramPacket(commitBytes,commitBytes.length,InetAddress.getByName(Constants.IP_MULTICAST),Constants.PORT_MULTICAST);
                ms.send(dp);
                LOG.log("Commit");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.log("Theres a updating working at the moment");
        }
    }


    public synchronized void setUpdater(boolean updater) {
        this.updater = updater;
    }

    public synchronized boolean imUpdating() {
        return updater;
    }

    public synchronized boolean isUpdating() {
        return updating;
    }

    public synchronized void setUpdating(boolean updating) {
        this.updating = updating;
    }

    public synchronized HeartBeatList getHbList() {
        return hbList;
    }

    public synchronized MulticastSocket getMs() {
        return ms;
    }

    public List<ClientReceiveMessage> getClients() {
        return server.getClients();
    }
}
