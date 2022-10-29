package pt.isec.pd.Server;

import pt.isec.pd.Server.data.Server;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        Server server = new Server(Integer.parseInt(args[0]),args[1]);
    }
}
