package pt.isec.pd.server.threads.heart_beat;

import pt.isec.pd.server.data.HeartBeatList;
import pt.isec.pd.server.data.Server;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;
import pt.isec.pd.shared_data.HeartBeatEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class HeartBeatReceiver extends Thread{
    private final Log LOG = Log.getLogger(Server.class);
    private final MulticastSocket ms;
    private final HeartBeatList hbList;
    public HeartBeatReceiver(MulticastSocket ms,HeartBeatList hbList) {
        this.ms = ms;
        this.hbList = hbList;
    }

    @Override
    public void run() {
        try {
            while(true) {
                DatagramPacket dp = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
                ms.receive(dp);
                HeartBeatEvent hbEvent = Utils.deserializeObject(dp.getData());
                LOG.log("New heartbeat\n" + hbEvent);

                synchronized (hbList) {
                    hbList.updateList(hbEvent);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
