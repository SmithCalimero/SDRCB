package pt.isec.pd.server.data;

import pt.isec.pd.server.threads.heart_beat.HeartBeatLifeTime;
import pt.isec.pd.server.threads.heart_beat.HeartBeatReceiver;
import pt.isec.pd.server.threads.heart_beat.HeartBeatSender;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.net.*;

public class HeartBeatController {
    private final Log LOG = Log.getLogger(Server.class);
    private final Server server;

    private HeartBeatEvent hbEvent;
    private final HeartBeatReceiver receiver;
    private final HeartBeatSender sender;
    private final HeartBeatLifeTime lifeTimeChecker;
    private final HeartBeatList hbList;

    private MulticastSocket ms;

    public HeartBeatController(HeartBeatList hbList, Server server) {
        joinGroup();

        this.server = server;
        this.hbList = hbList;
        receiver = new HeartBeatReceiver(ms,hbList);
        sender = new HeartBeatSender(ms,this);
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

        if (hbList.size() == 0) {
            server.createDataBase();
         } else {
            server.transferDataBase();
        }
    }

    public HeartBeatEvent updateHeartBeat() {
        if (hbEvent == null) {
            hbEvent = new HeartBeatEvent(server.getServerPort(), true, server.getDBVersion(),0);
        }
        return hbEvent;
    }
}
