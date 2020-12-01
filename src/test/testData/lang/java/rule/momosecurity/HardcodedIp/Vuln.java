
public class Vuln {
    public void foo() {
        String ip = <warning descr="MomoSec: 发现硬编码IP地址">"192.168.12.42"</warning>;
        String ip2 = System.getenv("IP_ADDRESS");

        String iplocal = "127.0.0.1";
    }
}
