package pt.isec.pd.client.gui.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Pair;
import pt.isec.pd.client.model.ModelManager;

public class LoginForm {
    @FXML
    public TextField usernameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Label errorMessage;

    private ModelManager model;

    public void setModel(ModelManager model) {
        this.model = model;
    }
    public void onLoginButtonClick() {
        errorMessage.setText("");
        Pair<Boolean,String> response = model.login(usernameField.getText(),passwordField.getText());
        if (response.getKey()) {
            model.next();
        } else {
            errorMessage.setText(response.getValue());
        }
    }

    public void onRegisterButtonClick() {

    }
}
