package pt.isec.pa.Server;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        SqLiteDB sqLiteDB = new SqLiteDB(args[0]);
        sqLiteDB.getCurrentVersion();
        sqLiteDB.updateVersion();
    }
}
