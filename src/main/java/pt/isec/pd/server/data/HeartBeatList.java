package pt.isec.pd.server.data;

import pt.isec.pd.shared_data.HeartBeatEvent;
import pt.isec.pd.shared_data.ServerAddress;

import java.util.*;

public class HeartBeatList extends LinkedList<HeartBeatEvent>{
    public void updateList(HeartBeatEvent element) {
        element.addTimeStamp(new Date());

        int index = indexOf(element);
        if (index == -1) {
            add(element);
            return;
        }
        add(index,element);
    }

    public List<ServerAddress> getOrderList() {
        List<ServerAddress> servers = new ArrayList<>();
        List<HeartBeatEvent> orderList = this;
        Collections.sort(orderList);

        for (HeartBeatEvent event : orderList) {
            servers.add(new ServerAddress("127.0.0.1",event.getPortTcp()));
        }

        return servers;
    }
}
