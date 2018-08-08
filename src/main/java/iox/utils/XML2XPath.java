package iox.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
// w  w w  . ja  v a  2s  . c  om
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

class FragmentContentHandler extends DefaultHandler {

	private final Logger log = LoggerFactory.getLogger(FragmentContentHandler.class);

	String xPath = "/";
	XMLReader xmlReader;
	FragmentContentHandler parent;
	StringBuilder characters = new StringBuilder();
	Map<String, Integer> elementNameCount = new HashMap<String, Integer>();
	List<String> xPaths = new ArrayList<String>();

	public FragmentContentHandler(XMLReader xmlReader) {
		this.xmlReader = xmlReader;
	}

	private FragmentContentHandler(String xPath, XMLReader xmlReader, FragmentContentHandler parent) {
		this(xmlReader);
		this.xPath = xPath;
		this.parent = parent;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		Integer count = elementNameCount.get(qName);
		if (null == count) {
			count = 1;
		} else {
			count++;
		}
		elementNameCount.put(qName, count);
		String childXPath = xPath + "/" + qName + "[" + count + "]";

		int attsLength = atts.getLength();
		for (int x = 0; x < attsLength; x++) {
			log.trace(childXPath + "[@" + atts.getQName(x) + "='" + atts.getValue(x) + ']');
			xPaths.add(childXPath + "[@" + atts.getQName(x) + "='" + atts.getValue(x) + ']');
			log.debug("xPaths=" + xPaths.size() + " " + xPaths);
		}

		FragmentContentHandler child = new FragmentContentHandler(childXPath, xmlReader, this);
		xmlReader.setContentHandler(child);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String value = characters.toString().trim();
		if (value.length() > 0) {
			log.trace(xPath + "='" + characters.toString() + "'");
			xPaths.add(xPath + "='" + characters.toString() + "'");
			log.debug("xPaths=" + xPaths.size() + " " + xPaths);
		}
		xmlReader.setContentHandler(parent);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characters.append(ch, start, length);
	}
}

public class XML2XPath implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(XML2XPath.class);
	InputSource xml;
	FragmentContentHandler fch;

	public XML2XPath(String s) {
		xml = new InputSource(new StringReader(s));
	}

	public static void main(String[] args) throws Exception {
		XML2XPath app = new XML2XPath(args[0]);
		log.debug("app=" + app);
		app.run();
	}

	public List<String> getXPaths() {
		return fch.xPaths;
	}

	@Override
	public void run() {
		log.debug("run==>");
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			fch = new FragmentContentHandler(xr);
			xr.setContentHandler(fch);
			xr.parse(xml);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("", e);
		}
		log.debug("<==run");
	}
}