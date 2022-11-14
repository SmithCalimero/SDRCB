package pt.isec.pd.client.gui;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.ModelManager;

public class LoginUI extends BorderPane {
    ModelManager model;
    Button btnNext;
    Button btnRegister;
    Button btnEdit;

    public LoginUI(ModelManager model) {
        this.model = model;
        createViews();
        registerHandlers();
        update();
    }

    private void createViews() {
        this.setStyle("-fx-background-color: #FFFFFF;");
        btnNext = new Button("Log-in");
        btnRegister = new Button("Registar");
        btnEdit = new Button("Editar");
        this.setCenter(new VBox(btnNext,btnRegister));
    }

    private void registerHandlers() {
        model.addPropertyChangeListener(ModelManager.PROP_STATE, evt -> {
            update();
        });
        btnNext.setOnAction(actionEvent -> {
            //TODO: write the error message in case of not login
            Pair<Boolean,String> response = model.login("ruben","1234");

            if(response.getKey()){
                model.next();
            }
        });
        btnRegister.setOnAction(actionEvent -> {
            model.next();
        });
    }

    private void update() {
        this.setVisible(model != null && model.getState() == State.LOGIN);
    }
}

