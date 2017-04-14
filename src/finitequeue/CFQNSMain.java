package finitequeue;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.File;

//import controlmidleserverstag.CFQNSHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dangbk on 16/04/2015.
 */
public class CFQNSMain {
    public static double[] muy = {0.2};
    public static double[] alpha = {0.02};//, 0.001};//,0.5, 1, 5, 10, 20, 50, 100, 150, 200};
    public static int[] capacity = {100,200,500};//, 3000, 2000, 1000};
    public static double[] listThrUp = {0.3};
    public static double[] listThrDown = {-0.3};
    public static int timetomiddle = 400;
    public static double[] theta = {0.02};

    public static void chayvoilamdathaydoimultiqueues() throws IOException {
         // file luu kq


        // // co dinh alpha, lamda thay doi

        boolean hasTimeInitQueue = true; // thay doi cai nay phai thay doi ham init() trong file datacenter

        double[] lamdaarray = {7,11,15};//1,3,5,7,9,11,13,15,17,19};
        int n = lamdaarray.length;

        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberSetupServerNoMidle = new double[n];
        double[] meanNumberOnServerNoMidle = new double[n];

        double[] meanWaittime = new double[n];
        double[] meanResponsetime = new double[n];
        int[] numberONTurnoff = new int[n];
        int[] numberOfQueue = new int[n];
        double[] meanNumberOfONQueue = new double[n];
        double[] meanNumberOfWAITINGQueue = new double[n];

        double[] meanNumberSetupServer = new double[n];
        double[] meanNumberOnServer = new double[n];
        double[] meanNumberMiddleServer = new double[n];
        double[] meanNumberOffToMiddleServer = new double[n];
        double[] timenextlist = new double[n];
        double[] jobLostRatio = new double[n];

        for (int indexMuy = 0; indexMuy < muy.length; indexMuy++) {
            for (int j = 0; j < alpha.length; j++) {
                for(int indexthrup = 0; indexthrup < listThrUp.length; indexthrup++) {
                    for (int indexthrdown = 0; indexthrdown < listThrDown.length; indexthrdown++) {
                        for (int indexK = 0; indexK < capacity.length; indexK++) {

                            // cho lambda thay doi
                            for (int i = 0; i < lamdaarray.length; i++) {

                                // chay co middle
                                // chay voi co control middle
                                System.out.println("multiqueue system (with middle) Starting simulation... chay voi alpha = " + alpha[j]
                                        + " muy = " + muy[indexMuy] + " capacity = " + capacity[indexK] + " lamda = " + lamdaarray[i]
                                        +" threshold down = "+listThrDown[indexthrdown]+" threshold up "+listThrUp[indexthrup]);
                                try {
                                    //------------------thiet lap tham so-----------------
                                    CFQNSHelper.reset();
                                    CFQNSHelper.setAlpha(alpha[j]);
                                    CFQNSHelper.thrUP = listThrUp[indexthrup];
                                    CFQNSHelper.thrDown = listThrDown[indexthrdown];

                                    CFQNSHelper.jobsqueuecapacity = capacity[indexK];
                                    CFQNSHelper.hostNum = capacity[indexK];

                                    CFQNSHelper.jobsqueuethresholdup = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrUP);
                                    CFQNSHelper.jobsqueuethresholddown = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrDown);
                                    CFQNSHelper.setMuy(muy[indexMuy]); // thoi gian phuc vu 1/ muy = 5

                                    CFQNSHelper.setTimeOffToMiddle(timetomiddle);

                                    CFQNSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);

//                        // thay doi thoi gian thu nghiem cho phu hop voi lambda
//                        CFQNSHelper.timeStartSimulate = CFQNSHelper.timeStartSimulate / lamdaarray[i];
//                        CFQNSHelper.totalTimeSimulate = CFQNSHelper.totalTimeSimulate / lamdaarray[i];

                                    boolean hasMiddle = true;

                                    calculatetimenextversion2();
                                    //^^^^^^^^^^^^^^thiet lap tham so (end)^^^^^^^^^^^^^^^

                                    // First step: Initialize the CloudSim package. It should be called
                                    // before creating any entities.
                                    int num_user = 1;   // number of cloud users
                                    Calendar calendar = Calendar.getInstance();
                                    boolean trace_flag = false;  // mean trace events

                                    // Initialize the CloudSim library
                                    CloudSim.init(num_user, calendar, trace_flag);

                                    // Second step: Create Datacenters
                                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                                    CFQNSDatacenter datacenter0 = CFQNSHelper.createDatacenter("Datacenter_0");
                                    datacenter0.state = CFQNSDatacenter.ON;
                                    datacenter0.numberONQueue = new CFQNSMiddleHostQueue<>();
                                    datacenter0.numberWAITINGQueue = new CFQNSMiddleHostQueue<>();
                                    datacenter0.numberONQueue.add(new Object());
                                    datacenter0.setStartState(0, (int) (CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext));
                                    datacenter0.setHasMiddle(hasMiddle);
                                    Log.printLine("main datacenter id " + datacenter0.getId());


                                    //Third step: Create Broker
                                    DatacenterBroker broker = CFQNSHelper.createBroker();


                                    int brokerId = broker.getId();

                                    // Sixth step: Starts the simulation
                                    double lastclock = CloudSim.startSimulation();
                                    // Final step: Print results when simulation is over
                                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                                    System.out.println();
                                    System.out.println();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                                    System.out.println("total time simulate: " + lastclock);
                                    System.out.println("(with middle) mean waitting time: " + ((CFQNSBroker) broker).getMeanWaittingTime());
                                    System.out.println("(with middle) mean response time: " + ((CFQNSBroker) broker).getMeanResponseTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                            System.out.println("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                                    System.out.println("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());
                                    System.out.println("mean Off to Middle server Length: " + datacenter0.getMeanNumberOff2MiddleServer());

                                    System.out.println("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                                    System.out.println("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                                    System.out.println("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                                    System.out.println("number of job (trong khoang thoi gian xet: " + ((CFQNSBroker) broker).getNumberOfJob());
                                    System.out.println("**** total job lost: " + datacenter0.getNumberOfJobLost());
                                    System.out.println("**** ti le job lost = joblost / numberofjob: " + datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob());
                                    System.out.println("total vm: " + CFQNSHelper.getVmid());
                                    System.out.println("total sub datacenter create: " + CFQNSHelper.listSubDatacenter.size());
                                    System.out.println("so lan tat may ON  " + CFQNSHelper.extraCount);
                                    System.out.println("so luong queue ON trung binh:  " + datacenter0.getMeanNumberOfONQueue());
                                    System.out.println("so luong queue Waiting trung binh:  " + datacenter0.getMeanNumberOfWAITINGQueue());
                                    System.out.println();


                                    meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                                    meanNumberOnServer[i] = datacenter0.getMeanNumberOnServer();
                                    meanNumberSetupServer[i] = datacenter0.getMeanNumberSetupServer();

                                    meanNumberOffToMiddleServer[i] = datacenter0.getMeanNumberOff2MiddleServer() ;// CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext;
                                    timenextlist[i] = CFQNSHelper.timenext;
                                    jobLostRatio[i] = datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob();

                                    meanWaittime[i] = ((CFQNSBroker) broker).getMeanWaittingTime();
                                    meanResponsetime[i] = ((CFQNSBroker) broker).getMeanResponseTime();
                                    numberONTurnoff[i] = CFQNSHelper.extraCount;

                                    numberOfQueue[i] = CFQNSHelper.listSubDatacenter.size();
                                    meanNumberOfONQueue[i] = datacenter0.getMeanNumberOfONQueue();
                                    meanNumberOfWAITINGQueue[i] = datacenter0.getMeanNumberOfWAITINGQueue();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.printLine("The simulation has been terminated due to an unexpected error");
                                }

                                //--------------------------------------- bo phan nay di doi voi infinite queue------------
                /*
                // chay khong co middle

                Log.printLine("(ko co middle) Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CMSHelper.reset();
                    CMSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSHelper.setAlpha(alpha[j]);
                    CMSHelper.setMuy(0.2);


                    CMSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = false;
//                    CMSHelper.totalJobs = 4100000;

                    //------------------thiet lap tham so (end)-----------------

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                    CMSDatacenter datacenter0 = CMSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CMSHelper.createBroker();

                    int brokerId = broker.getId();

                    // Sixth step: Starts the simulation
                    double lastclock = CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("(without middle) mean waitting time: " + ((CMSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CMSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CMSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
            */
                            }


                            // in ket qua: voi h = h[j]
                            // ket qua co the copy vao matlab de ve
                            FileWriter fw = null;
                            System.out.println();
                            try {
                                fw = new FileWriter("results_CFQNS_no_stag_alpha_" + CFQNSHelper.alpha + "_capacity_"
                                        + CFQNSHelper.jobsqueuecapacity + "_muy_" + CFQNSHelper.muy + "_thrUP_" + ((double) CFQNSHelper.jobsqueuethresholdup) / ((double) CFQNSHelper.jobsqueuecapacity)
                                        + "_thrDown_" + ((double) CFQNSHelper.jobsqueuethresholddown) / ((double) CFQNSHelper.jobsqueuecapacity) + "_timeinitqueue_" + hasTimeInitQueue + ".txt");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            fw.write("\n");
                            System.out.println("--------------------------------------");
                            fw.write("--------------------------------------\n");
                            fw.write("alpha = " + alpha[j] + "\n");
                            fw.write("\n");
                            System.out.println("close all");
                            fw.write("close all\n");

                            System.out.print("lamda = [");
                            fw.write("lamda = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f, ", lamdaarray[i]);
                                fw.write(lamdaarray[i] + ", ");
                            }
                            System.out.printf("%.2f ];", lamdaarray[n - 1]);
                            fw.write(lamdaarray[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean waitting time = [");
                            fw.write("mean_waitting_time" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f, ", meanWaittime[i]);
                                fw.write(meanWaittime[i] + ", ");
                            }
                            System.out.printf("%.2f ];", meanWaittime[n - 1]);
                            fw.write(meanWaittime[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean response time = [");
                            fw.write("mean_response_time" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f, ", meanResponsetime[i]);
                                fw.write(meanResponsetime[i] + ", ");
                            }
                            System.out.printf("%.2f ];", meanResponsetime[n - 1]);
                            fw.write(meanResponsetime[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean number middle server = [");
                            fw.write("mean_number_of_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f ,", meanNumberMiddleServer[i]);
                                fw.write(meanNumberMiddleServer[i] + ", ");
                            }
                            System.out.printf("%.2f ];", meanNumberMiddleServer[n - 1]);
                            fw.write(meanNumberMiddleServer[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean number setup server = [");
                            fw.write("mean_number_of_setup_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f ,", meanNumberSetupServer[i]);
                                fw.write(meanNumberSetupServer[i] + ", ");
                            }
                            System.out.printf("%.2f ];", meanNumberSetupServer[n - 1]);
                            fw.write(meanNumberSetupServer[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean number On server = [");
                            fw.write("mean_number_of_on_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f ,", meanNumberOnServer[i]);
                                fw.write(meanNumberOnServer[i] + ", ");
                            }
                            System.out.printf("%.2f ];", meanNumberOnServer[n - 1]);
                            fw.write(meanNumberOnServer[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean number Off to Middle server = [");
                            fw.write("mean_number_of_off_to_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.printf("%.2f ,", meanNumberOffToMiddleServer[i]);
                                fw.write(meanNumberOffToMiddleServer[i] + ", ");
                            }
                            System.out.printf("%.2f ];", meanNumberOffToMiddleServer[n - 1]);
                            fw.write(meanNumberOffToMiddleServer[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("mean number turned off = [");
                            fw.write("mean_number_turned_off" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.print(" ," + numberONTurnoff[i]);
                                fw.write(numberONTurnoff[i] + ", ");
                            }
                            System.out.print(" ," + numberONTurnoff[n - 1]);
                            fw.write(numberONTurnoff[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("max number of queue = [");
                            fw.write("max_number_of_queue" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.print(" ," + numberOfQueue[i]);
                                fw.write(numberOfQueue[i] + ", ");
                            }
                            System.out.print(" ," + numberOfQueue[n - 1]);
                            fw.write(numberOfQueue[n - 1] + "];\n");
                            System.out.println();

                            System.out.print("time next = [");
                            fw.write("time_next" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.print(" ," + timenextlist[i]);
                                fw.write(timenextlist[i] + ", ");
                            }
                            System.out.print(" ," + timenextlist[n - 1]);
                            fw.write(timenextlist[n - 1] + "];\n");


                            System.out.print("job lost ratio = [");
                            fw.write("job_lost_ratio" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.print(" ," + jobLostRatio[i]);
                                fw.write(jobLostRatio[i] + ", ");
                            }
                            System.out.print(" ," + jobLostRatio[n - 1]);
                            fw.write(jobLostRatio[n - 1] + "];\n");

                            System.out.print("mean number of ON queue = [");
                            fw.write("mean_number_of_ON_queue" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.print(" ," + meanNumberOfONQueue[i]);
                                fw.write(meanNumberOfONQueue[i] + ", ");
                            }
                            System.out.print(" ," + meanNumberOfONQueue[n - 1]);
                            fw.write(meanNumberOfONQueue[n - 1] + "];\n");

                            System.out.print("mean number of WAITING queue = [");
                            fw.write("mean_number_of_WAITING_queue" + CFQNSHelper.jobsqueuecapacity + " = [");
                            for (int i = 0; i < n - 1; i++) {
                                System.out.print(" ," + meanNumberOfWAITINGQueue[i]);
                                fw.write(meanNumberOfWAITINGQueue[i] + ", ");
                            }
                            System.out.print(" ," + meanNumberOfWAITINGQueue[n - 1]);
                            fw.write(meanNumberOfWAITINGQueue[n - 1] + "];\n");

                            fw.write("\n_____________________________\n\n");
                            System.out.println();

                            //------------ ko co middle-------------
/*
            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number setup server no middle = [");fw.write("mean_number_of_setup_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServerNoMidle[i]);fw.write(meanNumberSetupServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServerNoMidle[n-1]);fw.write(meanNumberSetupServerNoMidle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server no middle = [");fw.write("mean_number_of_on_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServerNoMidle[i]);fw.write(meanNumberOnServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServerNoMidle[n-1]);fw.write(meanNumberOnServerNoMidle[n-1] + "];\n");
            Log.printLine();
*/


                            System.out.println();

                            // in code ve:
                            fw.write("plot(lamda,mean_waitting_time,'r',lamda,mean_waitting_time_no_middle,'-xb');\n");
                            fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean waitting time');\n");
                            fw.write("legend('with middle','no middle');\n");
                            fw.write("ylim([0 max(max(mean_waitting_time),max(mean_waitting_time_no_middle))*10/9]);");

//            fw.write("ylim([0 18000]);\n");
                            fw.write("figure(2);\n");
                            fw.write("plot(lamda,mean_number_of_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number of middle server');\n");
                            fw.write("ylim([0 max(mean_number_of_middle_server)*10/9]);");

                            fw.write("figure(3);\n");
                            fw.write("plot(lamda,mean_number_of_off_to_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number off to middle server');\n");
                            fw.write("ylim([0 max(mean_number_of_off_to_middle_server)*10/9]);");

                            // ------ve do thi mean number of ON server va Setup server
                            fw.write("figure(4);\n");
                            fw.write("plot(lamda,mean_number_of_setup_server,'r',lamda,mean_number_of_setup_server_no_middle,'-xb');\n");
                            fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number setup server');\n");
                            fw.write("legend('with middle','without middle');\n");
                            fw.write("ylim([0 max(max(mean_number_of_setup_server),max(mean_number_of_setup_server_no_middle))*10/9]);");

                            fw.write("figure(5);\n");
                            fw.write("plot(lamda,mean_number_of_on_server,'r',lamda,mean_number_of_on_server_no_middle,'-xb');\n");
                            fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number on server');\n");
                            fw.write("legend('with middle','without middle');\n");
                            fw.write("ylim([0 max(max(mean_number_of_on_server),max(mean_number_of_on_server_no_middle))*10/9]);\n");
//            fw.write("ylim([0 1000]);\n");
                            System.out.println("--------------------------------------");
                            fw.write("--------------------------------------\n");
                            fw.close();
                        }
                    }
                }
            }
        }
    }

    public static void chayvoilamdathaydoisinglequeue() throws IOException {
        // file luu kq


        // // co dinh alpha, lamda thay doi

        boolean hasTimeInitQueue = true; // thay doi cai nay phai thay doi ham init() trong file datacenter

        double[] lamdaarray = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        int n = lamdaarray.length;

        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberSetupServerNoMidle = new double[n];
        double[] meanNumberOnServerNoMidle = new double[n];

        double[] meanWaittime = new double[n];
        double[] meanResponsetime = new double[n];
        int[] numberONTurnoff = new int[n];
        int[] numberOfQueue = new int[n];
        double[] meanNumberSetupServer = new double[n];
        double[] meanNumberOnServer = new double[n];
        double[] meanNumberMiddleServer = new double[n];
        double[] meanNumberOffToMiddleServer = new double[n];
        double[] timenextlist = new double[n];
        double[] jobLostRatio = new double[n];
        
        for (int indexMuy = 0; indexMuy < muy.length; indexMuy++) {
            for (int j = 0; j < alpha.length; j++) {

                for (int indexK = 0; indexK < capacity.length; indexK++) {

                    // cho lambda thay doi
                    for (int i = 0; i < lamdaarray.length; i++) {

                        // chay co middle
                        // chay voi co control middle
                        System.out.println("(with middle) Starting simulation... chay voi alpha = " + alpha[j]
                                +" muy = "+ muy[indexMuy]+ " capacity = "+capacity[indexK] + " lamda = " + lamdaarray[i]);
                        try {
                            //------------------thiet lap tham so-----------------
                            CFQNSHelper.reset();
                            CFQNSHelper.setAlpha(alpha[j]);
                            CFQNSHelper.jobsqueuecapacity = capacity[indexK];
                            CFQNSHelper.jobsqueuethresholdup = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrUP);
                            CFQNSHelper.jobsqueuethresholddown = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrDown);
                            CFQNSHelper.setMuy(muy[indexMuy]); // thoi gian phuc vu 1/ muy = 5
                            CFQNSHelper.setTimeOffToMiddle(timetomiddle);

                            CFQNSHelper.setLamda(lamdaarray[i]);
                            CFQNSHelper.isSingleQueue = true;
//                    CMSHelper.setControlTime(200);

//                        // thay doi thoi gian thu nghiem cho phu hop voi lambda
//                        CFQNSHelper.timeStartSimulate = CFQNSHelper.timeStartSimulate / lamdaarray[i];
//                        CFQNSHelper.totalTimeSimulate = CFQNSHelper.totalTimeSimulate / lamdaarray[i];

                            boolean hasMiddle = true;

                            calculatetimenext();
                            //^^^^^^^^^^^^^^thiet lap tham so (end)^^^^^^^^^^^^^^^

                            // First step: Initialize the CloudSim package. It should be called
                            // before creating any entities.
                            int num_user = 1;   // number of cloud users
                            Calendar calendar = Calendar.getInstance();
                            boolean trace_flag = false;  // mean trace events

                            // Initialize the CloudSim library
                            CloudSim.init(num_user, calendar, trace_flag);

                            // Second step: Create Datacenters
                            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                            CFQNSDatacenter datacenter0 = CFQNSHelper.createDatacenter("Datacenter_0");
                            datacenter0.state = CFQNSDatacenter.ON;
                            datacenter0.setStartState(0,(int) (CFQNSHelper.timeOffToMiddle/CFQNSHelper.timenext));
                            datacenter0.setHasMiddle(hasMiddle);
                            Log.printLine("main datacenter id "+datacenter0.getId());


                            //Third step: Create Broker
                            DatacenterBroker broker = CFQNSHelper.createBroker();


                            int brokerId = broker.getId();

                            // Sixth step: Starts the simulation
                            double lastclock = CloudSim.startSimulation();
                            // Final step: Print results when simulation is over
                            List<Cloudlet> newList = broker.getCloudletReceivedList();

                            CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                            System.out.println();
                            System.out.println();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                            System.out.println("total time simulate: " + lastclock);
                            System.out.println("(with middle) mean waitting time: " + ((CFQNSBroker) broker).getMeanWaittingTime());
                            System.out.println("(with middle) mean response time: " + ((CFQNSBroker) broker).getMeanResponseTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                            System.out.println("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                            System.out.println("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                            System.out.println("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                            System.out.println("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                            System.out.println("total time no Middle server: " + datacenter0.getTimeNoMiddle());
                           
                            System.out.println("number of job (trong khoang thoi gian xet: " + ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("**** total job lost: " + datacenter0.getNumberOfJobLost());
                            System.out.println("**** ti le job lost = joblost / numberofjob: " + datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("total vm: " + CFQNSHelper.getVmid());
                            System.out.println("total sub datacenter create: " + CFQNSHelper.listSubDatacenter.size());
                            System.out.println("so lan tat may ON  " + CFQNSHelper.extraCount);
                            System.out.println();


                            meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                            meanNumberOnServer[i] = datacenter0.getMeanNumberOnServer();
                            meanNumberSetupServer[i] = datacenter0.getMeanNumberSetupServer();

                            meanNumberOffToMiddleServer[i] = CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext;
                            timenextlist[i] = CFQNSHelper.timenext;
                            jobLostRatio[i] = datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob();

                            meanWaittime[i] = ((CFQNSBroker) broker).getMeanWaittingTime();
                            meanResponsetime[i] = ((CFQNSBroker) broker).getMeanResponseTime();
                            numberONTurnoff[i] = CFQNSHelper.extraCount;
                            numberOfQueue[i] = CFQNSHelper.listSubDatacenter.size();


//                Log.printLine(CMSHelper.getWaittingTime(newList));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.printLine("The simulation has been terminated due to an unexpected error");
                        }

                        //--------------------------------------- bo phan nay di doi voi infinite queue------------
                /*
                // chay khong co middle

                Log.printLine("(ko co middle) Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CMSHelper.reset();
                    CMSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSHelper.setAlpha(alpha[j]);
                    CMSHelper.setMuy(0.2);


                    CMSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = false;
//                    CMSHelper.totalJobs = 4100000;

                    //------------------thiet lap tham so (end)-----------------

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                    CMSDatacenter datacenter0 = CMSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CMSHelper.createBroker();

                    int brokerId = broker.getId();

                    // Sixth step: Starts the simulation
                    double lastclock = CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("(without middle) mean waitting time: " + ((CMSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CMSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CMSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
            */
                    }


                    // in ket qua: voi h = h[j]
                    // ket qua co the copy vao matlab de ve
                    FileWriter fw = null;
                    System.out.println();
                    try {
                        fw = new FileWriter("results_CFQNS_no_stag_alpha_" + CFQNSHelper.alpha + "_capacity_"
                                + CFQNSHelper.jobsqueuecapacity + "_muy_" + CFQNSHelper.muy + "_thrUP_" + ((double) CFQNSHelper.jobsqueuethresholdup) / ((double) CFQNSHelper.jobsqueuecapacity)
                                + "_thrDown_" + ((double) CFQNSHelper.jobsqueuethresholddown) / ((double) CFQNSHelper.jobsqueuecapacity) + "_timeinitqueue_" + hasTimeInitQueue + ".txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fw.write("\n");
                    System.out.println("--------------------------------------");
                    fw.write("--------------------------------------\n");
                    fw.write("alpha = " + alpha[j] + "\n");
                    fw.write("\n");
                    System.out.println("close all");
                    fw.write("close all\n");

                    System.out.print("lamda = [");
                    fw.write("lamda = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", lamdaarray[i]);
                        fw.write(lamdaarray[i] + ", ");
                    }
                    System.out.printf("%.2f ];", lamdaarray[n - 1]);
                    fw.write(lamdaarray[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean waitting time = [");
                    fw.write("mean_waitting_time" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanWaittime[i]);
                        fw.write(meanWaittime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanWaittime[n - 1]);
                    fw.write(meanWaittime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean response time = [");
                    fw.write("mean_response_time" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanResponsetime[i]);
                        fw.write(meanResponsetime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanResponsetime[n - 1]);
                    fw.write(meanResponsetime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number middle server = [");
                    fw.write("mean_number_of_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberMiddleServer[i]);
                        fw.write(meanNumberMiddleServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberMiddleServer[n - 1]);
                    fw.write(meanNumberMiddleServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number setup server = [");
                    fw.write("mean_number_of_setup_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberSetupServer[i]);
                        fw.write(meanNumberSetupServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberSetupServer[n - 1]);
                    fw.write(meanNumberSetupServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number On server = [");
                    fw.write("mean_number_of_on_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberOnServer[i]);
                        fw.write(meanNumberOnServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberOnServer[n - 1]);
                    fw.write(meanNumberOnServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number Off to Middle server = [");
                    fw.write("mean_number_of_off_to_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberOffToMiddleServer[i]);
                        fw.write(meanNumberOffToMiddleServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberOffToMiddleServer[n - 1]);
                    fw.write(meanNumberOffToMiddleServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number turned off = [");
                    fw.write("mean_number_turned_off" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + numberONTurnoff[i]);
                        fw.write(numberONTurnoff[i] + ", ");
                    }
                    System.out.print(" ," + numberONTurnoff[n - 1]);
                    fw.write(numberONTurnoff[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("max number of queue = [");
                    fw.write("max_number_of_queue" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + numberOfQueue[i]);
                        fw.write(numberOfQueue[i] + ", ");
                    }
                    System.out.print(" ," + numberOfQueue[n - 1]);
                    fw.write(numberOfQueue[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("time next = [");
                    fw.write("time_next" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + timenextlist[i]);
                        fw.write(timenextlist[i] + ", ");
                    }
                    System.out.print(" ," + timenextlist[n - 1]);
                    fw.write(timenextlist[n - 1] + "];\n");


                    System.out.print("job lost ratio = [");
                    fw.write("job_lost_ratio"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + jobLostRatio[i]);
                        fw.write(jobLostRatio[i] + ", ");
                    }
                    System.out.print(" ," + jobLostRatio[n - 1]);
                    fw.write(jobLostRatio[n - 1] + "];\n");


                    fw.write("\n_____________________________\n\n");
                    System.out.println();

                    //------------ ko co middle-------------
/*
            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number setup server no middle = [");fw.write("mean_number_of_setup_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServerNoMidle[i]);fw.write(meanNumberSetupServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServerNoMidle[n-1]);fw.write(meanNumberSetupServerNoMidle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server no middle = [");fw.write("mean_number_of_on_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServerNoMidle[i]);fw.write(meanNumberOnServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServerNoMidle[n-1]);fw.write(meanNumberOnServerNoMidle[n-1] + "];\n");
            Log.printLine();
*/


                    System.out.println();

                    // in code ve:
                    fw.write("plot(lamda,mean_waitting_time,'r',lamda,mean_waitting_time_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean waitting time');\n");
                    fw.write("legend('with middle','no middle');\n");
                    fw.write("ylim([0 max(max(mean_waitting_time),max(mean_waitting_time_no_middle))*10/9]);");

//            fw.write("ylim([0 18000]);\n");
                    fw.write("figure(2);\n");
                    fw.write("plot(lamda,mean_number_of_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number of middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_middle_server)*10/9]);");

                    fw.write("figure(3);\n");
                    fw.write("plot(lamda,mean_number_of_off_to_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number off to middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_off_to_middle_server)*10/9]);");

                    // ------ve do thi mean number of ON server va Setup server
                    fw.write("figure(4);\n");
                    fw.write("plot(lamda,mean_number_of_setup_server,'r',lamda,mean_number_of_setup_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number setup server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_setup_server),max(mean_number_of_setup_server_no_middle))*10/9]);");

                    fw.write("figure(5);\n");
                    fw.write("plot(lamda,mean_number_of_on_server,'r',lamda,mean_number_of_on_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number on server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_on_server),max(mean_number_of_on_server_no_middle))*10/9]);\n");
//            fw.write("ylim([0 1000]);\n");
                    System.out.println("--------------------------------------");
                    fw.write("--------------------------------------\n");
                    fw.close();
                }

            }
        }
    }

    public static void chayvoilamdathaydoisinglequeuekomiddle() throws IOException {
        // file luu kq


        // // co dinh alpha, lamda thay doi

        boolean hasTimeInitQueue = true; // thay doi cai nay phai thay doi ham init() trong file datacenter

        double[] lamdaarray = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        int n = lamdaarray.length;

        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberSetupServerNoMidle = new double[n];
        double[] meanNumberOnServerNoMidle = new double[n];

        double[] meanWaittime = new double[n];
        double[] meanResponsetime = new double[n];

        int[] numberONTurnoff = new int[n];
        int[] numberOfQueue = new int[n];
        double[] meanNumberSetupServer = new double[n];
        double[] meanNumberOnServer = new double[n];
        double[] meanNumberMiddleServer = new double[n];
//        double[] meanNumberOffToMiddleServer = new double[n];
        double[] timenextlist = new double[n];
        double[] jobLostRatio = new double[n];

        for (int indexMuy = 0; indexMuy < muy.length; indexMuy++) {
            for (int j = 0; j < alpha.length; j++) {

                for (int indexK = 0; indexK < capacity.length; indexK++) {

                    // cho lambda thay doi
                    for (int i = 0; i < lamdaarray.length; i++) {

                        // chay co middle
                        // chay voi co control middle
                        System.out.println("(without middle) Starting simulation... chay voi alpha = " + alpha[j]
                                +" muy = "+ muy[indexMuy]+ " capacity = "+capacity[indexK] + " lamda = " + lamdaarray[i]);
                        try {
                            //------------------thiet lap tham so-----------------
                            CFQNSHelper.reset();
                            CFQNSHelper.setAlpha(alpha[j]);
                            CFQNSHelper.jobsqueuecapacity = capacity[indexK];
                            CFQNSHelper.jobsqueuethresholdup = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrUP);
                            CFQNSHelper.jobsqueuethresholddown = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrDown);
                            CFQNSHelper.setMuy(muy[indexMuy]); // thoi gian phuc vu 1/ muy = 5
                            CFQNSHelper.setTimeOffToMiddle(timetomiddle);

                            CFQNSHelper.setLamda(lamdaarray[i]);
                            CFQNSHelper.isSingleQueue = true;
//                    CMSHelper.setControlTime(200);

//                        // thay doi thoi gian thu nghiem cho phu hop voi lambda
//                        CFQNSHelper.timeStartSimulate = CFQNSHelper.timeStartSimulate / lamdaarray[i];
//                        CFQNSHelper.totalTimeSimulate = CFQNSHelper.totalTimeSimulate / lamdaarray[i];

                            boolean hasMiddle = false;

                            calculatetimenext();
                            //^^^^^^^^^^^^^^thiet lap tham so (end)^^^^^^^^^^^^^^^

                            // First step: Initialize the CloudSim package. It should be called
                            // before creating any entities.
                            int num_user = 1;   // number of cloud users
                            Calendar calendar = Calendar.getInstance();
                            boolean trace_flag = false;  // mean trace events

                            // Initialize the CloudSim library
                            CloudSim.init(num_user, calendar, trace_flag);

                            // Second step: Create Datacenters
                            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                            CFQNSDatacenter datacenter0 = CFQNSHelper.createDatacenter("Datacenter_0");
                            datacenter0.state = CFQNSDatacenter.ON;
                            datacenter0.setStartState(0,(int) (CFQNSHelper.timeOffToMiddle/CFQNSHelper.timenext));
                            datacenter0.setHasMiddle(hasMiddle);
                            Log.printLine("main datacenter id "+datacenter0.getId());


                            //Third step: Create Broker
                            DatacenterBroker broker = CFQNSHelper.createBroker();


                            int brokerId = broker.getId();

                            // Sixth step: Starts the simulation
                            double lastclock = CloudSim.startSimulation();
                            // Final step: Print results when simulation is over
                            List<Cloudlet> newList = broker.getCloudletReceivedList();

                            CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                            System.out.println();
                            System.out.println();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                            System.out.println("total time simulate: " + lastclock);
                            System.out.println("(without middle) mean waitting time: " + ((CFQNSBroker) broker).getMeanWaittingTime());
                            System.out.println("(withouot middle) mean response time: " + ((CFQNSBroker) broker).getMeanResponseTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                            System.out.println("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                            System.out.println("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                            System.out.println("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                            System.out.println("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                            System.out.println("total time no Middle server: " + datacenter0.getTimeNoMiddle());
                            System.out.println("num of left job:"+ datacenter0.getNumberOfLeftJob());
                            System.out.println("num of completed job:"+ datacenter0.getNumberOfCompletedJob());
                            System.out.println("number of job (trong khoang thoi gian xet: " + ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("**** total job lost: " + datacenter0.getNumberOfJobLost());
                            System.out.println("**** ti le job lost = joblost / numberofjob: " + datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("total vm: " + CFQNSHelper.getVmid());
                            System.out.println("total sub datacenter create: " + CFQNSHelper.listSubDatacenter.size());
                            System.out.println("so lan tat may ON  " + CFQNSHelper.extraCount);
                            System.out.println();


                            meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                            meanNumberOnServer[i] = datacenter0.getMeanNumberOnServer();
                            meanNumberSetupServer[i] = datacenter0.getMeanNumberSetupServer();

//                            meanNumberOffToMiddleServer[i] = CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext;
                            timenextlist[i] = CFQNSHelper.timenext;
                            jobLostRatio[i] = datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob();

                            meanWaittime[i] = ((CFQNSBroker) broker).getMeanWaittingTime();
                            meanResponsetime[i] = ((CFQNSBroker) broker).getMeanResponseTime();
                            numberONTurnoff[i] = CFQNSHelper.extraCount;
                            numberOfQueue[i] = CFQNSHelper.listSubDatacenter.size();


//                Log.printLine(CMSHelper.getWaittingTime(newList));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.printLine("The simulation has been terminated due to an unexpected error");
                        }

                        //--------------------------------------- bo phan nay di doi voi infinite queue------------
                /*
                // chay khong co middle

                Log.printLine("(ko co middle) Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CMSHelper.reset();
                    CMSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSHelper.setAlpha(alpha[j]);
                    CMSHelper.setMuy(0.2);


                    CMSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = false;
//                    CMSHelper.totalJobs = 4100000;

                    //------------------thiet lap tham so (end)-----------------

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                    CMSDatacenter datacenter0 = CMSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CMSHelper.createBroker();

                    int brokerId = broker.getId();

                    // Sixth step: Starts the simulation
                    double lastclock = CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("(without middle) mean waitting time: " + ((CMSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CMSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CMSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
            */
                    }


                    // in ket qua: voi h = h[j]
                    // ket qua co the copy vao matlab de ve
                    FileWriter fw = null;
                    System.out.println();
                    try {
                        fw = new FileWriter("results_CFQNS_no_stag_alpha_" + CFQNSHelper.alpha + "_capacity_"
                                + CFQNSHelper.jobsqueuecapacity + "_muy_" + CFQNSHelper.muy + "_thrUP_" + ((double) CFQNSHelper.jobsqueuethresholdup) / ((double) CFQNSHelper.jobsqueuecapacity)
                                + "_thrDown_" + ((double) CFQNSHelper.jobsqueuethresholddown) / ((double) CFQNSHelper.jobsqueuecapacity) + "_timeinitqueue_" + hasTimeInitQueue + ".txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fw.write("\n");
                    System.out.println("---------without middle------------------");

                    fw.write("alpha = " + alpha[j] + "\n");
                    fw.write("\n");
                    fw.write("--------------------------------------\n");

                    System.out.println("close all");
                    fw.write("close all\n");

                    System.out.print("lamda = [");
                    fw.write("lamda = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", lamdaarray[i]);
                        fw.write(lamdaarray[i] + ", ");
                    }
                    System.out.printf("%.2f ];", lamdaarray[n - 1]);
                    fw.write(lamdaarray[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean waitting time = [");
                    fw.write("mean_waitting_time_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanWaittime[i]);
                        fw.write(meanWaittime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanWaittime[n - 1]);
                    fw.write(meanWaittime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean response time = [");
                    fw.write("mean_response_time_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanResponsetime[i]);
                        fw.write(meanResponsetime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanResponsetime[n - 1]);
                    fw.write(meanResponsetime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number middle server = [");
                    fw.write("mean_number_of_middle_server_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberMiddleServer[i]);
                        fw.write(meanNumberMiddleServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberMiddleServer[n - 1]);
                    fw.write(meanNumberMiddleServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number setup server = [");
                    fw.write("mean_number_of_setup_server_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberSetupServer[i]);
                        fw.write(meanNumberSetupServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberSetupServer[n - 1]);
                    fw.write(meanNumberSetupServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number On server = [");
                    fw.write("mean_number_of_on_server_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberOnServer[i]);
                        fw.write(meanNumberOnServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberOnServer[n - 1]);
                    fw.write(meanNumberOnServer[n - 1] + "];\n");
                    System.out.println();

//                    System.out.print("mean number Off to Middle server = [");
//                    fw.write("mean_number_of_off_to_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
//                    for (int i = 0; i < n - 1; i++) {
//                        System.out.printf("%.2f ,", meanNumberOffToMiddleServer[i]);
//                        fw.write(meanNumberOffToMiddleServer[i] + ", ");
//                    }
//                    System.out.printf("%.2f ];", meanNumberOffToMiddleServer[n - 1]);
//                    fw.write(meanNumberOffToMiddleServer[n - 1] + "];\n");
//                    System.out.println();

                    System.out.print("mean number turned off = [");
                    fw.write("mean_number_turned_off_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + numberONTurnoff[i]);
                        fw.write(numberONTurnoff[i] + ", ");
                    }
                    System.out.print(" ," + numberONTurnoff[n - 1]);
                    fw.write(numberONTurnoff[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("max number of queue = [");
                    fw.write("max_number_of_queue_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + numberOfQueue[i]);
                        fw.write(numberOfQueue[i] + ", ");
                    }
                    System.out.print(" ," + numberOfQueue[n - 1]);
                    fw.write(numberOfQueue[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("time next = [");
                    fw.write("time_next" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + timenextlist[i]);
                        fw.write(timenextlist[i] + ", ");
                    }
                    System.out.print(" ," + timenextlist[n - 1]);
                    fw.write(timenextlist[n - 1] + "];\n");


                    System.out.print("job lost ratio = [");
                    fw.write("job_lost_ratio_nomiddle"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + jobLostRatio[i]);
                        fw.write(jobLostRatio[i] + ", ");
                    }
                    System.out.print(" ," + jobLostRatio[n - 1]);
                    fw.write(jobLostRatio[n - 1] + "];\n");


                    fw.write("\n_____________________________\n\n");
                    System.out.println();

                    //------------ ko co middle-------------
/*
            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number setup server no middle = [");fw.write("mean_number_of_setup_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServerNoMidle[i]);fw.write(meanNumberSetupServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServerNoMidle[n-1]);fw.write(meanNumberSetupServerNoMidle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server no middle = [");fw.write("mean_number_of_on_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServerNoMidle[i]);fw.write(meanNumberOnServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServerNoMidle[n-1]);fw.write(meanNumberOnServerNoMidle[n-1] + "];\n");
            Log.printLine();
*/


                    System.out.println();

                    // in code ve:
                    fw.write("plot(lamda,mean_waitting_time,'r',lamda,mean_waitting_time_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean waitting time');\n");
                    fw.write("legend('with middle','no middle');\n");
                    fw.write("ylim([0 max(max(mean_waitting_time),max(mean_waitting_time_no_middle))*10/9]);");

//            fw.write("ylim([0 18000]);\n");
                    fw.write("figure(2);\n");
                    fw.write("plot(lamda,mean_number_of_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number of middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_middle_server)*10/9]);");

                    fw.write("figure(3);\n");
                    fw.write("plot(lamda,mean_number_of_off_to_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number off to middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_off_to_middle_server)*10/9]);");

                    // ------ve do thi mean number of ON server va Setup server
                    fw.write("figure(4);\n");
                    fw.write("plot(lamda,mean_number_of_setup_server,'r',lamda,mean_number_of_setup_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number setup server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_setup_server),max(mean_number_of_setup_server_no_middle))*10/9]);");

                    fw.write("figure(5);\n");
                    fw.write("plot(lamda,mean_number_of_on_server,'r',lamda,mean_number_of_on_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number on server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_on_server),max(mean_number_of_on_server_no_middle))*10/9]);\n");
//            fw.write("ylim([0 1000]);\n");
                    System.out.println("--------------------------------------");
                    fw.write("--------------------------------------\n");
                    fw.close();
                }

            }
        }
    }




    public static void chayvoilamdathaydoisinglequeuegioihansomaychu() throws IOException {
        // file luu kq


        // // co dinh alpha, lamda thay doi

        boolean hasTimeInitQueue = true; // thay doi cai nay phai thay doi ham init() trong file datacenter

        double[] lamdaarray = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        int n = lamdaarray.length;

        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberSetupServerNoMidle = new double[n];
        double[] meanNumberOnServerNoMidle = new double[n];

        double[] meanWaittime = new double[n];
        double[] meanResponsetime = new double[n];
        int[] numberONTurnoff = new int[n];
        int[] numberOfQueue = new int[n];
        double[] meanNumberSetupServer = new double[n];
        double[] meanNumberOnServer = new double[n];
        double[] meanNumberMiddleServer = new double[n];
        double[] meanNumberOffToMiddleServer = new double[n];
        double[] timenextlist = new double[n];
        double[] jobLostRatio = new double[n];
        double[] numberOfLeftJob =new double[n];
        double[] numberOfCompletedJob =new double[n];
	double[] leftJobRatio = new double[n];


        for (int indexMuy = 0; indexMuy < muy.length; indexMuy++) {
            for (int j = 0; j < alpha.length; j++) {

                for (int indexK = 0; indexK < capacity.length; indexK++) {

                    // cho lambda thay doi
                    for (int i = 0; i < lamdaarray.length; i++) {

                        // chay co middle
                        // chay voi co control middle
                        System.out.println("(with middle) Starting simulation... chay voi alpha = " + alpha[j]
                                +" muy = "+ muy[indexMuy]+ " capacity = "+capacity[indexK] + " lamda = " + lamdaarray[i]);
                        try {
                            //------------------thiet lap tham so-----------------
                            CFQNSHelper.reset();
                            CFQNSHelper.hostNum = 100;
                            CFQNSHelper.setAlpha(alpha[j]);
                            CFQNSHelper.jobsqueuecapacity = capacity[indexK];
                            CFQNSHelper.theta = theta[j];
                            CFQNSHelper.jobsqueuethresholdup = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrUP);
                            CFQNSHelper.jobsqueuethresholddown = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrDown);
                            CFQNSHelper.setMuy(muy[indexMuy]); // thoi gian phuc vu 1/ muy = 5
                            CFQNSHelper.setTimeOffToMiddle(timetomiddle);
                            CFQNSHelper.theta = theta[j];
                            CFQNSHelper.setLamda(lamdaarray[i]);
                            CFQNSHelper.isSingleQueue = true;
//                    CMSHelper.setControlTime(200);

//                        // thay doi thoi gian thu nghiem cho phu hop voi lambda
//                        CFQNSHelper.timeStartSimulate = CFQNSHelper.timeStartSimulate / lamdaarray[i];
//                        CFQNSHelper.totalTimeSimulate = CFQNSHelper.totalTimeSimulate / lamdaarray[i];

                            boolean hasMiddle = true;

                            calculateTau();
                            System.out.println("tau = "+CFQNSHelper.timenext);
                            //^^^^^^^^^^^^^^thiet lap tham so (end)^^^^^^^^^^^^^^^

                            // First step: Initialize the CloudSim package. It should be called
                            // before creating any entities.
                            int num_user = 1;   // number of cloud users
                            Calendar calendar = Calendar.getInstance();
                            boolean trace_flag = false;  // mean trace events

                            // Initialize the CloudSim library
                            CloudSim.init(num_user, calendar, trace_flag);

                            // Second step: Create Datacenters
                            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                            CFQNSDatacenter datacenter0 = CFQNSHelper.createDatacenter("Datacenter_0");
                            datacenter0.state = CFQNSDatacenter.ON;
//                            datacenter0.setStartState(0,(int) (CFQNSHelper.timeOffToMiddle/CFQNSHelper.timenext));
                            datacenter0.setHasMiddle(hasMiddle);
                            Log.printLine("main datacenter id "+datacenter0.getId());


                            //Third step: Create Broker
                            DatacenterBroker broker = CFQNSHelper.createBroker();


                            int brokerId = broker.getId();

                            // Sixth step: Starts the simulation
                            double lastclock = CloudSim.startSimulation();
                            // Final step: Print results when simulation is over
                            List<Cloudlet> newList = broker.getCloudletReceivedList();

                            CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                            System.out.println();
                            System.out.println();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                            System.out.println("total time simulate: " + lastclock);
                            System.out.println("(with middle) mean waitting time: " + ((CFQNSBroker) broker).getMeanWaittingTime());
                            System.out.println("(with middle) mean response time: " + ((CFQNSBroker) broker).getMeanResponseTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                            System.out.println("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                            System.out.println("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());
                            System.out.println("mean Off to Middle server Length: " + datacenter0.getMeanNumberOff2MiddleServer());

                            System.out.println("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                            System.out.println("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                            System.out.println("total time no Middle server: " + datacenter0.getTimeNoMiddle());
                            System.out.println("num of left job:"+ datacenter0.getNumberOfLeftJob());
                            System.out.println("num of completed job:"+ datacenter0.getNumberOfCompletedJob());
                            System.out.println("number of job (trong khoang thoi gian xet: " + ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("**** total job lost: " + datacenter0.getNumberOfJobLost());
                            System.out.println("**** ti le job lost = joblost / numberofjob: " + datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("total vm: " + CFQNSHelper.getVmid());
                            System.out.println("total sub datacenter create: " + CFQNSHelper.listSubDatacenter.size());
                            System.out.println("so lan tat may ON  " + CFQNSHelper.extraCount);
                            System.out.println("so job tinh kieu khac:"+datacenter0.number);
                            System.out.println();


                            meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                            meanNumberOnServer[i] = datacenter0.getMeanNumberOnServer();
                            meanNumberSetupServer[i] = datacenter0.getMeanNumberSetupServer();

                            meanNumberOffToMiddleServer[i] = datacenter0.getMeanNumberOff2MiddleServer() ;// CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext;
                            timenextlist[i] = CFQNSHelper.timenext;
                            numberOfCompletedJob[i] = datacenter0.getNumberOfCompletedJob();
                            numberOfLeftJob[i] = datacenter0.getNumberOfLeftJob();
                            jobLostRatio[i] = datacenter0.getNumberOfJobLost()/datacenter0.number; //((CFQNSBroker) broker).getNumberOfJob();

                            meanWaittime[i] = ((CFQNSBroker) broker).getMeanWaittingTime();
                            meanResponsetime[i] = ((CFQNSBroker) broker).getMeanResponseTime();
                            numberONTurnoff[i] = CFQNSHelper.extraCount;
                            numberOfQueue[i] = CFQNSHelper.listSubDatacenter.size();
			    leftJobRatio[i] = datacenter0.getNumberOfLeftJob()/(datacenter0.getNumberOfCompletedJob()+datacenter0.getNumberOfLeftJob());
                            
//                Log.printLine(CMSHelper.getWaittingTime(newList));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.printLine("The simulation has been terminated due to an unexpected error");
                        }

                        //--------------------------------------- bo phan nay di doi voi infinite queue------------
                /*
                // chay khong co middle

                Log.printLine("(ko co middle) Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CMSHelper.reset();
                    CMSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSHelper.setAlpha(alpha[j]);
                    CMSHelper.setMuy(0.2);


                    CMSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = false;
//                    CMSHelper.totalJobs = 4100000;

                    //------------------thiet lap tham so (end)-----------------

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                    CMSDatacenter datacenter0 = CMSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CMSHelper.createBroker();

                    int brokerId = broker.getId();

                    // Sixth step: Starts the simulation
                    double lastclock = CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("(without middle) mean waitting time: " + ((CMSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CMSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CMSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
            */
                    }


                    // in ket qua: voi h = h[j]
                    // ket qua co the copy vao matlab de ve
                    FileWriter fw = null;
                    
                    System.out.println();
                    try {
                        fw = new FileWriter("results_CFQNS_no_stag_alpha_" + CFQNSHelper.alpha + "_capacity_"
                                + CFQNSHelper.jobsqueuecapacity + "_muy_" + CFQNSHelper.muy + "_thrUP_" + ((double) CFQNSHelper.jobsqueuethresholdup) / ((double) CFQNSHelper.jobsqueuecapacity)
                                + "_thrDown_" + ((double) CFQNSHelper.jobsqueuethresholddown) / ((double) CFQNSHelper.jobsqueuecapacity) + "_timeinitqueue_" + hasTimeInitQueue + ".txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fw.write("\n");
                    System.out.println("--------------------------------------");
                    fw.write("--------------------------------------\n");
                    fw.write("alpha = " + alpha[j] + "\n");
                    fw.write("\n");
                    System.out.println("close all");
                    fw.write("close all\n");

                    System.out.print("lamda = [");
                    fw.write("lamda = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", lamdaarray[i]);
                        fw.write(lamdaarray[i] + ", ");
                    }
                    System.out.printf("%.2f ];", lamdaarray[n - 1]);
                    fw.write(lamdaarray[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean waitting time = [");
                    fw.write("mean_waitting_time" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanWaittime[i]);
                        fw.write(meanWaittime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanWaittime[n - 1]);
                    fw.write(meanWaittime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean response time = [");
                    fw.write("mean_response_time" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanResponsetime[i]);
                        fw.write(meanResponsetime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanResponsetime[n - 1]);
                    fw.write(meanResponsetime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number middle server = [");
                    fw.write("mean_number_of_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf( meanNumberMiddleServer[i]+",");
                        fw.write(meanNumberMiddleServer[i] + ", ");
                    }
                    System.out.printf( meanNumberMiddleServer[n - 1]+"]");
                    fw.write(meanNumberMiddleServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number setup server = [");
                    fw.write("mean_number_of_setup_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberSetupServer[i]);
                        fw.write(meanNumberSetupServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberSetupServer[n - 1]);
                    fw.write(meanNumberSetupServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number On server = [");
                    fw.write("mean_number_of_on_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberOnServer[i]);
                        fw.write(meanNumberOnServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberOnServer[n - 1]);
                    fw.write(meanNumberOnServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number Off to Middle server = [");
                    fw.write("mean_number_of_off_to_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberOffToMiddleServer[i]);
                        fw.write(meanNumberOffToMiddleServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberOffToMiddleServer[n - 1]);
                    fw.write(meanNumberOffToMiddleServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number turned off = [");
                    fw.write("mean_number_turned_off" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print( numberONTurnoff[i]+",");
                        fw.write(numberONTurnoff[i] + ", ");
                    }
                    System.out.print( numberONTurnoff[n - 1]+"];");
                    fw.write(numberONTurnoff[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("max number of queue = [");
                    fw.write("max_number_of_queue" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print( numberOfQueue[i]+",");
                        fw.write(numberOfQueue[i] + ", ");
                    }
                    System.out.print(  numberOfQueue[n - 1]+"]");
                    fw.write(numberOfQueue[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("time next = [");
                    fw.write("time_next" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(timenextlist[i]+",");
                        fw.write(timenextlist[i] + ", ");
                    }
                    System.out.print(  timenextlist[n - 1]+" ];");
                    fw.write(timenextlist[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("completed job =[");
                    fw.write("completed job"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print( numberOfCompletedJob[i]+" , ");
                        fw.write(numberOfCompletedJob[i] + ", ");
                    }
                    System.out.print(numberOfCompletedJob[n-1]+"];\n");
                    fw.write(numberOfCompletedJob[n-1]+"];\n");
                    
                    System.out.print("leave job =[");
                    fw.write("left job"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print( numberOfLeftJob[i]+",");
                        fw.write(numberOfLeftJob[i] + ", ");
                    }
                    System.out.print( numberOfLeftJob[n-1]+"]");
                    fw.write(numberOfLeftJob[n-1]+",]\n");
                    System.out.println();
                    
                    
                    
                    System.out.print("job lost ratio = [");
                    fw.write("job_lost_ratio"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf(  jobLostRatio[i]+",");
                        fw.write(jobLostRatio[i] + ", ");
                    }
                    System.out.print( jobLostRatio[n - 1]+"]");
                    fw.write(jobLostRatio[n - 1] + "];\n");
		    System.out.println();
		
		    System.out.printf("job left ratio = [");
                    fw.write("job_left_ratio"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                      System.out.printf(  leftJobRatio[i]+",");
                      fw.write(leftJobRatio[i] + ", ");
                    }
                    System.out.print( leftJobRatio[n - 1]+"]");
                    fw.write(leftJobRatio[n - 1] + "];\n");	



                    fw.write("\n_____________________________\n\n");
                    System.out.println();

                    //------------ ko co middle-------------
/*
            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number setup server no middle = [");fw.write("mean_number_of_setup_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServerNoMidle[i]);fw.write(meanNumberSetupServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServerNoMidle[n-1]);fw.write(meanNumberSetupServerNoMidle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server no middle = [");fw.write("mean_number_of_on_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServerNoMidle[i]);fw.write(meanNumberOnServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServerNoMidle[n-1]);fw.write(meanNumberOnServerNoMidle[n-1] + "];\n");
            Log.printLine();
*/


                    System.out.println();

                    // in code ve:
                    fw.write("plot(lamda,mean_waitting_time,'r',lamda,mean_waitting_time_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean waitting time');\n");
                    fw.write("legend('with middle','no middle');\n");
                    fw.write("ylim([0 max(max(mean_waitting_time),max(mean_waitting_time_no_middle))*10/9]);");

//            fw.write("ylim([0 18000]);\n");
                    fw.write("figure(2);\n");
                    fw.write("plot(lamda,mean_number_of_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number of middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_middle_server)*10/9]);");

                    fw.write("figure(3);\n");
                    fw.write("plot(lamda,mean_number_of_off_to_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number off to middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_off_to_middle_server)*10/9]);");

                    // ------ve do thi mean number of ON server va Setup server
                    fw.write("figure(4);\n");
                    fw.write("plot(lamda,mean_number_of_setup_server,'r',lamda,mean_number_of_setup_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number setup server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_setup_server),max(mean_number_of_setup_server_no_middle))*10/9]);");

                    fw.write("figure(5);\n");
                    fw.write("plot(lamda,mean_number_of_on_server,'r',lamda,mean_number_of_on_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number on server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_on_server),max(mean_number_of_on_server_no_middle))*10/9]);\n");
//            fw.write("ylim([0 1000]);\n");
                    System.out.println("--------------------------------------");
                    fw.write("--------------------------------------\n");
                    fw.close();
                }

            }
        }
    }

    public static void chayvoilamdathaydoisinglequeuekomiddlegioihansomaychu() throws IOException {
        // file luu kq


        // // co dinh alpha, lamda thay doi

        boolean hasTimeInitQueue = true; // thay doi cai nay phai thay doi ham init() trong file datacenter

        double[] lamdaarray = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        int n = lamdaarray.length;

        double[] meanWaittimeNoMiddle = new double[n];
        double[] meanNumberSetupServerNoMidle = new double[n];
        double[] meanNumberOnServerNoMidle = new double[n];

        double[] meanWaittime = new double[n];
        double[] meanResponsetime = new double[n];

        int[] numberONTurnoff = new int[n];
        int[] numberOfQueue = new int[n];
        double[] meanNumberSetupServer = new double[n];
        double[] meanNumberOnServer = new double[n];
        double[] meanNumberMiddleServer = new double[n];
//        double[] meanNumberOffToMiddleServer = new double[n];
        double[] timenextlist = new double[n];
        double[] jobLostRatio = new double[n];

        for (int indexMuy = 0; indexMuy < muy.length; indexMuy++) {
            for (int j = 0; j < alpha.length; j++) {

                for (int indexK = 0; indexK < capacity.length; indexK++) {

                    // cho lambda thay doi
                    for (int i = 0; i < lamdaarray.length; i++) {

                        // chay ko co middle
                        // chay voi co control middle
                        System.out.println("(without middle) Starting simulation... chay voi alpha = " + alpha[j]
                                +" muy = "+ muy[indexMuy]+ " capacity = "+capacity[indexK] + " lamda = " + lamdaarray[i]);
                        try {
                            //------------------thiet lap tham so-----------------
                            CFQNSHelper.reset();
                            CFQNSHelper.hostNum = 100;
                            CFQNSHelper.setAlpha(alpha[j]);
                            CFQNSHelper.jobsqueuecapacity = capacity[indexK];
                            CFQNSHelper.jobsqueuethresholdup = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrUP);
                            CFQNSHelper.jobsqueuethresholddown = (int) (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.thrDown);
                            CFQNSHelper.setMuy(muy[indexMuy]); // thoi gian phuc vu 1/ muy = 5
                            CFQNSHelper.setTimeOffToMiddle(timetomiddle);

                            CFQNSHelper.setLamda(lamdaarray[i]);
                            CFQNSHelper.isSingleQueue = true;
//                    CMSHelper.setControlTime(200);

//                        // thay doi thoi gian thu nghiem cho phu hop voi lambda
//                        CFQNSHelper.timeStartSimulate = CFQNSHelper.timeStartSimulate / lamdaarray[i];
//                        CFQNSHelper.totalTimeSimulate = CFQNSHelper.totalTimeSimulate / lamdaarray[i];

                            boolean hasMiddle = false;

//                            calculatetimenext();
                            //^^^^^^^^^^^^^^thiet lap tham so (end)^^^^^^^^^^^^^^^

                            // First step: Initialize the CloudSim package. It should be called
                            // before creating any entities.
                            int num_user = 1;   // number of cloud users
                            Calendar calendar = Calendar.getInstance();
                            boolean trace_flag = false;  // mean trace events

                            // Initialize the CloudSim library
                            CloudSim.init(num_user, calendar, trace_flag);

                            // Second step: Create Datacenters
                            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                            CFQNSDatacenter datacenter0 = CFQNSHelper.createDatacenter("Datacenter_0");
                            datacenter0.state = CFQNSDatacenter.ON;
//                            datacenter0.setStartState(0,(int) (CFQNSHelper.timeOffToMiddle/CFQNSHelper.timenext));
                            datacenter0.setHasMiddle(hasMiddle);
                            Log.printLine("main datacenter id "+datacenter0.getId());


                            //Third step: Create Broker
                            DatacenterBroker broker = CFQNSHelper.createBroker();


                            int brokerId = broker.getId();

                            // Sixth step: Starts the simulation
                            double lastclock = CloudSim.startSimulation();
                            // Final step: Print results when simulation is over
                            List<Cloudlet> newList = broker.getCloudletReceivedList();

                            CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                            System.out.println();
                            System.out.println();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                            System.out.println("total time simulate: " + lastclock);
                            System.out.println("(without middle) mean waitting time: " + ((CFQNSBroker) broker).getMeanWaittingTime());
                            System.out.println("(withouot middle) mean response time: " + ((CFQNSBroker) broker).getMeanResponseTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                            System.out.println("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                            System.out.println("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                            System.out.println("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                            System.out.println("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                            System.out.println("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                            System.out.println("number of job (trong khoang thoi gian xet: " + ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("**** total job lost: " + datacenter0.getNumberOfJobLost());
                            System.out.println("**** ti le job lost = joblost / numberofjob: " +datacenter0.getNumberOfJobLost()+" / "+ ((CFQNSBroker) broker).getNumberOfJob()+ " = "+ datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob());
                            System.out.println("total vm: " + CFQNSHelper.getVmid());
                            System.out.println("total sub datacenter create: " + CFQNSHelper.listSubDatacenter.size());
                            System.out.println("so lan tat may ON  " + CFQNSHelper.extraCount);
                            System.out.println();


                            meanNumberMiddleServer[i] = datacenter0.getMeanMiddleServerLength();
                            meanNumberOnServer[i] = datacenter0.getMeanNumberOnServer();
                            meanNumberSetupServer[i] = datacenter0.getMeanNumberSetupServer();

//                            meanNumberOffToMiddleServer[i] = CFQNSHelper.timeOffToMiddle / CFQNSHelper.timenext;
                            timenextlist[i] = CFQNSHelper.timenext;
                            jobLostRatio[i] = datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob();

                            meanWaittime[i] = ((CFQNSBroker) broker).getMeanWaittingTime();
                            meanResponsetime[i] = ((CFQNSBroker) broker).getMeanResponseTime();
                            numberONTurnoff[i] = CFQNSHelper.extraCount;
                            numberOfQueue[i] = CFQNSHelper.listSubDatacenter.size();


//                Log.printLine(CMSHelper.getWaittingTime(newList));

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.printLine("The simulation has been terminated due to an unexpected error");
                        }

                        //--------------------------------------- bo phan nay di doi voi infinite queue------------
                /*
                // chay khong co middle

                Log.printLine("(ko co middle) Starting simulation... chay voi lamda = ");
                try {
                    //------------------thiet lap tham so-----------------
                    CMSHelper.reset();
                    CMSHelper.setLamda(lamdaarray[i]);
//                    CMSHelper.setControlTime(200);
                    CMSHelper.setAlpha(alpha[j]);
                    CMSHelper.setMuy(0.2);


                    CMSHelper.setTimeOffToMiddle(200);

                    boolean hasMiddle = false;
//                    CMSHelper.totalJobs = 4100000;

                    //------------------thiet lap tham so (end)-----------------

                    // First step: Initialize the CloudSim package. It should be called
                    // before creating any entities.
                    int num_user = 1;   // number of cloud users
                    Calendar calendar = Calendar.getInstance();
                    boolean trace_flag = false;  // mean trace events

                    // Initialize the CloudSim library
                    CloudSim.init(num_user, calendar, trace_flag);

                    // Second step: Create Datacenters
                    //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

                    CMSDatacenter datacenter0 = CMSHelper.createDatacenter("Datacenter_0");
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
                    datacenter0.setHasMiddle(hasMiddle);


                    //Third step: Create Broker
                    DatacenterBroker broker = CMSHelper.createBroker();

                    int brokerId = broker.getId();

                    // Sixth step: Starts the simulation
                    double lastclock = CloudSim.startSimulation();
                    // Final step: Print results when simulation is over
                    List<Cloudlet> newList = broker.getCloudletReceivedList();

                    CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

                    Log.printLine();
                    Log.printLine();

//                    Log.printLine("mean waitting time: " + datacenter0.getMeanWaittingTime());
                    Log.printLine("total time simulate: " + lastclock);
                    Log.printLine("(without middle) mean waitting time: " + ((CMSBroker) broker).getMeanWaittingTime());

//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());
                    Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

                    Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
                    Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

                    Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

                    Log.printLine("total job: " + CMSHelper.getCloudletid());
                    Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
                    Log.printLine("**** ti le job lost = joblost / numberofjob: "+datacenter0.getNumberOfJobLost()/((CMSBroker) broker).getNumberOfJob());
                    Log.printLine("total vm: " + CMSHelper.getVmid());
                    Log.printLine();

                    meanWaittimeNoMiddle[i] = ((CMSBroker) broker).getMeanWaittingTime();
                    meanNumberOnServerNoMidle[i] =datacenter0.getMeanNumberOnServer();
                    meanNumberSetupServerNoMidle[i] = datacenter0.getMeanNumberSetupServer();

//                Log.printLine(CMSHelper.getWaittingTime(newList));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.printLine("The simulation has been terminated due to an unexpected error");
                }

                //--------------------------------------- bo phan nay di doi voi infinite queue------------
            */
                    }


                    // in ket qua: voi h = h[j]
                    // ket qua co the copy vao matlab de ve
                    FileWriter fw = null;
                    System.out.println();
                    try {
                        fw = new FileWriter("results_CFQNS_no_stag_alpha_" + CFQNSHelper.alpha + "_capacity_"
                                + CFQNSHelper.jobsqueuecapacity + "_muy_" + CFQNSHelper.muy + "_thrUP_" + ((double) CFQNSHelper.jobsqueuethresholdup) / ((double) CFQNSHelper.jobsqueuecapacity)
                                + "_thrDown_" + ((double) CFQNSHelper.jobsqueuethresholddown) / ((double) CFQNSHelper.jobsqueuecapacity) + "_timeinitqueue_" + hasTimeInitQueue + ".txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    fw.write("\n");
                    System.out.println("---------without middle------------------");

                    fw.write("alpha = " + alpha[j] + "\n");
                    fw.write("\n");
                    fw.write("--------------------------------------\n");

                    System.out.println("close all");
                    fw.write("close all\n");

                    System.out.print("lamda = [");
                    fw.write("lamda = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", lamdaarray[i]);
                        fw.write(lamdaarray[i] + ", ");
                    }
                    System.out.printf("%.2f ];", lamdaarray[n - 1]);
                    fw.write(lamdaarray[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean waitting time = [");
                    fw.write("mean_waitting_time_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanWaittime[i]);
                        fw.write(meanWaittime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanWaittime[n - 1]);
                    fw.write(meanWaittime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean response time = [");
                    fw.write("mean_response_time_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f, ", meanResponsetime[i]);
                        fw.write(meanResponsetime[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanResponsetime[n - 1]);
                    fw.write(meanResponsetime[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number middle server = [");
                    fw.write("mean_number_of_middle_server_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberMiddleServer[i]);
                        fw.write(meanNumberMiddleServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberMiddleServer[n - 1]);
                    fw.write(meanNumberMiddleServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number setup server = [");
                    fw.write("mean_number_of_setup_server_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberSetupServer[i]);
                        fw.write(meanNumberSetupServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberSetupServer[n - 1]);
                    fw.write(meanNumberSetupServer[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("mean number On server = [");
                    fw.write("mean_number_of_on_server_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.printf("%.2f ,", meanNumberOnServer[i]);
                        fw.write(meanNumberOnServer[i] + ", ");
                    }
                    System.out.printf("%.2f ];", meanNumberOnServer[n - 1]);
                    fw.write(meanNumberOnServer[n - 1] + "];\n");
                    System.out.println();

//                    System.out.print("mean number Off to Middle server = [");
//                    fw.write("mean_number_of_off_to_middle_server" + CFQNSHelper.jobsqueuecapacity + " = [");
//                    for (int i = 0; i < n - 1; i++) {
//                        System.out.printf("%.2f ,", meanNumberOffToMiddleServer[i]);
//                        fw.write(meanNumberOffToMiddleServer[i] + ", ");
//                    }
//                    System.out.printf("%.2f ];", meanNumberOffToMiddleServer[n - 1]);
//                    fw.write(meanNumberOffToMiddleServer[n - 1] + "];\n");
//                    System.out.println();

                    System.out.print("mean number turned off = [");
                    fw.write("mean_number_turned_off_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + numberONTurnoff[i]);
                        fw.write(numberONTurnoff[i] + ", ");
                    }
                    System.out.print(" ," + numberONTurnoff[n - 1]);
                    fw.write(numberONTurnoff[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("max number of queue = [");
                    fw.write("max_number_of_queue_nomiddle" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + numberOfQueue[i]);
                        fw.write(numberOfQueue[i] + ", ");
                    }
                    System.out.print(" ," + numberOfQueue[n - 1]);
                    fw.write(numberOfQueue[n - 1] + "];\n");
                    System.out.println();

                    System.out.print("time next = [");
                    fw.write("time_next" + CFQNSHelper.jobsqueuecapacity + " = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + timenextlist[i]);
                        fw.write(timenextlist[i] + ", ");
                    }
                    System.out.print(" ," + timenextlist[n - 1]);
                    fw.write(timenextlist[n - 1] + "];\n");


                    System.out.print("job lost ratio = [");
                    fw.write("job_lost_ratio_nomiddle"+CFQNSHelper.jobsqueuecapacity+" = [");
                    for (int i = 0; i < n - 1; i++) {
                        System.out.print(" ," + jobLostRatio[i]);
                        fw.write(jobLostRatio[i] + ", ");
                    }
                    System.out.print(" ," + jobLostRatio[n - 1]);
                    fw.write(jobLostRatio[n - 1] + "];\n");


                    fw.write("\n_____________________________\n\n");
                    System.out.println();

                    //------------ ko co middle-------------
/*
            Log.print("mean Waittime No Middle = [");fw.write("mean_waitting_time_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanWaittimeNoMiddle[i]);fw.write(meanWaittimeNoMiddle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanWaittimeNoMiddle[n-1]);fw.write(meanWaittimeNoMiddle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number setup server no middle = [");fw.write("mean_number_of_setup_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberSetupServerNoMidle[i]);fw.write(meanNumberSetupServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberSetupServerNoMidle[n-1]);fw.write(meanNumberSetupServerNoMidle[n-1] + "];\n");
            Log.printLine();

            Log.print("mean number On server no middle = [");fw.write("mean_number_of_on_server_no_middle = [");
            for (int i = 0; i < n-1; i++) {
                System.out.printf("%.2f ,", meanNumberOnServerNoMidle[i]);fw.write(meanNumberOnServerNoMidle[i] + ", ");
            }
            System.out.printf("%.2f ];", meanNumberOnServerNoMidle[n-1]);fw.write(meanNumberOnServerNoMidle[n-1] + "];\n");
            Log.printLine();
*/


                    System.out.println();

                    // in code ve:
                    fw.write("plot(lamda,mean_waitting_time,'r',lamda,mean_waitting_time_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean waitting time');\n");
                    fw.write("legend('with middle','no middle');\n");
                    fw.write("ylim([0 max(max(mean_waitting_time),max(mean_waitting_time_no_middle))*10/9]);");

//            fw.write("ylim([0 18000]);\n");
                    fw.write("figure(2);\n");
                    fw.write("plot(lamda,mean_number_of_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number of middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_middle_server)*10/9]);");

                    fw.write("figure(3);\n");
                    fw.write("plot(lamda,mean_number_of_off_to_middle_server,'r'); title('alpha = " + alpha[j] + "');xlabel('lamda');ylabel('mean number off to middle server');\n");
                    fw.write("ylim([0 max(mean_number_of_off_to_middle_server)*10/9]);");

                    // ------ve do thi mean number of ON server va Setup server
                    fw.write("figure(4);\n");
                    fw.write("plot(lamda,mean_number_of_setup_server,'r',lamda,mean_number_of_setup_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number setup server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_setup_server),max(mean_number_of_setup_server_no_middle))*10/9]);");

                    fw.write("figure(5);\n");
                    fw.write("plot(lamda,mean_number_of_on_server,'r',lamda,mean_number_of_on_server_no_middle,'-xb');\n");
                    fw.write("title('alpha = " + alpha[j] + "'); xlabel('lamda');ylabel('mean number on server');\n");
                    fw.write("legend('with middle','without middle');\n");
                    fw.write("ylim([0 max(max(mean_number_of_on_server),max(mean_number_of_on_server_no_middle))*10/9]);\n");
//            fw.write("ylim([0 1000]);\n");
                    System.out.println("--------------------------------------");
                    fw.write("--------------------------------------\n");
                    fw.close();
                }

            }
        }
    }

    public static void calculatetimenext() {
        //***************************************
        // thiet lap timenext trong thuat toan control middle

        // dat pi_00 = a;
        // bieu dien tat ca pi_ij theo a
        // bat dau:
        // hang 0:

        // mang luu gia tri limiting probability hang dang xet
        // cu dich den hang tiep theo thi mang lai giam di phan tu dau tien
        double[] hangtruoc = new double[CFQNSHelper.jobsqueuecapacity + 1];
        // he so
        double[] a = new double[CFQNSHelper.jobsqueuecapacity + 1];
        double[] b = new double[CFQNSHelper.jobsqueuecapacity + 1];
        // xet dong 0
        double sum = 0;
        double sum1 = 0;
        hangtruoc[0] = 1;
        sum = sum + hangtruoc[0];
        // tong cac pi_ii:
        double sum2 = hangtruoc[0];
        for (int i = 1; i < CFQNSHelper.jobsqueuecapacity; i++) {
            hangtruoc[i] = hangtruoc[i - 1] * CFQNSHelper.lamda / (CFQNSHelper.lamda + i * CFQNSHelper.alpha);
            sum = sum + hangtruoc[i];
            sum1 = sum1 + hangtruoc[i] * (i - 0);
        }
        hangtruoc[CFQNSHelper.jobsqueuecapacity] = hangtruoc[CFQNSHelper.jobsqueuecapacity - 1] * CFQNSHelper.lamda / (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.alpha);
        sum = sum + hangtruoc[CFQNSHelper.jobsqueuecapacity];

        // xet tu dong thu 1 tro di
        // pi_11:
        hangtruoc[1] = CFQNSHelper.lamda / CFQNSHelper.muy;
        sum = sum + hangtruoc[1];
        sum2 = sum2 + hangtruoc[1];
        // tinh he so:

        for (int i = 1; i < CFQNSHelper.jobsqueuecapacity; i++) { // i tu 1 den K-1
            // xet dong i:
            // tinh cac he so
            a[CFQNSHelper.jobsqueuecapacity] = (CFQNSHelper.jobsqueuecapacity - i + 1) * CFQNSHelper.alpha *
                    hangtruoc[CFQNSHelper.jobsqueuecapacity] / ((CFQNSHelper.jobsqueuecapacity - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

            b[CFQNSHelper.jobsqueuecapacity] = CFQNSHelper.lamda / ((CFQNSHelper.jobsqueuecapacity - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

            for (int j = CFQNSHelper.jobsqueuecapacity - 1; j > i; j--) {
                a[j] = (i * CFQNSHelper.muy * a[j + 1] + (j - i + 1) * CFQNSHelper.alpha * hangtruoc[j])
                        / (CFQNSHelper.lamda + (j - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);

                b[j] = CFQNSHelper.lamda / (CFQNSHelper.lamda + (j - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);
            }

            // tinh limiting probability cho dong thu i ke tu pi_i,i+1
            double temp = 0;
            for (int j = i + 1; j < CFQNSHelper.jobsqueuecapacity + 1; j++) {
                hangtruoc[j] = a[j] + b[j] * hangtruoc[j - 1];
                temp = temp + (j - i) * CFQNSHelper.alpha * hangtruoc[j];
                sum = sum + hangtruoc[j];
                sum1 = sum1 + hangtruoc[j] * (j - i);
            }

            // tinh limiting probability pi_i+1,i+1
            hangtruoc[i + 1] = temp / ((i + 1) * CFQNSHelper.muy);
            sum = sum + hangtruoc[i + 1];
            sum2 = sum2 + hangtruoc[i + 1];

        }

        // gia tri cua pi_00 = 1/sum
        //  => tong cac pi_ii = sum2/sum
        // tinh timenext:
        CFQNSHelper.timenext = 1 / (CFQNSHelper.alpha * sum1 / sum);
        System.out.println("time next: " + CFQNSHelper.timenext);

//        CFQNSHelper.timenext = 1/0.136;
//        System.out.println("time next: "+ CFQNSHelper.timenext);
    }

    public static int min(int a, int b){
        if(a>b) return b;
        else return a;
    }

    public static void calculatetimenextversion2() {
        // su dung de tinh time next voi mo hinh ma so luong server gioi han
        // va co finite capacity (co the lon hon so server)
        //***************************************
        // thiet lap timenext trong thuat toan control middle

        // dat pi_00 = a;
        // bieu dien tat ca pi_ij theo a
        // bat dau:
        // hang 0:

        // mang luu gia tri limiting probability hang dang xet
        // cu dich den hang tiep theo thi mang lai giam di phan tu dau tien

        // hostnumb cua CFwNSHelper chinh la c ( so server cua he thong)
        int K = CFQNSHelper.jobsqueuecapacity;
        int c = CFQNSHelper.hostNum;

        double[] hangtruoc = new double[K + 1];
        // he so
        double[] a = new double[K + 1];
        double[] b = new double[K + 1];

        // ----------xet dong 0
        double sum = 0;
        double sum1 = 0;
        hangtruoc[0] = 1;
        sum = sum + hangtruoc[0];
        // tong cac pi_ii:
//        double sum2 = hangtruoc[0];
        for (int j = 1; j < K; j++) {
            hangtruoc[j] = hangtruoc[j - 1] * CFQNSHelper.lamda / (CFQNSHelper.lamda + min(j,c) * CFQNSHelper.alpha);
            sum = sum + hangtruoc[j];
            sum1 = sum1 + hangtruoc[j] * (min(j,c) - 0);
        }
        hangtruoc[K] = hangtruoc[K - 1] * CFQNSHelper.lamda / (c * CFQNSHelper.alpha);
        sum = sum + hangtruoc[K];
        sum1 = sum1 + hangtruoc[K] * (min(K,c) - 0);
        // ^^^^^^^^^^^^^^^^^^^^^

        // xet tu dong thu 1 den dong c-1
        // pi_11:
        hangtruoc[1] = CFQNSHelper.lamda / CFQNSHelper.muy;
        sum = sum + hangtruoc[1];
//        sum2 = sum2 + hangtruoc[1];
        // tinh he so:

        for (int i = 1; i < c; i++) { // i tu 1 den c-1
            // xet dong i:
            // tinh cac he so
            a[K] = (c - i + 1) * CFQNSHelper.alpha *
                    hangtruoc[K] / ((c - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

            b[K] = CFQNSHelper.lamda / ((c - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

            for (int j = K - 1; j > i; j--) {
                a[j] = (i * CFQNSHelper.muy * a[j + 1] + min((c-i+1),(j - i + 1)) * CFQNSHelper.alpha * hangtruoc[j])
                        / (CFQNSHelper.lamda + min((c-i),(j - i)) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);

                b[j] = CFQNSHelper.lamda / (CFQNSHelper.lamda + min((c-i),(j - i)) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);
            }

            // tinh limiting probability cho dong thu i ke tu pi_i,i+1
            double temp = 0;
            for (int j = i + 1; j < K + 1; j++) {
                hangtruoc[j] = a[j] + b[j] * hangtruoc[j - 1];
                temp = temp + min((c-i),(j - i)) * CFQNSHelper.alpha * hangtruoc[j];
                sum = sum + hangtruoc[j];
                sum1 = sum1 + hangtruoc[j] * min((c-i),(j - i));
            }

            // tinh limiting probability pi_i+1,i+1
            hangtruoc[i + 1] = temp / ((i + 1) * CFQNSHelper.muy);
            sum = sum + hangtruoc[i + 1];
//            sum2 = sum2 + hangtruoc[i + 1];

        }

        // tinh dong cuoi cung (hang c)
        a[K] =   CFQNSHelper.alpha *
                hangtruoc[K] / (c * CFQNSHelper.muy);

        b[K] = CFQNSHelper.lamda / ( c * CFQNSHelper.muy);

        for (int j = K - 1; j > c; j--) {
            a[j] = (c * CFQNSHelper.muy * a[j + 1] +  CFQNSHelper.alpha * hangtruoc[j])
                    / (CFQNSHelper.lamda +  c * CFQNSHelper.muy - c * CFQNSHelper.muy * b[j + 1]);

            b[j] = CFQNSHelper.lamda / (CFQNSHelper.lamda + c * CFQNSHelper.muy - c * CFQNSHelper.muy * b[j + 1]);
        }
        // tinh limiting probability cho dong thu c ke tu pi_c,c+1

        for (int j = c + 1; j < K + 1; j++) {
            hangtruoc[j] = a[j] + b[j] * hangtruoc[j - 1];
            sum = sum + hangtruoc[j];
        }

        // gia tri cua pi_00 = 1/sum
        //  => tong cac pi_ii = sum2/sum
        // tinh timenext:
        CFQNSHelper.timenext = 1 / (CFQNSHelper.alpha * sum1 / sum);
//        System.out.println("time next (cal version 2) : " + CFQNSHelper.timenext);

//        CFQNSHelper.timenext = 1/0.136;
//        System.out.println("time next: "+ CFQNSHelper.timenext);
    }


    public static void test() {

        // chay voi co control middle
        Log.printLine("Starting simulation... chay voi lamda = ");
        try {
            //------------------thiet lap tham so-----------------
            CFQNSHelper.reset();
            CFQNSHelper.setLamda(5);
//                    CMSHelper.setControlTime(200);
            CFQNSHelper.setAlpha(0.01);
            CFQNSHelper.setMuy(0.01); // thoi gian phuc vu 1/ muy = 5


            CFQNSHelper.setTimeOffToMiddle(timetomiddle);
            boolean hasMiddle = true;

//                    CMSHelper.totalJobs = 100100000;

            calculatetimenext();
            //------------------thiet lap tham so (end)-----------------

            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

            CFQNSDatacenter datacenter0 = CFQNSHelper.createDatacenter("Datacenter_0");
            datacenter0.state = CFQNSDatacenter.ON;
//                    datacenter0.setStartState((int) (CMSHelper.lamda / CMSHelper.muy), 0);
            datacenter0.setHasMiddle(hasMiddle);


            //Third step: Create Broker
            DatacenterBroker broker = CFQNSHelper.createBroker();

            int brokerId = broker.getId();

            // Sixth step: Starts the simulation
            double lastclock = CloudSim.startSimulation();
            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            CloudSim.stopSimulation();

//                CMSHelper.printCloudletList(newList);

            Log.printLine();
            Log.printLine();

            Log.printLine("total time simulate: " + lastclock);
            Log.printLine("mean waitting time: " + ((CFQNSBroker) broker).getMeanWaittingTime());
            Log.printLine("mean response time: " + ((CFQNSBroker) broker).getMeanResponseTime());


//                    double t = 0;
//                    for(int index = 100000; index < ((CMSBroker) broker).listJob.size(); index++){
//                        t = t + ((CMSBroker) broker).listJob.get(index).getTimeStartExe()-
//                                ((CMSBroker) broker).listJob.get(index).getTimeCreate();
//                    }
//                    t = t/((CMSBroker) broker).listJob.size();
//                    Log.printLine("mean waitting time 2: "+t);
//                    Log.printLine("mean waitting time 2: of "+ newList.size()+" : " + CMSHelper.getMeanWaittingTime(newList));

//                    Log.printLine("mean jobs queue length : " + datacenter0.getMeanJobsQueueLength());

            Log.printLine("mean Middle server Length: " + datacenter0.getMeanMiddleServerLength());

            Log.printLine("mean setup server Length: " + datacenter0.getMeanNumberSetupServer());
            Log.printLine("mean ON server Length: " + datacenter0.getMeanNumberOnServer());

            Log.printLine("total time no Middle server: " + datacenter0.getTimeNoMiddle());

            Log.printLine("total job: " + CFQNSHelper.getCloudletid());
            Log.printLine("**** total job lost: " + datacenter0.getNumberOfJobLost());
            Log.printLine("**** ti le job lost = joblost / numberofjob: " + datacenter0.getNumberOfJobLost() / ((CFQNSBroker) broker).getNumberOfJob());

            Log.printLine("total vm: " + CFQNSHelper.getVmid());
            Log.printLine("total sub datacenter create: " + CFQNSHelper.listSubDatacenter.size());
            Log.printLine();


//                Log.printLine(CMSHelper.getWaittingTime(newList));

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }

    }


    public static void    main(String[] args) {

//        try {
//            chayvoihthaydoi();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        for(int i =0; i< 100; i++) System.out.println(StdRandom.exp(0.01));

        // thu tu 3 so la muy, alpha, cap

//        testhamcalv2();
//        comparecal1andcal2();

        if(args.length <3) {
            System.out.println(" ko thiet lap du thong so la muy, alpha, capacity ----> chay voi tham so mac dinh");
//            vetimenext(); return;

        }
        else {
            muy = new double[1];
            muy[0] = Double.parseDouble(args[0]);
            alpha = new double[1];
            alpha[0] = Double.parseDouble(args[1]);
            capacity = new int[1];
            capacity[0] = Integer.parseInt(args[2]);
            theta = new double[1];
            
            
            
            if(args.length == 5) {
                listThrDown = new double[1];
                listThrDown[0] = Double.parseDouble(args[3]);
                listThrUp = new double[1];
                listThrUp[0] = Double.parseDouble(args[4]);
            }
        }
        try {
            Log.disable();
           // chayvoilamdathaydoimultiqueues();
          chayvoilamdathaydoisinglequeuegioihansomaychu();
      //  	System.out.print("timenext:="+timenext);
//            chayvoilamdathaydoisinglequeue();
//            chayvoilamdathaydoisinglequeuekomiddle();

//            chayvoilamdathaydoisinglequeuegioihansomaychu();
//            chayvoilamdathaydoisinglequeuekomiddlegioihansomaychu();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // test args
        for(int i=0; i< args.length;i++){
            System.out.println(args[i]);
        }

//        khaosathamcal();
//        testhamcal();

//        Log.printLine("----------K = 500---------");
//        CFQNSHelper.jobsqueuecapacity = 500;
//        test();
        //Log.printLine("----------K = 400---------");
//        test();
//        test();
//        test();
//        test();
//        test();

    }



    public static void khaosathamcal() {
        CFQNSHelper.muy = 0.2;
        CFQNSHelper.alpha = 0.01;

        CFQNSHelper.lamda = 10;
        double mangtimenext[] = new double[36];
        for (int i = 1; i < 36; i++) {
            CFQNSHelper.jobsqueuecapacity = i * 10;
            calculatetimenext();
            mangtimenext[i] = CFQNSHelper.timenext;
        }
        for (int i = 1; i < 36; i++) {
            System.out.print(mangtimenext[i] + " ,");
        }
    }

    public static void vetimenext() {
        double mangtimenext[] = new double[21];
        CFQNSHelper.muy = 0.05;


        CFQNSHelper.alpha = 0.01;

        printnextime(100,100,0.01);
        printnextime(200,200,0.01);
        printnextime(500,500,0.01);
        printnextime(5000,5000,0.01);
//        printnextime(100,200,0.01);
//        printnextime(100,500,0.01);
//
//        printnextime(100,100,0.005);
//        printnextime(100,200,0.005);
//        printnextime(100,500,0.005);
//
//        printnextime(100,100,0.001);
//        printnextime(100,200,0.001);
//        printnextime(100,500,0.001);
    }

    public static void printnextime(int c,int k, double _alpha){
        double mangtimenext[] = new double[21];
        CFQNSHelper.alpha = _alpha;
        CFQNSHelper.hostNum = c;
        CFQNSHelper.jobsqueuecapacity = k;
        for (int i = 1; i < 21; i++) {
            CFQNSHelper.lamda = i;
            calculatetimenextversion2();
            mangtimenext[i] = CFQNSHelper.timenext;
        }
        System.out.print("timenext_00"+(int)(_alpha*1000)+"_"+c+"_"+k+" = [");
        for (int i = 1; i < 20; i++) {
            System.out.print(mangtimenext[i] + ", ");
        }
        System.out.println(mangtimenext[20]+"];");
    }
    public static void comparecal1andcal2() {
        double[] temp = new double[21];
        CFQNSHelper.muy = 0.2;
        CFQNSHelper.alpha = 0.01;
        CFQNSHelper.jobsqueuecapacity = 50;
        CFQNSHelper.hostNum = 50;

        for(int i = 1; i< 21; i++) {
            CFQNSHelper.lamda = i;
            calculatetimenext();
//            System.out.print(CFQNSHelper.timenext + ", ");
            temp[i] = CFQNSHelper.timenext;
        }
        for(int i = 1; i< 21; i++) {
            System.out.print(temp[i] + ", ");
        }

        System.out.println();
        for(int i = 1; i< 21; i++) {
            CFQNSHelper.lamda = i;
            calculatetimenextversion2();
//            System.out.print(CFQNSHelper.timenext + ", ");
            temp[i] = CFQNSHelper.timenext;
        }
        for(int i = 1; i< 21; i++) {
            System.out.print(temp[i] + ", ");
        }
        System.out.println();
    }

    public static void testhamcal() {
        CFQNSHelper.muy = 1;
        CFQNSHelper.alpha = 0.01;
        CFQNSHelper.jobsqueuecapacity = 50;
        CFQNSHelper.hostNum = 50;
        for (int z = 10; z < 50; z++) {
            CFQNSHelper.lamda = z;

            // lamda chay tu 1 den 50x1
            // thiet lap timenext trong thuat toan control middle

            // dat pi_00 = a;
            // bieu dien tat ca pi_ij theo a
            // bat dau:
            // hang 0:

            // mang luu gia tri limiting probability hang dang xet
            // cu dich den hang tiep theo thi mang lai giam di phan tu dau tien
            double[] hangtruoc = new double[CFQNSHelper.jobsqueuecapacity + 1];
            // he so
            double[] a = new double[CFQNSHelper.jobsqueuecapacity + 1];
            double[] b = new double[CFQNSHelper.jobsqueuecapacity + 1];
            // xet dong 0
            double sum = 0;
            double sum1 = 0;
            double block = 0; // xac suat block = tong pi_i,i
            hangtruoc[0] = 1; // pi_0,0
            sum = sum + hangtruoc[0];

            // tong cac pi_ii:
            double sum2 = hangtruoc[0];
            // hang 0 ( hang dau tien)
            for (int i = 1; i < CFQNSHelper.jobsqueuecapacity; i++) {
                hangtruoc[i] = hangtruoc[i - 1] * CFQNSHelper.lamda / (CFQNSHelper.lamda + i * CFQNSHelper.alpha);
                sum = sum + hangtruoc[i];
                sum1 = sum1 + hangtruoc[i] * (i - 0);
            }
            hangtruoc[CFQNSHelper.jobsqueuecapacity] = hangtruoc[CFQNSHelper.jobsqueuecapacity - 1] *
                    CFQNSHelper.lamda / (CFQNSHelper.jobsqueuecapacity * CFQNSHelper.alpha);
            sum = sum + hangtruoc[CFQNSHelper.jobsqueuecapacity];
            block = block + hangtruoc[CFQNSHelper.jobsqueuecapacity];

            // xet tu dong thu 1 tro di
            // pi_11:
            hangtruoc[1] = CFQNSHelper.lamda / CFQNSHelper.muy;
            sum = sum + hangtruoc[1];
            sum2 = sum2 + hangtruoc[1];

            // tinh he so:

            for (int i = 1; i < CFQNSHelper.jobsqueuecapacity; i++) { // i tu 1 den K-1
                // xet dong i:
                // tinh cac he so
                a[CFQNSHelper.jobsqueuecapacity] = (CFQNSHelper.jobsqueuecapacity - i + 1) * CFQNSHelper.alpha *
                        hangtruoc[CFQNSHelper.jobsqueuecapacity] / ((CFQNSHelper.jobsqueuecapacity - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

                b[CFQNSHelper.jobsqueuecapacity] = CFQNSHelper.lamda / ((CFQNSHelper.jobsqueuecapacity - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

                for (int j = CFQNSHelper.jobsqueuecapacity - 1; j > i; j--) {
                    a[j] = (i * CFQNSHelper.muy * a[j + 1] + (j - i + 1) * CFQNSHelper.alpha * hangtruoc[j])
                            / (CFQNSHelper.lamda + (j - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);

                    b[j] = CFQNSHelper.lamda / (CFQNSHelper.lamda + (j - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);
                }

                // tinh limiting probability cho dong thu i ke tu pi_i,i+1
                double temp = 0;
                for (int j = i + 1; j < CFQNSHelper.jobsqueuecapacity + 1; j++) {
                    hangtruoc[j] = a[j] + b[j] * hangtruoc[j - 1];
                    temp = temp + (j - i) * CFQNSHelper.alpha * hangtruoc[j];
                    sum = sum + hangtruoc[j];
                    sum1 = sum1 + hangtruoc[j] * (j - i);
                }
                block = block + hangtruoc[CFQNSHelper.jobsqueuecapacity];
                // tinh limiting probability pi_i+1,i+1
                hangtruoc[i + 1] = temp / ((i + 1) * CFQNSHelper.muy);
                sum = sum + hangtruoc[i + 1];
                sum2 = sum2 + hangtruoc[i + 1];


            }
            block = block + hangtruoc[CFQNSHelper.jobsqueuecapacity];
            Log.print(" " + block / sum + ",");


        }


    }


    public static void testhamcalv2() {
        CFQNSHelper.muy = 1;
        CFQNSHelper.alpha = 0.1;
        CFQNSHelper.jobsqueuecapacity = 50;
        CFQNSHelper.hostNum = 50;
        for (int z = 1; z < CFQNSHelper.hostNum; z++) { // cho lamda chay tu 1 den 100
            CFQNSHelper.lamda = z;

            // lamda chay tu 1 den 50x1
            // thiet lap timenext trong thuat toan control middle

            // dat pi_00 = a;
            // bieu dien tat ca pi_ij theo a
            // bat dau:
            // hang 0:

            // mang luu gia tri limiting probability hang dang xet
            // cu dich den hang tiep theo thi mang lai giam di phan tu dau tien

            // tinh mean queue length:

            int K = CFQNSHelper.jobsqueuecapacity;
            int c = CFQNSHelper.hostNum;

            double[] hangtruoc = new double[K + 1];
            // he so
            double[] a = new double[K + 1];
            double[] b = new double[K + 1];

            // ----------xet dong 0
            double sum = 0;
            double sum1 = 0;
            double queuelength = 0;
            double block =0;
            hangtruoc[0] = 1;
            sum = sum + hangtruoc[0];
            // tong cac pi_ii:
//        double sum2 = hangtruoc[0];
            for (int j = 1; j < K; j++) {
                hangtruoc[j] = hangtruoc[j - 1] * CFQNSHelper.lamda / (CFQNSHelper.lamda + min(j,c) * CFQNSHelper.alpha);
                sum = sum + hangtruoc[j];
                sum1 = sum1 + hangtruoc[j] * (min(j,c) - 0);
                queuelength = queuelength + hangtruoc[j] * j;
            }
            hangtruoc[K] = hangtruoc[K - 1] * CFQNSHelper.lamda / (c * CFQNSHelper.alpha);
            sum = sum + hangtruoc[K];
            sum1 = sum1 + hangtruoc[K] * (min(K,c) - 0);
            queuelength = queuelength + hangtruoc[K] * K;
            block = block +  hangtruoc[K];
            // ^^^^^^^^^^^^^^^^^^^^^

            // xet tu dong thu 1 den dong c-1
            // pi_11:
            hangtruoc[1] = CFQNSHelper.lamda / CFQNSHelper.muy;
            sum = sum + hangtruoc[1];
//        sum2 = sum2 + hangtruoc[1];
            // tinh he so:

            for (int i = 1; i < c; i++) { // i tu 1 den c-1
                // xet dong i:
                // tinh cac he so
                a[K] = (c - i + 1) * CFQNSHelper.alpha *
                        hangtruoc[K] / ((c - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

                b[K] = CFQNSHelper.lamda / ((c - i) * CFQNSHelper.alpha + i * CFQNSHelper.muy);

                for (int j = K - 1; j > i; j--) {
                    a[j] = (i * CFQNSHelper.muy * a[j + 1] + min((c-i+1),(j - i + 1)) * CFQNSHelper.alpha * hangtruoc[j])
                            / (CFQNSHelper.lamda + min((c-i),(j - i)) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);

                    b[j] = CFQNSHelper.lamda / (CFQNSHelper.lamda + min((c-i),(j - i)) * CFQNSHelper.alpha + i * CFQNSHelper.muy - i * CFQNSHelper.muy * b[j + 1]);
                }

                // tinh limiting probability cho dong thu i ke tu pi_i,i+1
                double temp = 0;
                for (int j = i + 1; j < K + 1; j++) {
                    hangtruoc[j] = a[j] + b[j] * hangtruoc[j - 1];
                    temp = temp + min((c-i),(j - i)) * CFQNSHelper.alpha * hangtruoc[j];
                    sum = sum + hangtruoc[j];
                    sum1 = sum1 + hangtruoc[j] * min((c-i),(j - i));
                    queuelength = queuelength + hangtruoc[j] * (j-i);
                }
                block = block +  hangtruoc[K];
                // tinh limiting probability pi_i+1,i+1
                hangtruoc[i + 1] = temp / ((i + 1) * CFQNSHelper.muy);
                sum = sum + hangtruoc[i + 1];
//            sum2 = sum2 + hangtruoc[i + 1];

            }

            // tinh dong cuoi cung (hang c)
            a[K] =   CFQNSHelper.alpha *
                    hangtruoc[K] / (c * CFQNSHelper.muy);

            b[K] = CFQNSHelper.lamda / ( c * CFQNSHelper.muy);

            for (int j = K - 1; j > c; j--) {
                a[j] = (c * CFQNSHelper.muy * a[j + 1] +  CFQNSHelper.alpha * hangtruoc[j])
                        / (CFQNSHelper.lamda +  c * CFQNSHelper.muy - c * CFQNSHelper.muy * b[j + 1]);

                b[j] = CFQNSHelper.lamda / (CFQNSHelper.lamda + c * CFQNSHelper.muy - c * CFQNSHelper.muy * b[j + 1]);
            }
            // tinh limiting probability cho dong thu c ke tu pi_c,c+1

            for (int j = c + 1; j < K + 1; j++) {
                hangtruoc[j] = a[j] + b[j] * hangtruoc[j - 1];
                sum = sum + hangtruoc[j];
                queuelength = queuelength + hangtruoc[j] * (j-c);
            }
            block = block +  hangtruoc[K];
            // in ra waiting time
//            System.out.print((queuelength/sum / z) +", ");
            // in ra mean queue length
//            System.out.print((queuelength/sum) +", ");

            // in ra block
            System.out.print((block/sum) +", ");
        }

        System.out.println();

    }
    public static void calculateTau(){
	    int K = CFQNSHelper.jobsqueuecapacity;
		int c = CFQNSHelper.hostNum;
		 double pi[][] = new double[CFQNSHelper.hostNum+1][CFQNSHelper.jobsqueuecapacity+1];
		 double a[][] = new double[CFQNSHelper.hostNum+1][CFQNSHelper.jobsqueuecapacity+1];  
		 double b[][] = new double[CFQNSHelper.hostNum+11][CFQNSHelper.jobsqueuecapacity+1];  
		 double s1[][] = new double[CFQNSHelper.hostNum+1][CFQNSHelper.jobsqueuecapacity+1];
		for(int i = 0; i<= c; i++){
			for(int j = 0;j <= K; j++){
				pi[i][j] = 0;
				a[i][j] = 0;
				b[i][j] =0;
				s1[i][j] =0;
			}
		}
		pi[0][0] = 1;
		int s = c;
		// i = 0
		b[0][K] = (CFQNSHelper.lamda)/(CFQNSHelper.theta*K+s*CFQNSHelper.alpha);
		for(int j  = K-1; j>=1; j--){
				b[0][j] = CFQNSHelper.lamda/(CFQNSHelper.lamda+ min(j, s, c)*CFQNSHelper.alpha
						+j*CFQNSHelper.theta- (j+1)*CFQNSHelper.theta*b[0][j+1]);
		}
		for(int j  = 1; j <= K; j++){
			pi[0][j] =	b[0][j]*pi[0][j-1];
				
		}
		//i =1 	
		for(int j = 1 ; j <= K ; j++){
		// tinh pi[1][1]
			pi[1][1] =pi[1][1]+min(j,c,s)*CFQNSHelper.alpha*pi[0][j]/CFQNSHelper.muy;
		}
		a[1][K] = Math.min(c,s)*CFQNSHelper.alpha*pi[0][K]/(CFQNSHelper.muy+Math.min(c-1, s)*CFQNSHelper.alpha
				+(K-1)*CFQNSHelper.theta);
		b[1][K] =  CFQNSHelper.lamda/(CFQNSHelper.muy+Math.min(c-1, s)*CFQNSHelper.alpha+
				(K-1)*CFQNSHelper.theta);
		for(int j = K-1; j>=2 ; j--){
			a[1][j] =((CFQNSHelper.muy+j*CFQNSHelper.theta)*a[1][j+1]+min(j,c,s)*CFQNSHelper.alpha*
					pi[0][j])/(CFQNSHelper.muy+CFQNSHelper.lamda+min(j-1,c-1,s)*CFQNSHelper.alpha+
							(j-1)*CFQNSHelper.theta-(CFQNSHelper.muy+j*CFQNSHelper.theta)*b[1][j+1]);
			
			b[1][j] = CFQNSHelper.lamda/(CFQNSHelper.muy+CFQNSHelper.lamda+min(j-1,c-1,s)*CFQNSHelper.alpha+
					(j-1)*CFQNSHelper.theta-(CFQNSHelper.muy+j*CFQNSHelper.theta)*b[1][j+1]);
		}
		for(int j = 2 ; j <= K ; j++){
			pi[1][j] = a[1][j]+ b[1][j]*pi[1][j-1];
		}
		// i = 2 den c-1
		for(int i = 2 ; i < c; i++ ){
			for(int j = i; j<= K;j++){
				pi[i][i] = pi[i][i] +( min(j-i+1,c-i+1,s)*CFQNSHelper.alpha*pi[i-1][j])/(CFQNSHelper.muy*i);
			}
			a[i][K] = Math.min(c-i+1, s)*CFQNSHelper.alpha*pi[i-1][K]/(Math.min(c-i, s)*CFQNSHelper.alpha+
					i*CFQNSHelper.muy+(K-i)*CFQNSHelper.theta);
			b[i][K] = CFQNSHelper.lamda/(Math.min(c-i, s)*CFQNSHelper.alpha+
					i*CFQNSHelper.muy+(K-i)*CFQNSHelper.theta);
			
			for(int j = K-1; j >= i+1; j--){
				s1[i][j] = CFQNSHelper.lamda + min(j-i, c-i, s)*CFQNSHelper.alpha+CFQNSHelper.muy*i+
						(j-i)*CFQNSHelper.theta;
				
				a[i][j] =((i*CFQNSHelper.muy+(j-i+1)*CFQNSHelper.theta)*a[i][j+1]+min(j-i+1,c-i+1,s)*CFQNSHelper.alpha*
						pi[i-1][j])/(s1[i][j]-(i*CFQNSHelper.muy+(j+1-i)*CFQNSHelper.theta)*b[i][j+1]);
				b[i][j] = CFQNSHelper.lamda/(s1[i][j]-(i*CFQNSHelper.muy+(j+1-i)*CFQNSHelper.theta)*b[i][j+1]);
				
			}
			for(int j = i+1 ; j<=K; j++){
				pi[i][j] = a[i][j]+b[i][j]*pi[i][j-1];
			}
		}	
	   // i = c
		for(int j = c; j<= K;j++){
			pi[c][c] = (pi[c][c] + min(j-c+1,1,s)*CFQNSHelper.alpha*pi[c-1][j])/(CFQNSHelper.muy*c);
		}
		a[c][K] =	 CFQNSHelper.alpha*pi[c-1][K]/(c*CFQNSHelper.muy+(K-c)*CFQNSHelper.theta);
		b[c][K] = CFQNSHelper.lamda/(c*CFQNSHelper.muy+(K-c)*CFQNSHelper.theta);
		for(int j = K-1; j >= c+1 ; j--){
			a[c][j]=((c*CFQNSHelper.muy+(j+1-c)*CFQNSHelper.theta)*a[c][j+1]+CFQNSHelper.alpha*pi[c-1][j])/
					(CFQNSHelper.lamda+ c*CFQNSHelper.muy+(j-c)*CFQNSHelper.theta-(c*CFQNSHelper.muy
							+(j-c+1)*CFQNSHelper.theta)*b[c][j+1]	);
			b[c][j] = CFQNSHelper.lamda/(CFQNSHelper.lamda+ c*CFQNSHelper.muy+(j-c)*CFQNSHelper.theta-(c*CFQNSHelper.muy
					+(j-c+1)*CFQNSHelper.theta)*b[c][j+1]);
			
			
					
		}
		for(int j= c+1;j <= K ; j++){
			pi[c][j] = a[c][j]+pi[c][j-1]*b[c][j];
		}
		
		// tinh To
		double  temp = 0;
		for(int i  = 0; i <= c; i++ ){
			for(int j = i; j <= K; j++){
				temp = temp + pi[i][j];
				
			}
			//System.out.println("temp  ="+temp);
		}
		
		
		// tinh pi[0][0]
		pi[0][0] = 1/temp;
		double temp2 = pi[0][0];
		//System.out.println("temp = "+temp);
		
		for(int i = 0; i<= c; i++){
			for(int j = i;j <= K; j++){
				if(j!=0) pi[i][j] =  pi[i][j]*temp2;
			}
		}
		temp2 = 0;
		for(int i = 0; i <= c; i++)
			for(int j = i+1; j<= K ; j++)
				temp2 = temp2 + pi[i][j]*min(c-i,j-i,s);
		CFQNSHelper.timenext =  1/(CFQNSHelper.alpha*temp2);
	
    	
    }
    public static int min(int a, int b ,int c){
		int min =a;
		if(b < min) min = b;
		if(c < min) min = c;
		return min;
	}
}
