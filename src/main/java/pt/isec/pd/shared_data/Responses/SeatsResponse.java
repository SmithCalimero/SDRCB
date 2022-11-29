package pt.isec.pd.shared_data.Responses;

import pt.isec.pd.shared_data.Seat;

import java.io.Serializable;
import java.util.List;

public class SeatsResponse implements Serializable {
    List<Seat> seats;

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}
