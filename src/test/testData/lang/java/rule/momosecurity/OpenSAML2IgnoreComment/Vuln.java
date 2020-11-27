import org.opensaml.xml.parse.StaticBasicParserPool;

public class Vuln {
    public void foo() {
        StaticBasicParserPool staticBasicParserPool = new StaticBasicParserPool();
        <error descr="MomoSec: 发现 OpenSAML2 认证绕过风险">staticBasicParserPool.setIgnoreComments(false)</error>;
    }
}
