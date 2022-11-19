package pt.isec.pd.server.data;

import pt.isec.pd.shared_data.HeartBeat;
import pt.isec.pd.shared_data.ServerAddress;

import java.util.*;

public class HeartBeatList extends LinkedList<HeartBeat>{
    public synchronized void updateList(HeartBeat element) {
        element.addTimeStamp(new Date());

        int index = indexOf(element);
        if (index == -1) {
            add(element);
            return;
        }

        remove(index);
        add(index,element);
    }

    public List<ServerAddress> getOrderList() {
        List<ServerAddress> servers = new ArrayList<>();
        List<HeartBeat> orderList = this;
        Collections.sort(orderList);

        for (HeartBeat event : orderList) {
            servers.add(new ServerAddress("127.0.0.1",event.getPortTcp()));
        }

        return servers;
    }
}
