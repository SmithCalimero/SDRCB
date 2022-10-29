package pt.isec.pa.Server.data;

import java.sql.SQLException;

public class Server {
    private final HeartBeatController hbc;
    private final DataBaseConn db;

    public Server(String dbPath) throws SQLException {
        db = new DataBaseConn(dbPath);
        hbc = new HeartBeatController();
    }
}
