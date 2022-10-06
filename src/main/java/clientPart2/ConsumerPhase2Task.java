package clientPart2;

import com.opencsv.CSVWriter;
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
  private CSVWriter csvWriter;

  public ConsumerPhase2Task(
      LinkedBlockingQueue<LiftRide> queue, SkiersApi skiersApi,
      AtomicInteger success, AtomicInteger fail, CountDownLatch countDownLatch,
      CSVWriter csvWriter) {
    this.queue = queue;
    this.skiersApi = skiersApi;
    this.success = success;
    this.fail = fail;
    this.countDownLatch = countDownLatch;
    this.csvWriter = csvWriter;
  }

  @Override
  public void run() {
    while(!queue.isEmpty()){
      LiftRide liftRide=queue.poll();
      try {
        long t1=System.currentTimeMillis();
        ApiResponse<Void> response = skiersApi
            .writeNewLiftRideWithHttpInfo(liftRide, 1, "2022", "1", 1);
        success.addAndGet(1);
        long t2=System.currentTimeMillis();
        csvWriter.writeNext(new String[]{String.valueOf(t1),"Post",String.valueOf(t2-t1),String.valueOf(response.getStatusCode())});
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
