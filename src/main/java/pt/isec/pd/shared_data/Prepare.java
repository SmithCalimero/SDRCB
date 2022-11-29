package pt.isec.pd.shared_data;

import pt.isec.pd.client.model.data.ClientData;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class Prepare implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int port;
    private List<String> sqlCommand;
    private ClientData data;
    private int nextVersion;

    public Prepare(int port, int serverPort, List<String> sqlCommand, ClientData data) {
        this.port = port;
        this.data = data;
        this.sqlCommand = sqlCommand;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getSqlCommand() {
        return sqlCommand;
    }

    public void setSqlCommand(List<String> sqlCommand) {
        this.sqlCommand = sqlCommand;
    }

    public int getNextVersion() {
        return nextVersion;
    }

    @Override
    public String toString() {
        return "port: " + port +
                ", sqlCommand: " + sqlCommand;
    }

    public ClientData getData() {
        return data;
    }

    public void setData(ClientData data) {
        this.data = data;
    }
}
