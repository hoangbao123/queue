package finitequeue;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CFQNSBroker extends DatacenterBroker {

    private double lamda;
    public CFQNSJobsQueue<CFQNSJob> jobBuffer = new CFQNSJobsQueue<>();

    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by Sim_entity class from
     *             simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public CFQNSBroker(String name, double _lamda) throws Exception {

        super(name);
        this.lamda = _lamda;

    }

    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
//            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
//                    + " has been created in Datacenter #" + mainDatacenterId + ", Host #"
//                    + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
                    + " failed in Datacenter #" + datacenterId);
        }

        incrementVmsAcks();

    }

    protected void createVmsInDatacenter(int datacenterId) {
        submitCloudlets();
    }

    protected void submitCloudlets() {
//        Log.printLine("broker send the cloudlet first");
        sendCloudlet();

        // kich hoat datacenter hoat dong ( bat lien tiep may
        // voi thuat toan control middle servers
        // ham nay chi duoc goi mot lan sau khi broker duoc khoi tao va cloudsim.startsimulation()
        sendNow(CFQNSHelper.getMainDatacenterId(), CFQNSConstants.ControlMiddleHostEvent, new Integer(0));

    }

    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // if the simulation finishes
            case CFQNSConstants.sendCloudletEvent:
                sendCloudlet();
                break;
            case CFQNSConstants.ReleaseBufferOfBroker:
                releaseBuffer();
                break;
            // other unknown tags are processed by this method
            default:

                break;
        }
        if (ev == null) {
            Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null. dd" + ev.getTag());
            return;
        }

    }

    private void sendCloudlet() {
//        if(CMSHelper.getCloudletid() > CMSHelper.totalJobs) CloudSim.terminateSimulation();
        CFQNSJob cloudlet = CFQNSHelper.createJob(getId());
//            Log.printLine(CloudSim.clock() + " : broker send  cloudlet " + cloudlet.getCloudletId());

        int queueid = -100;
        if(CFQNSHelper.isSingleQueue) queueid = selectDatacenterForSingleQueue();
        else queueid = selectBestDatacenterV11();

        if (queueid == -11) {
            //ko tim duoc queue cho job vao bufferd
//            System.out.println("!!!!!!!loi roi ");
            jobBuffer.add(cloudlet);
        } else {
            // neu tim duoc queue
//            Log.printLine("send cloudlet to "+ queueid);
            sendNow(queueid, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
        }
        send(getId(), StdRandom.exp(lamda), CFQNSConstants.sendCloudletEvent);
//        }


    }

    public void releaseBuffer() {
        // tac cho: nhieu thang datacenter cung mot luc bao cho broker release buffer
        // danh den gui hang loat den cac datacenter
        // va gui nhieu lan ---> fail vi het cho chua
//      Log.printLine("ham releaseBuffer() duoc thuc hien");
//        Log.printLine("release buffer in broker at time " + CloudSim.clock());
        if (jobBuffer.isEmpty()) {
//            Log.printLine("buffer is empty" + " at time " + CloudSim.clock());
        } else {

            // chon datacenter roi gui cho no
            // --------------tien hanh chon queue phu hop cho job
//        CFQNSDatacenter datacenterBest = null;
            // xet duyet tat ca cac loi init, waiting, on
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // duyet tat cac cac queue ma hoat dong va chua full
                if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
                        (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                    // lan chon ra cac datacenter trong
//                datacenterBest = temp;
                    for (int xxx = 0; xxx < CFQNSHelper.jobsqueuecapacity - temp.getSystemLength(); xxx++) {
                        // gui luong job = luong slot cua datacenter
                        if (jobBuffer.isEmpty()) {
                            break; // loai vong for 2
                        } else {
                            Log.printLine("release one job in buffer in broker successfully to datacenter "
                                    + temp.getId() + " at time" + CloudSim.clock());
                            // gui cloudlet trong buffer ra cho thang datacenter tim duoc
                            sendNow(temp.getId(), CloudSimTags.CLOUDLET_SUBMIT, jobBuffer.poll());

                        }
                    }
                    if (jobBuffer.isEmpty()) {
                        Log.printLine("release all of buffer" + " at time " + CloudSim.clock());
                        break; // loai vong for 1
                    }

                }
            }
        }
    }

    public int selectBestDatacenter() { // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len

        // ------------------ update trang thai cua he thong, tat hoac bat queue moi
        // tim xem co queue nao ko co job ko
        // queue ko co job chi co the la queue waiting
        // vi queue init van the co job neu chua init xong
        CFQNSDatacenter datacenternojob = null;

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem  queue nao hoat dong va ko co job
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                datacenternojob = temp;
                break;
            }
        }

        if (datacenternojob == null) {
            // neu ko co queue nao 0 job
            // kiem tra de init mot datacenter moi
            // tim queue max:
            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
                if (temp.state != CFQNSDatacenter.OFF) {
                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                }
            }
            if (min > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi
                datacenternojob = creatNewDatacenter();
                datacenternojob.init(); // khoi tao
            }

        } else {
            // neu da co roi thi kiem tra de tat no di
            // tim queue min:
            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // duyet cac queue hoat dong va khac queue no job dang xet
                if ((temp.state != CFQNSDatacenter.OFF) && (temp.getId() != datacenternojob.getId())) {
                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                }
            }
            if (min < CFQNSHelper.jobsqueuethresholddown) {
                // huy bo queue waiting
                datacenternojob.toOFF();
            }
        }
        // ^^^^^^^^^^^^ xong buoc update trang thai cua he thong


        // --------------tien hanh chon queue phu hop cho job
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter co hang doi lon nhat
                if (datacenterBest == null) datacenterBest = temp;
                else {
                    if (temp.getSystemLength()
                            > datacenterBest.getSystemLength())
                        datacenterBest = temp;
                }

            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            return -1;
            // neu tat ca deu day thi phai anable mot datacneter moi
//            for(int i =0; i< CMSHelper.listSubDatacenter.size();i++){
//                CMSDatacenter temp = CMSHelper.listSubDatacenter.get(i);
//                if(temp.state != CMSDatacenter.ON){ // tim xem co datacenter nao ko chay thi bat
//                    // no len
//                    // anable datacenter nay va tra lai id cua datacenter nay luon
//                    temp.toON();
//                    datacenterBest = temp;
//                    break;  // thoat khoi vong for
//                }
//            }


//            datacenterBest = datacenterWaiting;// neu tat ca da day thi cho job vao queue waiting hoac queue init
//            if(datacenterBest == null) {
//                Log.printLine("*** do dai list data: "+CMSHelper.listSubDatacenter.size());
//                Log.printLine("*** number of ON" + numbernotOFF);
//                Log.printLine("******* ko tim duoc datacenter init or waiting cho job");
//            }
            // neu van chua tim duoc datacenter
            // tao moi:
//            if(datacenterBest == null) {
//                datacenterBest = CMSHelper.createSubDatacenter("sub_system");
//                datacenterBest.toON();
///*
//                if(CMSHelper.listSubDatacenter.size() > 50){
//                    Log.printLine("******** so datacenter la: "+CMSHelper.listSubDatacenter.size()+" *****");
//                    for(int i =0; i< CMSHelper.listSubDatacenter.size();i++) {
//                        Log.print(" " + CMSHelper.listSubDatacenter.get(i).getQueueLength());
//                    }
//                }
//                */
//
//            }


        }

        return datacenterBest.getId();
    }

    public int selectBestDatacenterV2() { // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len

        // ------------------ update trang thai cua he thong, tat hoac bat queue moi
        // tim xem co queue nao ko co job ko
        // queue ko co job chi co the la queue waiting
        // vi queue init van the co job neu chua init xong
        CFQNSDatacenter datacenternojob = null;

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem  queue nao hoat dong va ko co job
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                datacenternojob = temp;
                break;
            }
        }

        if (datacenternojob == null) {
            // neu ko co queue nao 0 job
            // kiem tra de init mot datacenter moi
            // tim tong cac cho trong
            int numbetslot = 0;
            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
                if (temp.state != CFQNSDatacenter.OFF) {
                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
                }
            }

//            if(min > CFQNSHelper.jobsqueuethresholdup) {
//                // tao queue moi
//                datacenternojob = CFQNSHelper.createSubDatacenter("sub_system");
//                datacenternojob.init(); // khoi tao
//            }
            if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
                // tao queue moi
                datacenternojob = creatNewDatacenter();
                datacenternojob.init(); // khoi tao
            }

        } else {
            // neu da co roi thi kiem tra de tat no di
            // tim queue min:
            int numbetslot = 0;
            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // duyet cac queue hoat dong va khac queue no job dang xet
                if ((temp.state != CFQNSDatacenter.OFF) && (temp.getId() != datacenternojob.getId())) {
                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();

                }
            }
            if (numbetslot > (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // huy bo queue waiting
                datacenternojob.toOFF();
            }
        }
        // ^^^^^^^^^^^^ xong buoc update trang thai cua he thong


        // --------------tien hanh chon queue phu hop cho job
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter dau tien ma con cho trong
                datacenterBest = temp;
                break;

            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            return -1;


        }

        return datacenterBest.getId();
    }

    // giong voi select datacenter v2 nhung chon queue co do dai lon nhat
    public int selectBestDatacenterV3() { // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len

        // ------------------ update trang thai cua he thong, tat hoac bat queue moi
        // tim xem co queue nao ko co job ko
        // queue ko co job chi co the la queue waiting
        // vi queue init van the co job neu chua init xong
        CFQNSDatacenter datacenternojob = null;

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem  queue nao hoat dong va ko co job
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                datacenternojob = temp;
                break;
            }
        }

        if (datacenternojob == null) {
            // neu ko co queue nao 0 job
            // kiem tra de init mot datacenter moi
            // tim tong cac cho trong
            int numbetslot = 0;
            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
                if (temp.state != CFQNSDatacenter.OFF) {
                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
                }
            }

//            if(min > CFQNSHelper.jobsqueuethresholdup) {
//                // tao queue moi
//                datacenternojob = CFQNSHelper.createSubDatacenter("sub_system");
//                datacenternojob.init(); // khoi tao
//            }
            if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
                // tao queue moi
                datacenternojob = creatNewDatacenter();
                datacenternojob.init(); // khoi tao
            }

        } else {
            // neu da co roi thi kiem tra de tat no di
            // tim queue min:
            int numbetslot = 0;
            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // duyet cac queue hoat dong va khac queue no job dang xet
                if ((temp.state != CFQNSDatacenter.OFF) && (temp.getId() != datacenternojob.getId())) {
                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();

                }
            }
            if (numbetslot > (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // huy bo queue waiting
                datacenternojob.toOFF();
            }
        }
        // ^^^^^^^^^^^^ xong buoc update trang thai cua he thong


        // --------------tien hanh chon queue phu hop cho job
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter co hang doi lon nhat
                if (datacenterBest == null) datacenterBest = temp;
                else {
                    if (temp.getSystemLength()
                            > datacenterBest.getSystemLength())
                        datacenterBest = temp;
                }

            }
        }
        Log.printLine("chon datacenter dai nhat la: " + datacenterBest.getId());
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            return -1;


        }

        return datacenterBest.getId();
    }

    // ko chon datacenter o trang thai init nua
    // neu chi co datacenter o trang thai init la con trong
    // thi cho vao bo dem


    // policy select queue dau tien thoa man
    public int selectBestDatacenterV4() { // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len

        // ------------------ update trang thai cua he thong, tat hoac bat queue moi
        // tim xem co queue nao ko co job ko
        // queue ko co job chi co the la queue waiting
        // vi queue init van the co job neu chua init xong
        CFQNSDatacenter datacenternojob = null;

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem  queue nao hoat dong va ko co job
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                datacenternojob = temp;
                break;
            }
        }

        if (datacenternojob == null) {
            // neu ko co queue nao 0 job
            // kiem tra de init mot datacenter moi
            // tim tong cac cho trong
            int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
                if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
                }
            }

//            if(min > CFQNSHelper.jobsqueuethresholdup) {
//                // tao queue moi
//                datacenternojob = CFQNSHelper.createSubDatacenter("sub_system");
//                datacenternojob.init(); // khoi tao
//            }
            if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
                // tao queue moi
                datacenternojob = creatNewDatacenter();
                datacenternojob.init(); // khoi tao
            }

        } else {
            // neu da co roi thi kiem tra de tat no di
            // tim queue min:
            int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // duyet cac queue hoat dong va khac queue no job dang xet
                if ((temp.state != CFQNSDatacenter.OFF) && (temp.getId() != datacenternojob.getId())) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();

                }
            }
            if (numbetslot > (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // huy bo queue waiting
                datacenternojob.toOFF();
            }
        }
        // ^^^^^^^^^^^^ xong buoc update trang thai cua he thong


        // --------------tien hanh chon queue phu hop cho job
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter dau tien ma con cho trong
                datacenterBest = temp;
                break;

            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;


        }

        return datacenterBest.getId();
    }

    // policy select longest queue
    public int selectBestDatacenterV5() {
        // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len

        // ------------------ update trang thai cua he thong, tat hoac bat queue moi
        // tim xem co queue nao ko co job ko
        // queue ko co job chi co the la queue waiting
        // vi queue init van the co job neu chua init xong
        CFQNSDatacenter datacenternojob = null;

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem  queue nao hoat dong va ko co job
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                datacenternojob = temp;
                break;
            }
        }

        if (datacenternojob == null) {
            // neu ko co queue nao 0 job
            // kiem tra de init mot datacenter moi
            // tim tong cac cho trong
            int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
                if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
                }
            }

//            if(min > CFQNSHelper.jobsqueuethresholdup) {
//                // tao queue moi
//                datacenternojob = CFQNSHelper.createSubDatacenter("sub_system");
//                datacenternojob.init(); // khoi tao
//            }
            if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
                // tao queue moi
                datacenternojob = creatNewDatacenter();
                datacenternojob.init(); // khoi tao
            }

        } else {
            // neu da co roi thi kiem tra de tat no di
            // tim queue min:
            int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                // duyet cac queue hoat dong va khac queue no job dang xet
                if ((temp.state != CFQNSDatacenter.OFF) && (temp.getId() != datacenternojob.getId())) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                    numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();

                }
            }
            if (numbetslot > (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // huy bo queue waiting
                datacenternojob.toOFF();
            }
        }
        // ^^^^^^^^^^^^ xong buoc update trang thai cua he thong


        // --------------tien hanh chon queue phu hop cho job
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter co hang doi lon nhat
                if (datacenterBest == null) datacenterBest = temp;
                else {
                    if (temp.getSystemLength()
                            > datacenterBest.getSystemLength())
                        datacenterBest = temp;
                }

            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;


        }

        return datacenterBest.getId();
    }

    // policy select longest queue
    // thay doi doan tao queue hay kill queue
    public int selectBestDatacenterV6() {
        // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len


        // ____________________kiem tra de tao queue hoac xoa queue ___________

        // tim tong cac cho trong
        int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
            if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
            }
        }

        numbetslot = numbetslot - jobBuffer.getsize();

        if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
            // tao queue moi
            CFQNSDatacenter newdata = creatNewDatacenter();
            newdata.init(); // khoi tao
//            Log.enable();

//            Log.printLine(" sau khi tao co so queue " + CFQNSHelper.listSubDatacenter.size());
//            for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                if (temp.state != CFQNSDatacenter.OFF) {
//                    Log.print(" " + temp.getSystemLength());
//                }
//            }
//            Log.printLine();

//            Log.disable();
        } else {

            if (numbetslot > (2*CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // kill queue neu co queue trong
                // tim queue trong khoi tao moi nhat de kill

                // tim xem co queue nao ko co job ko
                // chon ra queue co thoi gian khoi tao muon nhat:
                CFQNSDatacenter datacenternojobnewest = null;

                for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                    CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                    // tim kiem  queue nao hoat dong va ko co job
                    if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                        if(datacenternojobnewest == null) datacenternojobnewest = temp;
                        else {
                            if(datacenternojobnewest.timestartinit < temp.timestartinit)
                                datacenternojobnewest = temp;
                        }

                    }
                }
                if(datacenternojobnewest != null) {
                    Log.printLine("**** tim duoc datacenter ko co job de kill");
                    datacenternojobnewest.toOFF();
//                    Log.enable();

//                    Log.printLine(">>>>sau khi kill queue co so queue " + CFQNSHelper.listSubDatacenter.size());
//                    for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                        CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                        if (temp.state != CFQNSDatacenter.OFF) {
//                            Log.print(" " + temp.getSystemLength());
//                        }
//                    }
//                    Log.printLine();

//                    Log.disable();
                }
            }
        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^ xong buoc update trang thai cua he thong^^^^^^^


        // --------------tien hanh chon queue phu hop cho job
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter co hang doi lon nhat
                if (datacenterBest == null) datacenterBest = temp;
                else {
                    if (temp.getSystemLength()
                            > datacenterBest.getSystemLength())
                        datacenterBest = temp;
                }

            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;

        }
//        Log.enable();
//        Log.printLine("****** tim duoc queue lon nhat co do dai " + datacenterBest.getSystemLength());
//        for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//            if (temp.state != CFQNSDatacenter.OFF) {
//                Log.print(" " + temp.getSystemLength());
//            }
//        }
//        Log.printLine();
//        Log.disable();
        return datacenterBest.getId();
    }

    // policy select first available queue va co chinh sua --> chon datacenter cu nhat (inittime nho nhat)
    // thay doi doan tao queue hay kill queue
    public int selectBestDatacenterV7() {
        // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len


        // ____________________kiem tra de tao queue hoac xoa queue ___________

        // tim tong cac cho trong
        int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
            if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
            }
        }

        numbetslot = numbetslot - jobBuffer.getsize();

        if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
            // tao queue moi
            CFQNSDatacenter newdata = creatNewDatacenter();
            newdata.init(); // khoi tao
//            Log.enable();
//            Log.printLine(" sau khi tao co so queue " + CFQNSHelper.listSubDatacenter.size());
//            for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                if (temp.state != CFQNSDatacenter.OFF) {
//                    Log.print(" " + temp.getSystemLength());
//                }
//            }
//            Log.printLine();
//            Log.disable();
        } else {

            if (numbetslot > (2*CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // kill queue neu co queue trong
                // tim queue trong khoi tao moi nhat de kill

                // tim xem co queue nao ko co job ko

                CFQNSDatacenter datacenternojobnewest = null;

                for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                    CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                    // tim kiem  queue nao hoat dong va ko co job
                    if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                        if(datacenternojobnewest == null) datacenternojobnewest = temp;
                        else {
                            if(datacenternojobnewest.timestartinit < temp.timestartinit)
                                // datacenter moi hon la datacenter co timestartinit lon hon
                                datacenternojobnewest = temp;
                        }
                    }
                }
                if(datacenternojobnewest != null) {
                    Log.printLine("**** tim duoc datacenter ko co job de kill");
                    datacenternojobnewest.toOFF();
//                    Log.enable();
//                    Log.printLine(">>>>sau khi kill queue co so queue " + CFQNSHelper.listSubDatacenter.size());
//                    for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                        CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                        if (temp.state != CFQNSDatacenter.OFF) {
//                            Log.print(" " + temp.getSystemLength());
//                        }
//                    }
//                    Log.printLine();
//                    Log.disable();
                }
            }
        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^ xong buoc update trang thai cua he thong^^^^^^^


        // _________________tien hanh chon queue phu hop cho job_______________
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter cu nhat ma con cho trong
                if(datacenterBest == null) datacenterBest = temp;
                else {
                    if(datacenterBest.timestartinit > temp.timestartinit)
                        // datacenter cu hon la datacenter co timestartinit nho hon
                        datacenterBest = temp;
                }


            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;

        }
//        Log.enable();
//        Log.printLine("****** tim duoc queue lon nhat co do dai " + datacenterBest.getSystemLength());
//        for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//            if (temp.state != CFQNSDatacenter.OFF) {
//                Log.print(" " + temp.getSystemLength());
//            }
//        }
//        Log.printLine();
//        Log.disable();
        return datacenterBest.getId();
    }

    // ke thua tu v7 (giong het)
    // dung voi policy trong paper
    // policy select first available queue va co chinh sua --> chon datacenter cu nhat (inittime nho nhat)

    public int selectBestDatacenterV11() {
        // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len


        // ____________________kiem tra de tao queue hoac xoa queue ___________

        // tim tong cac cho trong
        int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
            if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
            }
        }

        numbetslot = numbetslot - jobBuffer.getsize();

        if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
            // tao queue moi
            CFQNSDatacenter newdata = creatNewDatacenter();
            newdata.init(); // khoi tao
//            Log.enable();
//            Log.printLine(" sau khi tao co so queue " + CFQNSHelper.listSubDatacenter.size());
//            for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                if (temp.state != CFQNSDatacenter.OFF) {
//                    Log.print(" " + temp.getSystemLength());
//                }
//            }
//            Log.printLine();
//            Log.disable();
        } else {

            if (numbetslot > (2*CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // kill queue neu co queue trong
                // tim queue trong khoi tao moi nhat de kill

                // tim xem co queue nao ko co job ko

                CFQNSDatacenter datacenternojobnewest = null;

                for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                    CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                    // tim kiem  queue nao hoat dong va ko co job
                    if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                        if(datacenternojobnewest == null) datacenternojobnewest = temp;
                        else {
                            if(datacenternojobnewest.timestartinit < temp.timestartinit)
                                // datacenter moi hon la datacenter co timestartinit lon hon
                                datacenternojobnewest = temp;
                        }
                    }
                }
                if(datacenternojobnewest != null) {
                    Log.printLine("**** tim duoc datacenter ko co job de kill");
                    datacenternojobnewest.toOFF();
//                    Log.enable();
//                    Log.printLine(">>>>sau khi kill queue co so queue " + CFQNSHelper.listSubDatacenter.size());
//                    for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                        CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                        if (temp.state != CFQNSDatacenter.OFF) {
//                            Log.print(" " + temp.getSystemLength());
//                        }
//                    }
//                    Log.printLine();
//                    Log.disable();
                }
            }
        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^ xong buoc update trang thai cua he thong^^^^^^^


        // _________________tien hanh chon queue phu hop cho job_______________
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter cu nhat ma con cho trong
                if(datacenterBest == null) datacenterBest = temp;
                else {
                    if(datacenterBest.timestartinit > temp.timestartinit)
                        // datacenter cu hon la datacenter co timestartinit nho hon
                        datacenterBest = temp;
                }


            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;

        }
//        Log.enable();
//        Log.printLine("****** tim duoc queue lon nhat co do dai " + datacenterBest.getSystemLength());
//        for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//            if (temp.state != CFQNSDatacenter.OFF) {
//                Log.print(" " + temp.getSystemLength());
//            }
//        }
//        Log.printLine();
//        Log.disable();
        return datacenterBest.getId();
    }

    public int selectBestDatacenterV10() {
       // giong voi v7 nhung ma se luon chon duoc datacenter va job se ko bi buffer nua

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue

        // tiep theo la truong hop co tu 2 queue tro len


        // ____________________kiem tra de tao queue hoac xoa queue ___________

        // tim tong cac cho trong
        int numbetslot = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
            if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                numbetslot = numbetslot + CFQNSHelper.jobsqueuecapacity - temp.getSystemLength();
            }
        }

        numbetslot = numbetslot - jobBuffer.getsize();

        if (numbetslot < (CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholdup)) {
            // tao queue moi
            CFQNSDatacenter newdata = creatNewDatacenter();
            newdata.init(); // khoi tao
//            Log.enable();
//            Log.printLine(" sau khi tao co so queue " + CFQNSHelper.listSubDatacenter.size());
//            for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                if (temp.state != CFQNSDatacenter.OFF) {
//                    Log.print(" " + temp.getSystemLength());
//                }
//            }
//            Log.printLine();
//            Log.disable();
        } else {

            if (numbetslot > (2*CFQNSHelper.jobsqueuecapacity - CFQNSHelper.jobsqueuethresholddown)) {
                // kill queue neu co queue trong
                // tim queue trong khoi tao moi nhat de kill

                // tim xem co queue nao ko co job ko

                CFQNSDatacenter datacenternojobnewest = null;

                for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                    CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                    // tim kiem  queue nao hoat dong va ko co job
                    if ((temp.state != CFQNSDatacenter.OFF) && (temp.getSystemLength() == 0)) {
                        if(datacenternojobnewest == null) datacenternojobnewest = temp;
                        else {
                            if(datacenternojobnewest.timestartinit < temp.timestartinit)
                                // datacenter moi hon la datacenter co timestartinit lon hon
                                datacenternojobnewest = temp;
                        }
                    }
                }
                if(datacenternojobnewest != null) {
                    Log.printLine("**** tim duoc datacenter ko co job de kill");
                    datacenternojobnewest.toOFF();
//                    Log.enable();
//                    Log.printLine(">>>>sau khi kill queue co so queue " + CFQNSHelper.listSubDatacenter.size());
//                    for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//                        CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//                        if (temp.state != CFQNSDatacenter.OFF) {
//                            Log.print(" " + temp.getSystemLength());
//                        }
//                    }
//                    Log.printLine();
//                    Log.disable();
                }
            }
        }
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^ xong buoc update trang thai cua he thong^^^^^^^


        // _________________tien hanh chon queue phu hop cho job_______________
        CFQNSDatacenter datacenterBest = null;
        // xet duyet tat ca cac loi init, waiting, on
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // duyet tat cac cac queue ma hoat dong va chua full
            if ((temp.state != CFQNSDatacenter.OFF) &&
                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
                // chon ra datacenter cu nhat ma con cho trong
                if(datacenterBest == null) datacenterBest = temp;
                else {
                    if(datacenterBest.timestartinit > temp.timestartinit)
                        // datacenter cu hon la datacenter co timestartinit nho hon
                        datacenterBest = temp;
                }


            }
        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;

        }
//        Log.enable();
//        Log.printLine("****** tim duoc queue lon nhat co do dai " + datacenterBest.getSystemLength());
//        for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//            if (temp.state != CFQNSDatacenter.OFF) {
//                Log.print(" " + temp.getSystemLength());
//            }
//        }
//        Log.printLine();
//        Log.disable();
        return datacenterBest.getId();
    }


    public int selectBestDatacenterV8() {
        // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue (OK)

        // tiep theo la truong hop co tu 2 queue tro len


        // ____________________kiem tra de tao queue hoac xoa queue (update trang thai cua he thong) ___________

        // phan loai cac queue:
        List<CFQNSDatacenter> listONCluster_above = new ArrayList<>();
        List<CFQNSDatacenter> listONCluster_in = new ArrayList<>();
        List<CFQNSDatacenter> listONCluster_bellow = new ArrayList<>();
        List<CFQNSDatacenter> listWaitCluster = new ArrayList<>();
        List<CFQNSDatacenter> listInitCluster = new ArrayList<>();

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state == CFQNSDatacenter.ON) {
                if(temp.getSystemLength() >= CFQNSHelper.jobsqueuethresholdup)
                    listONCluster_above.add(temp);
                else if(temp.getSystemLength() >= CFQNSHelper.jobsqueuethresholddown)
                    listONCluster_in.add(temp);
                else listONCluster_bellow.add(temp);
            }
            else {
                if (temp.state == CFQNSDatacenter.WAITING) {
                    listWaitCluster.add(temp);
                }
                else {
                    if (temp.state == CFQNSDatacenter.INIT) {
                        listInitCluster.add(temp);
                    }

                }
            }
        }

        // ^^^^^^^^^ phan loai xong
        // bat dau kiem tra de kill queue
        if(!listInitCluster.isEmpty()){
            // kiem tra xem co queue nao duoi threhold down ko de kill
            if (!listONCluster_bellow.isEmpty()) {
                // tien hanh kill queue ma co thoi gian khoi tao gan nhat
                CFQNSDatacenter datacenterinitnewest = listInitCluster.get(0);

                for (int i = 0; i < listInitCluster.size(); i++) {
                    CFQNSDatacenter temp = listInitCluster.get(i);
                    // tim kiem  queue nao hoat dong va ko co job

                    if(datacenterinitnewest.timestartinit < temp.timestartinit)
                        datacenterinitnewest = temp;
                }
                if(datacenterinitnewest != null) {
                    Log.printLine("**** tim duoc datacenter init de kill");
                    datacenterinitnewest.toOFF();
                }
            }
        }

        if(!listWaitCluster.isEmpty()){
            // kiem tra xem co queue nao duoi threhold down ko de kill
            if (!listONCluster_bellow.isEmpty()) {
                // tien hanh kill queue ma co thoi gian khoi tao gan nhat
                CFQNSDatacenter datacenterwaitnewest = listWaitCluster.get(0);

                for (int i = 0; i < listWaitCluster.size(); i++) {
                    CFQNSDatacenter temp = listWaitCluster.get(i);
                    // tim kiem  queue nao hoat dong va ko co job

                    if(datacenterwaitnewest.timestartinit < temp.timestartinit)
                        datacenterwaitnewest = temp;
                }
                if(datacenterwaitnewest != null) {
                    Log.printLine("**** tim duoc datacenter init de kill");
                    datacenterwaitnewest.toOFF();
                }
            }
        }
        // ^^^^^^^^^^^^^^ xong buoc kiem tra va kill queue


        // ^^^^^^^^^^^^^^^^^^^^^^^^^^ xong buoc update trang thai cua he thong^^^^^^^


        // --------------tien hanh chon queue phu hop cho job
        // chon random trong dong clusterON in threshold neu co
        CFQNSDatacenter datacenterBest = null;
        if(!listONCluster_in.isEmpty()){
            // chon random ra mot cai
            int ran = StdRandom.uniform(listONCluster_in.size());
            datacenterBest = listONCluster_in.get(ran);
        }
        else {
            // chon ra trong dong cluster ON bellow:
            if(!listONCluster_bellow.isEmpty()){
                // chon random
                int ran = StdRandom.uniform(listONCluster_bellow.size());
                datacenterBest = listONCluster_bellow.get(ran);
            }
            else{
                // neu van khong co thi chon ra trong dong wait
                if(!listWaitCluster.isEmpty()){
                    int ran = StdRandom.uniform(listWaitCluster.size());
                    datacenterBest = listWaitCluster.get(ran);
                }
                else {
                    // neu van khong co nua thi phai chon trong dong cluster ON above
                    // va tien hanh init queue moi neu chua co cluster init hoac kich thuoc nho hon buffer
                    // chon cai dau tien con slot trong cac cluster ON above threshold
                    for(int i =0; i < listONCluster_above.size();i++){
                        if(listONCluster_above.get(i).getSystemLength() < CFQNSHelper.jobsqueuecapacity)
                            datacenterBest = listONCluster_above.get(i);
                    }
                    // kiem tra xem da co queue init chua
                    // neu chua thi init queue
                    if(listInitCluster.isEmpty()){
                        CFQNSDatacenter newdata = creatNewDatacenter();
                        newdata.init(); // khoi tao
                    }

//                    if(datacenterBest == null){
//                        // kiem tra  de init them queue neu can
//
//                    }
//                    else{
//                        // kiem tra xem da co queue init chua
//                        // neu chua thi init queue
//                        if(listInitCluster.isEmpty()){
//                            CFQNSDatacenter newdata = creatNewDatacenter();
//                            newdata.init(); // khoi tao
//                        }
//                    }
                }
            }
        }

//        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
////            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
//            // duyet tat cac cac queue ma hoat dong va chua full
//            if ((temp.state != CFQNSDatacenter.OFF) && (temp.state != CFQNSDatacenter.INIT) &&
//                    (temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity)) {
//                // chon ra datacenter co hang doi lon nhat
//                if (datacenterBest == null) datacenterBest = temp;
//                else {
//                    if (temp.getSystemLength()
//                            > datacenterBest.getSystemLength())
//                        datacenterBest = temp;
//                }
//
//            }
//        }
        // sau buoc nay da tim duoc queue lon nhat
        // luon phai tim thay neu ko thay la loi
        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;

        }
//        Log.enable();
//        Log.printLine("****** tim duoc queue lon nhat co do dai " + datacenterBest.getSystemLength());
//        for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//            if (temp.state != CFQNSDatacenter.OFF) {
//                Log.print(" " + temp.getSystemLength());
//            }
//        }
//        Log.printLine();
//        Log.disable();
        return datacenterBest.getId();
    }

    public int selectBestDatacenterV9() {
        // tra lai id cua datacenter cho duoc
        // chon datacenter phu hop nhat
        // ( load balacing )

        // can cap nhap lai trang thai de init queue hoac toOFF queue
        // dua theo tinh trang load cua cac queue

        // kiem tra nguong tren:

        // neu nhu ko co datacenter of waiting thi moi kiem tra nguong tren

        // viec select nay cung voi viec update trang thai he thong de khoi tao hoac tat
        // chi thuc hien khi hien tai co nhieu hon 2 datacenter on
        // do do can kiem tra truoc

        // nhung co the co nhieu datacenter init

        // ----------------kiem tra luong queue trong he thong xem co = 1 ko
        int numbernotOFF = 0;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state != CFQNSDatacenter.OFF) {
                numbernotOFF++;
            }
        }
        if (numbernotOFF == 0) {
            Log.printLine("******  loi tat het datacenter");
            return -1;
        }
        if (numbernotOFF == 1) {
//            Log.printLine("******  con 1 queue");
            // khi con 1 queue thi chi can
            // kiem tra no co lon hon threshold tren ko
            // de khoi tao queue moi
            CFQNSDatacenter datanotOFF = null;
            for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
                CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
                if (temp.state != CFQNSDatacenter.OFF) {
                    datanotOFF = temp;
                    break;
                }
            }
            if (datanotOFF.getSystemLength() > CFQNSHelper.jobsqueuethresholdup) {
                // tao queue moi

                CFQNSDatacenter newdata = creatNewDatacenter();

                newdata.init(); // khoi tao
            }
            return datanotOFF.getId(); // tra lai cai nay vi co moi mot cai

        }

        // ^^^^^^^^^^^^^^^^^^ ket thuc kiem tra luong queue (OK)

        // tiep theo la truong hop co tu 2 queue tro len


        // ____________________kiem tra de tao queue hoac xoa queue (update trang thai cua he thong) ___________

        // phan loai cac queue:
        List<CFQNSDatacenter> listONCluster_full = new ArrayList<>();
        List<CFQNSDatacenter> listONCluster_above = new ArrayList<>();
        List<CFQNSDatacenter> listONCluster_in = new ArrayList<>();
        List<CFQNSDatacenter> listONCluster_bellow = new ArrayList<>();
        List<CFQNSDatacenter> listWaitCluster = new ArrayList<>();
        List<CFQNSDatacenter> listInitCluster = new ArrayList<>();

        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            if (temp.state == CFQNSDatacenter.ON) {
                if(temp.getSystemLength() < CFQNSHelper.jobsqueuecapacity) {
                    if (temp.getSystemLength() >= CFQNSHelper.jobsqueuethresholdup)
                        listONCluster_above.add(temp);
                    else if (temp.getSystemLength() >= CFQNSHelper.jobsqueuethresholddown)
                        listONCluster_in.add(temp);
                    else listONCluster_bellow.add(temp);
                }
                else listONCluster_full.add(temp);
            }
            else {
                if (temp.state == CFQNSDatacenter.WAITING) {
                    listWaitCluster.add(temp);
                }
                else {
                    if (temp.state == CFQNSDatacenter.INIT) {
                        listInitCluster.add(temp);
                    }

                }
            }
        }

        // ^^^^^^^^^ phan loai xong
        // bat dau kiem tra de kill queue hoac init queue dua vao so luong jobs/ tong capacity
        // dem so luong jobs:

        int numberjobs = 0;
        int totalcap = 0;
//            int min = CFQNSHelper.jobsqueuecapacity;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem cac queue bat ky hoat dong va tim ra cai nho nhat (min)
            if (temp.state != CFQNSDatacenter.OFF) {
//                    if (temp.getSystemLength() < min) min = temp.getSystemLength();
                numberjobs = numberjobs + temp.getSystemLength();
                totalcap = totalcap + CFQNSHelper.jobsqueuecapacity;
            }
        }
        numberjobs = numberjobs + jobBuffer.getsize();
        float systemload = ((float)numberjobs) /((float)totalcap) ;

        if(systemload < CFQNSHelper.thrDown) {
            // tien hanh tat di mot queue
            if (!listInitCluster.isEmpty()) {
                // kiem tra xem co queue nao duoi threhold down ko de kill
                if (!listONCluster_bellow.isEmpty()) {
                    // tien hanh kill queue ma co thoi gian khoi tao gan nhat
                    CFQNSDatacenter datacenterinitnewest = listInitCluster.get(0);

                    for (int i = 0; i < listInitCluster.size(); i++) {
                        CFQNSDatacenter temp = listInitCluster.get(i);
                        // tim kiem  queue nao hoat dong va ko co job

                        if (datacenterinitnewest.timestartinit < temp.timestartinit)
                            datacenterinitnewest = temp;
                    }
                    if (datacenterinitnewest != null) {
                        Log.printLine("**** tim duoc datacenter init de kill");
                        datacenterinitnewest.toOFF();

//                        System.out.println("after kill init cluster:   queue ON "+ (listONCluster_full.size()+listONCluster_above.size()+
//                        listONCluster_in.size()+listONCluster_bellow.size())+" queue wait "+listWaitCluster.size()+
//                        " queue init "+(listInitCluster.size() - 1) +"___ job/cap: "+systemload);
                    }
                }
            }
            else {

                if (!listWaitCluster.isEmpty()) {
                    // kiem tra xem co queue nao duoi threhold down ko de kill
                    if (!listONCluster_bellow.isEmpty()) {
                        // tien hanh kill queue ma co thoi gian khoi tao gan nhat
                        CFQNSDatacenter datacenterwaitnewest = listWaitCluster.get(0);

                        for (int i = 0; i < listWaitCluster.size(); i++) {
                            CFQNSDatacenter temp = listWaitCluster.get(i);
                            // tim kiem  queue nao hoat dong va ko co job

                            if (datacenterwaitnewest.timestartinit < temp.timestartinit)
                                datacenterwaitnewest = temp;
                        }
                        if (datacenterwaitnewest != null) {
                            Log.printLine("**** tim duoc datacenter wait de kill");
                            datacenterwaitnewest.toOFF();

//                            System.out.println("after kill wait cluster:   queue ON " + (listONCluster_full.size() + listONCluster_above.size() +
//                                    listONCluster_in.size() + listONCluster_bellow.size()) + " queue wait " + (listWaitCluster.size() -1 )+
//                                    " queue init " + listInitCluster.size()+"___ job/cap: "+systemload);
                        }
                    }
                }
            }
        }
        else {
            if(systemload > CFQNSHelper.thrUP){
                // tien hanh init queue moi
                CFQNSDatacenter newdata = creatNewDatacenter();
                newdata.init(); // khoi tao

//                System.out.println("after      init         :   queue ON " + (listONCluster_full.size() + listONCluster_above.size() +
//                        listONCluster_in.size() + listONCluster_bellow.size()) + " queue wait " + (listWaitCluster.size()) +
//                        " queue init " + (listInitCluster.size()+1)+"___ job/cap: "+systemload);
            }
        }
        // ^^^^^^^^^^^^^^ xong buoc kiem tra va kill queue


        // ^^^^^^^^^^^^^^^^^^^^^^^^^^ xong buoc update trang thai cua he thong^^^^^^^


        // --------------tien hanh chon queue phu hop cho job
        // chon random trong dong clusterON in threshold neu co
        CFQNSDatacenter datacenterBest = null;
        if(!listONCluster_in.isEmpty()){
            // chon random ra mot cai
            int ran = StdRandom.uniform(listONCluster_in.size());
            datacenterBest = listONCluster_in.get(ran);
        }
        else {
            // chon ra trong dong cluster ON bellow:
            if(!listONCluster_bellow.isEmpty()){
                // chon random
                int ran = StdRandom.uniform(listONCluster_bellow.size());
                datacenterBest = listONCluster_bellow.get(ran);
            }
            else{
                // neu van khong co thi chon ra trong dong wait
                if(!listWaitCluster.isEmpty()){
                    int ran = StdRandom.uniform(listWaitCluster.size());
                    datacenterBest = listWaitCluster.get(ran);
                }
                else {
                    // neu van khong co nua thi phai chon trong dong cluster ON above (neu co)
                    // chon random trong dong do
                    if(!listONCluster_above.isEmpty()) {
                        int ran = StdRandom.uniform(listONCluster_above.size());
                        datacenterBest = listONCluster_above.get(ran);

                    }
                    // neu khong co thi phai cho vao buffer thoi

                }
            }
        }

        if (datacenterBest == null) {
            // neu ko tim thay thi he thong da bi loi
//            Log.printLine("***** he thong loi vi ko the tim duoc queue phu hop");
            Log.printLine("ko the tim duoc queue phu hop cho job -----> cho vao buffer");
            return -11;

        }
//        Log.enable();
//        Log.printLine("****** tim duoc queue lon nhat co do dai " + datacenterBest.getSystemLength());
//        for (int vvv = 0; vvv < CFQNSHelper.listSubDatacenter.size(); vvv++) {
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(vvv);
//            if (temp.state != CFQNSDatacenter.OFF) {
//                Log.print(" " + temp.getSystemLength());
//            }
//        }
//        Log.printLine();
//        Log.disable();
        return datacenterBest.getId();
    }

    public int selectDatacenterForSingleQueue(){
        // don gian chi la tra lai main datacenter id ma ko tao bat ky
        // mot subdatacenter id nao khac
        // very simple
        return CFQNSHelper.mainDatacenterId;
    }

    protected CFQNSDatacenter creatNewDatacenter() {
        // duyet tim datacenter ko hoat dong (OFF)
        CFQNSDatacenter newdatacenter = null;
        for (int i = 0; i < CFQNSHelper.listSubDatacenter.size(); i++) {
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            // tim kiem  queue ko nao hoat dong
            if (temp.state == CFQNSDatacenter.OFF) {
                newdatacenter = temp;
                break;
            }
        }
        if (newdatacenter == null) newdatacenter = CFQNSHelper.createSubDatacenter("sub_system");
        return newdatacenter;
    }

    protected void processCloudletReturn(SimEvent ev) {
        CFQNSJob cloudlet = (CFQNSJob) ev.getData();
//        listJob.add(cloudlet);

//        Log.printLine("da nhan job");
        if (cloudlet.getTimeComplete() == 0) {
//            System.out.println("*********  job chua hoan thanh duoc tra lai broker");
            return;
        }
        if (start) {

            numberofjob++;
            if (CloudSim.clock() > CFQNSHelper.totalTimeSimulate) {
                Log.printLine(CloudSim.clock());
                CloudSim.terminateSimulation();
            }
//        if(numberofjob > jobbatdau )
            totalwaitingTime = totalwaitingTime - cloudlet.getTimeCreate() + cloudlet.getTimeStartExe();
            totalResponseTime = totalResponseTime - cloudlet.getTimeCreate() + cloudlet.getTimeComplete();

        } else {
            if (CloudSim.clock() > CFQNSHelper.timeStartSimulate) {
                start = true;
//                System.out.println("bat dau dem so job");
            }

        }

    }

    public double getNumberOfJob() {
        return numberofjob;
    }

    public double totalwaitingTime = 0;
    public double totalResponseTime = 0;

    public int numberofjob = 0;
//    public int jobbatdau = 00000000;
//    public int jobketthuc = 50000000;


    public boolean start = false;

    protected void clearDatacenters() {

    }

    public List<CFQNSJob> listJob = new ArrayList<>();

    public double getMeanWaittingTime() {
        return totalwaitingTime / (numberofjob);
    }

    public double getMeanResponseTime() {
        return totalResponseTime / numberofjob;
    }
}