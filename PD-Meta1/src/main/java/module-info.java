module com.example.pdmeta1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.pdmeta1 to javafx.fxml;
    exports pt.isec.pa.Client.gui;
    opens pt.isec.pa.Client.gui to javafx.fxml;
}