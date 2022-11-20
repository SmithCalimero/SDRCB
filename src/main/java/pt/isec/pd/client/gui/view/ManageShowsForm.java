package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Show;

import java.util.List;

public class ManageShowsForm {
    public Button insertShowsButton;
    public TextField filePath;
    public ListView<Show> list;
    public AnchorPane pane;
    public Button retrocederButton;
    public Label result;
    public Button refreshButton;
    public Button removeButton;
    public Button handleVisibilityButton;
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

        retrocederButton.setOnAction(actionEvent ->{
            model.insertShowsTransition();
        });

        insertShowsButton.setOnAction(actionEvent -> {
            if (filePath.getText().isEmpty())
                return;
            String msg = model.insertShows(filePath.getText());
            result.setText(msg);

            List<Show> shows = model.consultShows(null);
            list.setItems(FXCollections.observableList(shows));
        });

        refreshButton.setOnAction(actionEvent -> {
            List<Show> shows = model.consultShows(null);
            list.setItems(FXCollections.observableList(shows));
        });

        removeButton.setOnAction(actionEvent -> {
            if (list.getSelectionModel().getSelectedItem() == null) {
                result.setText("Selecione o espetaculo que pretende remover");
                return;
            }
            Pair<Boolean, String> r = model.deleteShow(list.getSelectionModel().getSelectedItem().getId());
            if (r.getKey()) {
                List<Show> shows = model.consultShows(null);
                list.setItems(FXCollections.observableList(shows));
            }
            result.setText(r.getValue());
        });

        handleVisibilityButton.setOnAction(actionEvent -> {
            if (list.getSelectionModel().getSelectedItem() == null) {
                result.setText("Selecione o espetaculo que pretende tornar visivel");
                return;
            }
            String r = model.showVisible(list.getSelectionModel().getSelectedItem().getId());
            result.setText(r);
        });
    }
    private void update() {
        pane.setVisible(model != null && model.getState() == State.MANAGE_SHOWS);
    }
}
