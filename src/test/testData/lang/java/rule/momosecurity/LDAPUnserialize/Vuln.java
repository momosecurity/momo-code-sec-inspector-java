import javax.naming.directory.SearchControls;

public class Vuln {
    public void foo() {
        new SearchControls(1, 1L, 1, null, <error descr="MomoSec: 发现 LDAP 反序列化风险">true</error>, true);

        SearchControls searchControls = new SearchControls();
        searchControls.setReturningObjFlag(<error descr="MomoSec: 发现 LDAP 反序列化风险">true</error>);
    }
}