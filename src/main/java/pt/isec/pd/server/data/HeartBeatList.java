package pt.isec.pd.server.data;

import pt.isec.pd.shared_data.HeartBeatEvent;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class HeartBeatList extends LinkedList<HeartBeatEvent>{
    public void updateList(HeartBeatEvent element) {
        element.addTimeStamp(new Date());

        int index = indexOf(element);
        if (index == -1) {
            add(element);
            return;
        }
        replaceHeartBeat(index, element);
    }

    private void replaceHeartBeat(int index,HeartBeatEvent newHeartBeat) {
        remove(index);
        add(newHeartBeat);
    }

    public List<HeartBeatEvent> getOrderList() {
        List<HeartBeatEvent> orderList = this;
        Collections.sort(orderList);
        return orderList;
    }
}
