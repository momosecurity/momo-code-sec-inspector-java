import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Vuln {
    XStream classFieldXStream = new XStream(new DomDriver());

    public Vuln() {
        XStream.setupDefaultSecurity(classFieldXStream);
    }

    public void foo() {
        XStream xStream = <error descr="MomoSec: XStream 反序列化风险">new XStream(new DomDriver())</error>;
        xStream.fromXML("<xml></xml>");
    }

    public void  bar() {
        XStream xStream = new XStream(new DomDriver());
        XStream.setupDefaultSecurity(xStream);
        xStream.fromXML("<xml></xml>");
    }

}