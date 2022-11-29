package pt.isec.pd.client.model.data;

import java.io.Serial;
import java.io.Serializable;

public class ClientData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private int showId;
    private boolean isAdmin;
    private ClientAction action;
    private Object data;

    public ClientData() { }

    public ClientData(ClientData clientData) {
        this.id = clientData.getId();
        this.showId = clientData.getShowId();
        this.isAdmin = clientData.isAdmin;
        this.action = clientData.getAction();
        this.data = clientData.getData();
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public void setAction(ClientAction action) { this.action = action; }

    public ClientAction getAction() { return action; }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getShowId() {
        return showId;
    }

    public void setShowId(int showId) {
        this.showId = showId;
    }
}
