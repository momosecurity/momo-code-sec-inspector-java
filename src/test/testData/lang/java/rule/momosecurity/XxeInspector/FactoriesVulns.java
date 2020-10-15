import org.jdom.input.SAXBuilder;
import org.dom4j.io.SAXReader;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;

public class FactoriesVulns {
    public void v() {
        DocumentBuilderFactory dbf = <error descr="MomoSec: 疑似存在XXE漏洞">DocumentBuilderFactory.newInstance()</error>;
        SAXParserFactory spf = <error descr="MomoSec: 疑似存在XXE漏洞">SAXParserFactory.newInstance()</error>;
        SAXTransformerFactory sf = (SAXTransformerFactory) <error descr="MomoSec: 疑似存在XXE漏洞">SAXTransformerFactory.newInstance()</error>;
        SAXBuilder saxBuilder = <error descr="MomoSec: 疑似存在XXE漏洞">new SAXBuilder()</error>;
        SAXReader saxReader = <error descr="MomoSec: 疑似存在XXE漏洞">new SAXReader()</error>;
        XMLReader xmlReader = <error descr="MomoSec: 疑似存在XXE漏洞">XMLReaderFactory.createXMLReader()</error>;
        SchemaFactory schemaFactory = <error descr="MomoSec: 疑似存在XXE漏洞">SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")</error>;
        XMLInputFactory xmlInputFactory = <error descr="MomoSec: 疑似存在XXE漏洞">XMLInputFactory.newFactory()</error>;
        TransformerFactory transformerFactory = <error descr="MomoSec: 疑似存在XXE漏洞">TransformerFactory.newInstance()</error>;

        Schema schema = schemaFactory.newSchema();
        Validator validator = <error descr="MomoSec: 疑似存在XXE漏洞">schema.newValidator()</error>;
    }
}