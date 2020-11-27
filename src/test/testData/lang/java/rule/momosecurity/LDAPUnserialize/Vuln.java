import javax.naming.directory.SearchControls;

public class Vuln {
    public void foo() {
        <error descr="MomoSec: 发现 LDAP 反序列化风险">new SearchControls(1, 1L, 1, null, true, true)</error>;

        SearchControls searchControls = new SearchControls();
        <error descr="MomoSec: 发现 LDAP 反序列化风险">searchControls.setReturningObjFlag(true)</error>;
    }
}