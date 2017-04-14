package finitequeue;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

/**
 * Created by dang on 21/04/2015.
 */
public class CFQNSHost extends Host {

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param vmScheduler    the vm scheduler
     */
    private CFQNSJob job = null;
    public CFQNSHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
    }
    public double timeStartSetup;
    public double timeSetupComplete;
    public double timeSetupExpect;

    public boolean isSetUpmode = false;
    public CFQNSJob getJob() {
        return job;
    }

    public void setJob(CFQNSJob job) {
        if(job != null) {
            job.setTimeStartExe(CloudSim.clock());
            if(this.job !=null) Log.printLine("************ server co job roi lai chay job nua");
        }

        this.job = job;
    }
}
