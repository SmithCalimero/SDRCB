package pt.isec.pd.server.data;

import pt.isec.pd.server.data.database.CreateDataBase;
import pt.isec.pd.server.data.database.DBHandler;
import pt.isec.pd.server.threads.client.ClientManagement;
import pt.isec.pd.utils.Log;

import java.sql.SQLException;

public class Server {
    private final Log LOG = Log.getLogger(Server.class);
    private HeartBeatList hbList;
    private ClientManagement clientManagement;
    //private ClientController clientController;
    private final String dbPath;
    private HeartBeatController heartBeatController;
    private DBHandler dbHandler;

    public Server(int pingPort,String dbPath) {
        this.dbPath = dbPath;

        init(pingPort);
        start();
    }

    public void init(int pingPort) {
        hbList = new HeartBeatList();

        try {
            dbHandler = new DBHandler(dbPath);
        } catch (SQLException e) {
            LOG.log("DataBase could no be loaded");
            dbHandler = null;
        }

        heartBeatController = new HeartBeatController(hbList,this);
        clientManagement = new ClientManagement(pingPort, dbHandler,hbList,heartBeatController);

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
        if (dbHandler == null) {
            new CreateDataBase(dbPath);

            try {
                dbHandler = new DBHandler(dbPath);
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
            return dbHandler.getCurrentVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int getActiveConnections() {
        return clientManagement.getNumConnections();
    }

    public synchronized DBHandler getDbHandler() {
        return dbHandler;
    }
}
