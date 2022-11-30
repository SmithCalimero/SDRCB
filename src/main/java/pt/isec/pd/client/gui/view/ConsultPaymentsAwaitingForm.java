package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Responses.ConsultUnpayedReservationResponse;

import java.util.ArrayList;

public class ConsultPaymentsAwaitingForm {
    public AnchorPane pane;
    public ListView<Reserve> list;
    public Button cancelButton;

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

        model.addPropertyChangeListener(ClientAction.CONSULT_PAYMENTS_AWAITING.toString(), evt -> {
            updateList();
        });

        cancelButton.setOnAction(actionEvent -> {
            model.consultsPaymentsAwaitingTransition();
        });
    }


    private void update() {
        pane.setVisible(model != null && model.getState() == State.CONSULT_PAYMENTS_AWAITING);
    }

    private void updateList() {
        ConsultUnpayedReservationResponse response = (ConsultUnpayedReservationResponse) model.getResponse();

        // clear list
        list.getItems().clear();

        // write the updated list
        //ArrayList<Reserve> unpaidReserves = (ArrayList<Reserve>) model.getResponse();
        for (var r : response.getReserves())
            list.getItems().add(r);
    }
}
