package finitequeue;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Created by dangbk on 20/04/2015.
 */
public class CFQNSMiddleHostQueue<E>  {
    protected double waitingTime = 0;
    protected double lastUpdate = 0;
    protected Queue<E> middleQueue = new LinkedList<>();
    protected double timeEmpty = 0;

    protected long dem = 0;
    protected double timeOfQueue = 0;

    protected double oldMeanWaitingTime = 0;
    protected double oldMeanQueueLength = 0;

    public double getTimeEmpty() {
        return timeEmpty;
    }
    public CFQNSMiddleHostQueue(){
    }
    public boolean add(E e){

        updateWaitingtime();
        middleQueue.add(e);
        return true;
    }
    public E poll() {
        updateWaitingtime();
        return middleQueue.poll();
    }
    private double time = 0;
    public void updateWaitingtime(){
        time = CloudSim.clock();
        if(start) {
            waitingTime = waitingTime + (time - lastUpdate) * ( middleQueue.size());
            timeOfQueue = timeOfQueue + (time - lastUpdate);

        }
        else {
            if(time > CFQNSHelper.timeStartSimulate){
                start = true;
//                Log.printLine("bat dau");
            }
        }
        lastUpdate = time;
    }


    public boolean start = false;

    public int getsize(){
        return middleQueue.size();
    }

    public boolean isEmpty() {
        return middleQueue.isEmpty();
    }

    public double    getMeanQueueLength() {

        return waitingTime / timeOfQueue;
    }

    public double getTotalWaitingTime(){
        return waitingTime;
    }
    public double getTimeOfQueue(){
        return timeOfQueue;
    }

}
