package pt.isec.pd.client.model.data;

import pt.isec.pd.shared_data.Seat;

import java.io.IOException;
import java.util.ArrayList;

// TODO:
//  When a client enters SELECTING_SEATS fase:
//      - This thread is started receiving the JavaFX panel data (showId) & communication objects
//  When he leaves that state, this thread stops and a notification is sent to the server
//      - The server receives the notification and removes this client from the list of clients
//          needing the updated list

public class UpdateSeatsView extends Thread {
    Client client;
    CommunicationHandler ch;
    int showId;
    ArrayList<Seat> seats;
    boolean update = false;

    public UpdateSeatsView(Client client, CommunicationHandler communicationHandler, int showId) {
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
            seats = (ArrayList<Seat>) ch.readFromSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Then the client waits for an update while he is on VIEW_SEATS_PRICES state
        while(ch.getClientAction() == ClientAction.VIEW_SEATS_PRICES) {
            try {
                // Server notifies user that an update is needed
                update = (Boolean) ch.readFromSocket();

                // Even if he receives the notification, this next step verifies the current action
                if (update && ch.getClientAction() == ClientAction.VIEW_SEATS_PRICES) {
                    // Request the new list
                    ch.writeToSocket(ClientAction.VIEW_SEATS_PRICES,showId);

                    // Receive the updated list
                    seats = (ArrayList<Seat>) ch.readFromSocket();

                    // Reset update value
                    update = false;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        // Send to server the action that he's no longer on SELECTING_SEATS state
        client.notifyServer();
        super.interrupt();
    }

    public ArrayList<Seat> getSeatsList() { return seats; }
}
