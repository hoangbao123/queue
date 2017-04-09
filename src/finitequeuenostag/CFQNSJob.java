package finitequeue_nostag;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Created by dangbk on 20/04/2015.
 */
public class CFQNSJob {
    private double timeCreate = 0;
    private double timeStartExe = 0;
    private double timeComplete = 0;
    Host host = null;
    public CFQNSJob(){
        timeCreate = CloudSim.clock();
    }

    public double getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(double timeCreate) {
        this.timeCreate = timeCreate;
    }

    public double getTimeStartExe() {
        return timeStartExe;
    }

    public void setTimeStartExe(double timeStartExe) {
        this.timeStartExe = timeStartExe;
    }

    public double getTimeComplete() {
        return timeComplete;
    }

    public void setTimeComplete(double timeComplete) {
        this.timeComplete = timeComplete;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
}
