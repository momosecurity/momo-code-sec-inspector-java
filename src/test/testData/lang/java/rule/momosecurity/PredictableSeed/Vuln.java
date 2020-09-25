import java.security.SecureRandom;

public class Vuln {
    public void foo () {
        SecureRandom prng = new SecureRandom();

        <error descr="MomoSec: 发现固定的随机数种子风险">prng.setSeed(12345L)</error>;

    }
}