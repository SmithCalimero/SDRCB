package pt.isec.pd.server.threads.heart_beat;

import pt.isec.pd.server.data.*;
import pt.isec.pd.server.data.database.DBHandler;
import pt.isec.pd.server.threads.client.ClientReceiveMessage;
import pt.isec.pd.shared_data.*;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HeartBeatReceiver extends Thread{
    private final Log LOG = Log.getLogger(HeartBeatReceiver.class);
    private final MulticastSocket ms;
    private final HeartBeatList hbList;
    private final HeartBeatController controller;
    private final DBHandler dbHandler;
    private Prepare prepare;
    private final Server server;

    public HeartBeatReceiver(HeartBeatController controller, DBHandler dbHandler,Server server) {
        this.ms = controller.getMs();
        this.hbList = controller.getHbList();
        this.controller = controller;
        this.dbHandler = dbHandler;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while(true) {
                DatagramPacket dp = new DatagramPacket(new byte[Constants.MAX_BYTES],Constants.MAX_BYTES);
                ms.receive(dp);
                Object object = Utils.deserializeObject(dp.getData());

                if (object instanceof HeartBeat hbEvent) {
                    hbList.updateList(hbEvent);

                    List<HeartBeat> hbDbVersion = new ArrayList<>(List.copyOf(hbList));
                    hbDbVersion.sort(new CompareDbVersionHeartBeat());

                    if (!hbList.isEmpty() && controller.isEndOfStartup() && !controller.isUpdating()) {
                        HeartBeat highest = hbDbVersion.get(hbDbVersion.size() - 1);
                        if (controller.getHb().getDbVersion() < highest.getDbVersion()) {
                            LOG.log("Theres a highest version");
                            controller.setAvailable(false);

                            //Update the list
                            Date date = new Date();
                            hbList.removeIf(n -> (n.getTimeout().compareTo(date) < 0 || !n.isStatus()));

                            ListServerAddress list = new ListServerAddress();
                            list.setServers(hbList.getOrderList());

                            for (ClientReceiveMessage client : server.getClients()) {
                                client.getOos().writeObject(list);
                            }

                            //Sends the heartBeat
                            byte[] bytes = Utils.serializeObject(controller.updateHeartBeat());
                            dp = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(Constants.IP_MULTICAST),Constants.PORT_MULTICAST);
                            ms.send(dp);

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

                            controller.setAvailable(true);

                            bytes = Utils.serializeObject(controller.updateHeartBeat());
                            dp = new DatagramPacket(bytes,bytes.length, InetAddress.getByName(Constants.IP_MULTICAST),Constants.PORT_MULTICAST);
                            ms.send(dp);
                        }
                    }

                }  else if(!controller.imUpdating() && object instanceof Prepare prepare) {
                    controller.setUpdating(true);
                    LOG.log("Prepare receive; port: " + prepare.getPort());
                    this.prepare = prepare;

                    // 1. An update is needed
                    DatagramSocket ds = new DatagramSocket();
                    byte[] databaseVersion = Utils.serializeObject(prepare.getNextVersion());
                    DatagramPacket dpSend = new DatagramPacket(databaseVersion,0,databaseVersion.length, InetAddress.getByName(Constants.IP_LOCALHOST),prepare.getPort());
                    ds.send(dpSend);

                } else if(object instanceof Commit) {
                    if (!controller.imUpdating()) {
                        LOG.log("Commit receive: " + prepare.getData().getAction());
                        // 2. Update the database
                        dbHandler.updateDataBase(prepare.getSqlCommand());

                        switch (prepare.getData().getAction()) {
                            case SUBMIT_RESERVATION,DELETE_UNPAID_RESERVATION -> {
                                for (ClientReceiveMessage client : controller.getClients()) {
                                    dbHandler.viewSeatsAndPrices(prepare.getData(),client.getOos());
                                }
                            }
                            case INSERT_SHOWS,DELETE_SHOW,VISIBLE_SHOW ->  {
                                for (ClientReceiveMessage client : controller.getClients()) {
                                    dbHandler.selectShows(client.getOos());
                                }
                            }
                        }
                    }
                    controller.setUpdating(false);
                    controller.setUpdater(false);
                }
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
