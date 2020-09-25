import java.beans.XMLDecoder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;

public class Vuln {
    public void foo() throws FileNotFoundException {
        InputStream in = new FileInputStream("foo");
        <error descr="MomoSec: 发现 XMLDecoder 反序列化风险">new XMLDecoder(in)</error>;
    }
}