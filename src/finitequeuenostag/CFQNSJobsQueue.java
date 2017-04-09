package finitequeue_nostag;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dangbk on 17/04/2015.
 */
public class CFQNSJobsQueue<E>  {
    protected double waitingTime = 0;
    protected double lastUpdate = 0;
    protected Queue<E> jobsqueue = new LinkedList<>();

//    protected long dem = 0;
//    protected double timeOfQueue = 0;
//
//    protected double oldMeanWaitingTime = 0;
//    protected double oldMeanQueueLength = 0;
//
//    protected int numberJobLost = 0;
//    protected int capacity = 1000;
//    protected int threshold =  800;

    public CFQNSJobsQueue(){
//        this.capacity = CFQNSHelper.jobsqueuecapacity;
//        this.threshold = CFQNSHelper.jobsqueuethresholdup;

    }

//    protected boolean startCout = false;

    public boolean add(E e){
        // phuogn thuc nay tra lai false neu hang doi day (vuot qua threshold)
//        if(jobsqueue.size() >= capacity){
//            if (startCout)
//                numberJobLost ++;
//            else {
//                if(CloudSim.clock() > CFQNSHelper.timeStartSimulate) startCout = true;
//            }
//            return false;
//        }
//        // khong them job duoc nua vi da vuot qua gioi han
//        else {
//            if (((CFQNSJob) e).getTimeCreate() != 0)
//                ((CFQNSJob) e).setTimeCreate(CloudSim.clock());
            return jobsqueue.add(e);
//        }
    }
    public E poll() {

        return jobsqueue.poll();
    }

//    public int getNumberJobLost(){
//        return numberJobLost;
//    }
    //new
    public boolean deleteJob(E job){
    	return jobsqueue.remove(job);
    }	
    public boolean contains(E job){
    	if(jobsqueue.contains(job)) return true;
    	else return false;
    	
    }
    //new



    public int getsize(){
        return jobsqueue.size();
    }

    public boolean isEmpty() {
        return jobsqueue.isEmpty();
    }
//    public double getMeanWaitingTime(){
////        return tong/(solan -2);
//
//        return waitingTime / dem;
//    }
//    public double getMeanQueueLength() {
//        return oldMeanQueueLength;
////    }
//    public long getTotalItem(){
//        return dem;
//    }
}
