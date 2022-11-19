package pt.isec.pd.client.model.data;

import java.io.Serial;
import java.io.Serializable;

public class ClientData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private ClientAction action;
    private Object data;

    public ClientData() { }

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
}
