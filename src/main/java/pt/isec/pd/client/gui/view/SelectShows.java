package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Show;

import java.util.List;

public class SelectShows {
    public AnchorPane pane;
    public ListView<Show> list;
    public Button refreshButton;
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
            List<Show> listShows = model.consultShows(null);
            list.setItems(FXCollections.observableList(listShows));
        });

        list.setOnMouseClicked(actionEvent -> {
            if (list.getSelectionModel().getSelectedItem() != null) {
                model.seatsTransition(list.getSelectionModel().getSelectedItem().getId());
            }
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.SELECT_SHOWS);
    }
}
