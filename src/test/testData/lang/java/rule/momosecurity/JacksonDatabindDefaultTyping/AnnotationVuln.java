import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class AnnotationVuln {
    @JsonTypeInfo(<error descr="MomoSec: 发现Jackson反序列化风险">use = JsonTypeInfo.Id.CLASS</error>, include = JsonTypeInfo.As.PROPERTY)
    public String vuln1;

    @JsonTypeInfo(<error descr="MomoSec: 发现Jackson反序列化风险">use = JsonTypeInfo.Id.MINIMAL_CLASS</error>)
    public String vuln2;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public String common;

}