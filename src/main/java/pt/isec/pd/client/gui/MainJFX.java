package pt.isec.pd.client.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import pt.isec.pd.client.Main;
import pt.isec.pd.client.gui.view.LoginForm;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.shared_data.ServerAddress;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainJFX extends Application {
    ModelManager model;

    @Override
    public void start(Stage stage) throws IOException {
        //Receive arguments from main
        Parameters params = getParameters();
        List<String> arguments = params.getRaw();
        ServerAddress udpConn = new ServerAddress(arguments.get(0),Integer.parseInt(arguments.get(1)));

        model = new ModelManager(udpConn);

        /*BorderPane root = new RootPane(model);
        Scene scene = new Scene(root,1280,720);
        stage.setScene(scene);
        stage.setTitle("PD-meta1");
        stage.setMinWidth(400);
        stage.show();*/

        FXMLLoader fxmlLoader = new FXMLLoader(MainJFX.class.getResource("/fxml/login-form.fxml"));
        stage.setScene(new Scene(fxmlLoader.load(),640,360));
        stage.setTitle("PD-meta1");
        stage.setMinWidth(400);
        stage.show();

        LoginForm controller = fxmlLoader.<LoginForm>getController();
        controller.setModel(model);
        /*
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                model.disconnect();
            }
        });*/
    }
}
