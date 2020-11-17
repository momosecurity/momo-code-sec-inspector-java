import java.util.Properties;

public class Vuln {
    <warning descr="MomoSec: 发现硬编码凭证">private String fieldToken = "f9IJosm2M2H7EqDBTAE2L2FE6";</warning>

    private String bar() {
        return "aloha";
    }

    public void foo() {
        String commonVar = "f9IJosm2M2H7EqDBTAE2L2FE6";
        <warning descr="MomoSec: 发现硬编码凭证">String private_token = "f9IJosm2M2H7EqDBTAE2L2FE6";</warning>
        String admin_passwd = "admin123";


        String myToken;
        <warning descr="MomoSec: 发现硬编码凭证">myToken = "f9IJosm2M2H7EqDBTAE2L2FE6"</warning>;

        String method_call = bar();
    }

    public void checkOnPropertiesKey() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConstruct.AccessKey, "AccessKey");
        <warning descr="MomoSec: 发现硬编码凭证">properties.put(PropertyKeyConstruct.SecretKey, "f9IJosm2M2H7EqDBTAE2L2FE6")</warning>;
    }
}

class PropertyKeyConstruct {
    public static final String AccessKey = "AccessKey";
    public static final String SecretKey = "SecretKey";
}