package pt.isec.pa.Client.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import pt.isec.pa.Client.model.ModelManager;

public class StatusBar extends HBox {
    ModelManager model;
    Label lbMsg;

    public StatusBar(ModelManager model) {
        this.model = model;

        createViews();
        registerHandlers();
        update();
    }

    private void createViews() {
        Label lbMsgTitle = new Label("Message: ");
        lbMsgTitle.setPrefWidth(Integer.MAX_VALUE);
        lbMsgTitle.setAlignment(Pos.CENTER_RIGHT);
        lbMsg = new Label();
        lbMsg.setPrefWidth(Integer.MAX_VALUE);
        lbMsg.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().addAll(lbMsgTitle, lbMsg);
    }

    private void registerHandlers() {
    }

    private void update() {
    }
}
