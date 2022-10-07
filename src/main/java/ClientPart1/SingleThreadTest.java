package ClientPart1;

import assignment1.util.RandomUtil;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadTest {
  private static LinkedBlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>();
  private static final ProducerLiftRideTask producerLiftRideTask = new ProducerLiftRideTask(queue);
  private static final Thread producer = new Thread(producerLiftRideTask);
  private static final CountDownLatch countDownLatch1 = new CountDownLatch(1);
  private static AtomicInteger sucessNumber = new AtomicInteger(0);
  private static AtomicInteger failNumber = new AtomicInteger(0);

  public static void main(String[] args) throws InterruptedException {
    long start = System.currentTimeMillis();
    producer.start();
    ApiClient apiClient=new ApiClient();
    apiClient.setBasePath("http://52.33.248.44:8080/cs6650-lab_war/");
    SkiersApi skiersApi=new SkiersApi(apiClient);


    Thread thread = new Thread(() -> {
      for (int j = 0; j < 100; j++) {
        LiftRide object = queue.poll();
        try {
          ApiResponse<Void> voidApiResponse = skiersApi
              .writeNewLiftRideWithHttpInfo(object, RandomUtil.getRandom(1,10), "2022", "1", RandomUtil.getRandom(1,100000));
          sucessNumber.addAndGet(1);
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
    countDownLatch1.await();

    System.out.println("single thread 100 request spend "+String.valueOf(System.currentTimeMillis()-start)+
        " with success" + sucessNumber +"fail "+ failNumber);



  }

}
