package assignment1.util;

import java.util.Random;

public class RandomUtil {

  public static int getRandom(int min, int max) {
    Random r = new Random();
    return r.nextInt(max) + min;

  }

}
