package pt.isec.pd.server.threads.heart_beat;

import pt.isec.pd.server.data.*;
import pt.isec.pd.server.data.database.DBHandler;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;
import pt.isec.pd.shared_data.HeartBeatEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class HeartBeatReceiver extends Thread{
    private final Log LOG = Log.getLogger(Server.class);
    private final MulticastSocket ms;
    private final HeartBeatList hbList;
    private final HeartBeatController controller;
    private final DBHandler dbHandler;
    private Prepare prepare;

    public HeartBeatReceiver(HeartBeatController controller, DBHandler dbHandler) {
        this.ms = controller.getMs();
        this.hbList = controller.getHbList();
        this.controller = controller;
        this.dbHandler = dbHandler;
    }

    @Override
    public void run() {
        try {
            while(true) {
                DatagramPacket dp = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
                ms.receive(dp);
                Object object = Utils.deserializeObject(dp.getData());
                if (object instanceof HeartBeatEvent hbEvent) {
                    //LOG.log("\nHeartBeat\n" + hbEvent);
                    hbList.updateList(hbEvent);
                }  else if(!controller.imUpdating() && object instanceof Prepare prepare) {
                    LOG.log("\nPrepare receive; port: " + prepare.getPort());
                    this.prepare = prepare;

                    // 1. An update is needed
                    DatagramSocket ds = new DatagramSocket();
                    byte[] databaseVersion = Utils.serializeObject(prepare.getNextVersion());
                    DatagramPacket dpSend = new DatagramPacket(databaseVersion,0,databaseVersion.length, InetAddress.getByName(Constants.IP_LOCALHOST),prepare.getPort());
                    ds.send(dpSend);
                } else if(object instanceof Commit) {
                    if (!controller.imUpdating()) {
                        LOG.log("\nCommit receive\n");
                        // 2. Update the database
                        dbHandler.updateDataBase(prepare.getSqlCommand());
                    } else {
                        controller.setUpdater(false);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
