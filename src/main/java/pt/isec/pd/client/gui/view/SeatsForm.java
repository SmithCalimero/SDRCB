package pt.isec.pd.client.gui.view;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import pt.isec.pd.client.model.ModelManager;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.fsm.State;
import pt.isec.pd.shared_data.Responses.SeatsResponse;
import pt.isec.pd.shared_data.Responses.ShowsResponse;
import pt.isec.pd.shared_data.Responses.SubmitReservationResponse;
import pt.isec.pd.shared_data.Seat;

import java.util.*;

public class SeatsForm {
    public Label msg;
    @FXML
    private Button cancelButton;
    @FXML
    private AnchorPane pane;
    @FXML
    private Pane centerPane;
    private ModelManager model;
    private List<Seat> seats;
    private SeatsResponse seatsResponse;

    public void setModel(ModelManager model) {
        this.model = model;
        this.seats = new ArrayList<>();
        registerHandlers();
        update();
    }

    private void registerHandlers() {
        model.addPropertyChangeListener(ModelManager.PROP_STATE, evt -> {
            seats.clear();
            update();
        });

        model.addPropertyChangeListener(ClientAction.VIEW_SEATS_PRICES.toString(), evt -> {
            seats.clear();
            model.setMessage("");
            if (model.getState() == State.SEATS_PRICES)
                updateSeatsList();
        });

        model.addPropertyChangeListener(ClientAction.SELECT_SHOWS.toString(), evt -> {
            if (model.getState() == State.SEATS_PRICES) {
                try {
                    ShowsResponse showsResponse = (ShowsResponse) model.getResponse();
                    if (showsResponse != null && showsResponse.getShowId() == seatsResponse.getShowId()) {
                        model.previous();
                        model.setMessage("O show foi removido pelo administrado");
                    }
                } catch (ClassCastException ignored) {}
            }
        });


        model.addPropertyChangeListener(ClientAction.SUBMIT_RESERVATION.toString(), evt -> {
            msg.setText("");
            SubmitReservationResponse submitReservationResponse = (SubmitReservationResponse) model.getResponse();
            if (submitReservationResponse.isSuccess())
                model.payReservationTransition(submitReservationResponse.getResId());
            else
                msg.setText("There was a client that requested that seat first sorry!");
        });

        cancelButton.setOnAction(actionEvent -> {
            model.seatsTransition(null);
        });
    }

    private void update() {
        pane.setVisible(model != null && model.getState() == State.SEATS_PRICES);
    }

    private void updateSeatsList() {
        if (model.getResponse() instanceof SeatsResponse) {
            SeatsResponse seatsResponse = (SeatsResponse) model.getResponse();
            ArrayList<String> rows = new ArrayList<>();
            VBox container = new VBox();
            Popup stage = new Popup();
            container.prefWidthProperty().bind(pane.widthProperty().multiply(1));
            centerPane.getChildren().clear();

            // Get all the rows, to search by row
            for (var s : seatsResponse.getSeats())
                if (!rows.contains(s.getRow()))
                    rows.add(s.getRow());

            // Create rows
            for (var r : rows) {
                HBox row = new HBox(10);
                row.setPadding(new Insets(0, 0, 10, 0));
                row.getChildren().add(new Label(r + ": "));

                // Create columns
                for (var s : seatsResponse.getSeats()) {
                    if (s.getRow().equalsIgnoreCase(r)) {
                        VBox column = new VBox(10);
                        Button button = new Button(s.getNumber());
                        ImageView view = new ImageView(
                                new Image(String.valueOf(getClass().getResource("/icons/seat.png")))
                        );
                        view.setFitHeight(20);
                        view.setFitWidth(20);
                        view.preserveRatioProperty();
                        button.setGraphic(view);

                        column.getChildren().add(button);
                        column.setAlignment(Pos.CENTER);

                        VBox info = new VBox();
                        Label rowLabel = new Label("Fila: " + s.getRow());
                        Label number = new Label("Numero: " + s.getNumber());
                        Label price = new Label("Preco: " + s.getPrice());
                        rowLabel.setStyle("-fx-text-fill: white");
                        number.setStyle("-fx-text-fill: white");
                        price.setStyle("-fx-text-fill: white");
                        info.setBackground(Background.fill(Color.rgb(1,1,1,0.5)));
                        info.getChildren().addAll(rowLabel,number,price);

                        column.setOnMouseEntered(mouseEvent -> {
                            Bounds bound = column.localToScene(column.getBoundsInLocal());
                            stage.getContent().clear();
                            stage.getContent().add(info);
                            stage.setX(pane.getScene().getWindow().getX() + column.getLayoutX());
                            stage.setY(pane.getScene().getWindow().getY() + bound.getMinY() - 25);
                            stage.setAutoHide(true);
                            stage.show(pane.getScene().getWindow());
                        });

                        column.setOnMouseExited(mouseEvent -> {
                            stage.hide();
                        });

                        // If reserved button is disabled and red
                        if (s.isReserved()) {
                            seats.remove(s);
                            button.setBackground(Background.fill(Color.rgb(200, 0, 0, 0.5)));
                        }
                        else {
                            // If the user had this seat selected before de update (sets selected color)
                            if (seats.contains(s))
                                button.setBackground(Background.fill(Color.rgb(0, 200, 0, 0.65)));

                            button.setOnAction(actionEvent -> {
                                if (seats.contains(s)) {
                                    seats.remove(s);
                                    button.setStyle("-fx-background-color: #f2f2f2");
                                    button.setStyle("-fx-focus-color: transparent;");
                                    button.setStyle("-fx-faint-focus-color: transparent;");
                                } else {
                                    button.setBackground(Background.fill(Color.rgb(0, 200, 0, 0.65)));
                                    seats.add(s);
                                    System.out.println("ADDED SEAT: " + s.getNumber());
                                }
                            });
                        }

                        // Fill row
                        row.getChildren().add(column);
                        row.setAlignment(Pos.CENTER_LEFT);
                    }
                }
                // Add row to container
                container.getChildren().add(row);
            }

            HBox hBoxReserve = new HBox(10);
            Button reserveButton = new Button("Reservar");
            reserveButton.setOnAction(actionEvent -> {
                if (!seats.isEmpty()) {
                    model.submitReservation(seats);
                    seats.clear();
                }
            });

            hBoxReserve.getChildren().add(reserveButton);
            hBoxReserve.setAlignment(Pos.CENTER);
            container.getChildren().add(hBoxReserve);

            centerPane.getChildren().add(container);
        }
    }
}
