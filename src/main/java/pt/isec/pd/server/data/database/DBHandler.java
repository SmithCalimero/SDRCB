package pt.isec.pd.server.data.database;

import javafx.util.Pair;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Responses.*;
import pt.isec.pd.shared_data.Seat;
import pt.isec.pd.shared_data.Show;
import pt.isec.pd.shared_data.Triple;
import pt.isec.pd.utils.Log;
import pt.isec.pd.utils.Utils;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DBHandler {
    private final Log LOG = Log.getLogger(DBHandler.class);
    private final Connection connection;

    public DBHandler(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
    }

    public int getCurrentVersion() throws SQLException {
        Statement statement = connection.createStatement();

        String sqlQuery = "PRAGMA user_version;";
        ResultSet resultSet = statement.executeQuery(sqlQuery);

        int version = resultSet.getInt(1);

        statement.close();

        return version;
    }


    public void updateVersion(List<String> listQuery) throws SQLException {
        Statement statement = connection.createStatement();

        int currentVersion = getCurrentVersion();

        String sqlQuery = "PRAGMA user_version = " + (currentVersion + 1);
        statement.execute(sqlQuery);

        for (String query : listQuery) {
            statement.executeUpdate("INSERT INTO versao (id,[query]) " +
                    " VALUES ('"+  (currentVersion + 1) + "'," +
                    "\""+ query + "\"\n" +
                    ");");
        }

        statement.close();
    }


    public List<String> getListOfQuery(int myVersion, int newVersion) throws SQLException {
        Statement statement = connection.createStatement();
        List<String> commands = new ArrayList<>();
        //Returns all needed updates
        ResultSet result = statement.executeQuery("SELECT id," +
                "\"query\"" +
                " FROM versao WHERE id > + " + myVersion + " and id <=" + newVersion + " ORDER BY key asc;");
        while(result.next()) {
            result.getInt("id");
            commands.add(result.getString("query"));
        }

        result.close();
        statement.close();

        return commands;
    }

    public void updateToNewVersion(List<String> querys) throws SQLException {
        Statement statement = connection.createStatement();

        updateVersion(querys);

        for (String query : querys) {
            System.out.println(query);
            statement.executeUpdate(query);
        }

        statement.close();

    }

    //======================  ACTIONS ======================
    public synchronized List<String> register(ClientData clientData, ObjectOutputStream oos) throws IOException {
        int id = 0;     // 'id' is defined earlier because the users table can be empty
        int isAdmin = 0;
        int isAuthenticated = 0;
        boolean requestAccepted = true;
        String msg = "";
        String query = "";

        List<String> listQuery = new ArrayList<>();

        RegisterResponse registerResponse = new RegisterResponse();

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
                    query = "INSERT INTO utilizador(id,username,nome,password,administrador,autenticado)"
                            + "VALUES("
                            + "'" + ++id + "',"
                            + "'" + data.getFirst() + "',"
                            + "'" + data.getSecond() + "',"
                            + "'" + data.getThird() + "',"
                            + "'" + isAdmin + "',"
                            + "'" + isAuthenticated + "')";

                    int rs = statement.executeUpdate(query);
                    if (rs == 1) {
                        LOG.log("User[" + data.getFirst() + "] has registered successfully");
                        clientData.setId(id);
                        registerResponse.setSuccess(true);
                        listQuery.add(query);
                    }
                } catch (SQLException e) {
                    msg = "Unable to register user[" + data.getFirst() + "]";
                    registerResponse.setSuccess(false);
                    LOG.log(msg);
                } finally {
                    statement.close();
                    result.close();
                }
            }
        } catch (SQLException e) {
            msg = "Unable to get data from the database";
            registerResponse.setSuccess(false);
            LOG.log(msg);
        }

        registerResponse.setMsg(msg);
        oos.writeObject(registerResponse);

        return listQuery;
    }

    public synchronized List<String> login(ClientData clientData, ObjectOutputStream oos) throws IOException {
        boolean requestAccepted = false;
        int isAuthenticated = 0;
        boolean isAdmin = false;
        String msg;
        String query;
        List<String> listQuery = new ArrayList<>();

        LoginResponse loginResponse = new LoginResponse();

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
                        isAuthenticated = 1;
                        break;
                    }
                    // Validate username & password
                    else if (username.equals(loginData.getKey()) && password.equals(loginData.getValue()) && !authenticated) {
                        requestAccepted = true;
                        clientData.setId(id);
                        break;
                    }
                }

                loginResponse.setId(clientData.getId());

                // The user was not found
                if (requestAccepted) {
                    try {
                        query = "UPDATE utilizador SET autenticado = 1 WHERE id = '" + clientData.getId() + "'";
                        int rs = statement.executeUpdate(query);
                        if (rs == 1) {
                            msg = "User[" + loginData.getKey() + "]  logged in successfully";
                            LOG.log(msg);
                            listQuery.add(query);
                            loginResponse.setMsg(msg);
                            loginResponse.setSuccess(true);
                            loginResponse.setAdmin(isAdmin);
                            oos.writeObject(loginResponse);
                        }
                    } catch (SQLException e) {
                        msg = "Unable to login user[" + loginData.getKey() + "]";
                        LOG.log(msg);
                        loginResponse.setMsg(msg);
                        loginResponse.setSuccess(false);
                        loginResponse.setAdmin(isAdmin);
                        oos.writeObject(loginResponse);
                    } finally {
                        statement.close();
                        result.close();
                    }
                    return listQuery;
                }

                if (isAuthenticated == 0) {
                    msg = "The username " + loginData.getKey() + " or password are incorrect";
                    LOG.log(msg);
                } else {
                    msg = "This user " + loginData.getKey() + " is already authenticated";
                    LOG.log(msg);
                }

                loginResponse.setMsg(msg);
                loginResponse.setSuccess(false);
                oos.writeObject(loginResponse);
                statement.close();
                result.close();
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);
                loginResponse.setMsg(msg);
                loginResponse.setSuccess(false);
                oos.writeObject(loginResponse);
            }
        } catch(IOException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            loginResponse.setMsg(msg);
            loginResponse.setSuccess(false);
            oos.writeObject(loginResponse);
        }

        return listQuery;
    }

    public synchronized List<String> editClientData(ClientData clientData, ObjectOutputStream oos) throws IOException {
        boolean requestAccepted = true;
        String msg;
        String query;

        List<String> listQuery = new ArrayList<>();

        EditResponse editResponse = new EditResponse();

        try {
            // Receive the new value from client
            String newValue = ( String) clientData.getData();

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
                                editResponse.setMsg(msg);
                                editResponse.setSuccess(false);
                                oos.writeObject(editResponse);
                                requestAccepted = false;
                            }
                        }

                        // if unique=true, update value
                        if (requestAccepted) {
                            try {
                                query = "UPDATE utilizador SET nome = '" + newValue + "' WHERE id = '" + clientData.getId() + "'";
                                int rs = statement.executeUpdate(query);
                                if (rs == 1) {
                                    msg = "Name updated successfully";
                                    LOG.log(msg);
                                    editResponse.setMsg(msg);
                                    editResponse.setSuccess(true);
                                    oos.writeObject(editResponse);
                                    listQuery.add(query);
                                }
                            } catch (SQLException e) {
                                msg = "Unable to update name[" + newValue + "]";
                                LOG.log(msg);
                                editResponse.setMsg(msg);
                                editResponse.setSuccess(false);
                                oos.writeObject(editResponse);
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
                                editResponse.setMsg(msg);
                                editResponse.setSuccess(false);
                                oos.writeObject(editResponse);
                                requestAccepted = false;
                            }
                        }

                        // if unique=true, update value
                        if (requestAccepted) {
                            try {
                                query = "UPDATE utilizador SET username = '" + newValue + "' WHERE id = '" + clientData.getId() + "'";
                                int rs = statement.executeUpdate(query);
                                if (rs == 1) {
                                    msg = "Username updated successfully";
                                    LOG.log(msg);
                                    editResponse.setMsg(msg);
                                    editResponse.setSuccess(true);
                                    oos.writeObject(editResponse);

                                    listQuery.add(query);
                                }
                            } catch (SQLException e) {
                                msg = "Unable to update username[" + newValue + "]";
                                LOG.log(msg);
                                editResponse.setMsg(msg);
                                editResponse.setSuccess(false);
                                oos.writeObject(editResponse);
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
                            editResponse.setMsg(msg);
                            editResponse.setSuccess(false);
                            oos.writeObject(editResponse);
                            requestAccepted = false;
                        }

                        // Update password
                        if (requestAccepted) {
                            try {
                                query = "UPDATE utilizador SET password = '" + newValue + "' WHERE id = '" + clientData.getId() + "'";
                                int rs = statement.executeUpdate(query);
                                if (rs == 1) {
                                    msg = "Password updated successfully";
                                    LOG.log(msg);
                                    editResponse.setMsg(msg);
                                    editResponse.setSuccess(true);
                                    oos.writeObject(editResponse);

                                    listQuery.add(query);
                                }
                            } catch (SQLException e) {
                                msg = "Unable to update password";
                                LOG.log(msg);
                                editResponse.setMsg(msg);
                                editResponse.setSuccess(false);
                                oos.writeObject(editResponse);
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
                editResponse.setMsg(msg);
                editResponse.setSuccess(false);
                oos.writeObject(editResponse);
            }
        } catch(IOException e) {
            msg = "Unable to get the data from user";
            LOG.log(msg);
            editResponse.setMsg(msg);
            editResponse.setSuccess(false);
            oos.writeObject(editResponse);
        }

        return listQuery;
    }

    public synchronized List<String> consultPaymentsAwaiting(ClientData clientData, ObjectOutputStream oos) throws SQLException, IOException, ClassNotFoundException {
        List<Reserve> reserves = new ArrayList<>();
        ConsultUnpayedReservationResponse response = new ConsultUnpayedReservationResponse();
        Statement statement = connection.createStatement();

        // Execute query to get the clients name
        ResultSet clientResult = statement.executeQuery(
                "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
        );

        String clientName = clientResult.getString(1);

        // Execute query to search reserves
        ResultSet result = statement.executeQuery(
                "SELECT * FROM reserva;"
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
                            false,
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

        // Set response
        response.setReserves(reserves);

        // Send response to client
        oos.writeObject(response);

        statement.close();
        result.close();
        clientResult.close();

        return new ArrayList<>();
    }

    public synchronized List<String> consultPayedReservations(ClientData clientData, ObjectOutputStream oos) throws IOException {
        // Stores reserves awaiting payment to be sent to the user
        ArrayList<Reserve> reserves = new ArrayList<>();
        String msg;

        ConsultPayedReservationResponse consultPayedReservationResponse = new ConsultPayedReservationResponse();

        try {
            Statement statement = connection.createStatement();

            // Execute query to get the clients name
            ResultSet clientName = statement.executeQuery(
                    "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
            );

            String name = clientName.getString(1);

            // Execute query to search reserves
            ResultSet result = statement.executeQuery(
                    "SELECT * FROM reserva"
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
                            true,
                            userId,
                            showId
                    ));
                }
            }

            consultPayedReservationResponse.setReserves(reserves);

            if (reserves.isEmpty())
                LOG.log("No payments awaiting from user [" + name + "]");

            // Send list to client
            oos.writeObject(consultPayedReservationResponse);

            statement.close();
            result.close();
            clientName.close();
        } catch(SQLException e) {
            msg = "Unable to get data from the database";
            LOG.log(msg);
            oos.writeObject(msg);
        }

        return new ArrayList<>();
    }

    public synchronized List<String> consultShows(ClientData clientData, ObjectOutputStream oos) throws IOException {
        // stores reserves to be sent to the user
        ArrayList<Show> shows = new ArrayList<>();
        ConsultShowsFilterResponse response = new ConsultShowsFilterResponse();
        StringBuilder query = new StringBuilder();

        try {
            // Received filters from user
            HashMap<String, String> filters = (HashMap<String, String>) clientData.getData();
            try {
                Statement statement = connection.createStatement();

                ResultSet clientName = statement.executeQuery(
                        "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
                );

                // Verify the type of info the user pretends to be searched
                var filtersEntry = filters.entrySet().iterator();
                while(filtersEntry.hasNext()) {
                    Map.Entry<String,String> filter = filtersEntry.next();
                    System.out.println(filter.getValue());
                    query.append(filter.getKey()).append(" LIKE '%").append(filter.getValue()).append("%' ");
                    if (filtersEntry.hasNext()) {
                        query.append(" and ");
                    } else {
                        query.append(";");
                    }
                }

                ResultSet result = statement.executeQuery("SELECT * FROM espetaculo WHERE " + query);

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
                }
                result.close();

                shows.removeIf(item -> !item.isVisible());
                response.setShows(shows);

                if (shows.isEmpty())
                    LOG.log("Shows not found based on the received filters");

                oos.writeObject(response);
                statement.close();
                clientName.close();
            } catch (SQLException e) {
                LOG.log("Unable to get data from the database");
            }
        } catch(IOException e) {
            LOG.log("Unable to get the data from user");
        }

        return new ArrayList<>();
    }

    public synchronized List<String> consultShowsAdmin(ObjectOutputStream oos) throws IOException {
        // stores reserves to be sent to the user
        ArrayList<Show> shows = new ArrayList<>();
        ShowsResponse showsResponse = new ShowsResponse();

        try {
            try {
                Statement statement = connection.createStatement();

                ResultSet result = statement.executeQuery("SELECT * FROM espetaculo");

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
                }

                if (shows.isEmpty())
                    LOG.log("Shows not found based on the received filters");

                showsResponse.setAction(ClientAction.CONSULT_SHOWS_ALL);
                showsResponse.setShows(shows);
                oos.writeObject(showsResponse);

                result.close();
                statement.close();
            } catch (SQLException e) {
                LOG.log("Unable to get data from the database");
            }
        } catch(IOException e) {
            LOG.log("Unable to get the data from user");
        }

        return new ArrayList<>();
    }

    public synchronized List<String> selectShows(ObjectOutputStream oos) throws IOException {
        ArrayList<Show> availableShows = new ArrayList<>();
        ShowsResponse response = new ShowsResponse();

        try {
            Statement statement = connection.createStatement();

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

                if (visible) {
                    try {
                        // Get the show date (assuming its formatted: "yyyy-MM-dd HH:mm:ss")
                        Date showDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);

                        Date currentDate = new Date();

                        long  diffInMillis = showDate.getTime() - currentDate.getTime();
                        if (diffInMillis > 0) {
                            long diff = TimeUnit.MILLISECONDS.toHours(diffInMillis);

                            if (diff >= 24) {
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
                                        true
                                ));
                            }
                        }
                    } catch(ParseException e) {
                        LOG.log("Unable to format the show Date");
                    }
                }
            }

            // Send available shows to user
            response.setAction(ClientAction.SELECT_SHOWS);
            response.setShows(availableShows);
            oos.writeObject(response);
            if (availableShows.isEmpty())
                LOG.log("No shows available at the moment");

            statement.close();
            result.close();
        } catch (SQLException e) {
            LOG.log( "Unable to get data from the database");
        }

        return new ArrayList<>();
    }

    public synchronized List<String> viewSeatsAndPrices(ClientData clientData, ObjectOutputStream oos) throws IOException {
        SeatsResponse seatsResponse = new SeatsResponse();
        List<Seat> available = new ArrayList<>();

        try {
            // At this moment the client has already selected the show, so we search by the show id
            // Receive show id
            int recShowId = clientData.getShowId();
            seatsResponse.setShowId(recShowId);
            
            // Get unavailable seats ids
            ArrayList<Integer> unavailable = new ArrayList<>();

            try {
                Statement statement = connection.createStatement();
                ResultSet reservedSeats = statement.executeQuery("SELECT id_lugar FROM reserva_lugar");

                while (reservedSeats.next()) {
                    int reservedSeatId = reservedSeats.getInt("id_lugar");
                    unavailable.add(reservedSeatId);
                }

                ResultSet availableSeats = statement.executeQuery("SELECT * FROM lugar");

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

                seatsResponse.setSeats(available);
                oos.writeObject(seatsResponse);

                statement.close();
                availableSeats.close();
                reservedSeats.close();
            } catch(SQLException e) {
                LOG.log("Unable to get data from the database");
            }
        } catch(IOException e) {
            LOG.log("Unable to read show id from user");
        }

        return new ArrayList<>();
    }

    public synchronized List<String> submitReservation(ClientData clientData, ObjectOutputStream oos) throws IOException {
        Date currentTime = new Date();
        int isPaid = 0;
        String query;
        List<String> listQuery = new ArrayList<>();
        SubmitReservationResponse response = new SubmitReservationResponse();

        try {
            // Receive reserve from client  (format: showId, List<Seat>)
            Pair<Integer, List<Seat>> reserve = (Pair<Integer, List<Seat>>) clientData.getData();

            try {
                Statement statement = connection.createStatement();

                // Execute a query to get all reserves id
                ResultSet reserves = statement.executeQuery("SELECT id FROM reserva");

                // Execute a query to get the clients name
                ResultSet usernameResult = statement.executeQuery("SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'");

                String username = usernameResult.getString(1);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String dateString = dateFormat.format(currentTime);

                try {
                    // Insert reserve
                    query = "INSERT INTO reserva(data_hora,pago,id_utilizador,id_espetaculo)" +
                            "VALUES('"
                            + dateString + "','"
                            + isPaid + "','"
                            + clientData.getId() + "','"
                            + reserve.getKey() + "')";

                    statement.executeUpdate(query);
                    listQuery.add(query);

                    ResultSet resultSet  = statement.executeQuery(
                            "SELECT id FROM reserva" +
                                    " WHERE id_utilizador='"+  clientData.getId() + "' and id_espetaculo=' " + reserve.getKey() +
                                    "' and data_hora='" + dateString + "';");

                    int id = resultSet.getInt(1);

                    // Insert seats
                    for (var s : reserve.getValue()) {
                        query = "INSERT INTO reserva_lugar (" +
                                " id_reserva," +
                                " id_lugar)" +
                                " VALUES (" +
                                id +  " ," +
                                s.getId() + ");";
                        statement.executeUpdate(query);
                        listQuery.add(query);
                    }

                    LOG.log("Reservation submitted with success from user[" + username + "]");
                    clientData.setData(id);
                    response.setSuccess(true);
                    response.setResId(id);
                    oos.writeObject(response);

                    return listQuery;
                } catch (SQLException e) {
                    LOG.log("Unable to submit reservation from user[" + username + "]");
                    response.setSuccess(false);
                    oos.writeObject(response);
                    return listQuery;
                } finally {
                    statement.close();
                    reserves.close();
                    usernameResult.close();
                }
            } catch(SQLException e) {
                LOG.log("Unable to get data from the database");
                return listQuery;
            }
        } catch(IOException e) {
            LOG.log("Unable to read data from user");
            return listQuery;
        }
    }

    public synchronized List<String> deleteUnpaidReservation(ClientData clientData, ObjectOutputStream oos) throws IOException {
        DeleteReservationResponse response = new DeleteReservationResponse();
        String query;
        List<String> listQuery = new ArrayList<>();
        List<Reserve> reserves = new ArrayList<>();

        try {
            // Read the ID of the reserve to be deleted
            Integer reservationId = (Integer) clientData.getData();
            try {
                // Create statement
                Statement statement = connection.createStatement();

                // Execute a query to get the clients name
                ResultSet usernameSet = statement.executeQuery(
                        "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
                );

                String username = usernameSet.getString(1);

                ResultSet result = statement.executeQuery(
                        "SELECT id,pago, id_utilizador FROM reserva"
                );

                while (result.next()) {
                    // If the IDs match & paid=false, delete row
                    int idReserva = result.getInt("id");
                    boolean pago = result.getBoolean("pago");
                    int idUtilizador = result.getInt("id_utilizador");

                    if (reservationId == idReserva && !pago && clientData.getId() == idUtilizador) {
                        try {
                            query = "DELETE FROM reserva WHERE id = '" + reservationId + "';";
                            statement.executeUpdate(query);
                            listQuery.add(query);
                            query = "DELETE FROM reserva_lugar WHERE id_reserva = '" + reservationId + "';";
                            statement.executeUpdate(query);
                            listQuery.add(query);

                            LOG.log("User[" + username + "] deleted unpaid successfully reservation id[" + reservationId + "]");
                            response.setSuccess(true);

                            // Execute query to search reserves
                            result = statement.executeQuery(
                                    "SELECT * FROM reserva;"
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
                                                false,
                                                userId,
                                                showId
                                        ));
                                    } catch (Exception e) {
                                        LOG.log("Unable to consult unpaid reserves from user [" + username + "]");
                                    }
                                }
                            }

                            if (reserves.isEmpty())
                                LOG.log("No payments awaiting from user [" + username + "]");

                            response.setReserves(reserves);
                            oos.writeObject(response);
                        } catch (SQLException e) {
                            LOG.log("Unable to delete unpaid reservation from user [" + username + "]");
                            response.setSuccess(false);
                            oos.writeObject(response);
                        } finally {
                            statement.close();
                            result.close();
                            if (username != null) usernameSet.close();
                        }
                    }
                }
            } catch(SQLException e) {
                LOG.log("Unable to get data from the database");
            }
        } catch (IOException e) {
            LOG.log("Unable to read data from user");
        }

        return listQuery;
    }

    public synchronized List<String> payReservation(ClientData clientData, ObjectOutputStream oos) throws IOException {
        List<String> listQuery = new ArrayList<>();
        String query;

        PayReservationResponse response = new PayReservationResponse();
        try {
            // Receive reserve ID for payment
            Integer resId = (Integer) clientData.getData();

            try {
                Statement statement = connection.createStatement();

                // Search reserve by id
                ResultSet result = statement.executeQuery(
                        "SELECT * FROM reserva WHERE id = '" + resId + "'"
                );

                // Verify if the reservation exists
                if (result.next()) {
                    // Get username
                    ResultSet usernameRes = statement.executeQuery(
                            "SELECT username FROM utilizador WHERE id = '" + clientData.getId() + "'"
                    );
                    String username = usernameRes.getString("username");

                    // Set paid=true
                    query = "UPDATE reserva SET pago = '" + 1 + "' WHERE id = '" + resId + "'";
                    statement.executeUpdate(query);

                    listQuery.add(query);
                    LOG.log("Reservation from user [" + username + "] was paid successfully...");
                    response.setSuccess(true);
                    oos.writeObject(response);

                    statement.close();
                    result.close();
                    usernameRes.close();
                }

            } catch (SQLException e) {
                LOG.log("Unable to get data from the database");
            }
        } catch (IOException  e) {
            LOG.log("Unable to read data from user");
        }

        return listQuery;
    }

    public synchronized List<String> showVisible(ClientData clientData, ObjectOutputStream oos) throws IOException {
        Integer showId = (Integer) clientData.getData();
        String query;
        String msg;

        List<String> listQuery = new ArrayList<>();

        HandleVisibleShowResponse response = new HandleVisibleShowResponse();

        try {
            Statement statement = connection.createStatement();

            ResultSet visivel = statement.executeQuery(
                    "SELECT visivel FROM espetaculo WHERE id = '" + showId + "'");

            if(visivel.getBoolean(1)) {
                msg = "This show is visible";

                query = "UPDATE espetaculo SET visivel = 0 WHERE id = '" + showId + "'";
                statement.executeUpdate(query);
                listQuery.add(query);

                LOG.log(msg);
                response.setSuccess(true);
                response.setMsg(msg);
                oos.writeObject(response);
                return listQuery;
            }

            query = "UPDATE espetaculo SET visivel = 1 WHERE id = '" + showId + "'";
            statement.executeUpdate(query);
            listQuery.add(query);

            msg = "The show is now visible";
            LOG.log(msg);
            response.setSuccess(true);
            response.setMsg("The show is now visible");
            oos.writeObject(response);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listQuery;
    }


    public synchronized List<String> insertShows(ClientData clientData, ObjectOutputStream oos) throws IOException {
        String msg;
        InsertShowResponse response = new InsertShowResponse();

        List<String> listQuery = new ArrayList<>();
        String query;

        try {
            String filePath = (String) clientData.getData();
            Pair<Show, Map<String, List<Seat>>> mapShows = Utils.readFile(filePath);

            if (mapShows == null) {
                response.setSuccess(false);
                response.setMsg("There was a problem reading from the file");
                oos.writeObject(response);
                return listQuery;
            }
            try {
                // Create connection
                Statement statement = connection.createStatement();

                //Check if the show exists
                ResultSet idExist = statement.executeQuery(
                        "SELECT id FROM espetaculo WHERE descricao = '" + mapShows.getKey().getDescription() + "'"
                );

                if (idExist.next()) {
                    response.setSuccess(false);
                    response.setMsg("This show already exists in the database");
                    oos.writeObject(response);
                    return listQuery;
                }

                query = "INSERT INTO espetaculo(descricao,tipo,data_hora,duracao,local,localidade,pais,classificacao_etaria) "
                        + "VALUES("
                        + "'" + mapShows.getKey().getDescription() + "',"
                        + "'" + mapShows.getKey().getType() + "',"
                        + "'" + mapShows.getKey().getDateHour() + "',"
                        + "'" + mapShows.getKey().getDuration() + "',"
                        + "'" + mapShows.getKey().getLocation() + "',"
                        + "'" + mapShows.getKey().getLocality() + "',"
                        + "'" + mapShows.getKey().getCountry() + "',"
                        + "'" + mapShows.getKey().getAgeClassification() + "')";

                statement.executeUpdate(query);
                listQuery.add(query);

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
                        query = "INSERT INTO lugar(fila,assento,preco,espetaculo_id) "
                                + "VALUES ("
                                + "'" + seat.getRow() + "',"
                                + "'" + seat.getNumber() + "',"
                                + "'" + seat.getPrice() + "',"
                                + "'" + idShow + "')";
                        statement.executeUpdate(query);
                        listQuery.add(query);
                    }
                }

                response.setSuccess(true);
                response.setMsg("The show was successfully added");
                oos.writeObject(response);
            } catch(SQLException e) {
                msg = "Unable to get data from the database";
                LOG.log(msg);

                response.setSuccess(true);
                response.setMsg(msg);
                oos.writeObject(response);
            }
        } catch(IOException e) {
            msg = "Unable to read data from user";
            LOG.log(msg);

            response.setSuccess(true);
            response.setMsg(msg);
            oos.writeObject(response);
        }

        return listQuery;
    }

    public synchronized List<String> deleteShow(ClientData clientData, ObjectOutputStream oos) throws IOException {
        boolean hasPaidReserve = false;
        String msg;
        List<String> listQuery = new ArrayList<>();
        String query;
        DeleteResponse deleteResponse = new DeleteResponse();

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
                                deleteResponse.setSuccess(false);
                                deleteResponse.setMsg(msg);
                                oos.writeObject(deleteResponse);
                                hasPaidReserve = true;
                            }
                        }
                    }
                        // If there are no paid reservations associated, the show can be deleted
                        if (!hasPaidReserve) {
                            try {
                                query = "DELETE FROM espetaculo WHERE id = '" + deleteShowId + "'";
                                statement.executeUpdate(query);
                                listQuery.add(query);

                                query = "DELETE from lugar WHERE espetaculo_id = "+ deleteShowId + ";";
                                statement.executeUpdate(query);
                                listQuery.add(query);


                                msg = "Show[" + deleteShowId + "] was deleted successfully...";
                                LOG.log(msg);

                                listQuery.add(query);
                                deleteResponse.setSuccess(true);
                                deleteResponse.setMsg(msg);
                                oos.writeObject(deleteResponse);
                            } catch (SQLException e) {
                                msg = "Unable to delete show[" + deleteShowId + "]";
                                LOG.log(msg);
                                deleteResponse.setSuccess(false);
                                deleteResponse.setMsg(msg);
                                oos.writeObject(deleteResponse);
                            } finally {
                                statement.close();
                                reservations.close();
                                isAdmin.close();
                            }
                        }

            } else {
                msg = "Unable to delete show. Only the admin can execute this function";
                LOG.log(msg);
                deleteResponse.setSuccess(false);
                deleteResponse.setMsg(msg);
                oos.writeObject(deleteResponse);
                statement.close();
                isAdmin.close();
            }
        } catch(SQLException e) {
            msg = "Unable to get data from the database";
            LOG.log(msg);
            deleteResponse.setSuccess(false);
            deleteResponse.setMsg(msg);
            oos.writeObject(deleteResponse);
        }

        return listQuery;
    }

    public synchronized List<String> disconnect(ClientData clientData, ObjectOutputStream oos) throws SQLException, IOException, ClassNotFoundException {
        String query;
        List<String> listQuery = new ArrayList<>();
        DisconnectResponse response = new DisconnectResponse();

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
                        listQuery.add(query);

                        LOG.log("User[" + username + "] logged out successfully");
                        if (oos != null) {
                            oos.writeObject(response);
                        }
                    } catch (SQLException e) {
                        LOG.log("Unable to logout user[" + username + "]");
                    } finally {
                        statement.close();
                        result.close();
                    }
                }
            }
        } catch(SQLException e) {
            LOG.log( "Unable to get data from the database");
        }

        return listQuery;
    }

    public synchronized void updateDataBase(List<String> sqlCommand) {
        try {
            Statement statement = connection.createStatement();
            for (String command : sqlCommand) {
                statement.executeUpdate(command);
            }
            updateVersion(sqlCommand);
            LOG.log("Database updated");
        } catch (SQLException e) {
            LOG.log("Error updating database");
        }
    }
}