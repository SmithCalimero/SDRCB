package pt.isec.pd.client.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import pt.isec.pd.client.gui.view.*;
import pt.isec.pd.client.model.ModelManager;

import java.io.IOException;

public class RootPane extends BorderPane {
    ModelManager model;

    public RootPane(ModelManager model) {
        this.model = model;

        createViews();
        registerHandlers();
        update();
    }

    private void createViews() {
        FXMLLoader login = new FXMLLoader(RootPane.class.getResource("/fxml/login-form.fxml"));
        FXMLLoader register = new FXMLLoader(RootPane.class.getResource("/fxml/register-form.fxml"));
        FXMLLoader edit = new FXMLLoader(RootPane.class.getResource("/fxml/edit-form.fxml"));
        FXMLLoader menuClient = new FXMLLoader(RootPane.class.getResource("/fxml/menu-client-form.fxml"));
        FXMLLoader shows = new FXMLLoader(RootPane.class.getResource("/fxml/shows-form.fxml"));

        StackPane stackPane;
        try {
            stackPane = new StackPane(login.load(),register.load(),
                    edit.load(),menuClient.load(),shows.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        login.<LoginForm>getController().setModel(model);
        register.<RegisterForm>getController().setModel(model);
        edit.<EditForm>getController().setModel(model);
        menuClient.<MenuClientForm>getController().setModel(model);
        shows.<ShowsForm>getController().setModel(model);

        this.setCenter(stackPane);
    }

    private void registerHandlers() { }

    private void update() { }
}
