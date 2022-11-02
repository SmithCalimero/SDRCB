package pt.isec.pd.server.data;

import pt.isec.pd.server.data.database.CreateDataBase;
import pt.isec.pd.server.data.database.DataBaseHandler;
import pt.isec.pd.server.threads.ClientPingHandler;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;

public class Server {
    private final Log LOG = Log.getLogger(Server.class);
    private final HeartBeatList hbList;
    private final ServerSocket serverSocket;
    private final String dbPath;
    private final HeartBeatController hbc;
    private DataBaseHandler db;
    private final ClientPingHandler cc;

    public Server(int port,String dbPath) {
        this.dbPath = dbPath;
        hbList = new HeartBeatList();

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            db = new DataBaseHandler(dbPath);
        } catch (SQLException e) {
            LOG.log("DataBase could no be loaded");
            db = null;
        }

        hbc = new HeartBeatController(hbList,this);
        cc = new ClientPingHandler(port,LOG);
    }

    public boolean createDataBase() {
        if (db == null) {
            new CreateDataBase(dbPath);

            try {
                db = new DataBaseHandler(dbPath);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return true;
        }
        return false;
    }

    public int getTcpPort() {
        return serverSocket.getLocalPort();
    }

    public int getDBVersion() {
        try {
            return db.getCurrentVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
