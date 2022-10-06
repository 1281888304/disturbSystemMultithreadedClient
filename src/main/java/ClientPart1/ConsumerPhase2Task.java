package ClientPart1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerPhase2Task implements Runnable{

  private LinkedBlockingQueue<LiftRide> queue;
  private SkiersApi skiersApi;
  private AtomicInteger success;
  private AtomicInteger fail;
  private CountDownLatch countDownLatch;


  public ConsumerPhase2Task(
      LinkedBlockingQueue<LiftRide> queue, SkiersApi skiersApi,
      AtomicInteger success, AtomicInteger fail, CountDownLatch countDownLatch) {
    this.queue = queue;
    this.skiersApi = skiersApi;
    this.success = success;
    this.fail = fail;
    this.countDownLatch = countDownLatch;
  }

  @Override
  public void run() {
    while(!queue.isEmpty()){
      LiftRide liftRide=queue.poll();
      try {

        ApiResponse<Void> response = skiersApi
            .writeNewLiftRideWithHttpInfo(liftRide, 1, "2022", "1", 1);
        success.addAndGet(1);
      } catch (ApiException e) {
        //re-try logic
        boolean flag=false;
        for(int i=0;i<5;i++){
          try {
            ApiResponse<Void> retryResponse = skiersApi
                .writeNewLiftRideWithHttpInfo(liftRide, 1, "2022", "1", 1);
            if(retryResponse.getStatusCode()==200 || retryResponse.getStatusCode()==201){
              flag=true;
              success.addAndGet(1);
              break;
            }
          } catch (ApiException apiException) {
            apiException.printStackTrace();
          }
        }
        if(!flag){
          fail.addAndGet(1);
        }
      }

    }
    countDownLatch.countDown();
  }
}
