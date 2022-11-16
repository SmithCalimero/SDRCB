package pt.isec.pd.shared_data;

import pt.isec.pd.utils.Constants;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class HeartBeatEvent implements Serializable,Comparable<HeartBeatEvent> {
    private int portTcp;
    private boolean status;
    private int dbVersion;
    private int activeConnections;
    private Date timeout;

    public HeartBeatEvent(int portTcp,boolean status,int dbVersion,int activeConnections) {
        this.portTcp = portTcp;
        this.status = status;
        this.dbVersion = dbVersion;
        this.activeConnections = activeConnections;
        this.timeout = addTimeStamp(new Date());
    }

    public Date addTimeStamp(Date currentTime) {
        currentTime.setSeconds(currentTime.getSeconds() + Constants.TIMESTAMP);
        timeout = currentTime;
        return timeout;
    }

    public int getPortTcp() {
        return portTcp;
    }

    public void setPortTcp(int portTcp) {
        this.portTcp = portTcp;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    public Date getTimeout() {
        return timeout;
    }

    public void setTimeout(Date timeout) {
        this.timeout = timeout;
    }

    @Override
    public int compareTo(HeartBeatEvent o) {
        return activeConnections - o.activeConnections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeartBeatEvent that = (HeartBeatEvent) o;
        return portTcp == that.portTcp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(portTcp);
    }

    @Override
    public String toString() {
        return  "tcp port: " + portTcp + " activeConnection: " + activeConnections +  "\n";
    }
}