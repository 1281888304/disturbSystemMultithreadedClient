package clientPart2;

import assignment1.util.RandomUtil;
import com.opencsv.CSVWriter;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MainTest {

  private static LinkedBlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>();
  private static final ProducerLiftRideTask producerLiftRideTask = new ProducerLiftRideTask(queue);
  private static final Thread producer = new Thread(producerLiftRideTask);

  private static final CountDownLatch countDownLatch1 = new CountDownLatch(1);
  private static final CountDownLatch countDownLatch2 = new CountDownLatch(200);
  private static AtomicInteger sucessNumber = new AtomicInteger(0);
  private static AtomicInteger failNumber = new AtomicInteger(0);

  public static void main(String[] args) throws IOException {
    //start csv writer
    CSVWriter csvWriter = new CSVWriter(new FileWriter("/Users/zhangqinghang/Downloads/assignment1.csv"));

    csvWriter.writeNext(new String[]{"start","requestTpe","latency","responseCode"});


    //start produce the liferide object to the queue
    long start = System.currentTimeMillis();
    producer.start();

    //consumer start
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath("http://34.221.255.59:8080/cs6650-lab_war/");
    SkiersApi skiersApi = new SkiersApi(apiClient);
    //start 32 thread for phase1
    for (int i = 0; i < 32; i++) {
      Thread thread = new Thread(() -> {
        for (int j = 0; j < 1000; j++) {
          LiftRide object = queue.poll();
          try {
            long t1=System.currentTimeMillis();
            ApiResponse<Void> voidApiResponse = skiersApi
                .writeNewLiftRideWithHttpInfo(object, RandomUtil.getRandom(1,10), "2022", "1", RandomUtil.getRandom(1,100000));
            sucessNumber.addAndGet(1);
            long t2=System.currentTimeMillis();
            csvWriter.writeNext(new String[]{String.valueOf(t1),"Post",String.valueOf(t2-t1),String.valueOf(voidApiResponse.getStatusCode())});
          } catch (ApiException e) {
            //retry logic
            boolean flag=false;
            for(int k=0;k<5;k++){
              try {
                ApiResponse<Void> retryResponse = skiersApi
                    .writeNewLiftRideWithHttpInfo(object, 1, "2022", "1", 1);
                if(retryResponse.getStatusCode()==200 || retryResponse.getStatusCode()==201){
                  sucessNumber.addAndGet(1);
                  flag=true;
                  break;
                }
              } catch (ApiException apiException) {
                System.out.println("error on retry period ");
              }
            }
            if(!flag){
              failNumber.addAndGet(1);
            }
          }
        }
        countDownLatch1.countDown();
      });
      thread.start();
    }
    try {
      countDownLatch1.await();
      System.out.println("pharse 1 done with " + sucessNumber + " success request " + failNumber
          + " fail request"
          + " spend time " + String.valueOf(System.currentTimeMillis() - start));
      //open thread to take
      for(int i=0;i<200;i++){
        Thread phase2Thread=new Thread(new ConsumerPhase2Task(queue,skiersApi,sucessNumber,failNumber,countDownLatch2,csvWriter));
        phase2Thread.start();
      }
      countDownLatch2.await();
      long timeUsed=System.currentTimeMillis()-start;
      System.out.println("phase 2 done with "+sucessNumber +" success request "+failNumber+" fail request"
      +" spend total wall->"+String.valueOf(timeUsed));
      System.out.println("throughput "+ (int)200000/(timeUsed/1000)+" per second");
    } catch (InterruptedException e) {
      System.out.println("error on count down latch await the main thread");
    }

  }

}
