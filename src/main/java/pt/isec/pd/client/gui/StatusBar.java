package pt.isec.pd.client.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import pt.isec.pd.client.model.ModelManager;


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
        Label lbMsgTitle = new Label("");
        lbMsgTitle.setPrefWidth(Integer.MAX_VALUE);
        lbMsgTitle.setAlignment(Pos.CENTER);
        lbMsg = new Label();
        lbMsg.setPrefWidth(Integer.MAX_VALUE);
        lbMsg.setAlignment(Pos.CENTER);
        Label boaPos = new Label("");
        boaPos.setPrefWidth(Integer.MAX_VALUE);
        boaPos.setAlignment(Pos.CENTER);
        this.getChildren().addAll(lbMsgTitle, lbMsg, boaPos);
        setAlignment(Pos.CENTER);
    }

    private void registerHandlers() {
        model.addPropertyChangeListener(ModelManager.PROP_DATA, evt -> {
            update();
        });
    }

    private void update() {
        lbMsg.setText(model.getMessage());
    }
}

