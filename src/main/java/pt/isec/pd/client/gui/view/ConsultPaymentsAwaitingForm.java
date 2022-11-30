package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Reserve;
import pt.isec.pd.shared_data.Responses.ConsultUnpayedReservationResponse;
import pt.isec.pd.shared_data.Seat;

import java.util.ArrayList;
import java.util.List;

public class ConsultPaymentsAwaitingForm {
    @FXML
    private AnchorPane pane;
    @FXML
    private  ListView<Reserve> list;
    @FXML
    private Button cancelButton;
    @FXML
    private Button payButton;
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

        payButton.setOnAction(actionEvent -> {
            int resId = list.getSelectionModel().getSelectedItem().getId();
            if (resId != 0)
                model.payReservationTransition(resId);
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
        for (var r : response.getReserves())
            list.getItems().add(r);
    }
}
