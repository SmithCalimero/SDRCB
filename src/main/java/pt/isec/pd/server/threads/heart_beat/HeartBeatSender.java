package pt.isec.pd.server.threads.heart_beat;

import pt.isec.pd.server.data.HeartBeatController;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class HeartBeatSender extends Thread {
    private final MulticastSocket ms;
    private final HeartBeatController hbController;

    public HeartBeatSender(MulticastSocket ms, HeartBeatController hbController) {
        this.ms = ms;
        this.hbController = hbController;
    }

    @Override
    public void run() {
        send();
    }

    public void send() {
        while(true) {
            try {
                Thread.sleep(10 * Constants.TO_SECONDS);
                byte[] bytes = Utils.serializeObject(hbController.updateHeartBeat());

                DatagramPacket dp = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(Constants.IP_MULTICAST),Constants.PORT_MULTICAST);
                ms.send(dp);
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
