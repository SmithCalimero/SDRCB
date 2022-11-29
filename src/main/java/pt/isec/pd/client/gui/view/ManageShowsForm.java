package pt.isec.pd.client.gui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Responses.HandleVisibleShowResponse;
import pt.isec.pd.shared_data.Responses.InsertShowResponse;
import pt.isec.pd.shared_data.Responses.ShowsResponse;
import pt.isec.pd.shared_data.Show;

import java.util.List;

public class ManageShowsForm {
    public Button insertShowsButton;
    public TextField filePath;
    public ListView<Show> list;
    public AnchorPane pane;
    public Button retrocederButton;
    public Label result;
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

        model.addPropertyChangeListener(ClientAction.CONSULT_SHOWS_ALL.toString(), evt -> {
            ShowsResponse showsResponse = (ShowsResponse) model.getResponse();
            list.setItems(FXCollections.observableList(showsResponse.getShows()));
        });

        model.addPropertyChangeListener(ClientAction.INSERT_SHOWS.toString(), evt -> {
            InsertShowResponse showsResponse = (InsertShowResponse) model.getResponse();
            result.setText(showsResponse.getMsg());
        });

        model.addPropertyChangeListener(ClientAction.DELETE_SHOW.toString(), evt -> {
            InsertShowResponse insertShowResponse = (InsertShowResponse) model.getResponse();
            result.setText(insertShowResponse.getMsg());
        });

        model.addPropertyChangeListener(ClientAction.VISIBLE_SHOW.toString(), evt -> {
            HandleVisibleShowResponse handleVisibleShowResponse = (HandleVisibleShowResponse) model.getResponse();
            result.setText(handleVisibleShowResponse.getMsg());
        });

        retrocederButton.setOnAction(actionEvent ->{
            model.insertShowsTransition();
        });

        insertShowsButton.setOnAction(actionEvent -> {
            if (filePath.getText().isEmpty())
                return;
            model.insertShows(filePath.getText());
        });

        removeButton.setOnAction(actionEvent -> {
            if (list.getSelectionModel().getSelectedItem() == null) {
                result.setText("Selecione o espetaculo que pretende remover");
                return;
            }

            model.deleteShow(list.getSelectionModel().getSelectedItem().getId());
        });

        handleVisibilityButton.setOnAction(actionEvent -> {
            if (list.getSelectionModel().getSelectedItem() == null) {
                result.setText("Selecione o espetaculo que pretende tornar visivel");
                return;
            }

            model.showVisible(list.getSelectionModel().getSelectedItem().getId());
        });
    }
    private void update() {
        pane.setVisible(model != null && model.getState() == State.MANAGE_SHOWS);
    }
}
