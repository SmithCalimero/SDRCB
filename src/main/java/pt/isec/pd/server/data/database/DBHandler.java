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
import java.util.concurrent.TimeUnit;

public class DBHandler {
    private final Log LOG = Log.getLogger(DBHandler.class);
    private Connection connection;

    public DBHandler(String path) throws SQLException {
        // Test if the path of the database actually exist; (Throws exception)
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch(SQLException e) {
            LOG.log("Error connecting to database: " + e);
        }
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

    //======================  ACTIONS ======================
    public synchronized String register(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        int id = 0;     // 'id' is defined earlier because the users table can be empty
        int isAdmin = 0;
        boolean isAuthenticated = false;
        boolean requestAccepted = true;
        String msg = "";
        String querry = "";

        try {
            // Receives clients register data (format: username, name, password)
            Triple<String, String, String> data = (Triple<String, String, String>) clientData.getData();

            try {
                // Create a statement
                Statement statement = connection.createStatement();

                // Execute a query
                ResultSet result = statement.executeQuery(
                        "SELECT * FROM utilizador"
                );

                // Verify if admin is registered
                if (data.getFirst().equalsIgnoreCase("admin") &&
                        data.getSecond().equalsIgnoreCase("admin"))
                    isAdmin = 1;

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
                        querry = "INSERT INTO utilizador(id,username,nome,password,administrador,autenticado)"
                                + "VALUES("
                                + "'" + ++id + "',"
                                + "'" + data.getFirst() + "',"
                                + "'" + data.getSecond() + "',"
                                + "'" + data.getThird() + "',"
                                + "'" + isAdmin + "',"
                                + "'" + isAuthenticated + "')";
                        int rs = statement.executeUpdate(querry);
                        if (rs == 1) {
                            LOG.log("User[" + data.getFirst() + "] has registered successfully");
                            clientData.setId(id);
                        }
                    } catch (SQLException e) {
                        msg = "Unable to register user[" + data.getFirst() + "]";
                        LOG.log(msg);
                    } finally {
                        statement.close();
                        result.close();
                    }
                }
                oos.writeObject(msg);
            } catch (SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }
        return querry;
    }

    public synchronized String login(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        boolean requestAccepted = false;
        boolean isAuthenticated = false;
        boolean isAdmin = false;
        String msg = "";
        String query = "";
        try {
            // Receives clients data (format: username, password)
            Pair<String, String> loginData = (Pair<String, String>) clientData.getData();

            try {
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

                // The user was not found
                if (requestAccepted) {
                    try {
                        query = "UPDATE utilizador SET autenticado = 1 WHERE id = '" + clientData.getId() + "'";
                        int rs = statement.executeUpdate(query);
                        if (rs == 1) {
                            msg = "User[" + loginData.getKey() + "]  logged in successfully";
                            LOG.log(msg);
                            oos.writeObject(new Triple<>(true, isAdmin, msg));
                        }
                    } catch (SQLException e) {
                        msg = "Unable to login user[" + loginData.getKey() + "]";
                        LOG.log(msg);
                        oos.writeObject(new Triple<>(false, isAdmin, msg));
                    } finally {
                        statement.close();
                        result.close();
                    }
                    return query;
                }

                if (!isAuthenticated) {
                    msg = "The username " + loginData.getKey() + " or password are incorrect";
                    LOG.log(msg);
                } else {
                    msg = "This user " + loginData.getKey() + " is already authenticated";
                    LOG.log(msg);
                }

                oos.writeObject(new Triple<>(false, null, msg));
                statement.close();
                result.close();
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String editClientData(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        boolean requestAccepted = true;
        String msg = "";

        try {
            // Receive the new value from client
            String newValue = (String) ois.readObject();

            try {
                // Create statement
                Statement statement = connection.createStatement();

                switch (clientData.getAction()) {
                    case EDIT_NAME -> {
                        ResultSet result = statement.executeQuery(
                                "SELECT nome FROM utilizador"
                        );

                        while (result.next()) {
                            // Verify if its a unique username
                            if (newValue.equals(result.getString("nome"))) {
                                msg = "This name[" + newValue + "] already exists...";
                                LOG.log(msg);
                                requestAccepted = false;
                                oos.writeObject(msg);
                            }
                        }

                        // if unique=true, update value
                        if (requestAccepted) {
                            try {
                                int rs = statement.executeUpdate(
                                        "UPDATE utilizador SET nome = '" + newValue + "' WHERE id = '" + clientData.getId() + "'"
                                );
                                if (rs == 1) {
                                    msg = "Name updated successfully";
                                    LOG.log(msg);
                                    oos.writeObject(msg);
                                }
                            } catch (SQLException e) {
                                msg = "Unable to update name[" + newValue + "]";
                                LOG.log(msg);
                                oos.writeObject(msg);
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
                                msg = "This username[" + newValue + "] already exists...";
                                LOG.log(msg);
                                oos.writeObject(msg);
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
                                    msg = "Username updated successfully";
                                    LOG.log(msg);
                                    oos.writeObject(msg);
                                }
                            } catch (SQLException e) {
                                msg = "Unable to update username[" + newValue + "]";
                                LOG.log(msg);
                                oos.writeObject(msg);
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
                            msg = "The password cant be the same";
                            LOG.log(msg);
                            oos.writeObject(msg);
                            requestAccepted = false;
                        }

                        // Update password
                        if (requestAccepted) {
                            try {
                                int rs = statement.executeUpdate(
                                        "UPDATE utilizador SET password = '" + newValue + "' WHERE id = '" + clientData.getId() + "'"
                                );
                                if (rs == 1) {
                                    msg = "Password updated successfully";
                                    LOG.log(msg);
                                    oos.writeObject(msg);
                                }
                            } catch (SQLException e) {
                                msg = "Unable to update password";
                                LOG.log(msg);
                                oos.writeObject(msg);
                            } finally {
                                result.close();
                            }
                        }
                    }
                }

                if (statement != null) statement.close();
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException | ClassNotFoundException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String consultPaymentsAwaiting(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
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

        return "";
    }

    public synchronized String consultPayedReservations(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        // Stores reserves awaiting payment to be sent to the user
        ArrayList<Reserve> reserves = new ArrayList<>();
        String msg = "";

        try {
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

            while (result.next()) {
                int id = result.getInt("id");
                String date = result.getString("data_hora");
                boolean paid = result.getBoolean("pago");
                int userId = result.getInt("id_utilizador");
                int showId = result.getInt("id_espetaculo");

                // Validate client id & verify if the reserve is paid
                if (clientData.getId() == userId && paid) {
                    reserves.add(new Reserve(
                            id,
                            date,
                            paid,
                            userId,
                            showId
                    ));
                }
            }

            if (reserves.isEmpty())
                LOG.log("No payments awaiting from user [" + clientName + "]");

            // Send list to client
            oos.writeObject(reserves);

            statement.close();
            result.close();
            clientName.close();
        } catch(SQLException e) {
            msg = "Unable to get data from the database";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String consultShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        // stores reserves to be sent to the user
        ArrayList<Show> shows = new ArrayList<>();
        String msg = "";

        try {
            // Received filters from user
            HashMap<String, String> filters = (HashMap<String, String>) clientData.getData();

            try {
                // Create statement
                Statement statement = connection.createStatement();

                // Execute query to get the clients name
                ResultSet clientName = statement.executeQuery(
                        "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
                );

                // Verify the type of info the user pretends to be searched
                for (var i : filters.entrySet()) {
                    // Get all the shows from DB
                    ResultSet result = statement.executeQuery(
                            "SELECT * FROM espetaculo WHERE " + i.getKey() + " LIKE '%" + i.getValue() + "%'"
                    );

                    // Add objects to array
                    while (result.next()) {
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

                shows.removeIf(item -> !item.isVisible());


                if (shows.isEmpty())
                    LOG.log("Shows not found based on the received filters");

                // Send list to client
                oos.writeObject(shows);

                statement.close();
                clientName.close();
            } catch (SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String consultShowsAdmin(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        // stores reserves to be sent to the user
        ArrayList<Show> shows = new ArrayList<>();
        String msg = "";

        try {
            // Received filters from user
            HashMap<String, String> filters = (HashMap<String, String>) clientData.getData();

            try {
                // Create statement
                Statement statement = connection.createStatement();


                ResultSet result = statement.executeQuery("SELECT * FROM espetaculo");

                // Add objects to array
                while (result.next()) {
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

                if (shows.isEmpty())
                    LOG.log("Shows not found based on the received filters");

                // Send list to client
                oos.writeObject(shows);

                statement.close();
            } catch (SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String selectShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        ArrayList<Show> availableShows = new ArrayList<>();
        String msg = "";

        try {
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
                    try {
                        // Get the show date (assuming its formatted: "dd/MM/yyyy HH:mm:ss")
                        Date showDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);

                        // Get the current day
                        Date currentDate = new Date();

                        // Verify if remain at least 24 hours before the show
                        long  diffInMillies = Math.abs(showDate.getTime() - currentDate.getTime()) ;
                        long diff = TimeUnit.MILLISECONDS.toHours(diffInMillies);
                        //Duration difference = Duration.between(showDate,currentDate);
                        //String difResult = String.format("%d:%02d", difference.toHours(), difference.toMinutes());

                        // If at least 24 hours before the show, it can be selected
                        if (diff <= 24) {
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
                    } catch(ParseException e) {
                        msg = "Unable to format the show Date";
                        LOG.log(msg);
                        oos.writeObject(msg);
                    }
                }
            }

            // Send available shows to user
            oos.writeObject(availableShows);

            if (availableShows.isEmpty())
                LOG.log("No shows available at the moment");

            statement.close();
            result.close();
        } catch (SQLException e) {
            msg = "Unable to get data from the database";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String viewSeatsAndPrices(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        String msg = "";

        try {
            // At this moment the client has already selected the show, so we search by the show id
            // Receive show id
            Integer recShowId = (Integer) clientData.getData();

            // Get unavailable seats ids
            ArrayList<Integer> unavailable = new ArrayList<>();

            try {
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

                statement.close();
                availableSeats.close();
                reservedSeats.close();
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException e) {
            msg = "Unable to read show id from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized boolean submitReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        int id = 0;
        Date currentTime = new Date();  // Get current time of the server to set in reserve object
        boolean isPaid = false;
        String msg = "";

        try {
            // Receive reserve from client  (format: showId, List<Seat>
            Pair<Integer, ArrayList<Seat>> reserve = (Pair<Integer, ArrayList<Seat>>) ois.readObject();

            try {
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

                    LOG.log("Reservation submitted with success from user[" + username + "]");
                    oos.writeObject(true);
                    return true;
                } catch (SQLException e) {
                    LOG.log("Unable to submit reservation from user[" + username + "]");
                    oos.writeObject(false);
                    return false;
                } finally {
                    statement.close();
                    reserves.close();
                    username.close();
                    return false;
                }
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
                return false;
            }
        } catch(IOException | ClassNotFoundException e) {
            msg = "Unable to read data from user";
            LOG.log(msg);
            oos.writeObject(msg);
            return false;
        }
    }

    public synchronized String deleteUnpaidReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        String msg = "";
        try {
            // Read the ID of the reserve to be deleted
            Integer reservationId = (Integer) ois.readObject();

            try {
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

                while (result.next()) {
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
                        } catch (SQLException e) {
                            LOG.log("Unable to delete unpaid reservation from user [" + username + "]");
                            oos.writeObject(false);
                        } finally {
                            statement.close();
                            result.close();
                            if (username != null) username.close();
                        }
                    }
                }
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            msg = "Unable to read data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String payReservation(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        String msg = "";

        try {
            // Receive reserve ID for payment
            Integer resId = (Integer) ois.readObject();

            try {
                // Create connection
                Statement statement = connection.createStatement();

                // Search reserve by id
                ResultSet result = statement.executeQuery(
                        "SELECT * FROM reserva WHERE id = '" + resId + "'"
                );

                // Verify if the time exceeds the limit to pay
                String date = result.getString("data_hora");

                try {
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
                } catch(ParseException e) {
                    msg = "Unable to format the show Date";
                    LOG.log(msg);
                    oos.writeObject(msg);
                }
            } catch (SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            msg = "Unable to read data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String showVisible(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        Integer showId = (Integer) clientData.getData();
        String query;
        String msg;

        try {
            Statement statement = connection.createStatement();

            ResultSet visivel = statement.executeQuery(
                    "SELECT visivel FROM espetaculo WHERE id = '" + showId + "'");

            if(visivel.getBoolean(1)) {
                msg = "This show is already visible";
                LOG.log(msg);
                oos.writeObject(msg);
                return "";
            }

            query = "UPDATE espetaculo SET visivel = 1 WHERE id = '" + showId + "'";
            statement.executeUpdate(query);

            msg = "The show is now visible";
            LOG.log(msg);
            oos.writeObject(msg);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "";
    }


    public synchronized String insertShows(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        String msg = "";

        try {
            // Receive the new value from client
            String filePath = (String) clientData.getData();
            Pair<Show, Map<String, List<Seat>>> mapShows = Utils.readFile(filePath);

            if (mapShows == null) {
                oos.writeObject("There was a problem reading from the file");
                return "";
            }
            try {
                // Create connection
                Statement statement = connection.createStatement();

                //Check if the show exists
                ResultSet idExist = statement.executeQuery(
                        "SELECT id FROM espetaculo WHERE descricao = '" + mapShows.getKey().getDescription() + "'"
                );

                if (idExist.next()) {
                    oos.writeObject("This show already exists in the database");
                    return "";
                }

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
                        "SELECT id FROM espetaculo WHERE descricao = '" + mapShows.getKey().getDescription() + "'"
                );
                int idShow = id.getInt(1);
                Map<String, List<Seat>> seats = mapShows.getValue();
                //Go through all the rows
                for (String key : seats.keySet()) {
                    //Go through all the seats
                    List<Seat> seatsList = seats.get(key);
                    for (Seat seat : seatsList) {
                        statement.executeUpdate(
                                "INSERT INTO lugar(fila,assento,preco,espetaculo_id) "
                                        + "VALUES ("
                                        + "'" + seat.getRow() + "',"
                                        + "'" + seat.getNumber() + "',"
                                        + "'" + seat.getPrice() + "',"
                                        + "'" + idShow + "')");
                    }

                }

                oos.writeObject("The show was successfully added");
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                oos.writeObject(msg);
            }
        } catch(IOException e) {
            msg = "Unable to read data from user";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return "";
    }

    public synchronized String deleteShow(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        boolean hasPaidReserve = false;
        String msg = "";
        try {
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
                    Integer deleteShowId = (Integer) clientData.getData();

                    // Search reservations
                    ResultSet reservations = statement.executeQuery(
                            "SELECT * from reserva WHERE id_espetaculo = '" + deleteShowId + "'");

                    // If there are no reservations associated
                    if (!reservations.next())
                        hasPaidReserve = false;

                    else {
                        // Search reservations and verify if a paid reservation exists
                        while (reservations.next()) {
                            // If a paid reserve exists, the show can't be deleted
                            if (reservations.getBoolean("pago")) {
                                msg = "Show[" + deleteShowId + "] already has a paid reservation...";
                                LOG.log(msg);
                                oos.writeObject(new Pair<>(false,msg));
                                hasPaidReserve = true;
                            }
                        }
                    }
                        // If there are no paid reservations associated, the show can be deleted
                        if (!hasPaidReserve) {
                            try {

                                statement.executeUpdate(
                                        "DELETE FROM espetaculo WHERE id = '" + deleteShowId + "'"
                                );
                                msg = "Show[" + deleteShowId + "] was deleted successfully...";
                                LOG.log(msg);
                                oos.writeObject(new Pair<>(true,msg));

                            } catch (SQLException e) {
                                msg = "Unable to delete show[" + deleteShowId + "]";
                                LOG.log(msg);
                                oos.writeObject(new Pair<>(false,msg));
                            } finally {
                                statement.close();
                                reservations.close();
                                isAdmin.close();
                            }
                        }

            } else {
                msg = "Unable to delete show. Only the admin can execute this function";
                LOG.log(msg);
                oos.writeObject(new Pair<>(false,msg));
                statement.close();
                isAdmin.close();
            }
        } catch(SQLException e) {
            msg = "Unable to get data from the database";
            LOG.log(msg);
            oos.writeObject(new Pair<>(false,msg));
        }

        return "";
    }

    public synchronized String disconnect(ClientData clientData, ObjectOutputStream oos, ObjectInputStream ois) throws SQLException, IOException, ClassNotFoundException {
        String msg = "";
        String query = "";

        try {
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
                        query = "UPDATE utilizador SET autenticado = 0 WHERE id = '" + id + "'";
                        // Update authenticated value
                        statement.executeUpdate(query);
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
        } catch(SQLException e) {
            msg = "Unable to get data from the database";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return query;
    }

    public synchronized void updateDataBase(String sqlCommand) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sqlCommand);
            LOG.log("Database updated");
        } catch (SQLException e) {
            LOG.log("Error updating database");
        }
    }
}