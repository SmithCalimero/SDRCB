package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Seat;

import java.util.List;

public class SeatsForm {
    public AnchorPane pane;
    public ListView<Seat> list;
    public Button cancelButton;
    public Button reservarButton;
    private ModelManager model;

    public void setModel(ModelManager model) {
        this.model = model;

        registerHandlers();
        update();
    }

    private void registerHandlers() {
        model.addPropertyChangeListener(ModelManager.PROP_STATE, evt -> {
            update();
        });

        model.addPropertyChangeListener(ModelManager.PROP_DATA, evt -> {
            List<Seat> newSeats = model.getSeatsAndPrices();
            list.getItems().clear();
            list.setItems(FXCollections.observableList(newSeats));
        });

        cancelButton.setOnAction(actionEvent -> {
            model.seatsTransition(null);
        });

        reservarButton.setOnAction(actionEvent -> {
            List<Seat> seats = list.getSelectionModel().getSelectedItems().stream().toList();
            if (!seats.isEmpty()) {
                model.submitReservation(seats);
            }
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.SEATS_PRICES);
    }
}
