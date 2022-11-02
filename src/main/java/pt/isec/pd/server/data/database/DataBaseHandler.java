package pt.isec.pd.server.data.database;

import java.sql.*;

public class DataBaseHandler {
    private final Connection dbConn;
    public DataBaseHandler(String path) throws SQLException {
        dbConn = DriverManager.getConnection("jdbc:sqlite:" + path);

        //Just to test if the path of the database actually exist; (Throws exception)
        listUsers(null);
    }

    public void close() throws SQLException {
        if (dbConn != null)
            dbConn.close();
    }

    public void listUsers(String whereName) throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, username, nome, password FROM utilizador";
        if (whereName != null)
            sqlQuery += " WHERE name like '%" + whereName + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String username = resultSet.getString("username");
            String nome = resultSet.getString("nome");
            String password = resultSet.getString("password");
        }

        resultSet.close();
        statement.close();
    }

    public int getCurrentVersion() throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "PRAGMA user_version;";
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        int version = resultSet.getInt(1);

        System.out.println(version);
        statement.close();

        return version;
    }

    public void updateVersion() throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "PRAGMA user_version = " + (getCurrentVersion() + 1);
        statement.execute(sqlQuery);

        System.out.println(getCurrentVersion());

        statement.close();
    }

    public void insertUser(String nome, String password,String username) throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "INSERT INTO users VALUES (NULL,'" + nome + "','" + username + "','" + password + "')";
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void updateUser(int id, String name, String birthdate) throws SQLException {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "UPDATE users SET name='" + name + "', " +
                "BIRTHDATE='" + birthdate + "' WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
    }
}