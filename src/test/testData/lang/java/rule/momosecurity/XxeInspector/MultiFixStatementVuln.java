import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXTransformerFactory;

public class MultiFixStatementVuln {
    public void hasVuln() {
        SAXTransformerFactory tf = (SAXTransformerFactory) <error descr="MomoSec: 疑似存在XXE漏洞">SAXTransformerFactory.newInstance()</error>;
    }

    public void notCompleteFix() {
        SAXTransformerFactory tf2 = (SAXTransformerFactory) <error descr="MomoSec: 疑似存在XXE漏洞">SAXTransformerFactory.newInstance()</error>;

        tf2.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    }

    public void noVuln() {
        SAXTransformerFactory tf3 = (SAXTransformerFactory) SAXTransformerFactory.newInstance();

        tf3.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        tf3.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    }
}