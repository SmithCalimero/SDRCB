package pt.isec.pd.server.data;

import java.io.Serial;
import java.io.Serializable;

public class Commit implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int nextVersion;

    public Commit(int nextVersion) {
        this.nextVersion = nextVersion;
    }

    public int getNextVersion() {
        return nextVersion;
    }
}
