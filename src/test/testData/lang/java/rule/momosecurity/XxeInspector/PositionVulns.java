import javax.xml.parsers.DocumentBuilderFactory;

public class PositionVulns {
    DocumentBuilderFactory classDbf;
    DocumentBuilderFactory classDbfInitVuln = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
    DocumentBuilderFactory classDefInitNoVuln = DocumentBuilderFactory.newInstance();
    static DocumentBuilderFactory classDefStaticNoVuln = DocumentBuilderFactory.newInstance();

    static {
        classDefStaticNoVuln.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    }

    public PositionVulns() {
        System.out.println("constructor");
        classDefInitNoVuln.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    }

    public void classFieldVuln() {
        classDbf = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
    }

    public void localVarVuln() {
        DocumentBuilderFactory localDbf = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
    }

    public void localVarWithTryNoVuln() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature("fake", false);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception e) {

        }
    }

    public void localVarWithCommentNoVuln() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // 禁用 外部实体
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    }
}