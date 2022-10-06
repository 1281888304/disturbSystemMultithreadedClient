package ClientPart1;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

public class SingleThreadTest {

  public static void main(String[] args) {
    ApiClient apiClient=new ApiClient();
    apiClient.setBasePath("http://54.189.139.99:8080/cs6650-lab_war/");
    SkiersApi skiersApi=new SkiersApi(apiClient);
    LiftRide liftRide=new LiftRide();
    liftRide.setLiftID(1);
    liftRide.setTime(1);

  }

}
