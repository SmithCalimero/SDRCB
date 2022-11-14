package pt.isec.pd.client.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;

public class EditForm {
    public AnchorPane pane;
    public ChoiceBox choiceBox;
    public Button editButton;
    public TextField editField;

    private ModelManager model;

    public void setModel(ModelManager model) {
        this.model = model;

        registerHandlers();
        update();
    }
    private void registerHandlers() {
        choiceBox.getSelectionModel().select(0);

        model.addPropertyChangeListener(ModelManager.PROP_STATE, evt -> {
            update();
        });

        editButton.setOnAction(actionEvent -> {
            switch ((String) choiceBox.getSelectionModel().getSelectedItem()) {
                case "Username" -> model.edit(ClientAction.EDIT_USERNAME,editField.getText());
                case "Name" -> model.edit(ClientAction.EDIT_NAME,editField.getText());
                case "Password" -> model.edit(ClientAction.EDIT_PASSWORD,editField.getText());
            }
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.EDIT_USER);
    }
}
