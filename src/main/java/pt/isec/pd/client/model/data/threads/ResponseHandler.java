package pt.isec.pd.client.model.data.threads;

import javafx.application.Platform;
import pt.isec.pd.client.model.data.ClientAction;
import pt.isec.pd.client.model.data.ClientData;
import pt.isec.pd.shared_data.Responses.EditResponse;
import pt.isec.pd.shared_data.Responses.LoginResponse;
import pt.isec.pd.shared_data.Responses.RegisterResponse;

import javax.swing.text.Style;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ResponseHandler extends Thread {
    private ObjectInputStream ois;
    private PropertyChangeSupport pcs;
    private Object data;
    private ClientData clientData;
    public ResponseHandler(ObjectInputStream ois, PropertyChangeSupport pcs, ClientData clientData) {
        this.pcs = pcs;
        this.ois = ois;
        this.clientData = clientData;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Object object = ois.readObject();
                data = object;

                if(object instanceof RegisterResponse) {
                    Platform.runLater(() -> {
                        pcs.firePropertyChange(ClientAction.REGISTER.toString(),null,null);
                    });
                } else if (object instanceof LoginResponse loginResponse) {
                    Platform.runLater(() -> {
                        //Sets the id and admin parameters
                        synchronized (clientData) {
                            clientData.setId(loginResponse.getId());
                            clientData.setAdmin(loginResponse.isAdmin());
                        }
                        pcs.firePropertyChange(ClientAction.LOGIN.toString(),null,null);
                    });
                } else if (object instanceof EditResponse) {
                    Platform.runLater(() -> {
                        pcs.firePropertyChange(ClientAction.EDIT_DATA.toString(),null,null);
                    });
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized Object getResponse() {
        return data;
    }
}
