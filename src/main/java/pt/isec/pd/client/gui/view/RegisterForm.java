package pt.isec.pd.client.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;

public class RegisterForm {
    @FXML
    public AnchorPane pane;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nameField;
    @FXML
    public TextField userNameField;
    @FXML
    public Button loginButton;
    @FXML
    public Button registerButton;
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
        loginButton.setOnAction(actionEvent -> {
            model.swapToRegister();
        });

        registerButton.setOnAction(actionEvent -> {
            model.register(userNameField.getText(),nameField.getText(),passwordField.getText());
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.REGISTER);
    }
}
