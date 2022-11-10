package pt.isec.pd.client.model.data;

import java.io.Serial;
import java.io.Serializable;

public class ClientData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private ClientAction action;

    public ClientData() { }

    public int getId() { return id; }

    public String getUsername() { return username; }

    public void setId(int id) { this.id = id; }

    public void setUsername(String username) { this.username = username; }

    public void setAction(ClientAction action) { this.action = action; }

    public ClientAction getAction() { return action; }
}
