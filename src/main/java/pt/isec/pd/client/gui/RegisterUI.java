package pt.isec.pd.client.gui;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.client.model.ModelManager;

public class RegisterUI extends BorderPane {
    ModelManager model;
    Button btnNext;

    public RegisterUI(ModelManager model) {
        this.model = model;
        createViews();
        registerHandlers();
        update();
    }

    private void createViews() {
        this.setStyle("-fx-background-color: #FFFFFF;");
        btnNext = new Button("Register");
        this.setCenter(btnNext);
    }

    private void registerHandlers() {
        model.addPropertyChangeListener(ModelManager.PROP_STATE, evt -> {
            update();
        });
        btnNext.setOnAction(actionEvent -> {
            model.register();
        });
    }

    private void update() {
        this.setVisible(model != null && model.getState() == State.REGISTER);
    }
}

