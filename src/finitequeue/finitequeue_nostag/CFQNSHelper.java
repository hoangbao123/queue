package finitequeue_nostag;



import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CFQNSHelper {


    public static boolean isSingleQueue = false;
    public static int brokerId;
    public static double timenext = 0;
//    public static int datacenterId;
    public static int vmid = 0;
    public static int extraCount = 0;
    public static int jobsqueuecapacity = 500;
    public static double thrUP = 0.7;
    public static double thrDown = 0.4;
    public static int jobsqueuethresholdup = (int) (jobsqueuecapacity * thrUP);
    public static int jobsqueuethresholddown = (int) (jobsqueuecapacity * thrDown);
    public static int mainDatacenterId;

    public static List<CFQNSDatacenter> listSubDatacenter = new ArrayList<>();
//    public static List<Host> hostListStatic;

    public static int cloudletid = 0;
    public static int vmMips = 100000;
    public static int hostNum = 100; // phai thoa man dieu kien  hostnum * muy >  lamda

    public static double theta = 0.02 ;// thoi gian job di
    public static double lamda = 0.1; // thoi gian trung binh giua 2 job la 1/lamda
    public static double muy = 0.1;  // thoi gian trung binh de complete 1 jobs laf 1/muy
    public static double controlTime = 0.1; // thoi gian cho moi vong lap trong thuat toan bat server
    public static double timeOffToMiddle = 120;
    public static double alpha = 0.05; // thoi gian trung binh de bat may la 1/alpha

    public static CFQNSDatacenter cmsdatacenter = null;

    public static CFQNSBroker mainBroker = null;

    public static double totalTimeSimulate = 2e6; // tong cong thoi gian mo phong
    public static double timeStartSimulate = 1e6; // thoi gian bat dau dem
    
    public static void setLamda(double lamda) {
        CFQNSHelper.lamda = lamda;
    }

    public static void reset(){

        brokerId = -1;
        timenext = 0;

        vmid = 0;
        extraCount = 0;
        jobsqueuecapacity = 500;
        thrUP = 0.7;
        thrDown = 0.5;
        jobsqueuethresholdup = (int) (jobsqueuecapacity * thrUP);
        jobsqueuethresholddown = (int) (jobsqueuecapacity * thrDown);
        mainDatacenterId = -1;

        listSubDatacenter = new ArrayList<>();
//        hostListStatic = null;

        cloudletid = 0;
        vmMips = 100000;
        hostNum = 10000; // phai thoa man dieu kien  hostnum * muy >  lamda

        
        lamda = 0.1; // thoi gian trung binh giua 2 job la 1/lamda
        muy = 0.1;  // thoi gian trung binh de complete 1 jobs laf 1/muy
        controlTime = 0.1; // thoi gian cho moi vong lap trong thuat toan bat server
        timeOffToMiddle = 120;
         alpha = 0.05; // thoi gian trung binh de bat may la 1/alpha

        cmsdatacenter = null;

        mainBroker = null;

        totalTimeSimulate = 8e6; // tong cong thoi gian mo phong
        timeStartSimulate = 4e6; // thoi gian bat dau dem
    }

    public static int getVmid() {
        return vmid;
    }


    public static int getCloudletid() {
        return cloudletid;
    }
    public static void setMuy(double muy) {
        CFQNSHelper.muy = muy;
    }

    public static void setControlTime(double controlTime) {
       CFQNSHelper.controlTime = controlTime;
    }

    public static void setTimeOffToMiddle(double timeOffToMiddle) {
       CFQNSHelper.timeOffToMiddle = timeOffToMiddle;
    }

    public static void setAlpha(double alpha) {
        CFQNSHelper.alpha = alpha;
    }



    public static Vm getVm(){
        vmid ++;
        int mips = vmMips;
        long size = 10000; // image size (MB)
        int ram = 512; // vm memory (MB)
        long bw = 1000;
        int pesNumber = 1; // number of cpus
        String vmm = "Xen"; // VMM name

        Vm vm = new Vm(vmid, getBrokerId(), mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
        return vm;
    }
    public static void setBrokerId(int i) {
        brokerId = i;
    }

    public static int getBrokerId() {
        return brokerId;
    }

    public static double getLamda() {
        return lamda;
    }

    public static int getMainDatacenterId() {
        return mainDatacenterId;
    }

    public static void setMainDatacenterId(int mainDatacenterId) {
        CFQNSHelper.mainDatacenterId = mainDatacenterId;
    }

    public static int getVmMips() {
        return vmMips;
    }

    public static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

                Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

//    public static double getMeanWaittingTime(List<Cloudlet> list) {
//        double waittime = 0;
//        for(int k = 0 ; k < list.size(); k++ ) {
//            waittime = waittime + list.get(k).getExecStartTime() -( (CMSJob) list.get(k)).getTimeCreate();
//        }
//        waittime = waittime / (list.size() - 0 );
//        return waittime;
//    }

//    public static double getWaittingTime(List<Cloudlet> list) {
//        int size = list.size(); double waittime = 0;
//        Cloudlet cloudlet;
//        for (int i = 0; i < size; i++) {
//            cloudlet = list.get(i);
//
//            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
//
//                waittime = waittime + cloudlet.getWaitiddngTime();
//            }
//        }
//        return waittime;
//    }

    public static CFQNSDatacenter createDatacenter(String name) {
        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();
        List<Host> hostListOn = new ArrayList<Host>();
        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 150000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Host with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        for (int i = 0; i< hostNum; i++) {
            hostList.add(
                    new CFQNSHost(
                            i,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList)
                    )
            ); // This is our machine
        }

//        hostListStatic = hostList;
        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        CFQNSDatacenter datacenter = null;
        try {
            datacenter = new CFQNSDatacenter(name, characteristics,
                    new CFQNSVmAllocationPolicy(hostListOn),
                    storageList, 0,
                    alpha , muy, lamda, controlTime, timeOffToMiddle );
        } catch (Exception e) {
            e.printStackTrace();
        }

        setMainDatacenterId(datacenter.getId());
        listSubDatacenter.add(datacenter);
        // can thay doi phuong thuc tren
//        datacenter.controlMiddleHost();
        //cmsdatacenter = datacenter;

//        datacenter.listHostOff.addAll(hostListStatic);
        return datacenter;
    }

    public static CFQNSDatacenter createSubDatacenter(String name) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();
        List<Host> hostListOn = new ArrayList<Host>();
        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 150000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Host with its id and list of PEs and add them to the list of machines
        int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        for (int i = 0; i< hostNum; i++) {
            hostList.add(
                    new CFQNSHost(
                            i,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList)
                    )
            ); // This is our machine
        }

//        hostListStatic = hostList;
        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        CFQNSDatacenter datacenter = null;
        try {
            datacenter = new CFQNSDatacenter(name, characteristics,
                    new CFQNSVmAllocationPolicy(hostListOn),
                    storageList, 0,
                    alpha , muy, lamda, controlTime, timeOffToMiddle );
        } catch (Exception e) {
            e.printStackTrace();
        }

//        setMainDatacenterId(datacenter.getId());
        listSubDatacenter.add(datacenter);
        // can thay doi phuong thuc tren
//        datacenter.controlMiddleHost();
        //cmsdatacenter = datacenter;

//        datacenter.listHostOff.addAll(hostListStatic);
        return datacenter;

    }

    public static DatacenterBroker createBroker(){

        DatacenterBroker broker = null;
        try {
            broker = new CFQNSBroker("Broker", lamda);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        setBrokerId(broker.getId());
        mainBroker = (CFQNSBroker)broker;
        return broker;
    }

    public static CFQNSJob createJob(int _brokerid) {

        cloudletid++;
        return  new CFQNSJob();
    }

}
