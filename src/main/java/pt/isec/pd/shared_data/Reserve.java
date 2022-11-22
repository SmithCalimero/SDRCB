package pt.isec.pd.shared_data;

import java.io.Serial;
import java.io.Serializable;

public class Reserve implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String dateHour;
    private boolean paied;
    private int userId;
    private int showId;

    public Reserve(int id, String dateHour, boolean paied, int userId, int showId) {
        this.id = id;
        this.dateHour = dateHour;
        this.paied = paied;
        this.userId = userId;
        this.showId = showId;
    }

    public int getId() { return id; }

    public String getDateHour() { return dateHour; }

    public boolean isPaied() { return paied; }

    public int getUserId() { return userId; }

    public int getShowId() { return showId; }

    @Override
    public String toString() {
        return "id: " + id + "\n" +
                "date: " + dateHour + "\n" +
                "userId: " + userId + "\n" +
                "showId: " + showId;
    }
}
