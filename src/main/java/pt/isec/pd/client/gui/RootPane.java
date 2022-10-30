package pt.isec.pd.client.gui;

import javafx.scene.layout.*;
import pt.isec.pd.client.model.ModelManager;

public class RootPane extends BorderPane {
    ModelManager model;

    public RootPane(ModelManager model) {
        this.model = model;

        createViews();
        registerHandlers();
        update();
    }

    private void createViews() {
        StackPane stackPane = new StackPane(
                new LoginUI(model), new RegisterUI(model),
                new EditUserUI(model));
        this.setCenter(stackPane);
        this.setBottom(new StatusBar(model));
    }

    private void registerHandlers() { }

    private void update() { }
}