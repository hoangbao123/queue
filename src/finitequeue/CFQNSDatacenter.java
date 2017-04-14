package finitequeue;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;


import sun.util.logging.resources.logging;

import java.util.*;

/**
 * Created by dangbk on 15/04/2015.
 */
public class CFQNSDatacenter extends Datacenter {

    public static int OFF =0;
    public static int WAITING = 1;
    public static int ON = 2;
    public double timestartinit = 0;
    //    public static int ONMAINTAIN = 10;
    public static int INIT = 3;
    public int state = CFQNSDatacenter.OFF;
    public int eventNum = 0;

    protected int nummberOfJobLost = 0; // chua co bien set thoi gian bat dau dem, o day la dem trong toan thoi gian

    protected Queue<Host> listHostOff = new LinkedList<>();
    public CFQNSJobsQueue<CFQNSJob> jobsqueue;
    public CFQNSMiddleHostQueue<Host> listHostMIDDLE;

    // them 2 bien nay de tinh luong may trung binh (gia hang doi)
    public CFQNSMiddleHostQueue<Object> hostOnNumber;
    public CFQNSMiddleHostQueue<Object> hostSetupNumber;
    public CFQNSMiddleHostQueue<Object> hostOff2MiddleNumber;

    public static CFQNSMiddleHostQueue<Object> numberONQueue = new CFQNSMiddleHostQueue<>();
    public static CFQNSMiddleHostQueue<Object> numberWAITINGQueue = new CFQNSMiddleHostQueue<>();
    
    //-----------------

    public List<Host> listHostON;
    public List<Host> listHostInSetupMode;
    //    private Host hostInOffToOn = null;
    private double alpha;
    private double controlTime;
    private double muy;
    private double lamda;
    private double timeOffToMiddle;
    private double verifySetupMode = -1;
    private boolean hasMiddle = true;
    private double numberOfJobLeave =0, numberOfCompletedJob=0;
    public double number=0;
    public boolean start = false;

//    private boolean disableEventCMS = false;
//    private boolean isSubSystem = false;
//    private boolean running = true;  // dung de kich hoat hoac vo hieu subsystem

//    public void setIsSubSystem(boolean isSubSystem) {
//        this.isSubSystem = isSubSystem;
//    }

    public CFQNSDatacenter(String name, DatacenterCharacteristics characteristics,
                           VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                           double schedulingInterval, double _alpha,
                           double _muy, double _lamda, double controltime,
                           double _timeOffToMiddle) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.alpha = _alpha;
        this.muy = _muy;
        this.lamda = _lamda;
        this.controlTime = controltime;
        this.timeOffToMiddle = _timeOffToMiddle;

        listHostOff.addAll(this.getHostList());
//        for(int i = 0;i<500;i++){
//            listHostOff.add(this.getHostList().get(i));
//        }
        listHostON = vmAllocationPolicy.getHostList();

        jobsqueue = new CFQNSJobsQueue<>();
        listHostMIDDLE = new CFQNSMiddleHostQueue<>();
        hostOnNumber = new CFQNSMiddleHostQueue<>();
        hostSetupNumber = new CFQNSMiddleHostQueue<>();
        hostOff2MiddleNumber = new CFQNSMiddleHostQueue<>();

        listHostInSetupMode = new ArrayList<>();

    }




    public void init(){
        if(state == CFQNSDatacenter.OFF) {
            state = CFQNSDatacenter.INIT;
            timestartinit = CloudSim.clock();
//        System.out.println("init datacenter "+getId());
            int numberMiddle = (int) (CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext);
            for (int i = 0; i < numberMiddle; i++) {

                hostOff2MiddleNumber.add(new Object());
            }

            send(getId(), CFQNSHelper.timeOffToMiddle, CFQNSConstants.InitSubsystemSuccess, new Integer(eventNum));

        }
    }
    private void processInitSubsystemSuccess(SimEvent ev) {

        int en = ((Integer) ev.getData()).intValue();
        if(en == eventNum) { // kiem tra xem queue da bi tat chua
            Log.printLine("********* event init success datacenter "+getId());
            state = CFQNSDatacenter.WAITING;
            numberWAITINGQueue.add(new Object());
            int numberMiddle = (int) (CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext);

            for (int i = 0; i < numberMiddle; i++) {
                if(listHostOff.isEmpty()) System.out.println("!!!!!!!!!!! het host OFF");

                listHostMIDDLE.add(listHostOff.poll());
                hostOff2MiddleNumber.poll();
            }
            // kiem tra neu hang doi khong rong thi chuyen sang ON luon
            if (!jobsqueue.isEmpty()) toON(); // ok ko van de gi chi can kiem tra jobsqueue
        }
        else {
            Log.printLine("********* event init success ko duoc thuc hien vi event cu datacenter "+getId());
        }

        // sau khi init success can thong bao voi datacenter broker
        // de release buffer
        CFQNSHelper.mainBroker.releaseBuffer();
    }
    public void toON() { // from waiting


//        System.out.println("toON datacenter "+ getId());
        // can xet 2 truong hop tu OFF to ON
        // can dong bo neu nhu da datacenter dang chay thi khogn the kich hoat nua
        if(state == CFQNSDatacenter.ON) return;
        if(state == CFQNSDatacenter.WAITING) numberWAITINGQueue.poll();
        numberONQueue.add(new Object());
//        System.out.println("number ON queue "+numberONQueue.getsize());
//        Log.printLine(""+ getId()+" "+ getName()+ " duoc kich hoat");
        state = CFQNSDatacenter.ON;
        eventNum++;
        // kich hoat lai qua trinh bat may tu off sang middle
        sendNow(getId(), CFQNSConstants.ControlMiddleHostEvent, new Integer(eventNum));


    }
    
    public void toWAITING(){ // from ON
        if(state == CFQNSDatacenter.ON) {


            Log.printLine("to waiting datacenter " + getId());
            if (state == CFQNSDatacenter.WAITING) return;
            numberONQueue.poll();
//            System.out.println("number ON queue " + numberONQueue.getsize());

            numberWAITINGQueue.add(new Object());
            state = CFQNSDatacenter.WAITING;
            eventNum++;  // chuyen trang thai va eventnum, event control middle server be canceled
            // can phai viet them de tat het cac may ON, MIDDLE, SETUP ve OFF
            // con oFF->MIDDle thi phai xu ly o cac event bat thanh cong bat thanh cong xong thi tat luon
            //
//        listHostOff.addAll(listHostON);
//        listHostON.clear();
            // bo 2 dong code tren vi may on sau khi hoan thanh xong job ma khong co job thi
            // tu dong tat

            // giam so luong MIDDLE di
            int numberofmiddletokeep = (int) (CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext);
            if (listHostMIDDLE.getsize() < numberofmiddletokeep) {

//            Log.printLine("!!!!!!!!  datacenter ko du server o middle de giam di");
                // cho nay phai duy tri bat may (ham controlMiddleServer de duy tri bat may bao gio du may middle thi thoi)
//            state = CFQNSDatacenter.ONMAINTAIN;
            }
            while (listHostMIDDLE.getsize() > numberofmiddletokeep) {
                listHostOff.add(listHostMIDDLE.poll());
            }
            turnOffAllHostInSetupMode();

            // **** can update  mean number of server tai day
            // update xong thi xoa het nh?ng biet dem number server d
//        while (!hostOnNumber.isEmpty()) hostOnNumber.poll();
//        while (!hostSetupNumber.isEmpty()) hostSetupNumber.poll();
        }
    }

    public double getNumberOfLeftJob(){
    	return numberOfJobLeave;
    }
    public double getNumberOfCompletedJob(){
    	return numberOfCompletedJob;
    }

    public void toOFForWAITING(){
        // day la xu ly khi hang doi rong
        // can xet xem neu queue nho nhat ma lon hon threshold duoi thi toOFF
        // nguoc la thi toWAITING

        // chu y neu nhu khong con queue khac ngoai queue nay thi phai
//        int min = CFQNSHelper.jobsqueuecapacity;
//        for(int i =0; i< CFQNSHelper.listSubDatacenter.size();i++){
////            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
//            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
//            // duyet cac queue con hoat dong va khac queue nay
//            if((temp.state != CFQNSDatacenter.OFF) && (temp.getId() != this.getId())) {
//                // chi xet nhung queue ma khong bi tat
//                // va khac queue nay
//                if (temp.getSystemLength() < min) {
//                    min = temp.getSystemLength();
//                }
//            }
//        }
//        // kiem tra min nay:
//        // chu y truong hop khong con queue nao khac thi min van se la lon nhat va queue ko bi
//        // tat ve off
//        if(min < CFQNSHelper.jobsqueuethresholddown) toOFF();
//        else
        if ( !CFQNSHelper.isSingleQueue) toWAITING(); // chi towaiting trong truong hop multiqueues
    }

    public void toOFF(){  // from ON or WAITING

        int temp = hostOff2MiddleNumber.getsize();
        for(int i = 0; i< temp;i++ ){
            hostOff2MiddleNumber.poll();
        }

        Log.printLine("to off datacenter "+getId());
        // tuong tu can dong bo giong nhu enable:
        if(state == CFQNSDatacenter.OFF) return;
        if(state == CFQNSDatacenter.ON){
            numberONQueue.poll();
//            System.out.println("number ON queue " + numberONQueue.getsize());

        }
        if(state == CFQNSDatacenter.WAITING) numberWAITINGQueue.poll();
//        Log.printLine(""+ getId()+" "+ getName()+ " duoc vo hieu");

        // vo hieu subsystem
        state = CFQNSDatacenter.OFF;
        // cho nay co van de neu chi de moi running = false lieu da du de vo hieu
        // hoa qua trinh bat may tu off sang middle
        // neu toOFF xong roi lai enable luon thi dan den co 2 event
        // control middle server lien luc dan toi so luong may bat tu off sang
        // middle tang len gap doi ??? giai quyet the nao day???
        // tuc la lam sao phai ngat duoc cai event con chua duoc gui toi
        // ----> them mot bien nua de dong bo
        //
//        disableEventCMS = true;
        eventNum++;

        // can phai viet them de tat het cac may ON, MIDDLE, SETUP ve OFF
        // con oFF->MIDDle thi phai xu ly o cac event bat thanh cong bat thanh cong xong thi tat luon
        //
//        listHostOff.addAll(listHostON);
//        listHostON.clear();
        // bo 2 dong code tren vi may on sau khi hoan thanh xong job ma khong co job thi
        // tu dong tat
        while(!listHostMIDDLE.isEmpty()){
            listHostOff.add(listHostMIDDLE.poll());
        }
        turnOffAllHostInSetupMode();

        // **** can update  mean number of server tai day
        // update xong thi xoa het nh?ng biet dem number server d
//        while (!hostOnNumber.isEmpty()) hostOnNumber.poll();
//        while (!hostSetupNumber.isEmpty()) hostSetupNumber.poll();
    }

    protected void turnOffAllHostInSetupMode() {
        while (listHostInSetupMode.size()> 0) {
            Host h = (Host) listHostInSetupMode.get(listHostInSetupMode.size() -1);
            ((CFQNSHost)h).isSetUpmode = false;
            listHostInSetupMode.remove(listHostInSetupMode.size() - 1);
            hostSetupNumber.poll();

            listHostOff.add(h);
//            System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

        }

        // kiem tra trong he thong neu ko con job nao, job trong he thong gom
        // job trong queue va job trong host ON
        if(jobsqueue.isEmpty() && listHostON.isEmpty()) toOFForWAITING();

    }

    public void setHasMiddle(boolean hasMiddle) {
        this.hasMiddle = hasMiddle;
    }




    public void setStartState(int numberOnServer, int numberMiddleServer){
        for (int i = 0; i< numberOnServer; i++) {
            if(listHostOff.isEmpty())
//                System.out.println("!!!!!!!!!!! het host OFF");
            {

            }
            else
                listHostON.add(listHostOff.poll());

        }
        for(int i = 0; i< numberMiddleServer; i++){
            if(listHostOff.isEmpty()) {

            }
//                System.out.println("!!!!!!!!!!! het host OFF");
            else
                listHostMIDDLE.add(listHostOff.poll());
        }
    }

    // xu ly su kien moi
    protected void processOtherEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics inquiry
            case CFQNSConstants.TurnONSuccess:
                processTurnONSuccess(ev);
                break;

            // Resource dynamic info inquiry
            case CFQNSConstants.MiddleSucess:
                processMiddleSuccess(ev);
                break;
            case CFQNSConstants.ControlMiddleHostEvent:
                controlMiddleHost(ev);
                break;
            case CFQNSConstants.JobComplete:
                processJobComplete(ev);
                break;
            case CFQNSConstants.InitSubsystemSuccess:
                processInitSubsystemSuccess(ev);
                break;
            case CFQNSConstants.ProcessJobLeave :
            	processJobLeave(ev);
            	break;
            default:
                if (ev == null) {
                    Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
                }
                break;
        }
    }

    protected void processJobComplete(SimEvent ev) {
        CFQNSJob job =(CFQNSJob) ev.getData();
        ((CFQNSHost)job.getHost()).setJob(null);
        if(start) numberOfCompletedJob++;
        //Log.printLine("job hoan thanh:"+count2);
        job.setTimeComplete(CloudSim.clock());
        Log.printLine("job hoan thanh voi thoi gian cho la " + (job.getTimeStartExe() - job.getTimeCreate()));

//      Log.printLine("so host on: " + listHostON.size() + " so host off:" + listHostOff.size() + " so host middle: " + listHostMIDDLE.getsize());

//      Log.printLine(CloudSim.clock() + " : cloudlet " + cl.getCloudletId() + " complete");

        sendNow(CFQNSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, job);

        // neu cloudlet duoc da hoan thanh
        // kiem tra hang doi va tat host:
        if (!jobsqueue.isEmpty()) { // neu co
            // chuyen ngay cloudlet do cho host vua thuc hien xong cloudlet do
            assignJobToHost(jobsqueue.poll(), job.getHost());
            // kiem tra tiep neu hang doi khong con thi tat may dang trong setup mode
            turnOffAHostInSetupMode();
        } else { // neu hang doi khong con
            // tat host vua thuc hien xong di
            Log.printLine("hang doi het job -> tat may on di");
            turnOffHostOn(job.getHost());

            // tat host o trang thai setup di
            turnOffAHostInSetupMode();
            if(listHostON.isEmpty()) toOFForWAITING();
        }
        // sau khi xu ly ben tren, luong job trong he thong giam xuong
        // bao cho broker de giai phong job trong buffer:
        // !!!!! chu y cho nay chua giai quyet duoc viec phai tat may ON
        // thi moi giam duoc do dai he thong
        // boi vi may ON do ko nen tat ma co the thuc hien luon job release tu buffer
        // tam thoi cu de the
        CFQNSHelper.mainBroker.releaseBuffer();
    }


    private void processMiddleSuccess(SimEvent ev) {
//        System.out.println(getId()+" bat middle thanh cong "+hostOff2MiddleNumber.getsize());
//        hostOff2MiddleNumber.poll();
        Host h = (Host) ev.getData();
//        System.out.println("bat sang middle thanh cong");
        if( state != CFQNSDatacenter.ON) {
            if(state == CFQNSDatacenter.WAITING){
                int numberofmiddletokeep = (int)(CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext);
                if(listHostMIDDLE.getsize() < numberofmiddletokeep) {

                    listHostMIDDLE.add(h);
                    hostOff2MiddleNumber.poll();
//                    Log.printLine("!!!! bat trong waiting thanh cong. so middle dang co"+listHostMIDDLE.getsize());
                    if (!jobsqueue.isEmpty()) {

                        checkAndTurnMiddleToOn();
                    }
                    else {
//            if(isSubSystem)
                        if(listHostON.isEmpty()) toOFForWAITING();
                    }
                } else {
                    listHostOff.add(h);
                    hostOff2MiddleNumber.poll();
                }
            }
            else {
                // chi khi nao may o trang thai ON thi moi co thuat toan
                // bat may tu off sang middle
                // init thi da co event khac xu ly
                // neu datacenter ko o trang thai ON (la OFF hoac Waiting)
                // tat di

                // tat di do day la cluster da bi turn off
                listHostOff.add(h);


            }
//            System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

            return;
        }
        else {
//        Log.printLine(CloudSim.clock()+ " : host "+h.getId()+" turn to Middle success");
            listHostMIDDLE.add(h);
            hostOff2MiddleNumber.poll();
            if (!jobsqueue.isEmpty()) {

                checkAndTurnMiddleToOn();
            } else {
//            if(isSubSystem)
                if (listHostON.isEmpty()) toOFForWAITING();
            }
        }
//        System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

    }


    public void controlMiddleHost(SimEvent ev) {

        // kiem tra event nay la event moi hay event cu
        if(state == CFQNSDatacenter.ON) {
            int et = ((Integer) ev.getData()).intValue();
            if (et == eventNum) { // neu day la event moi
                if (state == CFQNSDatacenter.ON) {
                    if (hasMiddle) {
                        double timenext = CFQNSHelper.timenext;
                        if (!listHostOff.isEmpty()) {
                            turnAOffToMiddle();
                        }

                        else {
//                            System.out.println("********** het may off ");
                        }


                        send(getId(), timenext, CFQNSConstants.ControlMiddleHostEvent, new Integer(eventNum));
                    }
                }
            }
        }
//        else {
//            if(state == CFQNSDatacenter.WAITING) {
//                int et = ((Integer) ev.getData()).intValue();
//                if (et == (eventNum-1)) {
//                    if (hasMiddle) {
//                        int numberMiddle = (int) (CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext);
//
//                        if(listHostMIDDLE.getsize() < numberMiddle) {
//                            Log.printLine("!!!!!!!!! maintain da chay");
//                            Log.printLine(listHostMIDDLE.getsize()+"  "+ numberMiddle);
//                            Log.printLine(listHostMIDDLE.getsize() < numberMiddle);
//                            double timenext = CFQNSHelper.timenext;
//                            if (!listHostOff.isEmpty()) {
//                                turnAOffToMiddle();
//                            } else {
//                                Log.printLine("**********het may off ");
//                            }
//
//
//
//                            send(getId(), timenext, CFQNSConstants.ControlMiddleHostEvent, new Integer(eventNum - 1)); // danh so la event cu
//                        }
//                    }
//                }
//            }
//        }
    }

    protected void turnAOffToMiddle() {
        if (!listHostOff.isEmpty()) {
//            Log.printLine(CloudSim.clock()+" : turn a off server to middle");
//            System.out.println("bat mot may tu off" +hostOff2MiddleNumber.getsize());

            hostOff2MiddleNumber.add(new Object());
            send(getId(), CFQNSHelper.timeOffToMiddle, CFQNSConstants.MiddleSucess, listHostOff.poll());
//            System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

        }
        else {
//            System.out.println("!!!!!!!!!!! het host OFF");
        }
    }

    // override xu ly su kien cloudlet moi den:
    public void processCloudletSubmit(SimEvent ev, boolean ack) {
    	
    	if(CFQNSHelper.timeStartSimulate < CloudSim.clock()){
    		start = true;
    	}

        if(state == CFQNSDatacenter.WAITING) toON();
        updateCloudletProcessing();
        try {
            // gets the Cloudlet object
        	if(start)	number++;
            CFQNSJob cl = (CFQNSJob) ev.getData();
            // tim cho cloudlet mot host ON:
            boolean hasAOnHost = false;
            //tim xem co host nao ON ma ko co jobs nao dang thuc hien tren do ko
            for (Host h : listHostON) {
                if (((CFQNSHost)h).getJob() == null) {

//                    Log.printLine("host "+h.getId()+" chay "+
//                            ((CMSHost) h).getCurrentvm().getCloudletScheduler().runningCloudlets());

                    System.out.println("datacenter "+ getName() + "co server ON khong co job");

                    jobsqueue.add(cl);
                    jobsqueue.poll();
                    assignJobToHost(cl, h);
                    hasAOnHost = true;
                    break;
                }

            }
            // neu ko tim duoc host ON, chi don gian la dua cloud vao hang doi va bat may tu middle sang ON
            // va bat may tu Middle sang ON
            if (!hasAOnHost) {

                // kiem tra xem datacenter con kha nang chua job khong
                if(getSystemLength() < CFQNSHelper.jobsqueuecapacity){
                    jobsqueue.add(cl);
                    deleteJobInQueue(cl);
                }
                else{
                    Log.printLine("!!!!!!!!!!!! block job");
                    // datacenter het kha nang chua
                    // tra cloudlet chua hoan thanh nay ve cho broker
                    // va dem luong job lost
                    if (startCountJobLost) {
                        nummberOfJobLost ++;
                        Log.printLine("dem job lost");

                    } else {
                        if (CloudSim.clock() > CFQNSHelper.timeStartSimulate) startCountJobLost = true;
                    }

                    Log.printLine("datacenter "+getId()+" day tai thoi diem "+CloudSim.clock());
                    sendNow(CFQNSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, cl);
                }

//                if(!jobsqueue.add(cl)) // neu hang doi day
//                {
//                    // tra lai job cho broker
//                    // job nay chua duoc set time complete nen no la job chua hoan thanh
//                    // broker nhan duoc job nay se xu ly de gui sang datacenter khac
//
//                    sendNow(CFQNSHelper.brokerId, CloudSimTags.CLOUDLET_RETURN, cl);
//                }

                if(hasMiddle) checkAndTurnMiddleToOn();
                else checkAndTurnOffToOn();
            }

        } catch (ClassCastException c) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
            c.printStackTrace();
        } catch (Exception e) {
            Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
            e.printStackTrace();
        }

        checkCloudletCompletion();
    }
    protected void processJobLeave(SimEvent ev){
    	CFQNSJob cl = (CFQNSJob)ev.getData();
    	
    	if(jobsqueue.contains(cl)){
    		jobsqueue.deleteJob(cl);
    		if(start) numberOfJobLeave++;
    		//Log.printLine("So job roi di: "+count+"job so: "+cl.getTimeCreate());
        	//Log.printLine("So job roi di: "+count+"job so: "+((CMSSJob)ev.getData()).getCloudletId());
    		//xoa job tra ve true neu xoa thanh cong
    		if(jobsqueue.isEmpty() ){ //sau khi xoa ma queue trong
    			//if(!hostOnNumber.isEmpty()){
    				//CMSSHost host =  (CMSSHost) hostOnNumber.poll();
    			//	CMSSJob cl =  jobsqueue.remove();
    				//assignJobToHost(cl, host);
    			
    			//}
    			//tat het cac may tu middle->off va on->off
//    			 while (listHostInSetupMode.size()> 0) {
//    		            Host h = (Host) listHostInSetupMode.get(listHostInSetupMode.size() -1);
//    		            ((CFQNSHost)h).isSetUpmode = false;
//    		            listHostInSetupMode.remove(listHostInSetupMode.size() - 1);
//    		            hostSetupNumber.poll();
//
//    		            listHostOff.add(h);
////    		            System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());
//
//    		        }
	    			while(listHostInSetupMode.size()>0){
	    					Host h = (Host) listHostInSetupMode.get(listHostInSetupMode.size()-1);
	    				((CFQNSHost)h).isSetUpmode = false;
			            listHostInSetupMode.remove(listHostInSetupMode.size() - 1);
			            hostSetupNumber.poll();
	
			            listHostOff.add(h);
	    			
	    			}
    			
    			
    		
    		}
    		else{//tiep tuc he thong nhu ban dau
    			// lay job dau tien gan cho host dang co san
    			//neu ko co
    			// van de la khi 1 job roi di, vi co 3 job dan trong hang doi
    			// j1,j2,j3: ca ba job nay deu yeu cau 1 may tu middle-> on, 2 may tu off->sang
    			//j1 roi di, con j2,j3 nhung van co 3 may dang setup -> thua -> tat may nao 
    			// -> hay giu nguyen trang thai
    			//->hay tat may bat muon nhat
    			// neu tat may bat muon nhat, thi co truong hop 1 job vua den thi sao,
    			if(jobsqueue.getsize() < listHostInSetupMode.size()){
	    			if(listHostInSetupMode.size()>0){
	    				Host h = (Host) listHostInSetupMode.get(listHostInSetupMode.size()-1);
	    				((CFQNSHost)h).isSetUpmode = false;
			            listHostInSetupMode.remove(listHostInSetupMode.size() - 1);
			            hostSetupNumber.poll();
	
			            listHostOff.add(h);
	    			}
    			}	
    		}
    	}//neu khong xoa thanh cong thi job da duoc nhan, ko co gi xay ra
    	
    }
    protected void deleteJobInQueue(CFQNSJob cl){
    	double t1 = StdRandom.exp(CFQNSHelper.theta);
    	send(getId(), t1,CFQNSConstants.ProcessJobLeave,cl);
    	//System.out.println("tau = "+CFQNSHelper.timenext+"timedelay= "+t1 );
    }
    protected void checkAndTurnOffToOn() {

        if(listHostInSetupMode.size() < jobsqueue.getsize()) {
            if (!listHostOff.isEmpty()) {
                Host h = listHostOff.poll();
//                System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

                turnAOffToON(h);

            }
            else {
//                 System.out.println("!!!!!!!!!!! het host OFF");
            }
        }
    }

    protected void turnAOffToON(Host h) {

//        Log.enable();
//        Log.printLine("--- cho mot may off vao setup--");
//        Log.printLine("so may ON: "+ listHostON.size());
//        Log.printLine("so may setup: "+listHostInSetupMode.size());
//        Log.printLine("tong so job trong he thong: "+ getSystemLength());
//        Log.disable();

//        System.out.println("bat 1 may tu off sang on");
        ((CFQNSHost) h).isSetUpmode = true;
        listHostInSetupMode.add(h); // cho vao setupmode
        ((CFQNSHost) h).timeStartSetup = CloudSim.clock();
        hostSetupNumber.add(new Object());
//        Log.printLine("bat may sang ON thoi gian bat la " + (CFQNSHelper.timeOffToMiddle+StdRandom.exp(CFQNSHelper.alpha)) + " , so luong may SETUP la " + listHostInSetupMode.size());
        double temp = CFQNSHelper.timeOffToMiddle + StdRandom.exp(CFQNSHelper.alpha);
        ((CFQNSHost) h).timeSetupExpect = temp;
        send(getId(), temp, CFQNSConstants.TurnONSuccess, h); // bat dau bat
//        System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

    }

    protected void assignJobToHost(CFQNSJob cl, Host host) {

        cl.setHost(host);
        ((CFQNSHost) host).setJob(cl);
        send(getId(),StdRandom.exp(CFQNSHelper.muy), CFQNSConstants.JobComplete, cl); // bat dau bat


    }

    public void checkCloudletCompletion() {

    }

    protected void turnOffAHostInSetupMode() {
        Log.printLine("kiem tra xem so host setup co >  so job dang doi khong");
        if (listHostInSetupMode.size()>jobsqueue.getsize()) {
//            System.out.println("lon hon roi --> tat 1 may o setup di, so setup con lai la " + listHostInSetupMode.size());
            Host h = (Host) listHostInSetupMode.get(listHostInSetupMode.size() -1);
            ((CFQNSHost)h).isSetUpmode = false;
            listHostInSetupMode.remove(listHostInSetupMode.size() - 1);
            hostSetupNumber.poll();

            listHostOff.add(h);
//            System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());
        }
        if(jobsqueue.isEmpty() && listHostON.isEmpty()) toOFForWAITING();
    }

    // bat may tu Middle sang on co kiem tra cac dieu kien
    protected   void checkAndTurnMiddleToOn() {
//        if (hostInSetupMode == null) { // kiem tra neu ko co may dang bat tu Middle sang ON


        // kiem tra so may o middle dang bat co bang so job khong

        if(listHostInSetupMode.size() < jobsqueue.getsize()){
            if (!listHostMIDDLE.isEmpty()) { // kiem tra neu co may o MIDDLE

//                Log.printLine("bat dau bay may o middle sang on");
                Host h = listHostMIDDLE.poll();
                if(h == null ) System.out.println(" null ma van co middle voi so luon " + listHostMIDDLE.getsize());
                else
                    turnAMiddleToON(h);
            }
        }
//        else Log.printLine("co may dang o setupmode nen ko bat tiep duoc");
    }

    protected   void turnAMiddleToON(Host h) {
        ((CFQNSHost)h).isSetUpmode = true;
        listHostInSetupMode.add(h); // cho vao setupmode
        ((CFQNSHost)h).timeStartSetup = CloudSim.clock();
        hostSetupNumber.add(new Object());
//        Log.printLine("turn on");
//        Log.printLine(CloudSim.clock() + " : host" + h.getId()+" turn from Middle to ON");

        double temp = StdRandom.exp(CFQNSHelper.alpha);
        ((CFQNSHost)h).timeSetupExpect = temp;
        send(getId(), temp, CFQNSConstants.TurnONSuccess, h); // bat dau bat
//        System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

    }

    // xu ly event khi ma turn middle sang on thanh cong
    protected void processTurnONSuccess(SimEvent ev) {
//            System.out.println("bat thanh cong mot may on");
        if (listHostInSetupMode.size() > 0) {
            Host h = ((Host) ev.getData());
            if ((((CFQNSHost) h).isSetUpmode) &&
                    ((((CFQNSHost) h).timeStartSetup + ((CFQNSHost) h).timeSetupExpect) == CloudSim.clock()) ) { // xac nhan su kien co dung la cua host muon bat

                ((CFQNSHost) h).isSetUpmode = false;
                listHostON.add(h);
                hostOnNumber.add(new Object());
                listHostInSetupMode.remove(h);
                hostSetupNumber.poll();

                Log.printLine("bat may thanh cong, so host on: " + listHostON.size() + " so host off:" + listHostOff.size() + " so host middle: " + listHostMIDDLE.getsize());
//                Log.printLine("total host: " + (listHostON.size()+listHostOff.size()+listHostMIDDLE.getsize()));

//                System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());
                // lay 1 jobs trong queue ra de thuc hien tren host nay
                // neu ko co jobs nao thi tat luon may nay
                if (!jobsqueue.isEmpty()) {

                    // thuc hien cloudlet tren host nay

                    assignJobToHost(jobsqueue.poll(), listHostON.get(listHostON.size() - 1));

                    //Kiem tra xem trong queue con job khong neu con thi tiep tuc bat may tu middle sang ON
                    if (!jobsqueue.isEmpty()) {
                        if (hasMiddle) checkAndTurnMiddleToOn();
                        else checkAndTurnOffToOn();
                    } else {
//                        if(isSubSystem)
                        if (listHostON.isEmpty()) toOFForWAITING();

                    }

                } else {
                    // tat luon may on nay
                    turnOffHostOn(listHostON.get(listHostON.size() - 1));
                }

            }
            else{
//                    System.out.println("sao lai co server issetupmode = false the nay");
//                    System.out.println("so setup: "+listHostInSetupMode.size());
//                    if(listHostInSetupMode.remove(h)) System.out.println("??? xoa duoc la sao");
//                    System.out.println("so setup: "+listHostInSetupMode.size());

            }

        }


    }

    protected void turnOffHostOn(Host host) {
//        CFQNSHelper.extraCount ++;

        listHostON.remove(host); hostOnNumber.poll();
        listHostOff.add(host);
//        System.out.println("--------- so may ON: " + listHostON.size() + " SETUP " + listHostInSetupMode.size() + " MIDDLE " + listHostMIDDLE.getsize() + " OFF " + listHostOff.size());

    }

    protected void processVmDestroy(SimEvent ev, boolean ack) {

    }

//    public double getMeanWaittingTime() {
//        return jobsqueue.getMeanWaitingTime();
//    }
//    public double getMeanJobsQueueLength() {
//        return jobsqueue.getMeanQueueLength();
//    }


    public double getMeanMiddleServerLength() {


//        return listHostMIDDLE.getMeanQueueLength();

        // sua lai nhu sau:
        // duyet tat ca cac datacenter va lay thong tin ve tat ca waiting time
        double totalWaitingTime = 0;
        for(int i =0; i< CFQNSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            totalWaitingTime =totalWaitingTime + temp.getMiddleTotalWaitingTime();

        }
//        Log.printLine("totalWaitingTime  "+ totalWaitingTime);
//        Log.printLine("getWaitingTimeOfQueue   "+this.listHostMIDDLE.start);
        return totalWaitingTime / (CFQNSHelper.totalTimeSimulate - CFQNSHelper.timeStartSimulate);
    }



    public double getMeanNumberSetupServer(){
//        return hostSetupNumber.getMeanQueueLength();
        double totalWaitingTime = 0;
        for(int i =0; i< CFQNSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            totalWaitingTime = totalWaitingTime + temp.getSetupTotalWaitingTime();

        }

        return totalWaitingTime / (CFQNSHelper.totalTimeSimulate - CFQNSHelper.timeStartSimulate);
    }

    public double getMeanNumberOff2MiddleServer(){
//        return hostSetupNumber.getMeanQueueLength();
        double totalWaitingTime = 0;
        for(int i =0; i< CFQNSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            totalWaitingTime = totalWaitingTime + temp.getOff2MiddleTotalWaitingTime();

        }

        return totalWaitingTime / (CFQNSHelper.totalTimeSimulate - CFQNSHelper.timeStartSimulate);
    }

    public double getOff2MiddleTotalWaitingTime(){
        return hostOff2MiddleNumber.getTotalWaitingTime();
    }

    public double getMeanNumberOnServer(){
//        return hostOnNumber.getMeanQueueLength();
        double totalWaitingTime = 0;
        for(int i =0; i< CFQNSHelper.listSubDatacenter.size();i++){
//            System.out.println("******* " + CMSHelper.listSubDatacenter.size());
            CFQNSDatacenter temp = CFQNSHelper.listSubDatacenter.get(i);
            totalWaitingTime = totalWaitingTime + temp.getOnTotalWaitingTime();

        }

        return totalWaitingTime / (CFQNSHelper.totalTimeSimulate - CFQNSHelper.timeStartSimulate);
    }

    public double getOnTotalWaitingTime() {
        return hostOnNumber.getTotalWaitingTime();
    }



    public double getSetupTotalWaitingTime() {
        return hostSetupNumber.getTotalWaitingTime();
    }



    public double getMiddleTotalWaitingTime() {
        return listHostMIDDLE.getTotalWaitingTime();
    }





    public double getTimeNoMiddle(){
        return listHostMIDDLE.getTimeEmpty();
    }
//    public long getTotalJob(){
//        return jobsqueue.getTotalItem();
//    }

    public int getSystemLength(){
        return (jobsqueue.getsize() + listHostON.size()) ;
    }

    public int getNumberOfJobLost(){
//        return jobsqueue.getNumberJobLost();

        return nummberOfJobLost;
    }

    public static double getMeanNumberOfONQueue(){
//        return numberONQueue.getMeanQueueLength();
//        System.out.print(numberONQueue.getTotalWaitingTime()+ " / " +numberONQueue.getTimeOfQueue());
        if(numberONQueue.getTotalWaitingTime() == 0) return numberONQueue.getsize();
        else
            return numberONQueue.getMeanQueueLength();
    }
    public static double getMeanNumberOfWAITINGQueue(){
        if(numberWAITINGQueue.getTotalWaitingTime() == 0) return numberWAITINGQueue.getsize();
        else
            return numberWAITINGQueue.getMeanQueueLength();
    }
    public boolean startCountJobLost = false;

}
