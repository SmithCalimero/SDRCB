package pt.isec.pd.client.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;

public class LoginForm {
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label errorMessage;
    @FXML
    public AnchorPane pane;
    @FXML
    public Button loginButton;
    @FXML
    public Button registerButton;
    public Label registerLabel;

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
            errorMessage.setText("");
            Pair<Boolean,String> response = model.login(usernameField.getText(),passwordField.getText());
            if (response.getKey()) {
                model.next();
            } else {
                errorMessage.setText(response.getValue());
            }
        });
        registerButton.setOnAction(actionEvent -> {
            model.swapToRegister();
        });
    }


    private void update() {
        pane.setVisible(model != null && model.getState() == State.LOGIN);
    }
}
