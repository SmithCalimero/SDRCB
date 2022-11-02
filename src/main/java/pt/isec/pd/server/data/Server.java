package pt.isec.pd.server.data;

import pt.isec.pd.server.data.database.ClientController;
import pt.isec.pd.server.data.database.CreateDataBase;
import pt.isec.pd.server.data.database.DataBaseHandler;
import pt.isec.pd.server.threads.ClientPingHandler;
import pt.isec.pd.server.threads.DataBase.DataBaseSender;
import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.utils.Log;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class Server {
    private final Log LOG = Log.getLogger(Server.class);
    private final HeartBeatList hbList;
    private final ClientController clientController;
    private final String dbPath;
    private final HeartBeatController hbc;
    private DataBaseHandler db;
    private final ClientPingHandler clientPing;

    public Server(int port,String dbPath) {
        this.dbPath = dbPath;
        hbList = new HeartBeatList();

        try {
            db = new DataBaseHandler(dbPath);
        } catch (SQLException e) {
            LOG.log("DataBase could no be loaded");
            db = null;
        }

        clientController = new ClientController();
        hbc = new HeartBeatController(hbList,this);
        clientPing = new ClientPingHandler(port,LOG);
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

    public int getServerPort() {
        return clientController.getServerPort();
    }

    public int getDBVersion() {
        try {
            return db.getCurrentVersion();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDataBase() {
        HeartBeatEvent hbEvent = hbList.getOrderList().get(0);
        try {
            Socket socket = new Socket("localhost",hbEvent.getPortTcp());
            LOG.log("starting database transfer");
            new DataBaseSender(dbPath,socket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
