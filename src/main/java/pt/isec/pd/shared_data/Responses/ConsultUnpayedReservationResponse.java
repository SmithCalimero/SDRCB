package pt.isec.pd.shared_data.Responses;

import pt.isec.pd.shared_data.Reserve;

import java.util.ArrayList;

public class ConsultUnpayedReservationResponse extends Response {
    ArrayList<Reserve> reserves;

    public ArrayList<Reserve> getReserves() {
        return reserves;
    }

    public void setReserves(ArrayList<Reserve> reserves) {
        this.reserves = reserves;
    }
}
