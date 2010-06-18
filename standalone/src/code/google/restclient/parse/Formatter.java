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

/**
 * @author Yaduvendra.Singh
 */
public class Formatter {

    private static final Logger LOG = Logger.getLogger(Formatter.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    public static String getPrettyXml(InputStream xmlStream, boolean indent, boolean breakCharNode) {
        int tagsCount = 0;
        boolean isCharNode = false;
        if ( xmlStream == null ) {
            if ( DEBUG_ENABLED ) LOG.debug("getPrettyXml() - XML input stream passed is null!");
            return null;
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("getPrettyXml() - Starting transformation ...");
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
                    if ( DEBUG_ENABLED ) LOG.debug("START_ELEMENT: " + xsr.getLocalName());
                    addIndent(xsw, indent, tagsCount);
                    xsw.writeStartElement(xsr.getLocalName());
                    tagsCount++;

                    for ( int i = 0; i < xsr.getAttributeCount(); i++ ) { // tag's attributes
                        if ( DEBUG_ENABLED ) {
                            LOG.debug("@attribute: " + xsr.getAttributeLocalName(i) + "=" + xsr.getAttributeValue(i));
                        }
                        xsw.writeAttribute(xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                    }
                }
                if ( event == XMLStreamConstants.CHARACTERS ) { // tag's text value
                    if ( DEBUG_ENABLED ) LOG.debug("#text: " + xsr.getText());
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
                    if ( DEBUG_ENABLED ) LOG.debug("[CDATA]: " + xsr.getText());
                    if ( xsr.getText() != null && !"".equals(xsr.getText().trim()) ) {
                        xsw.writeCData(xsr.getText());
                    }
                }
                if ( event == XMLStreamConstants.END_ELEMENT ) { // end tag
                    if ( DEBUG_ENABLED ) LOG.debug("END_ELEMENT");
                    if ( tagsCount > 0 ) {
                        if ( breakCharNode || !isCharNode ) addIndent(xsw, indent, tagsCount - 1);
                        isCharNode = false;
                        xsw.writeEndElement();
                        tagsCount--;
                    } else {
                        if ( DEBUG_ENABLED ) {
                            LOG.debug("getPrettyXml() - Invalid xml data! Opening tags not equal to closing tags." + " Even then continuing ...");
                        }
                    }
                }
            }
            xsw.writeEndDocument();

        } catch ( XMLStreamException xse ) {
            LOG.error("getPrettyXml() - Error while handling xml stream -> " + xse);
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

    public static String getPrettyXml(String xmlStr, boolean indent, boolean breakCharNode) {
        if ( xmlStr != null && !"".equals(xmlStr) ) {
            InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
            return getPrettyXml(is, indent, breakCharNode);
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("getPrettyXml() - XML string passed is null!");
        }
        return null;
    }

    private static void addIndent(XMLStreamWriter xsw, boolean indent, int indentCount) throws XMLStreamException {
        xsw.writeCharacters(getPrettyXmlIndent(indent, indentCount));
    }

    public static String getPrettyXmlIndent(boolean indent, int count) {
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
