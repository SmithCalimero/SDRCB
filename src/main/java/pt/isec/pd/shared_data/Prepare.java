package pt.isec.pd.shared_data;

import java.io.Serial;
import java.io.Serializable;

public class Prepare implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int port;
    private String sqlCommand;
    private int nextVersion;

    public Prepare(int port,int serverPort, String sqlCommand) {
        this.port = port;
        this.sqlCommand = sqlCommand;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSqlCommand() {
        return sqlCommand;
    }

    public void setSqlCommand(String sqlCommand) {
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
}
