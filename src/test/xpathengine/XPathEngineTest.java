package test.xpathengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import models.Doc;
import xpathengine.Token;
import xpathengine.Token.Type;
import xpathengine.TokenIterator;
import xpathengine.XPathEngineImpl;
import xpathengine.XPathQuery;
import junit.framework.TestCase;

public class XPathEngineTest extends TestCase {

	private static final String RES_PATH = "resources/";
	
	private String loadFile(String filename) {
		File f = new File(getClass().getResource(RES_PATH + filename).getFile());
        return readFile(f);
	}
	
	private String readFile(File file) {
		StringBuilder result = new StringBuilder();
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				result.append(sc.nextLine()).append("\n");
			}
		} catch (FileNotFoundException e) {
		} finally {
			if (sc != null) {
				sc.close();
			}
		}
		
		return result.toString();
	}

	private Node loadDOM(String filename) {
		return Doc.getDOM(loadFile(filename), true);
	}
	
	public void testMatchOneLevel() {
		Node doc = loadDOM("xml/simple.xml");
		
		String q = "/body";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchOneLevelAttMatch() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[@abc=\"xyz\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}
	
	public void testMatchOneLevelAttMatchFail() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[@abc=\"wrongVal\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertFalse(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchOneLevelTwoAttMatch() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[@abc=\"xyz\"][@att2=\"cis555\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchOneLevelTextMatch() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[text()=\"test value\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}
	
	public void testMatchOneLevelTextMatchFail() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[text()=\"wrong value\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertFalse(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchOneLevelContains() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[contains(text(),\"test\")]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchOneLevelContainsFail() {
		Node doc = loadDOM("xml/single.xml");
		
		String q = "/head[contains(text(),\"notcontains\")]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertFalse(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchSimpleTwoLevels() {
		Node doc = loadDOM("xml/simple.xml");
		
		String q = "/body/test";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchSimpleTwoLevelsFail() {
		Node doc = loadDOM("xml/simple.xml");
		
		String q = "/body/nopath";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertFalse(XPathEngineImpl.matchToken(doc, it));
	}

	public void testMatchTwoLevelsTextMatch() {
		Node doc = loadDOM("xml/simpleMultiple.xml");
		
		String q = "/body/test2[text()=\"match this!\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}
	
	public void testMatchTestStep() {
		Node doc = loadDOM("xml/simpleMultiple.xml");
		
		String q = "/body[test2]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertTrue(XPathEngineImpl.matchToken(doc, it));
	}
	
	public void testMatchTestStepFail() {
		Node doc = loadDOM("xml/simpleMultiple.xml");
		
		String q = "/body[noElem]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertEquals(Type.XPATH, it.curr().getType());
		assertFalse(XPathEngineImpl.matchToken(doc, it));
	}
	
	public void testMatchDeepXML() {
		Node doc = loadDOM("xml/deep.xml");
		
		String[] paths = {
				"/a/b/c",
				"/a/b/c[text()=\"string with quote in c\"]",
				"/a/b[@att=\"123\"]",
				"/a[b]",
				"/d/e[f/foo]",
				"/d/e[f/foo]/f/bar[text()=\"else\"]",
				"/d/e/f[foo[contains(text(),\"some\")]][bar]"
		};
		for (String q : paths) {
			TokenIterator it = getIterator(q);
			assertTrue(XPathEngineImpl.matchToken(doc, it));
		}
	}
	
	private TokenIterator getIterator(String query) {
		Token[] tokens = XPathQuery.getCheckedTokens(query);
		TokenIterator it = new TokenIterator(tokens);
		return it;
	}
	
	public void testMultipleDeepXML() {
		Document doc = (Document) loadDOM("xml/deep.xml");
		
		String[] paths = {
				"/a/b/c",
				"/a/b/c[text()=\"string with quote in c\"]",
				"/a/b[@att=\"123\"]",
				"/a[b]",
				"/d/e[f/foo]",
				"/d/e[f/foo]/f/bar[text()=\"else\"]",
				"/d/e/f[foo[contains(text(),\"some\")]][bar]",
				"/does/not/exist"
		};
		XPathEngineImpl engine = new XPathEngineImpl();
		engine.setXPaths(paths);
		for (int i = 0; i < paths.length; i++) {
			assertTrue(engine.isValid(i));
		}
		assertFalse(engine.isValid(-1));
		assertFalse(engine.isValid(paths.length));
		boolean[] result = engine.evaluate(doc);
		for (int i = 0; i < paths.length; i++) {
			if (i < paths.length - 1) {
				assertTrue(result[i]);
			} else {
				assertFalse(result[i]);
			}
		}
	}
	
}





