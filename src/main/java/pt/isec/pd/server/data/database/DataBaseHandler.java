package pt.isec.pd.server.data.database;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.Show;
import pt.isec.pd.shared_data.Triple;
import pt.isec.pd.utils.Constants;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.Date;

public class DataBaseHandler {
    private final Log LOG = Log.getLogger(DataBaseHandler.class);
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
    public synchronized void register(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // TODO::
        //  client:
        //      sends Triple with register data and if this function returns true,
        //      stores the data in his ClientData class
        //  server:
        //      the return is a boolean (requestAccepted=return)

        int id = 0;     // 'id' is defined earlier because the users table can be empty
        boolean isAdmin = false;
        boolean isAuthenticated = false;
        boolean requestAccepted = true;
        String msg = "";

        // Receives clients register data (format: username, name, password)
        Triple<String,String,String> data = (Triple<String,String,String>) ois.readObject();

        // Create a statement
        Statement statement = connection.createStatement();

        // Execute a query
        ResultSet result = statement.executeQuery(
                "SELECT * FROM utilizador"
        );

        // Verify if admin is registered
        if (data.getFirst().equalsIgnoreCase("admin") &&
                data.getSecond().equalsIgnoreCase("admin"))
            isAdmin = true;

        // If table has registered users, verify if username and name are unique
        while (result.next()) {
            id = result.getInt("id");
            String username = result.getString("username");
            String name = result.getString("nome");

            // Validate client username & name
            if (username.equals(data.getFirst()) && name.equals(data.getSecond())) {
                msg = "User[" + data.getFirst() + "] already exists";
                LOG.log(msg);
                requestAccepted = false;
                break;
            }
        }

        if (requestAccepted) {
            try {
                // Register user
                int rs = statement.executeUpdate(
                        "INSERT INTO utilizador(id,username,nome,password,administrador,autenticado)"
                                + "VALUES("
                                + "'" + ++id + "',"
                                + "'" + data.getFirst() + "',"
                                + "'" + data.getSecond() + "',"
                                + "'" + data.getThird() + "',"
                                + "'" + isAdmin + "',"
                                + "'" + isAuthenticated + "')"
                );
                if (rs == 1) {
                    LOG.log("User[" + data.getFirst() + "] has registered successfully");
                    clientData.setId(id);
                }
            } catch(SQLException e) {
                msg = "Unable to register user[" + data.getFirst() + "]";
                LOG.log(msg);
            } finally {
                statement.close();
                result.close();
            }
        }
        oos.writeObject(msg);
    }

    public synchronized void login(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException,IOException, ClassNotFoundException {
        boolean requestAccepted = false;
        boolean isAuthenticated = false;
        boolean isAdmin = false;

        // Receives clients data (format: username, password)
        Pair<String,String> loginData = (Pair<String,String>) ois.readObject();

        // Database search
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
                "SELECT id, username, password, administrador, autenticado FROM utilizador"
        );

        // If table has registered users
        while (result.next()) {
            int id = result.getInt("id");
            String username = result.getString("username");
            String password = result.getString("password");
            isAdmin = result.getBoolean("administrador");
            boolean authenticated = result.getBoolean("autenticado");

            // Validate login data and authentication
            if (username.equals(loginData.getKey()) && password.equals(loginData.getValue()) && authenticated) {
                isAuthenticated = true;
                break;
            }
            // Validate username & password
            else if (username.equals(loginData.getKey()) && password.equals(loginData.getValue()) && !authenticated) {
                requestAccepted = true;
                clientData.setId(id);
                break;
            }
        }

        oos.writeObject(clientData.getId());

        String msg;
        // The user was not found
        if (requestAccepted) {
            try {
                int rs = statement.executeUpdate(
                        "UPDATE utilizador SET autenticado = 1 WHERE id = '" + clientData.getId() + "'"
                );
                if (rs == 1) {
                    msg = "User[" + loginData.getKey() + "]  logged in successfully";
                    LOG.log(msg);
                    oos.writeObject(new Triple<>(true,isAdmin,msg));
                }
            } catch (SQLException e) {
                msg = "Unable to login user[" + loginData.getKey() + "]";
                LOG.log(msg);
                oos.writeObject(new Triple<>(false,isAdmin,msg));
            } finally {
                statement.close();
                result.close();
            }
            return;
        }

        if (!isAuthenticated) {
            msg = "The username " + loginData.getKey() + " or password are incorrect";
            LOG.log(msg);
        } else  {
            msg = "This user " + loginData.getKey() + " is already authenticated";
            LOG.log(msg);
        }

        oos.writeObject(new Triple<>(false,null,msg));
        statement.close();
        result.close();
    }

    public synchronized void editClientData(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // Create statement
        Statement statement = connection.createStatement();
        boolean requestAccepted = true;

        // Receive the new value from client
        String newValue = (String) ois.readObject();

        switch (clientData.getAction()) {
            case EDIT_NAME -> {
                ResultSet result = statement.executeQuery(
                        "SELECT nome FROM utilizador"
                );

                while (result.next()) {
                    // Verify if its a unique username
                    if (newValue.equals(result.getString("nome"))) {
                        LOG.log("This name[" + newValue + "] already exists...");
                        requestAccepted = false;
                    }
                }

                // if unique=true, update value
                if (requestAccepted) {
                    try {
                        int rs = statement.executeUpdate(
                                "UPDATE utilizador SET nome = '" + newValue + "' WHERE id = '" + clientData.getId() + "'"
                        );
                        if (rs == 1) {
                            LOG.log("Name updated successfully");
                        }
                    } catch(SQLException e) {
                        LOG.log("Unable to update name[" + newValue + "]");
                    } finally {
                        result.close();
                    }
                }
            }
            case EDIT_USERNAME -> {
                ResultSet result = statement.executeQuery(
                        "SELECT username FROM utilizador"
                );

                while (result.next()) {
                    // Verify if its a unique username
                    if (newValue.equals(result.getString("username"))) {
                        LOG.log("This username[" + newValue + "] already exists...");
                        requestAccepted = false;
                    }
                }

                // if unique=true, update value
                if (requestAccepted) {
                    try {
                        int rs = statement.executeUpdate(
                                "UPDATE utilizador SET username = '" + newValue + "' WHERE id = '" + clientData.getId() + "'"
                        );
                        if (rs == 1) {
                            LOG.log("Username updated successfully");
                        }
                    } catch(SQLException e) {
                        LOG.log("Unable to update username[" + newValue + "]");
                    } finally {
                        result.close();
                    }
                }

                result.close();
            }
            case EDIT_PASSWORD -> {
                ResultSet result = statement.executeQuery(
                        "SELECT password FROM utilizador WHERE id = '" + clientData.getId() + "'"
                );

                // Verify if the password it's the same
                if (newValue.equals(result.getString("password"))) {
                    LOG.log("The password cant be the same");
                    requestAccepted = false;
                }

                // Update password
                if (requestAccepted) {
                    try {
                        int rs = statement.executeUpdate(
                                "UPDATE utilizador SET password = '" + newValue + "' WHERE id = '" + clientData.getId() + "'"
                        );
                        if (rs == 1) {
                            LOG.log("Password updated successfully");
                        }
                    } catch(SQLException e) {
                        LOG.log("Unable to update password");
                    } finally {
                        result.close();
                    }
                }
            }
        }

        if (statement != null) statement.close();
    }

    public synchronized void consultPaymentsAwaiting(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // Stores reserves awaiting payment to be sent to the user
        ArrayList<Reserve> reserves = new ArrayList<>();

        // Create statement
        Statement statement = connection.createStatement();

        // Execute query to search reserves
        ResultSet result = statement.executeQuery(
                "SELECT * FROM reserva"
        );

        // Execute query to get the clients name
        ResultSet clientName = statement.executeQuery(
                "SELECT username FROM utilizadores WHERE id = '" + clientData.getId() + "'"
        );

        while(result.next()) {
            int id = result.getInt("id");
            String date = result.getString("data_hora");
            boolean paid = result.getBoolean("pago");
            int userId = result.getInt("id_utilizador");
            int showId = result.getInt("id_espetaculo");

            // Validate client id & verify if the reserve is unpaid
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
                    LOG.log("Unable to consult unpaid reserves from user [" + clientName + "]");
                }
            }
        }

        if (reserves.isEmpty())
            LOG.log("No payments awaiting from user [" + clientName + "]");

        // Send list to client
        oos.writeObject(reserves);

        statement.close();
        result.close();
        if (clientName != null) clientName.close();
    }

    public synchronized void consultPayedReservations(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // Stores reserves awaiting payment to be sent to the user
        ArrayList<Reserve> reserves = new ArrayList<>();

        // Create statement
        Statement statement = connection.createStatement();

        // Execute query to search reserves
        ResultSet result = statement.executeQuery(
                "SELECT * FROM reserva"
        );

        // Execute query to get the clients name
        ResultSet clientName = statement.executeQuery(
                "SELECT username FROM utilizadores WHERE id = '" + clientData.getId() + "'"
        );

        while(result.next()) {
            int id = result.getInt("id");
            String date = result.getString("data_hora");
            boolean paid = result.getBoolean("pago");
            int userId = result.getInt("id_utilizador");
            int showId = result.getInt("id_espetaculo");

            // Validate client id & verify if the reserve is paid
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
                    LOG.log("Unable to consult paid reserves from user [" + clientName + "]");
                    e.printStackTrace();
                }
            }
        }

        if (reserves.isEmpty())
            LOG.log("No payments awaiting from user [" + clientName + "]");

        // Send list to client
        oos.writeObject(reserves);

        statement.close();
        result.close();
        if (clientName != null) clientName.close();
    }

    public synchronized void consultShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // stores reserves to be sent to the user
        ArrayList<Show> shows = new ArrayList<>();

        // Received filters from user
        HashMap<String,String> filters = (HashMap<String,String>) ois.readObject();

        // Create statement
        Statement statement = connection.createStatement();

        // Execute query to get the clients name
        ResultSet clientName = statement.executeQuery(
                "SELECT username FROM utilizadores WHERE id = '" + clientData.getId() + "'"
        );

        // Verify the type of info the user pretends to be searched
        for (var i : filters.entrySet()) {
            System.out.println(i.getKey() + ": " + i.getValue());

            // Get all the shows from DB
            ResultSet result = statement.executeQuery(
                    "SELECT * FROM espetaculo WHERE '" + i.getKey() + "' LIKE '%" + i.getValue() + "%'"
            );

            // Add objects to array
            while(result.next()) {
                shows.add(new Show(
                        result.getInt("id"),
                        result.getString("descricao"),
                        result.getString("tipo"),
                        result.getString("data_hora"),
                        result.getInt("duracao"),
                        result.getString("local"),
                        result.getString("localidade"),
                        result.getString("pais"),
                        result.getString("classificacao_etaria"),
                        result.getBoolean("visivel"))
                );
                result.close();
            }
        }

        if (shows.isEmpty())
            LOG.log("Shows not found based on the received filters");

        // Send list to client
        oos.writeObject(shows);

        statement.close();
        if (clientName != null) clientName.close();
    }

    public synchronized void selectShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException, ParseException {
        //  When client clicks a button to select a show this function returns a list of available shows
        //  Prevents: client click and then server says not available
        //  This way he only sees what's available

        // List to store available shows to send to the client
        ArrayList<Show> availableShows = new ArrayList<>();

        // Create statement
        Statement statement = connection.createStatement();

        // Execute query to get the shows
        ResultSet result = statement.executeQuery(
                "SELECT * FROM espetaculo"
        );

        while (result.next()) {
            int id = result.getInt("id");
            String description = result.getString("descricao");
            String type = result.getString("tipo");
            String date = result.getString("data_hora");
            int duration = result.getInt("duracao");
            String local = result.getString("local");
            String locality = result.getString("localidade");
            String country = result.getString("pais");
            String ageClassification = result.getString("classificacao_etaria");
            boolean visible = result.getBoolean("visivel");

            // If the show is visible
            if (visible) {
                // Get the show date (assuming its formatted: "dd/MM/yyyy HH:mm:ss")
                Date showDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date);

                // Get the current day
                Date currentDate = new Date();

                // Verify if remain at least 24 hours before the show
                Duration difference = Duration.between((Temporal) showDate,(Temporal) currentDate);
                String difResult = String.format("%d:%02d",difference.toHours(), difference.toMinutes());

                // If at least 24 hours before the show, it can be selected
                if (difference.toHours() < 24) {
                    availableShows.add(new Show(
                            id,
                            description,
                            type,
                            date,
                            duration,
                            local,
                            locality,
                            country,
                            ageClassification,
                            visible
                    ));
                }
            }
        }

        // Send available shows to user
        oos.writeObject(availableShows);

        if (availableShows.isEmpty())
            LOG.log("No shows available at the moment");

        statement.close();
        result.close();
    }

    public synchronized void viewSeatsAndPrices(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {
        try {
            // At this moment the client has already selected the show, so we search by the show id
            // Receive show id
            Integer recShowId = (Integer) ois.readObject();

            // Get unavailable seats ids
            ArrayList<Integer> unavailable = new ArrayList<>();

            Statement statement = connection.createStatement();
            ResultSet reservedSeats = statement.executeQuery(
                    "SELECT id_lugar FROM reserva_lugar"
            );

            while (reservedSeats.next()) {
                int reservedSeatId = reservedSeats.getInt("id_lugar");
                unavailable.add(reservedSeatId);
            }

            // Get available seats
            ArrayList<Seat> available = new ArrayList<>();

            ResultSet availableSeats = statement.executeQuery(
                    "SELECT * FROM lugar"
            );

            while (availableSeats.next()) {
                int seatId = availableSeats.getInt("id");
                String row = availableSeats.getString("fila");
                String seat = availableSeats.getString("assento");
                double price = availableSeats.getDouble("preco");
                int showId = availableSeats.getInt("espetaculo_id");

                // ignore this seat, skips to next
                if (unavailable.contains(seatId))
                    continue;

                if (showId == recShowId) {
                    available.add(new Seat(
                            seatId,
                            row,
                            seat,
                            price,
                            showId
                    ));
                }
            }

            // Send available seats list to client
            oos.writeObject(available);

            ResultSet username = statement.executeQuery(
                    "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
            );

            /*if (available.isEmpty())
                LOG.log("No available seats for the selected show[" + recShowId + "] by user[" + username + "]");
            else
                LOG.log("Available seats for the selected show[" + recShowId + "] sent to user[" + username + "]");
            username.close();
            */

            statement.close();
            availableSeats.close();
            reservedSeats.close();
        } catch(IOException e) {
            LOG.log("Unable to read show id from client");
        }
    }

    public synchronized boolean submitReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {
        int id = 0;
        Date currentTime = new Date();  // Get current time of the server to set in reserve object
        boolean isPaid = false;

        // Receive reserve from client  (format: showId, List<Seat>
        Pair<Integer,ArrayList<Seat>> reserve = (Pair<Integer,ArrayList<Seat>>)ois.readObject();

        // Create statement
        Statement statement = connection.createStatement();

        // Execute a query to get all reserves id
        ResultSet reserves = statement.executeQuery(
                "SELECT id FROM reserva"
        );

        // Execute a query to get the clients name
        ResultSet username = statement.executeQuery(
                "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
        );

        // Verify if reserves table is empty
        if (reserves.next())
            while (reserves.next())
                id = reserves.getInt("id");

        try {
            // Insert reserve
            statement.executeQuery(
                    "INSERT INTO reserva(id,data_hora,pago,id_utilizador,id_espetaculo)" +
                            "VALUES('"
                            + ++id + "','"
                            + currentTime + "','"
                            + isPaid + "','"
                            + clientData.getId() + "','"
                            + reserve.getKey() + "')"
            );

            // Insert seats
            for (var s : reserve.getValue()) {
                statement.executeQuery(
                        "INSERT INTO reserva_lugar(id_reserva,id_lugar)" +
                                "VALUES('" + reserve.getKey() + "','" + s.getId() + "')"
                );
            }

            LOG.log("Reservation submitted with success from user [" + username + "]");
            oos.writeObject(true);
            return true;
        } catch (SQLException e) {
            LOG.log("Unable to submit reservation from user[" + username + "]");
            oos.writeObject(false);
            return false;
        } finally {
            statement.close();
            reserves.close();
            if (username != null) username.close();
            return false;
        }
    }

    public synchronized void deleteUnpaidReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // Read the ID of the reserve to be deleted
        Integer reservationId = (Integer)ois.readObject();

        // Create statement
        Statement statement = connection.createStatement();

        // Execute a query to get reserves data
        ResultSet result = statement.executeQuery(
                "SELECT id, pago, id_utilizador FROM reserva"
        );

        // Execute a query to get the clients name
        ResultSet username = statement.executeQuery(
                "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
        );

        while(result.next()) {
            // If the IDs match & paid=false, delete row
            if (reservationId == result.getInt("id") &&
                    !result.getBoolean("pago") &&
                    clientData.getId() == result.getInt("id_utilizador")) {
                try {
                    statement.executeQuery(
                            "DELETE FROM reserva WHERE id = '" + reservationId + "'"
                    );
                    LOG.log("User[" + username + "] deleted unpaid successfully reservation id[" + reservationId + "]");
                    oos.writeObject(true);
                } catch(SQLException e) {
                    LOG.log("Unable to delete unpaid reservation from user [" + username + "]");
                    oos.writeObject(false);
                } finally {
                    statement.close();
                    result.close();
                    if (username != null) username.close();
                }
            }
        }
    }

    public synchronized void payReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException, ParseException {
        // Receive reserve ID for payment
        Integer resId = (Integer) ois.readObject();

        // Create connection
        Statement statement = connection.createStatement();

        // Search reserve by id
        ResultSet result = statement.executeQuery(
                "SELECT * FROM reserva WHERE id = '" + resId + "'"
        );

        // Verify if the time exceeds the limit to pay
        String date = result.getString("data_hora");

        // Get the reservation date (assuming its formatted: "dd/MM/yyyy HH:mm:ss")
        Date reserveDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(date);

        // Get the current day
        Date currentDate = new Date();

        // Get the difference between the two dates
        long difference = currentDate.getTime() - reserveDate.getTime();

        // Calculate time in seconds
        long differenceSeconds = (difference / 1000) % 60;

        // Get username
        ResultSet usernameRes = statement.executeQuery(
                "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
        );
        String username = usernameRes.getString("username");

        // Verify if the time limit was exceeded
        if (differenceSeconds > Constants.TIME_TO_PAY) {
            LOG.log("TIME EXCEEDED: Unable to pay reservation from user [" + username + "]");
            oos.writeObject(false);
        } else {
            // Set paid=true
            statement.executeQuery(
                    "UPDATE reserva SET pago = '" + 1 + "' WHERE id = '" + resId + "'"
            );
            LOG.log("Reservation from user [" + username + "] was paid successfully...");
            oos.writeObject(true);
        }

        statement.close();
        result.close();
        usernameRes.close();
    }

    public synchronized boolean insertShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {
        // Receive the new value from client
        String filePath = (String) ois.readObject();
        Pair<Show, Map<String, List<Seat>>> mapShows = Utils.readFile(filePath);

        // Create connection
        Statement statement = connection.createStatement();

        // Add Show
        statement.executeUpdate(
                "INSERT INTO espetaculo(descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria) "
                        + "VALUES("
                        + "'" + mapShows.getKey().getDescription() + "',"
                        + "'" + mapShows.getKey().getType() + "',"
                        + "'" + mapShows.getKey().getDateHour() + "',"
                        + "'" + mapShows.getKey().getDuration() + "',"
                        + "'" + mapShows.getKey().getLocation() + "',"
                        + "'" + mapShows.getKey().getLocality() + "',"
                        + "'" + mapShows.getKey().getCountry() + "',"
                        + "'" + mapShows.getKey().getAgeClassification() + "')"
        );

        //Get id of show
        ResultSet id = statement.executeQuery(
                "SELECT id FROM espetaculo WHERE descricao = '" + clientData.getId() + "'"
        );

        Map<String, List<Seat>> seats= mapShows.getValue();
        //Go through all the rows
        for (String key : seats.keySet()) {
            //Go through all the seats
            for (Seat seat : seats.get(key)) {
                statement.executeUpdate(
                        "INSERT INTO lugar(fila,assento,preco,espetaculo_id) "
                                + "VALUES ("
                                + "'" + seat.getRow() + "',"
                                + "'" + seat.getPrice() + "',"
                                + "'" + id.getInt(0) + "')");
            }
        }
        return false;
    }

    public synchronized void deleteShow(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException, SQLException {
        boolean hasPaidReserve = false;

        // Create statement
        Statement statement = connection.createStatement();

        // Execute a query to get the clients data
        ResultSet isAdmin = statement.executeQuery(
                "SELECT username, nome FROM utilizador WHERE id = '" + clientData.getId() + "'"
        );

        // Verify if the client has admin privilege
        if (isAdmin.getString("username").equalsIgnoreCase("admin") &&
                isAdmin.getString("nome").equalsIgnoreCase("admin")) {
            // Receive from client the ID of the show to be deleted
            Integer deleteShowId = (Integer)ois.readObject();

            // Search reservations
            ResultSet reservations = statement.executeQuery(
                    "SELECT * from reserva WHERE id_espetaculo = '" + deleteShowId + "'"
            );

            // If there are no reservations associated
            if (!reservations.next())
                hasPaidReserve = false;
            else {
                // Search reservations and verify if a paid reservation exists
                while(reservations.next()) {
                    // If a paid reserve exists, the show can't be deleted
                    if (reservations.getBoolean("pago")) {
                        LOG.log("Show[" + deleteShowId + "] already has a paid reservation...");
                        oos.writeObject(false);
                        hasPaidReserve = true;
                    }
                }

                // If there are no paid reservations associated, the show can be deleted
                if (!hasPaidReserve) {
                    try {
                        statement.executeQuery(
                                "DELETE FROM espetaculo WHERE id = '" + deleteShowId + "'"
                        );
                        LOG.log("Show[" + deleteShowId + "] was deleted successfully...");
                        oos.writeObject(true);
                    } catch(SQLException e) {
                        LOG.log("Unable to delete show[" + deleteShowId + "]");
                        oos.writeObject(false);
                    } finally {
                        statement.close();
                        reservations.close();
                        isAdmin.close();
                    }
                }
            }
        } else {
            LOG.log("Unable to delete show. Only the admin can execute this function");
            oos.writeObject(false);
            statement.close();
            isAdmin.close();
        }
    }

    public synchronized void disconnect(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        // Create statement
        Statement statement = connection.createStatement();
        // Execute a query to get the clients data
        ResultSet result = statement.executeQuery(
                "SELECT id,username, nome, autenticado FROM utilizador"
        );

        while (result.next()) {
            int id = result.getInt("id");
            String username = result.getString("username");
            boolean authenticated = result.getBoolean("autenticado");

            // User found and its authenticated
            if (clientData.getId() == id && authenticated) {
                try {
                    // Update authenticated value
                    statement.executeUpdate(
                            "UPDATE utilizador SET autenticado = 0 WHERE id = '" + id + "'"
                    );
                    LOG.log("User[" + username + "] logged out successfully");
                    oos.writeObject(true);
                } catch (SQLException e) {
                    LOG.log("Unable to logout user[" + username + "]");
                    oos.writeObject(false);
                } finally {
                    statement.close();
                    result.close();
                }
            }
        }
    }

    //======================  OTHER METHODS ======================
    private synchronized boolean readFile(String filePath) {
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
            LOG.log("File successfully read...");
            return true;
        } catch(IOException e) {
            LOG.log("File not found: " + e);
            return false;
        }
    }
}