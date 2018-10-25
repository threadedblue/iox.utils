package iox.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
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
	XPathCallBack callback;
	boolean doingText;
	StringBuilder textContent;

	public XML2DDXPath(String s, XPathCallBack callback) {
		xml = new InputSource(new StringReader(s));
		this.callback = callback;
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
			log.debug("startElement uri=" + uri + " qName=" + qName);
			Integer count = elementNameCount.get(qName);
			if (null == count) {
				count = 1;
			} else {
				count++;
			}
			elementNameCount.put(qName, count);
			String childXPath = xPath + "/" + qName + "[" + count + "]";
			if (isText(qName)) {
				doingText = true;
				textContent = new StringBuilder();
				log.debug(childXPath);
			}
			if(doingText && isReference(qName)) {
				return;
			}
			if (!doingText) {
				log.debug(childXPath);
				int attsLength = atts.getLength();
				for (int x = 0; x < attsLength; x++) {
					log.trace(childXPath + "@" + atts.getQName(x) + "=" + atts.getValue(x));
					callback.process(childXPath + "@" + atts.getQName(x) + "=" + atts.getValue(x));
				}
			}
			FragmentContentHandler child = new FragmentContentHandler(childXPath, xmlReader, this);
			xmlReader.setContentHandler(child);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			log.debug("isText=" + isText(qName));
			if (isText(qName)) {
				log.trace("text-content=" + xPath + "='" + textContent.toString() + "'");
//				callback.process(xPath + "='" + textContent.toString() + "'");
//				textContent = new StringBuilder();
				doingText = false;
				callback.process(xPath);
			} 
			log.debug("doingText=" + doingText);
			if (!doingText){
				String value = characters.toString().trim();
				if (value.length() > 0) {
//					log.trace("content=" + xPath + "='" + characters.toString() + "'");
//					callback.process(xPath + "='" + characters.toString() + "'");
					callback.process(xPath);
				}
			}
			log.debug("endElement qName=" + qName);
			xmlReader.setContentHandler(parent);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			String s = new String(ch);
			if (doingText) {
				s = s.replaceAll("\\n", "");
//				log.trace("characters=" + s);
				s = s.replaceAll("\\<[^\\>]*\\>","");
				s = s.replaceAll("\\s+", " ");
				s = s.trim();
				textContent.append(s);
			} else {
				characters.append(s);
			}
		}
	}

	boolean isText(String qName) {
		return "text".equals(qName);
	}

	boolean isReference(String qName) {
		return "reference".equals(qName);
	}}