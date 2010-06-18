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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 * This class uses mapping conventions of jettison to convert xml to/from json
 * 
 * @author Yaduvendra.Singh
 */

public class XMLJson {

    private static final Logger LOG = Logger.getLogger(XMLJson.class);
    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    private static int tagsCount = 0;

    /**
     * Converts xml input stream into json string
     * 
     * @param InputStream
     * @return json string
     */
    public static String xml2Json(InputStream xmlStream) {

        if ( xmlStream == null ) {
            if ( DEBUG_ENABLED ) LOG.debug("xml2Json() - XML input stream passed is null!");
            return null;
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("xml2Json() - Starting transformation ...");
        }

        StringWriter strWriter = new StringWriter();
        XMLStreamReader xsr = null;
        AbstractXMLStreamWriter xsw = null;
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            xsr = xif.createXMLStreamReader(xmlStream);

            MappedNamespaceConvention convention = new MappedNamespaceConvention();
            xsw = new MappedXMLStreamWriter(convention, strWriter);

            xsw.writeStartDocument();
            while ( xsr.hasNext() ) {
                int event = xsr.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) { // start tag
                    tagsCount++;
                    if ( DEBUG_ENABLED ) LOG.debug("START_ELEMENT: " + xsr.getLocalName());
                    xsw.writeStartElement(xsr.getLocalName());
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
                        xsw.writeEndElement();
                        tagsCount--;
                    } else {
                        if ( DEBUG_ENABLED ) {
                            LOG.debug("xml2Json() - Invalid xml data! Opening tags not equal to closing tags."
                                + " Even then continuing ...");
                        }
                    }
                }
            }
            xsw.writeEndDocument();

        } catch ( XMLStreamException xse ) {
            LOG.error("xml2Json() - Error while handling xml stream -> " + xse);
        } finally {
            try {
                if ( xsr != null ) xsr.close();
                if ( xsw != null ) xsw.close();
                if ( strWriter != null ) strWriter.close();
            } catch ( XMLStreamException xse ) {
                LOG.error("xml2Json() - Error while closing xml stream reader/writer -> " + xse);
            } catch ( IOException ioe ) {
                LOG.error("xml2Json() - Error closing strWriter (string writer) -> " + ioe);
            }
        }
        if ( DEBUG_ENABLED ) LOG.debug("xml2Json() - json output =>\n" + strWriter.toString());
        return strWriter.toString();
    }

    /**
     * Converts xml string to json string. Avoid using this method because of its poor performance. It kills the purpose
     * of using XML stream reader, to achieve higher performance. Prefer to use xml2Json(InputStream is)
     * 
     * @param xmlStr
     * @return String json string
     */
    public static String xml2Json(String xmlStr) {
        if ( xmlStr != null && !"".equals(xmlStr) ) {
            InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
            return xml2Json(is);
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("xml2Json() - XML string passed is null!");
            return null;
        }
    }

    /**
     * Converts json string to xml string
     * 
     * @param jsonStr
     * @return xml string
     */
    public static String json2Xml(String jsonStr) {

        if ( jsonStr == null || !"".equals(jsonStr) ) {
            if ( DEBUG_ENABLED ) LOG.debug("json2Xml() - Json string passed is null!");
            return null;
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("json2Xml() - Starting transformation ...");
        }

        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamReader xsr = null;
        XMLStreamWriter xsw = null;
        try {
            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            xsw = xof.createXMLStreamWriter(strWriter);

            JSONObject jsonObj = new JSONObject(jsonStr);
            MappedNamespaceConvention convention = new MappedNamespaceConvention();
            xsr = new MappedXMLStreamReader(jsonObj, convention);

            xsw.writeStartDocument();
            while ( xsr.hasNext() ) {
                int event = xsr.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) { // start tag
                    tagsCount++;
                    if ( DEBUG_ENABLED ) LOG.debug("START_ELEMENT: " + xsr.getLocalName());
                    xsw.writeStartElement(xsr.getLocalName());
                    for ( int i = 0; i < xsr.getAttributeCount(); i++ ) { // tag's attributes
                        if ( DEBUG_ENABLED )
                            LOG.debug("@attribute: " + xsr.getAttributeLocalName(i) + "=" + xsr.getAttributeValue(i));
                        xsw.writeAttribute(xsr.getAttributeLocalName(i), xsr.getAttributeValue(i));
                    }
                }
                if ( event == XMLStreamConstants.CHARACTERS ) { // tag's text value
                    if ( DEBUG_ENABLED ) LOG.debug("#text: " + xsr.getText());
                    if ( xsr.getText() != null && !"".equals(xsr.getText().trim()) ) {
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
                        xsw.writeEndElement();
                        tagsCount--;
                    } else {
                        if ( DEBUG_ENABLED ) {
                            LOG.debug("json2Xml() - Invalid json data! Opening tags not equal to closing tags."
                                + " Even then continuing ...");
                        }
                    }
                }
            }
            xsw.writeEndDocument();

        } catch ( XMLStreamException xse ) {
            LOG.error("json2Xml() - Error while handling json stream -> " + xse);
        } catch ( JSONException je ) {
            LOG.error("json2Xml() - Error with json -> " + je);
        } finally {
            try {
                if ( xsr != null ) xsr.close();
                if ( xsw != null ) xsw.close();
                if ( strWriter != null ) strWriter.close();
            } catch ( XMLStreamException xse ) {
                LOG.error("json2Xml() - Error while closing json stream reader/writer -> " + xse);
            } catch ( IOException ioe ) {
                LOG.error("json2Xml() - Error closing strWriter (string writer) -> " + ioe);
            }
        }
        if ( DEBUG_ENABLED ) LOG.debug("json2Xml() - xml output =>\n" + strWriter.toString());
        return strWriter.toString();
    }

    /**
     * Converts json input steam to xml string
     * 
     * @param jsonInputStream
     * @return xml string
     */
    public static String json2Xml(InputStream jsonInputStream) {

        if ( jsonInputStream == null ) {
            if ( DEBUG_ENABLED ) LOG.debug("json2Xml() - Json input stream passed is null!");
            return null;
        } else {
            if ( DEBUG_ENABLED ) LOG.debug("json2Xml() - Starting transformation ...");
        }

        StringBuffer buf = new StringBuffer();
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(jsonInputStream));
        String xmlStr = null;
        String line = null;
        try {
            while ( (line = bufReader.readLine()) != null ) {
                buf.append(line);
            }
            xmlStr = json2Xml(buf.toString());
        } catch ( IOException ioe ) {
            LOG.error("json2Xml() - Error reading json buffered reader -> " + ioe);
        } finally {
            try {
                if ( bufReader != null ) bufReader.close();
            } catch ( IOException ioe ) {
                LOG.error("json2Xml() - Error closing json buffered reader -> " + ioe);
            }
        }
        return xmlStr;
    }

    /* Test Methods */
    /*
    private static void testXml2Json(int methodType) {
    	InputStream fis = getXmlFileInputStream();
    	String xmlStr = "<root>"
    					+	"<item id=\"1\" type=\"video &amp; &quot;me&quot;\">\"check video1 &amp; video\"</item>"
    					+	"<field id=\"1\"> </field>"
    					+"</root>";
    	InputStream is = new ByteArrayInputStream(xmlStr.getBytes());
    	
    	if(methodType == 1) {
    		//LOG.debug(xml2Json(fis));
    		LOG.debug(xml2Json(is));	// test xml2Json(InputStream is)
    	}		
    	if(methodType == 2) {
    		LOG.debug(xml2Json(xmlStr));	// test xml2Json(String xmlStr)
    	}		
    	try {
    		fis.close();
    		is.close();
    	} catch(IOException ioe){
    		ioe.printStackTrace();
    	}
    }
    	
    private static void testJson2Xml(int methodType){
    	InputStream fis = getXmlFileInputStream();
    	String jsonStr = "{root:{item:{@id:\"1\",@type:\"video & me\",$:\"check video1 & video2\"}}}";
    	
    	if(methodType == 1) {
    		LOG.debug(json2Xml(jsonStr));	// test json2Xml(String jsonStr)
    	}		
    	if(methodType == 2) {
    		//String jsonStr = xml2Json(fis);
    		InputStream jsonInputStream = new ByteArrayInputStream(jsonStr.getBytes());
    		LOG.debug(json2Xml(jsonInputStream));	// test json2Xml(InputStream jsonInputStream)
    	}		
    	try {
    		fis.close();
    	} catch(IOException ioe){
    		ioe.printStackTrace();
    	}
    }
    
    private static FileInputStream getXmlFileInputStream() {
    	FileInputStream fis = null;
    	try {
    		File file = new File("C:/E/MLB/svn_mlb/mlbam/cms/service/data/seed/dam_view/MLBLIVE.xml");	// enter path of xml file
    		fis = new FileInputStream(file);
    	} catch(FileNotFoundException e) {
    		if( DEBUG_ENABLED ) LOG.debug(e);
    	}
    	return fis;
    }
    
    public static void main(String[] args) {
    	testXml2Json(1);	// xml input stream to json string
    	//testXml2Json(2);	// xml string to json string
    	//testJson2Xml(1);	// json string to xml string
    	//testJson2Xml(2);	// json input stream to xml string
    }
    */
}
