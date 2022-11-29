package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Reserve;

public class ConsultPaymentsAwaitingForm {
    public AnchorPane pane;
    public Button refreshButton;
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

        refreshButton.setOnAction(actionEvent -> {
            //list.setItems(FXCollections.observableList(model.consultsPaymentsAwaiting()));
        });

        cancelButton.setOnAction(actionEvent -> {
            model.consultsPaymentsAwaitingTransition();
        });
    }


    private void update() {
        pane.setVisible(model != null && model.getState() == State.CONSULT_PAYMENTS_AWAITING);
    }
}
