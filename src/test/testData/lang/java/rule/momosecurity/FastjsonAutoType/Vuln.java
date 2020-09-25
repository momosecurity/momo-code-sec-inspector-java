import com.alibaba.fastjson.parser.ParserConfig;

public class Vuln {
    public void foo() {
        <error descr="MomoSec: 发现fastjson反序列化风险">ParserConfig.getGlobalInstance().setAutoTypeSupport(true)</error>;
    }

    public void bar() {
        ParserConfig localConf = new ParserConfig();
        <error descr="MomoSec: 发现fastjson反序列化风险">localConf.setAutoTypeSupport(true)</error>;
    }

    public void notvul() {
        ParserConfig.getGlobalInstance().setAutoTypeSupport(false);
    }
}