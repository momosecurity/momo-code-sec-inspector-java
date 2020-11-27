import java.util.Random;

public class Vuln {
    public void foo() {
        Random random = <error descr="MomoSec: 发现不安全的伪随机数生成器">new Random()</error>;
        random.nextInt();
    }

}
