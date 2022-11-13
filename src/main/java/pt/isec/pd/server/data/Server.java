package pt.isec.pd.server.data;

import pt.isec.pd.server.data.database.CreateDataBase;
import pt.isec.pd.server.data.database.DataBaseHandler;
import pt.isec.pd.server.threads.client.ClientManagement;
import pt.isec.pd.server.threads.DataBase.DataBaseSender;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class Server {
    private final Log LOG = Log.getLogger(Server.class);
    private HeartBeatList hbList;
    private ClientManagement clientManagement;
    //private ClientController clientController;
    private final String dbPath;
    private HeartBeatController heartBeatController;
    private DataBaseHandler dataBaseHandler;

    public Server(int pingPort,String dbPath) {
        this.dbPath = dbPath;

        init(pingPort);
        start();
    }

    public void init(int pingPort) {
        hbList = new HeartBeatList();

        try {
            dataBaseHandler = new DataBaseHandler(dbPath);
        } catch (SQLException e) {
            LOG.log("DataBase could no be loaded");
            dataBaseHandler = null;
        }

        //clientController = new ClientController(pingPort);
        clientManagement = new ClientManagement(pingPort,dataBaseHandler,hbList);
        heartBeatController = new HeartBeatController(hbList,this);
    }

    public void start() {
        heartBeatController.start();

       /* if (hbList.size() == 0) {
            createDataBase();
        } else {
            transferDataBase();
        }*/

        //clientController.start();
        clientManagement.startPingHandler();
        clientManagement.start();
    }

    public boolean createDataBase() {
        if (dataBaseHandler == null) {
            new CreateDataBase(dbPath);

            try {
                dataBaseHandler = new DataBaseHandler(dbPath);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return true;
        }
        return false;
    }

    public int getServerPort() {
        return clientManagement.getServerPort();
    }

    public int getDBVersion() {
        try {
            return dataBaseHandler.getCurrentVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    public void transferDataBase() {
        HeartBeatEvent hbEvent = hbList.getOrderList().get(0);
        try {
            Socket socket = new Socket("localhost",hbEvent.getPortTcp());
            LOG.log("starting database transfer");
            new DataBaseSender(dbPath,socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
}
