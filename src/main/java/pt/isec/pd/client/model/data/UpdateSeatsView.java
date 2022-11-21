package pt.isec.pd.client.model.data;

import javafx.application.Platform;
import pt.isec.pd.shared_data.Seat;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

// TODO:
//  When a client enters SELECTING_SEATS fase:
//      - This thread is started receiving the JavaFX panel data (showId) & communication objects
//  When he leaves that state, this thread stops and a notification is sent to the server
//      - The server receives the notification and removes this client from the list of clients
//          needing the updated list

public class UpdateSeatsView extends Thread {
    public static final String PROP_DATA  = "data";
    Client client;
    CommunicationHandler ch;
    int showId;
    List<Seat> seats;
    boolean update = false;
    PropertyChangeSupport pcs;

    public UpdateSeatsView(Client client, CommunicationHandler communicationHandler, int showId, PropertyChangeSupport pcs) {
        this.pcs = pcs;
        this.client = client;
        this.ch = communicationHandler;
        this.showId = showId;
        this.seats = new ArrayList<>();
    }

    @Override
    public void run() {
        // Send the request and receive the list for the first time
        try {
            ch.writeToSocket(ClientAction.VIEW_SEATS_PRICES,showId);
            setSeat((List<Seat>) ch.readFromSocket());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Then the client waits for an update while he is on VIEW_SEATS_PRICES state
        while(true) {
            try {
                // Server notifies user that an update is needed
                update = (Boolean) ch.readFromSocket();
                if (!update)
                    break;

                // Request the new list
                ch.writeToSocket(ClientAction.VIEW_SEATS_PRICES,showId);
                // Receive the updated list
                setSeat((List<Seat>) ch.readFromSocket());

                // Reset update value
                update = false;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void setSeat(List<Seat> newSeats){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                seats = newSeats;
                pcs.firePropertyChange(PROP_DATA,null,seats);
            }
        });

    }

    public List<Seat> getSeatsList() {
        return seats;
    }

    public void close() {
        try {
            ClientData clientData = new ClientData();
            clientData.setAction(ClientAction.STOPPED_VIEWING_SEATS);
            ch.getOos().writeObject(clientData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
