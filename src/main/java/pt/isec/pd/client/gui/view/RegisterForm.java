package pt.isec.pd.client.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Responses.RegisterResponse;

public class RegisterForm {
    public AnchorPane pane;
    public PasswordField passwordField;
    public TextField nameField;
    public TextField userNameField;
    public Button loginButton;
    public Button registerButton;
    public Label errorMsg;
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

        //Request
        registerButton.setOnAction(actionEvent -> {
            model.register(userNameField.getText(),nameField.getText(),passwordField.getText());
        });

        // Response
        model.addPropertyChangeListener(ClientAction.REGISTER.toString(), evt -> {
            RegisterResponse registerResponse = (RegisterResponse) model.getResponse();
            if (registerResponse.isSuccess()) {
                model.swapToRegister();
            } else {
                errorMsg.setText(registerResponse.getMsg());
            }
        });

        loginButton.setOnAction(actionEvent -> {
            model.swapToRegister();
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.REGISTER);
    }
}
