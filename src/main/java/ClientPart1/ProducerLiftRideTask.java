package ClientPart1;

import assignment1.util.RandomUtil;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerLiftRideTask implements Runnable{

  private LinkedBlockingQueue<LiftRide> queue;

  public ProducerLiftRideTask(
      LinkedBlockingQueue<LiftRide> queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    for(int i=0;i<200000;i++){
      LiftRide liftRide=new LiftRide();
      liftRide.setLiftID(RandomUtil.getRandom(1,40));
      liftRide.setTime(RandomUtil.getRandom(1,360));
      try {
        queue.put(liftRide);
      } catch (InterruptedException e) {
        System.out.println("error on put produce task (liferide object )to queue");
      }
    }
  }


}
