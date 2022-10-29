package pt.isec.pa.Server;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        SqLiteDB sqLiteDB = new SqLiteDB(Integer.parseInt(args[0]),args[1]);
        sqLiteDB.getCurrentVersion();
        sqLiteDB.updateVersion();
    }
}
