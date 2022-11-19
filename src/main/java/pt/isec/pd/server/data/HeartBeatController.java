package pt.isec.pd.server.data;

import pt.isec.pd.server.threads.heart_beat.HeartBeatLifeTime;
import pt.isec.pd.server.threads.heart_beat.HeartBeatReceiver;
import pt.isec.pd.server.threads.heart_beat.HeartBeatSender;
import pt.isec.pd.shared_data.Commit;
import pt.isec.pd.shared_data.HeartBeat;
import pt.isec.pd.shared_data.Prepare;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.net.*;

public class HeartBeatController {
    private final Log LOG = Log.getLogger(Server.class);
    private final Server server;

    private HeartBeat hbEvent;
    private final HeartBeatReceiver receiver;
    private final HeartBeatSender sender;
    private final HeartBeatLifeTime lifeTimeChecker;
    private final HeartBeatList hbList;
    private boolean updater = false;
    private boolean updating = false;

    private MulticastSocket ms;

    public HeartBeatController(HeartBeatList hbList, Server server) {
        joinGroup();

        this.server = server;
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

        sender.start();
    }

    public HeartBeat updateHeartBeat() {
        hbEvent = new HeartBeat(server.getServerPort(), true, server.getDBVersion(), server.getActiveConnections());
        return hbEvent;
    }

    // Only called when a request from client updated the database
    public void updateDataBase(String sqlCommand) {
        if (!isUpdating()) {
            try {
                LOG.log("Update Starting...");
                setUpdater(true);
                DatagramSocket ds = new DatagramSocket();
                ds.setSoTimeout(1000);

                // 1. Send the 'prepare' object to the multicast
                Prepare prepare = new Prepare(ds.getLocalPort(),server.getServerPort(),sqlCommand);
                LOG.log("SqlCommand: " + sqlCommand);
                byte[] bytes = Utils.serializeObject(prepare);
                DatagramPacket dp = new DatagramPacket(bytes,bytes.length,InetAddress.getByName(Constants.IP_MULTICAST),Constants.PORT_MULTICAST);
                ms.send(dp);

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
}
