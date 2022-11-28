package pt.isec.pd.shared_data;

import java.util.Comparator;

public class CompareDbVersionHeartBeat implements Comparator<HeartBeat> {
    @Override
    public int compare(HeartBeat o1, HeartBeat o2) {
        return Integer.compare(o1.getDbVersion(),o2.getDbVersion());
    }
}
