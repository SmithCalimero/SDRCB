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
import pt.isec.pd.shared_data.Responses.LoginResponse;

public class LoginForm {
    public TextField usernameField;
    public PasswordField passwordField;
    public Label errorMessage;
    public AnchorPane pane;
    public Button loginButton;
    public Button registerButton;

    private ModelManager model;

    public void setModel(ModelManager model) {
        this.model = model;

        registerHandlers();
        update();
        clearView();
    }
    private void registerHandlers() {
        model.addPropertyChangeListener(ModelManager.PROP_STATE, evt -> {
            update();
        });

        model.addPropertyChangeListener(ClientAction.LOGIN.toString(), evt -> {
            LoginResponse loginResponse = (LoginResponse) model.getResponse();

            errorMessage.setText("");
            if (loginResponse.isSuccess()) {
                model.next();
            } else {
                errorMessage.setText(loginResponse.getMsg());
            }
        });

        loginButton.setOnAction(actionEvent -> {
            model.login(usernameField.getText(),passwordField.getText());
        });

        registerButton.setOnAction(actionEvent -> {
            clearView();
            model.swapToRegister();
        });
    }


    private void update() {
        pane.setVisible(model != null && model.getState() == State.LOGIN);
    }

    private void clearView() {
        usernameField.clear();
        passwordField.clear();
        //registerLabel.setText("");
        errorMessage.setText("");
    }
}
