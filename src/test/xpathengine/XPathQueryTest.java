package test.xpathengine;

import xpathengine.Token;
import xpathengine.Token.Type;
import xpathengine.TokenIterator;
import xpathengine.XPathQuery;
import junit.framework.TestCase;

public class XPathQueryTest extends TestCase {
	
	public void testInvalidEmptyQuery() {
		String query = "";
		assertFalse(XPathQuery.isValid(query));
	}
	
	public void testInvalidOnlyAxisQuery() {
		String query = "/";
		assertFalse(XPathQuery.isValid(query));
	}
	
	public void testInvalidNoAxisQuery() {
		String query = "test";
		assertFalse(XPathQuery.isValid(query));
	}
	
	public void testInvalidNoAxisPathQuery() {
		String query = "test/to/path";
		assertFalse(XPathQuery.isValid(query));
	}
	
	public void testValidAxisNode() {
		String query = "/test";
		assertTrue(XPathQuery.isValid(query));
	}

	public void testValidAxisPath() {
		String query = "/test/to/node";
		assertTrue(XPathQuery.isValid(query));
	}

	public void testValidTestStep() {
		String query = "/test[something]";
		assertTrue(XPathQuery.isValid(query));
	}

	public void testValidTestStepPath() {
		String query = "/test[something/else]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testValidTestAttrib() {
		String query = "/test[@att=\"123\"]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testValidTestContains() {
		String query = "/test[contains(text(),\"123\")]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testInvalidTestContains2() {
		String query = "/test[contains(text(),\"123)]";
		assertFalse(XPathQuery.isValid(query));
	}
	
	public void testValidTestTextEqual() {
		String query = "/test[text()=\"123\"]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testInvalidTestTextEqual() {
		String query = "/test[text()=\"123]";
		assertFalse(XPathQuery.isValid(query));
	}
	
	public void testValidNestedTest() {
		String query = "/d/e/f[foo[text()=\"something\"]]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testValidMultipleTests() {
		String query = "/d/e/f[@att=\"abc\"][bar]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testValidTextMatchWhitespace() {
		String query = "/d/e/f[  @att =  \"abc\" ][bar ]";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testValidTestThenPath() {
		String query = "/d/e/f[@att=\"abc\"][bar]/ab/c";
		assertTrue(XPathQuery.isValid(query));
	}
	
	public void testValidSpaceBetweenNodename() {
		String q = "/a /b/c";
		assertTrue(XPathQuery.isValid(q));
	}
	
	public void testValidSpaceBetweenNodetest() {
		String q = "/a /b/c [text()=\"abc\"]";
		assertTrue(XPathQuery.isValid(q));
	}
	
	public void testValidIdentifier() {
		String iden = "abc";
		assertTrue(XPathQuery.isValidIdentifier(iden));
	}

	public void testValidIdentifierUnderscoreStart() {
		String iden = "_abc";
		assertTrue(XPathQuery.isValidIdentifier(iden));
	}
	
	public void testValidIdentifierXmlStart() {
		String iden = "xml_abc";
		assertFalse(XPathQuery.isValidIdentifier(iden));
	}

	public void testValidIdentifierXmlStart2() {
		String iden = "xMLabc";
		assertFalse(XPathQuery.isValidIdentifier(iden));
	}
	
	public void testValidIdentifierComma() {
		String iden = "test,asd";
		assertFalse(XPathQuery.isValidIdentifier(iden));
	}
	
	public void testValidIdentifierParens() {
		String iden = "test()=\"asd\"";
		assertFalse(XPathQuery.isValidIdentifier(iden));
	}
	
	public void testCrazyXPath() {
		String q = "/a/b[foo[text()=\"#$(/][]\"]][bar]/hi[@asdf=\"#$(&[]\"][this][is][crazy]";
		assertTrue(XPathQuery.isValid(q));
	}
	
	
	public void testGetTokensSingleNode() {
		String q = "/abc";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		assertTrue(tokens != null);
		assertEquals(3, tokens.length);
		
		Token head = new Token(Type.XPATH, "");
		Token t1 = new Token(Type.AXIS, "/");
		Token t2 = new Token(Type.NODENAME, "abc");
		assertEquals(head, tokens[0]);
		assertEquals(t1, tokens[1]);
		assertEquals(t2, tokens[2]);
	}
	
	public void testGetTokensMultipleNodes() {
		String q = "/abc/def/test";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		assertTrue(tokens != null);
		assertEquals(7, tokens.length);
		
		Token head = new Token(Type.XPATH, "");
		Token t1 = new Token(Type.AXIS, "/");
		Token t2 = new Token(Type.NODENAME, "abc");
		Token t3 = new Token(Type.NODENAME, "def");
		Token t4 = new Token(Type.NODENAME, "test");
		assertEquals(head, tokens[0]);
		assertEquals(t1, tokens[1]);
		assertEquals(t2, tokens[2]);
		assertEquals(t1, tokens[3]);
		assertEquals(t3, tokens[4]);
		assertEquals(t1, tokens[5]);
		assertEquals(t4, tokens[6]);
	}
	
	public void testGetTokensTest() {
		String q = "/abc[@att=\"test\"]";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		assertTrue(tokens != null);
		assertEquals(4, tokens.length);
		
		Token head = new Token(Type.XPATH, "");
		Token t1 = new Token(Type.AXIS, "/");
		Token t2 = new Token(Type.NODENAME, "abc");
		Token t3 = new Token(Type.TEST, "@att=\"test\"");
		assertEquals(head, tokens[0]);
		assertEquals(t1, tokens[1]);
		assertEquals(t2, tokens[2]);
		assertEquals(t3, tokens[3]);
	}
	
	public void testIterator() {
		String q = "/abc";
		Token[] tokens = XPathQuery.getCheckedTokens(q);
		TokenIterator it = new TokenIterator(tokens);
		assertTrue(it.hasCurr());
		assertTrue(it.hasNext());
		assertFalse(it.hasPrev());
		assertEquals(tokens[0], it.curr());
		assertEquals(tokens[1], it.next());
		it.step();
		assertEquals(tokens[1], it.curr());
		it.stepBack();
		assertEquals(tokens[0], it.curr());
		it.step(3);
		assertNull(it.curr());
		assertEquals(tokens[2], it.prev());
		assertEquals(tokens[1], it.prev(2));
	}
	
	public void test() {
//		"/test[contains(text(),\"123\")]"
		String q = "/rss[contains(text(), \"NYT\")]";
		assertTrue(XPathQuery.isValid(q));
	}
	
	
}







