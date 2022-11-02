module com.example.pdmeta {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.example.pdmeta1 to javafx.fxml;
    exports pt.isec.pd.client.gui;
    opens pt.isec.pd.client.gui to javafx.fxml;
}