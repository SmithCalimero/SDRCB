package pt.isec.pd.server.data;

import pt.isec.pd.server.data.database.CreateDataBase;
import pt.isec.pd.server.data.database.DBHandler;
import pt.isec.pd.server.threads.client.ClientManagement;
import pt.isec.pd.server.threads.client.ClientReceiveMessage;
import pt.isec.pd.utils.Log;

import java.sql.SQLException;
import java.util.List;

public class Server {
    private final Log LOG = Log.getLogger(Server.class);
    private HeartBeatList hbList;
    private ClientManagement cm;
    private final String dbPath;
    private HeartBeatController hbController;
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

        hbController = new HeartBeatController(hbList,this);
        cm = new ClientManagement(pingPort, dbHandler,hbList, hbController);
    }

    public void start() {
        hbController.start();

        cm.startPingHandler();
        cm.start();
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
        return cm.getServerPort();
    }

    public int getDBVersion() {
        try {
            return dbHandler.getCurrentVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int getActiveConnections() {
        return cm.getNumConnections();
    }

    public synchronized DBHandler getDbHandler() {
        return dbHandler;
    }

    public synchronized List<ClientReceiveMessage> getClients() {
        return cm.getClientsThread();
    }
}
