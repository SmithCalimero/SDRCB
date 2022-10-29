package pt.isec.pd.client.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ServerAddress;

import java.io.IOException;
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

        BorderPane root = new RootPane(model);
        Scene scene = new Scene(root,1280,720);
        stage.setScene(scene);
        stage.setTitle("PD");
        stage.setMinWidth(400);
        stage.show();



    }
}
