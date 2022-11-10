package pt.isec.pd.server.data.database;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.Show;
import pt.isec.pd.shared_data.Triple;
import pt.isec.pd.utils.Constants;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

public class DataBaseHandler {
    private final Connection connection;
    
    public DataBaseHandler(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        //Just to test if the path of the database actually exist; (Throws exception)
        getUsers(null);
    }

    public void getUsers(String whereName) throws SQLException {
        Statement statement = connection.createStatement();

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
        Statement statement = connection.createStatement();

        String sqlQuery = "PRAGMA user_version;";
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        int version = resultSet.getInt(1);

        System.out.println(version);
        statement.close();

        return version;
    }

    public void updateVersion() throws SQLException {
        Statement statement = connection.createStatement();

        String sqlQuery = "PRAGMA user_version = " + (getCurrentVersion() + 1);
        statement.execute(sqlQuery);

        System.out.println(getCurrentVersion());

        statement.close();
    }

    /*public void insertUser(String nome, String password,String username) throws SQLException {
        Statement statement = conn.createStatement();

        String sqlQuery = "INSERT INTO users VALUES (NULL,'" + nome + "','" + username + "','" + password + "')";
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void updateUser(int id, String name, String birthdate) throws SQLException {
        Statement statement = conn.createStatement();

        String sqlQuery = "UPDATE users SET name='" + name + "', " +
                "BIRTHDATE='" + birthdate + "' WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
    }

    public void close() throws SQLException {
        if (conn != null)
            conn.close();
    } */

    //======================  ACTIONS ======================
    public boolean register(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // TODO::
        //  client sends Strings and if this function returns true, he stores the data in his ClientData class
        //  the return is a boolean

        // 'id' is defined earlier because the users table can be empty
        int id = 0;
        boolean isAdmin = false;

        // 1) Receives clients register data
        // format: username, name, password
        Triple<String,String,String> data = (Triple<String,String,String>) ois.readObject();

        // 2) Database search
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT id, username, nome FROM utilizador"
        );

        while(result.next()) {
            id = result.getInt("id");
            String username = result.getString("username");
            String name = result.getString("nome");

            // 3) Validate client username & name
            if (username.equals(data.getFirst()) && name.equals(data.getSecond())) {
                System.out.println("User already exists...");
                return false;
            }
        }

        // 4) Verify if admin is registerings
        if (data.getFirst().equalsIgnoreCase("admin") &&
                data.getSecond().equalsIgnoreCase("admin"))
            isAdmin = true;

        try {
            // 5) Register user
            statement.executeUpdate(
                    "INSERT INTO utilizador(id,username,nome,password,admistrador,autenticado)"
                            + "VALUES("
                            + "'" + ++id + "',"
                            + "'" + data.getFirst() + "',"
                            + "'" + data.getSecond() + "',"
                            + "'" + data.getThird() + "',"
                            + "'" + isAdmin + "',"
                            + "'" + 0 + "')"
            );
        } catch (SQLException e) {
            System.out.println("Unable to register user [" + clientData.getUsername() + "]");
            return false;
        }

        statement.close();
        result.close();

        System.out.println("User[" + clientData.getUsername() + "] registered successfully...");
        return true;
    }

    public boolean login(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // 1) Receives clients password
        String sentPassword = (String) ois.readObject();

        // 2) Database search
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT username, nome, password, administrador, autenticado FROM utilizador"
        );

        while(result.next()) {
            String username = result.getString("username");
            String name = result.getString("nome");
            String password = result.getString("password");
            boolean admin = result.getBoolean("administrador");
            boolean authenticated = result.getBoolean("autenticado");

            // 3) Validate authentication
            if (authenticated) {
                System.out.println("This user[" + clientData.getUsername() + "] is already authenticated...");
                return false;
            }

            // 4) Validate username & password
            if (username.equals(clientData.getUsername()) && password.equals(sentPassword)) {
                statement.executeUpdate(
                        "UPDATE utilizador SET autenticado = 1 WHERE id = '" + clientData.getId() + "'"
                );
                System.out.println("User[" + clientData.getUsername() + "]  logged in successfully...");

                statement.close();
                result.close();
                return true;
            }
        }

        statement.close();
        result.close();
        return false;
    }

    public boolean editClientData(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {        // 2) Create connection
        Statement statement = connection.createStatement();

        switch(clientData.getAction()) {
            case EDIT_NAME -> {
                // 3) Receive the new name
                String newName = (String) ois.readObject();

                ResultSet resultSet = statement.executeQuery(
                        "SELECT nome FROM utilizador"
                );

                while(resultSet.next()) {
                    // 4) Verify if its a unique username
                    if (newName.equals(resultSet.getString("nome"))) {
                        System.out.println("This name[" + newName+ "] already exists...");
                        return false;
                    }
                }

                // 5) if unique=true, update value
                statement.executeUpdate(
                        "UPDATE utilizador SET nome = '" + newName + "' WHERE id = '" + clientData.getId() + "'"
                );
                return true;
            }
            case EDIT_USERNAME -> {
                // 3) Receive the new username
                String newUserName = (String) ois.readObject();

                ResultSet resultSet = statement.executeQuery(
                        "SELECT user_name FROM utilizador"
                );

                while(resultSet.next()) {
                    // 4) Verify if its a unique username
                    if (newUserName.equals(resultSet.getString("user_name"))) {
                        System.out.println("This username[" + newUserName+ "] already exists...");
                        return false;
                    }
                }

                // 5) if unique=true, update value
                statement.executeUpdate(
                        "UPDATE utilizador SET user_name = '" + newUserName + "' WHERE id = '" + clientData.getId() + "'"
                );
                return true;
            }
            case EDIT_PASSWORD -> {
                // 3) Receive the new password
                String newPassword = (String) ois.readObject();

                ResultSet resultSet = statement.executeQuery(
                        "SELECT user_name, password FROM utilizador WHERE id = '" + clientData.getUsername() + "'"
                );

                // 4) Verify if the password its the same
                if (newPassword.equals(resultSet.getString("password"))) {
                    System.out.println("The password cant be the same...");
                    return false;
                }

                // 5) Update password
                statement.executeUpdate(
                        "UPDATE utilizador SET password = '" + newPassword + "' WHERE id = '" + clientData.getId() + "'"
                );
                return true;
            }
        }
        return false;
    }

    public boolean consultPaymentsAwaiting(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // stores reservers to be sent to the user
        ArrayList<Reserve> reserves = new ArrayList<>();

        // 1) Database search reservations
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT * FROM reserva"
        );

        while(result.next()) {
            int id = result.getInt("id");
            String date = result.getString("data_hora");
            boolean paid = result.getBoolean("pago");
            int userId = result.getInt("id_utilizador");
            int showId = result.getInt("id_espetaculo");

            // 2) Validate client id
            if (clientData.getId() == userId && !paid) {
                try {
                    reserves.add(new Reserve(
                            id,
                            date,
                            paid,
                            userId,
                            showId
                    ));
                } catch (Exception e) {
                    System.out.println("Unable to consult unpaied reserves from user [" + clientData.getUsername() + "]");
                }
            }
        }

        statement.close();
        result.close();

        // 5) Send list to client
        oos.writeObject(reserves);

        if (reserves.isEmpty()) {
            System.out.println("No payments awating from user [" + clientData.getUsername() + "]");
            return false;
        }
        return true;
    }

    public boolean consultPayedReservations(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // stores reservers to be sent to the user
        ArrayList<Reserve> reserves = new ArrayList<>();

        // 1) Database search reservations
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT * FROM reserva"
        );

        while(result.next()) {
            int id = result.getInt("id");
            String date = result.getString("data_hora");
            boolean paid = result.getBoolean("pago");
            int userId = result.getInt("id_utilizador");
            int showId = result.getInt("id_espetaculo");

            // 2) Validate client id
            if (clientData.getId() == userId && paid) {
                try {
                    reserves.add(new Reserve(
                            id,
                            date,
                            paid,
                            userId,
                            showId
                    ));
                } catch (Exception e) {
                    System.out.println("Unable to consult paied reserves from user [" + clientData.getUsername() + "]");
                }
            }
        }

        statement.close();
        result.close();

        // 5) Send list to client
        oos.writeObject(reserves);

        if (reserves.isEmpty()) {
            System.out.println("No paied reservers from user [" + clientData.getUsername() + "]");
            return false;
        }
        return true;
    }

    public boolean consultShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // stores reservers to be sent to the user
        ArrayList<Show> shows = new ArrayList<>();

        // 1) Received filters from user
        HashMap<String,String> filters = (HashMap<String,String>) ois.readObject();

        // 2) Verify the type of info the user pretends to be searched
        for (var i : filters.entrySet()) {
            System.out.println(i.getKey() + ": " + i.getValue());

            // 3) Database search based on key and value
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(
                    "SELECT * FROM espetaculo WHERE '" + i.getKey() + "' LIKE '" + i.getValue() + "'"
            );

            // 4) Add objects to array
            while(result.next()) {
                try {
                    shows.add(new Show(
                            result.getInt("id"),
                            result.getString("descricao"),
                            result.getString("tipo"),
                            result.getString("data_hora"),
                            result.getString("duracao"),
                            result.getString("local"),
                            result.getString("localidade"),
                            result.getString("pais"),
                            result.getString("classificacao_etaria"),
                            result.getBoolean("visivel"))
                    );
                } catch (Exception e) {
                    System.out.println("Unable to consult show(s) from user [" + clientData.getUsername() + "]");
                    return false;
                }
            }

            statement.close();
            result.close();
        }

        // 5) Send list to client
        oos.writeObject(shows);

        if (shows.isEmpty()) {
            System.out.println("Shows not found based on the received filters...");
            return false;
        }
        return false;
    }

    public boolean selectShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException, ParseException {
        // 1) Receive show id
        Integer showId = (Integer)ois.readObject();

        // 2) Get show pt.isec.pd.server.data based on the received id
        Statement statement = connection.createStatement();
        ResultSet seats = statement.executeQuery(
                "SELECT data_hora FROM espetaculo WHERE id = '" + showId + "'"
        );

        String showDateStr = seats.getString("data_hora");

        // 3) Get the show date (assuming its formatted: "dd/MM/yyyy HH:mm:ss"
        Date showDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(showDateStr);

        // 4) Get the current day
        Date currentDate = new Date();

        // 5) Verify if remain at least 24 hours to the show
        Duration duration = Duration.between((Temporal) showDate,(Temporal) currentDate);
        String result = String.format("%d:%02d",duration.toHours(), duration.toMinutes());

        statement.close();
        seats.close();

        // 6) If at least 24 hours before the show, it can be selected
        if (duration.toHours() > 24)
            return true;

        System.out.println("Unable to select show[" + showId + "]. There are less than 24 hours before the show...");
        return false;
    }

    public boolean viewSeatsAndPrices(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {
        // 1) Receive show id
        Integer recShowId = (Integer)ois.readObject();

        // 2) Get unavailable seats
        ArrayList<Integer> unavailable = new ArrayList<>();

        Statement statement = connection.createStatement();
        ResultSet reservedSeats = statement.executeQuery(
                "SELECT id_lugar FROM reserva_lugar"
        );

        while(reservedSeats.next()) {
            int reservedSeatId = reservedSeats.getInt("id_lugar");
            unavailable.add(reservedSeatId);
        }

        // 3) Get available seats
        ArrayList<Seat> available = new ArrayList<>();

        ResultSet availableSeats = statement.executeQuery(
                "SELECT * FROM lugar"
        );

        while(availableSeats.next()) {
            int seatId = availableSeats.getInt("id");
            String row = availableSeats.getString("fila");
            String seat = availableSeats.getString("assento");
            double price = availableSeats.getDouble("preco");
            int showId = availableSeats.getInt("espetaculo_id");

            // ignore this seat, skips to next
            for (var u : unavailable)
                if (u == seatId)
                    continue;

            if (showId == recShowId) {
                available.add(new Seat(
                                seatId,
                                row,
                                seat,
                                price,
                                showId
                        )
                );
            }
        }

        // 4) Send available seats list to client
        oos.writeObject(available);

        System.out.println("Unable to view seat(s) and price(s) from user [" + clientData.getUsername() + "]");
        return false;
    }

    public boolean submitReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // 1) Receive reserve from client
        Pair<Reserve,ArrayList<Seat>> reserve = (Pair<Reserve,ArrayList<Seat>>)ois.readObject();

        try {
            // 2) Create connection
            Statement statement = connection.createStatement();

            // 3) Insert reserve
            statement.executeQuery(
                    "INSERT INTO reserva(id,data_hora,pago,id_utilizador,id_espetaculo)" +
                            "VALUES('"
                            + reserve.getKey().getId() + "','"
                            + reserve.getKey().getDateHour() + "','"
                            + reserve.getKey().isPaied() + "','"
                            + reserve.getKey().getUserId() + "','"
                            + reserve.getKey().getShowId() + "')"
            );

            // 4) Insert seats
            for (var s : reserve.getValue()) {
                statement.executeQuery(
                        "INSERT INTO reserva_lugar(id_reserva,id_lugar)" +
                                "VALUES('" + reserve.getKey().getId() + "','" + s.getId() + "')"
                );
            }

            System.out.println("Reservation submited with success from user [" + clientData.getUsername() + "]");
            return true;
        } catch(SQLException e) {
            System.out.println("Unable to submit reservation from user [" + clientData.getUsername() + "]");
            return false;
        }
    }

    public boolean deleteUnpaidReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // 1) Read the ID of the reservation to be deleted
        Integer reservationId = (Integer)ois.readObject();

        // 2) Database search
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT id, pago nome FROM reserva"
        );

        while(result.next()) {
            // 3) If the IDs match & paid=false, delete row
            if (reservationId == result.getInt("id") && !result.getBoolean("pago")) {
                statement.executeQuery(
                        "DELETE FROM reserva WHERE id = '" + reservationId + "'"
                );
                statement.close();
                result.close();
                System.out.println("Unpaid reservation id[" + reservationId + "] was deleted successfully...");
                return true;
            }
        }

        statement.close();
        result.close();
        System.out.println("Unable to delete unpaid reservation from user [" + clientData.getUsername() + "]");
        return false;
    }

    public boolean payReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException, ParseException {
        // 1) Receive reserve ID for payment
        Integer resId = (Integer) ois.readObject();

        // 2) Create connection
        Statement statement = connection.createStatement();

        // 3) Search reserver by id
        ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM reserva WHERE id = '" + resId + "'"
        );

        // 4) Verify if the time exceeds the limit to pay
        String date = resultSet.getString("data_hora");

        // 5) Get the reservation date (assuming its formatted: "dd/MM/yyyy HH:mm:ss")
        Date reserveDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date);

        // 6) Get the current day
        Date currentDate = new Date();

        // 7) Get the difference between the two dates
        long difference = currentDate.getTime() - reserveDate.getTime();

        // 8) Calculate time in seconds
        long differenceSeconds = (difference / 1000) % 60;

        // 6) Verify if the time limit was exceeded
        if (differenceSeconds > Constants.TIME_TO_PAY) {
            System.out.println("TIME EXCEEDED: Unable to pay reservation from user [" + clientData.getUsername() + "]");
            statement.close();
            resultSet.close();
            return false;
        }

        // 7) Set paid=true
        statement.executeQuery(
                "UPDATE reserva SET pago = '" + 1 + "' WHERE id = '" + resId + "'"
        );
        System.out.println("Reservation from user [" + clientData.getUsername() + "] was paid succesfully...");

        statement.close();
        resultSet.close();
        return true;
    }

    public static boolean insertShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) {
        System.out.println("Unable to insert show(s) from user [" + clientData.getUsername() + "]");
        return false;
    }

    public boolean deleteShow(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {
        // 1) Receive from client the ID of the show to be deleted
        Integer deleteShowId = (Integer)ois.readObject();

        // 2) Search reservations
        Statement statement = connection.createStatement();
        ResultSet reservations = statement.executeQuery(
                "SELECT * from reserva WHERE id_espetaculo = '" + deleteShowId + "'"
        );

        // 3) If there are no reservations associated, delete show from table
        if (!reservations.next()) {
            statement.executeQuery(
                    "DELETE FROM espetaculo WHERE id = '" + deleteShowId + "'"
            );
            System.out.println("No reservations for the show[" + deleteShowId + "]...");

            statement.close();
            reservations.close();
            return true;
        }

        // 4) Search reservations and verify if a paid reservation exists
        while(reservations.next()) {
            // 5) If a paid reservation exists, the show can't be deleted
            if (reservations.getBoolean("pago")) {
                System.out.println("Show[" + deleteShowId + "] already has a paid reservation...");

                statement.close();
                reservations.close();
                return false;
            }
        }

        // 6) There are no paid reservations associated, the show can be deleted
        statement.executeQuery(
                "DELETE FROM espetaculo WHERE id = '" + deleteShowId + "'"
        );
        System.out.println("Show[" + deleteShowId + "] was deleted successfully...");

        statement.close();
        reservations.close();
        return true;
    }

    public boolean disconnect(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // 1) Database search
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT id,username, nome, autenticado FROM utilizador"
        );

        while (result.next()) {
            int id = result.getInt("id");
            String username = result.getString("username");
            String name = result.getString("nome");
            boolean authenticated = result.getBoolean("autenticado");

            // 2) User found and its authenticated
            if (username.equals(clientData.getUsername()) && authenticated) {
                // 3) Update authenticated value
                statement.executeUpdate(
                        "UPDATE utilizador SET autenticado = 0 WHERE id = '" + id + "'"
                );
                System.out.println("User[" + clientData.getUsername() + "] logged out successfully...");

                statement.close();
                result.close();
                return true;
            }
        }

        statement.close();
        result.close();
        return false;
    }

    //======================  OTHER METHODS ======================
    private static boolean readFile(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String currentLine;
            String[] parts;

            while((currentLine = br.readLine()) != null) {
                // divide string
                parts = currentLine.split(";");

                // display string
                for(var p : parts)
                    System.out.print(p + " ");
                System.out.println();
            }

            br.close();
            System.out.println("File successfully read...");
            return true;
        } catch(IOException e) {
            System.out.println("File not found: " + e);
            return false;
        }
    }
}