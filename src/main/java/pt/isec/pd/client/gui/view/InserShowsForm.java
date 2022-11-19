package pt.isec.pd.client.gui.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Show;

public class InserShowsForm {
    public Button insertShowsButton;
    public TextField filePath;
    public ListView<Show> list;
    public AnchorPane pane;
    public Button retrocederButton;
    public Label result;
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

        retrocederButton.setOnAction(actionEvent ->{
            model.insertShowsTransition();
        });

        insertShowsButton.setOnAction(actionEvent -> {
            if (filePath.getText().isEmpty())
                return;
            Show show = model.insertShows(filePath.getText());
            if (show != null) {
                result.setText(show.toString());
            } else {
                result.setText("Não foi possivel adicionar show");
            }
        });
    }
    private void update() {
        pane.setVisible(model != null && model.getState() == State.INSERT_SHOWS);
    }
}
