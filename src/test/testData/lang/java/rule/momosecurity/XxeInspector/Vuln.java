import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.XMLConstants;

public class Vuln {
    DocumentBuilderFactory classDbf;
    SAXParserFactory classSpf;
    SAXTransformerFactory classSf;
    DocumentBuilderFactory classDbf2_vul = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
    DocumentBuilderFactory classDef2_novul = DocumentBuilderFactory.newInstance();
    static DocumentBuilderFactory classDef3_novul = DocumentBuilderFactory.newInstance();
    SAXTransformerFactory classSf_novul;

    static {
        classDef3_novul.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    }

    public Vuln() {
        System.out.println("constructor");
        classDef2_novul.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    }

    public void classFieldVulns() {
        classDbf = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
        classSpf = <error descr="MomoSec: 疑似存在XXE漏洞">SAXParserFactory.newInstance()</error>;
        classSf = (SAXTransformerFactory) <error descr="MomoSec: 疑似存在XXE漏洞">SAXTransformerFactory.newInstance()</error>;
    }

    public void classFieldNoVuln() {
        classSf_novul = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

        System.out.println("disturb!");

        classSf_novul.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    }

    public void localVarVulns() {
        DocumentBuilderFactory dbf = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
        SAXParserFactory spf = <error descr="MomoSec: 疑似存在XXE漏洞">SAXParserFactory.newInstance()</error>;
        SAXTransformerFactory sf = (SAXTransformerFactory) <error descr="MomoSec: 疑似存在XXE漏洞">SAXTransformerFactory.newInstance()</error>;
    }

    public void localVarWithCommentNoVuln() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // 禁用 外部实体
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    }

    public void localVarWithTryNoVuln() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature("fake", false);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception e) {

        }
    }
}