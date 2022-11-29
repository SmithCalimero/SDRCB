package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Seat;

import java.util.List;

public class PayReservationForm {
    public AnchorPane pane;
    public Button pagarButton;
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

        model.addPropertyChangeListener(ClientAction.PAY_RESERVATION.toString(), evt -> {
            model.payReservationTransition(0);
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.PAY_RESERVATION);
    }
}
