package pt.isec.pd.server.data;

import pt.isec.pd.server.connection.HeartBeatController;
import pt.isec.pd.server.connection.ClientPingHandler;
import pt.isec.pd.utils.Log;

import java.sql.SQLException;

public class Server {
    private final Log LOG = Log.getLogger(Server.class);
    private final HeartBeatController hbc;
    private final DataBaseHandler db;
    private final ClientPingHandler cc;

    public Server(int port,String dbPath) throws SQLException {
        db = new DataBaseHandler(dbPath);
        hbc = new HeartBeatController();
        cc = new ClientPingHandler(port,LOG);
    }
}
