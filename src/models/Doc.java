package models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

/**
 * Represents document retrieved from a URL, which is the primary key
 * of the document
 */
public class Doc {

	public static final String HASH_ALGO = "SHA-256";
	public static enum DocType {
		HTML, XML, RSS, UNKNOWN
	}

	private String docId; 
	private String document;
	private DocType docType;

	protected Doc() {}

	public Doc(String document, DocType type, String url) {
		this.document = document;
		this.docId = url;
		this.docType = type;
	}

	public String getId() {
		return docId;
	}
	
	public String getUrl() {
		return docId;
	}
	
	public DocType getDocType() {
		return this.docType;
	}

	public String getDocumentString() {
		return document;
	}
	
	// removes XML processing instructions before returning the document string
	public String getDocumentStringNoInstruction() {
		String regex = "<\\?xml.*\\?>";
		return document.replaceAll(regex, "").trim();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Doc)) {
			return false;
		}
		Doc other = (Doc) o;
		return this.docId.equals(other.docId);
	}
	
	public static String getDocId(Document document) {
		return getDocHash(document);
	}

	/**
	 * @param doc
	 * 		String representation of the HTML or XML document
	 * @param isXML
	 * 		Whether String representation is in XML format
	 * @return
	 * 		DOM root node representing the document
	 */
	public static Document getDOM(String doc, boolean isXML) {
		InputStream docStream = new ByteArrayInputStream(doc.getBytes());
		OutputStream parsedStream = new ByteArrayOutputStream();
		Tidy tidy = new Tidy();
		tidy.setXmlTags(isXML);
		tidy.setTidyMark(false); // do not add Tidy headers
		tidy.setQuiet(true);
		tidy.setShowErrors(0);
		tidy.setShowWarnings(false);
		return tidy.parseDOM(docStream, parsedStream);
	}
	
	
	public static Doc.DocType getDocType(String mimeType) {
		if (mimeType == null) {
			return DocType.UNKNOWN;
		}
		String[] split = mimeType.split(";");
		if (split[0].equals("text/html")) {
			return DocType.HTML;
		} else if (split[0].equals("text/xml")
					|| split[0].equals("application/xml")
					|| split[0].endsWith("+xml")) {
			return DocType.XML;
		} else {
			return DocType.UNKNOWN;
		}
	}
	
	public static String getDocHash(Document document) {
		try {
			String docString = getDocString(document);
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
			byte[] hash = digest.digest(docString.getBytes(StandardCharsets.UTF_8));
			return new String(hash);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Invalid hash algorithm");
			return null;
		}
	}

	public static String getDocString(Document document) {
		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}





