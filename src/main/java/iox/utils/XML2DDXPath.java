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

public class XML2DDXPath implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(XML2DDXPath.class);
	InputSource xml;
	FragmentContentHandler fch;
	XPathCallBack cb;

	public XML2DDXPath(String s, XPathCallBack cb) {
		xml = new InputSource(new StringReader(s));
		this.cb = cb;
	}

	public static void main(String[] args) throws Exception {
		XML2DDXPath app = new XML2DDXPath(args[0], null);
		log.debug("app=" + app);
		app.run();
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

	class FragmentContentHandler extends DefaultHandler {

		private final Logger log = LoggerFactory.getLogger(FragmentContentHandler.class);

		String xPath = "/";
		XMLReader xmlReader;
		FragmentContentHandler parent;
		StringBuilder characters = new StringBuilder();
		Map<String, Integer> elementNameCount = new HashMap<String, Integer>();

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
				log.trace(childXPath + "@" + atts.getQName(x) + "=" + atts.getValue(x));
				cb.process(childXPath + "@" + atts.getQName(x) + "=" + atts.getValue(x));
			}

			FragmentContentHandler child = new FragmentContentHandler(childXPath, xmlReader, this);
			xmlReader.setContentHandler(child);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String value = characters.toString().trim();
			if (value.length() > 0) {
				log.trace(xPath + "='" + characters.toString() + "'");
				cb.process(xPath + "='" + characters.toString() + "'");
			}
			xmlReader.setContentHandler(parent);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			characters.append(ch, start, length);
		}
	}
}