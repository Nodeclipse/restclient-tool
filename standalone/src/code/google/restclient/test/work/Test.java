/*******************************************************************************
 * Copyright (c) 2010 Yadu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Yadu - initial API and implementation
 ******************************************************************************/

package code.google.restclient.test.work;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import code.google.restclient.mime.MimeTypeUtil;

/**
 * @author Yaduvendra.Singh
 */
public class Test {
    public static void main(String[] args) {
        // testTransformer();
        xmlPrettyPrinter();
        /*
        try {
        	testRegex();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        */
        // getFileSize();
        // encodeUTF8();
        // testSysProps();
        // testLog4j();
        // testMimeType();
    }

    private static void testMimeType() {
        // File tmpFile = new File("C:/E/Projects_Other/eclipse/RestClient/logs/files/test.txt");
        String tmpFile = "C:/E/Projects_Other/eclipse/RestClient/logs/files/test.txt";
        System.out.println("mime type  => " + MimeTypeUtil.getMimeType(tmpFile));
        System.out.println("media type  => " + MimeTypeUtil.getMediaType(tmpFile));
    }

    private static void testLog4j() {
        PropertyConfigurator.configure(org.apache.log4j.helpers.Loader.getResource("rc-log4j.properties"));
        Logger log = Logger.getLogger(Test.class); // loads props automatically if log4j.properties is present in
        // classpaht
        log.debug("write something to log file");
    }

    private static void testSysProps() {
        System.out.println("User Home Dir: " + System.getProperty("user.home"));
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("OS Name: " + System.getProperty("os.name"));
        System.out.println("OS Version: " + System.getProperty("os.version"));
    }

    private static void encodeUTF8() {
        try {
            System.out.println("= (UTF-8) => " + URLEncoder.encode("=", "UTF-8"));
            System.out.println("= (UTF-8) => " + URLDecoder.decode("=", "UTF-8"));
        } catch ( UnsupportedEncodingException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void getFileSize() {
        File file = new File("C:/E/Projects_Other/eclipse/RestClient/image_test_item.txt");
        float size = (float) file.length() / 1000; // KBs
        System.out.println("getFileSize(): file size = " + size);
    }

    private static void testRegex() throws IOException {
        String regex = "^[a-zA-Z_]+\\(\\)[\\s]*(: | -)"; // (?i)<head>"; //\\."+RCConstants.TEMP_FILE_EXT+"$
        String newToken = "";
        String str = "streamToString() - error occurred while converting";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        matcher.find();
        System.out.println("Group: " + matcher.group());
        System.out.println("New Str: " + str.replaceFirst(regex, newToken).trim());
    }

    private static void testTransformer() {
        ByteArrayOutputStream s;
        try {
            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            TransformerFactory tf = TransformerFactory.newInstance();
            // tf.setAttribute("indent-number", new Integer(4));
            Transformer t = tf.newTransformer();

            Element a, b;

            a = d.createElement("a");
            b = d.createElement("b");

            a.appendChild(b);

            d.appendChild(a);

            t.setOutputProperty(OutputKeys.INDENT, "yes");

            s = new ByteArrayOutputStream();

            t.transform(new DOMSource(d), new StreamResult(s));

            System.out.println(new String(s.toByteArray()));
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static void xmlPrettyPrinter() {
        String xmlStr =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><QueryMessage\n"
                + "        xmlns=\"http://www.SDMX.org/resources/SDMXML/schemas/v2_0/message\"\n"
                + "        xmlns:query=\"http://www.SDMX.org/resources/SDMXML/schemas/v2_0/query\">\n" + "    <Query>\n"
                + "        <query:CategorySchemeWhere>\n"
                + "   \t\t\t\t\t         <query:AgencyID>A</query:AgencyID>\n<query:AgencyID>true</query:AgencyID>\n"
                + "        </query:CategorySchemeWhere>\n" + "    </Query>\n\n\n\n\n"
                + " <test>hello<you><![CDATA[<sender>John Smith</sender>]]></you></test> " + "</QueryMessage>";

        System.out.println("raw xml:\n" + xmlStr);

        String transformedStr = null;
        if ( xmlStr != null && !"".equals(xmlStr) ) {
            InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
            transformedStr = getPrettyXml(is, true, true);
        } else {
            System.out.println("xmlPrettyPrinter() - XML string passed is null!");
        }

        System.out.println("************ transformed str: *************\n" + transformedStr);

    }

    public static String getPrettyXml(InputStream xmlStream, boolean indent, boolean breakCharNode) {
        int tagsCount = 0;
        boolean isCharNode = false;
        if ( xmlStream == null ) {
            System.out.println("getPrettyXml() - XML input stream passed is null!");
            return null;
        } else {
            System.out.println("getPrettyXml() - Starting transformation ...");
        }

        StringWriter strWriter = new StringWriter();
        XMLStreamReader xsr = null;
        // AbstractXMLStreamWriter xsw = null;
        XMLStreamWriter xsw = null;

        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            xsr = xif.createXMLStreamReader(xmlStream);

            // get JSON writer
            /*
            //Configuration config = new Configuration();
            //config.setTypeConverter(new SimpleConverter()); //Simple converter prints every value in quotes
            //MappedNamespaceConvention convention = new MappedNamespaceConvention(config);
            MappedNamespaceConvention convention = new MappedNamespaceConvention(config);
            xsw = new MappedXMLStreamWriter(convention, strWriter);
            */
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xsw = xof.createXMLStreamWriter(strWriter);

            xsw.writeStartDocument();
            while ( xsr.hasNext() ) {
                int event = xsr.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) { // start tag
                    System.out.println("START_ELEMENT: " + xsr.getLocalName());
                    addIndent(xsw, indent, tagsCount);
                    xsw.writeStartElement(xsr.getLocalName());
                    tagsCount++;

                    for ( int i = 0; i < xsr.getAttributeCount(); i++ ) { // tag's attributes
                        System.out.println("@attribute: " + xsr.getAttributeLocalName(i) + "=" + xsr.getAttributeValue(i));

                        xsw.writeAttribute(xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                    }
                }
                if ( event == XMLStreamConstants.CHARACTERS ) { // tag's text value
                    System.out.println("#text: " + xsr.getText());
                    if ( xsr.getText() != null && !"".equals(xsr.getText().trim()) ) {
                        if ( breakCharNode ) {
                            addIndent(xsw, indent, tagsCount);
                            isCharNode = false;
                        }
                        isCharNode = true;
                        xsw.writeCharacters(xsr.getText());
                    }
                }
                if ( event == XMLStreamConstants.CDATA ) { // cdata
                    System.out.println("[CDATA]: " + xsr.getText());
                    if ( xsr.getText() != null && !"".equals(xsr.getText().trim()) ) {
                        xsw.writeCData(xsr.getText());
                    }
                }
                if ( event == XMLStreamConstants.END_ELEMENT ) { // end tag
                    System.out.println("END_ELEMENT");
                    if ( tagsCount > 0 ) {
                        if ( breakCharNode || !isCharNode ) addIndent(xsw, indent, tagsCount - 1);
                        isCharNode = false;
                        xsw.writeEndElement();
                        tagsCount--;
                    } else {
                        System.out
                                  .println("getPrettyXml() - Invalid xml data! Opening tags not equal to closing tags." + " Even then continuing ...");
                    }
                }
            }
            xsw.writeEndDocument();

        } catch ( XMLStreamException xse ) {
            System.out.println("xml2Json() - Error while handling xml stream -> " + xse);
        } finally {
            try {
                if ( xsr != null ) xsr.close();
                if ( xsw != null ) xsw.close();
                if ( strWriter != null ) strWriter.close();
            } catch ( XMLStreamException xse ) {
                System.out.println("xml2Json() - Error while closing xml stream reader/writer -> " + xse);
            } catch ( IOException ioe ) {
                System.out.println("xml2Json() - Error closing strWriter (string writer) -> " + ioe);
            }
        }
        System.out.println("xml2Json() - json output =>\n" + strWriter.toString());
        return strWriter.toString();
    }

    private static void addIndent(XMLStreamWriter xsw, boolean indent, int indentCount) throws XMLStreamException {
        xsw.writeCharacters(getPrettyIndent(indent, indentCount));
    }

    public static String getPrettyIndent(boolean indent, int count) {
        String str = "    ";
        StringBuffer buf = new StringBuffer();
        buf.append("\n");
        if ( indent ) {
            while ( count >= 1 ) {
                buf.append(str);
                count--;
            }
        }
        return buf.toString();
    }
}
