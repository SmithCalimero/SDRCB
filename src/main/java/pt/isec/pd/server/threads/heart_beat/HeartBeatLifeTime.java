package pt.isec.pd.server.threads.heart_beat;

import pt.isec.pd.server.data.HeartBeatList;
import pt.isec.pd.server.data.Server;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;

import java.util.Date;

public class HeartBeatLifeTime extends Thread{
    private final Log LOG = Log.getLogger(Server.class);
    private final HeartBeatList hbList;

    public HeartBeatLifeTime(HeartBeatList hbList) {
        this.hbList = hbList;
    }

    @Override
    public void run() {
        while (true) {
            Date date = new Date();
            synchronized (hbList) {
                for (int i = 0; i < hbList.size(); i++) {
                    if (hbList.get(i).getTimeout().compareTo(date) > 0) {
                        break;
                    }
                    LOG.log("HeartBeat " + hbList.get(i).getPortTcp() + " was removed");
                    hbList.remove(hbList.get(i));
                }
            }

            try {
                Thread.sleep(Constants.TO_SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
