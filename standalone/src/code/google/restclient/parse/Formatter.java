/*******************************************************************************
 * Copyright (c) 2010 Yadu. All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html Contributors: Yadu - initial API
 * and implementation
 ******************************************************************************/

package code.google.restclient.parse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Yaduvendra.Singh
 */
public class Formatter {

    private static final Logger LOG = Logger.getLogger(Formatter.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();
    private static final String NEW_LINE = "\n";

    public static String getIndentedJson(String jsonStr, int indentCount) {
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            return jsonObj.toString(indentCount);
        } catch ( JSONException e ) {
            LOG.warn("getPrettyJson() - Json string is not properly formatted", e);
            return "";
        }
    }

    public static String getIndentedXml(String xmlStr, int indentFactor) {
        if ( xmlStr != null && !"".equals(xmlStr) ) {
            InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
            String prettyXml = getIndentedXml(is, indentFactor);
            return prettyXml;
        } else {
            LOG.warn("getPrettyXml() - XML string passed is null!");
        }
        return "";
    }

    public static String getIndentedXml(InputStream xmlStream, int indentFactor) {
        int tagsCount = 0;

        if ( xmlStream == null ) {
            if ( DEBUG_ENABLED ) LOG.debug("getPrettyXml() - XML input stream passed is null!");
            return null;
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("getPrettyXml() - Starting transformation ...");
        }

        StringWriter strWriter = new StringWriter();
        XMLStreamReader xsr = null;
        XMLStreamWriter xsw = null;
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            xsr = xif.createXMLStreamReader(xmlStream);

            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xsw = xof.createXMLStreamWriter(strWriter);

            xsw.writeStartDocument();
            int prevEvent = -1;
            while ( xsr.hasNext() ) {
                int event = xsr.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) { // start tag
                    if ( DEBUG_ENABLED ) LOG.debug("START_ELEMENT: " + xsr.getLocalName());
                    addXmlIndent(xsw, indentFactor, tagsCount);
                    xsw.writeStartElement(xsr.getLocalName());
                    tagsCount++;

                    for ( int i = 0; i < xsr.getAttributeCount(); i++ ) { // tag's attributes
                        if ( DEBUG_ENABLED ) LOG.debug("@attribute: " + xsr.getAttributeLocalName(i) + "=" + xsr.getAttributeValue(i));
                        xsw.writeAttribute(xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                    }
                }
                if ( event == XMLStreamConstants.CHARACTERS ) { // tag's text value
                    if ( DEBUG_ENABLED ) LOG.debug("#text: " + xsr.getText());
                    if ( !xsr.isWhiteSpace() ) xsw.writeCharacters(xsr.getText());
                }
                if ( event == XMLStreamConstants.CDATA ) { // cdata
                    if ( DEBUG_ENABLED ) LOG.debug("[CDATA]: " + xsr.getText());
                    if ( !xsr.isWhiteSpace() ) xsw.writeCData(xsr.getText());
                }
                if ( event == XMLStreamConstants.END_ELEMENT ) { // end tag
                    if ( DEBUG_ENABLED ) LOG.debug("END_ELEMENT: " + xsr.getLocalName());
                    if ( tagsCount > 0 ) {
                        if ( prevEvent == event ) addXmlIndent(xsw, indentFactor, tagsCount - 1);
                        xsw.writeEndElement();
                        tagsCount--;
                    } else {
                        if ( DEBUG_ENABLED ) {
                            LOG.debug("getPrettyXml() - Invalid xml data! Opening tags not equal to closing tags." + " Even then continuing ...");
                        }
                    }
                }
                prevEvent = event;
            }
            xsw.writeEndDocument();

        } catch ( XMLStreamException xse ) {
            LOG.warn("getPrettyXml() - Error while handling xml stream -> " + xse);
            return ""; // return blank string in case of parsing error
        } finally {
            try {
                if ( xsr != null ) xsr.close();
                if ( xsw != null ) xsw.close();
                if ( strWriter != null ) strWriter.close();
            } catch ( XMLStreamException xse ) {
                LOG.error("getPrettyXml() - Error while closing xml stream reader/writer -> " + xse);
            } catch ( IOException ioe ) {
                LOG.error("getPrettyXml() - Error closing strWriter (string writer) -> " + ioe);
            }
        }
        if ( DEBUG_ENABLED ) LOG.debug("getPrettyXml() - json output =>\n" + strWriter.toString());
        return strWriter.toString();
    }

    private static void addXmlIndent(XMLStreamWriter xsw, int indentFactor, int indentCount) throws XMLStreamException {
        xsw.writeCharacters(getIndent(indentFactor, indentCount));
    }

    private static String getIndent(int indentFactor, int indentCount) {
        StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE);
        while ( indentCount >= 1 ) {
            sb.append(getIndentSize(indentFactor));
            indentCount--;
        }
        return sb.toString();
    }

    private static String getIndentSize(int indentFactor) {
        StringBuilder indent = new StringBuilder();
        String ws = " ";
        if ( indentFactor == 0 ) ws = "";
        else {
            while ( indentFactor >= 1 ) {
                indent.append(ws);
                indentFactor--;
            }
        }
        return indent.toString();
    }
    /*
        public static void main(String[] args) {
            String xmlStr =
                "<root>" + "<item id=\"1\" type=\"video &amp; &quot;me&quot;\" > check video1 &amp; video </item>"
                    + " <field id=\"1\"><![CDATA[In XML you need elements which have a starting tag <song> and end tag </song>]]></field>"
                    + "<field id=\"2\"> <A></A>  <B> </B></field>" + "<field id=\"3\"></field>" + "</root>";
            String prettyXml = getIndentedXml(xmlStr, 2);
            System.out.println("Pretty Xml: \n" + prettyXml);
        }
    */
}
