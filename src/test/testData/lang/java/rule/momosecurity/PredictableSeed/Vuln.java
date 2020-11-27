import java.security.SecureRandom;

public class Vuln {
    public void v1 () {
        SecureRandom prng = new SecureRandom();
        <error descr="MomoSec: 发现固定的随机数种子风险">prng.setSeed(12345L)</error>;
    }

    public void v2() {
        SecureRandom prng = <error descr="MomoSec: 发现固定的随机数种子风险">new SecureRandom("hello".getBytes("us-ascii"))</error>;
    }
}