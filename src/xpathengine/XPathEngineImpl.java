/**
 * @author Nathaniel Chan
 */
package xpathengine;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xpathengine.Token.TestType;
import xpathengine.Token.Type;

public class XPathEngineImpl implements XPathEngine {

	private String[] xpaths = null;

	public XPathEngineImpl() {}

	public void setXPaths(String[] s) {
		this.xpaths = s;
	}

	public boolean isValid(int i) {
		if (this.xpaths == null || i >= xpaths.length || i < 0) {
			return false;
		}
		return XPathQuery.isValid(xpaths[i]);
	}

	public boolean[] evaluate(Document d) {
		if (xpaths == null) {
			return new boolean[0];
		}
		// evaluate document for each XPath specified
		boolean[] result = new boolean[xpaths.length];
		for (int i = 0; i < xpaths.length; i++) {
			String query = xpaths[i];
			if (isValid(i)) {
				// tokenize XPath query
				Token[] tokens = XPathQuery.getCheckedTokens(query);
				result[i] = checkQueryMatch(d, tokens);				
			} else {
				result[i] = false;
			}
		}
		return result; 
	}

	/**
	 * Checks whether a DOM document matches a tokenized XPath query.
	 * Method invokes recursive descent algorithm.
	 */
	public static boolean checkQueryMatch(Document d, Token[] tokens) {
		TokenIterator it = new TokenIterator(tokens);
		return matchToken(d, it);
	}

	/**
	 * Step in recursive descent parser
	 * @param n
	 * 		current node in DOM in consideration
	 * @param it
	 * 		tokens from XPath that are being evaluated. Method is responsible
	 * 		for setting the pointer in the iterator to the appropriate position
	 * 		for further parsing
	 * @return
	 * 		whether current token matches the given node
	 */
	public static boolean matchToken(Node n, TokenIterator it) {
		if (!it.hasCurr()) {
			return true;
		}
		Token curr = it.curr();
		switch (curr.type) {
		
		// XPath -> axis step
		case XPATH:
			return matchAxisStep(n, it);
		
		// axis -> /
		case AXIS:
			it.step();
			return curr.val.equals("/");
			
		// step -> nodename([test])*(axis step)?
		case NODENAME:
			// check if current node equals node name required
			String nodeName = n.getNodeName();
			boolean equals = curr.val.equals(nodeName);
			
			// if not equal then further checking is not necessary
			if (!equals) {
				return false;
			}

			// increment pointer to token after nodename
			it.step();
			Token afterName = it.curr();
			// perform tests (if they exist) until no more tests are present
			while (afterName != null && afterName.type != Type.AXIS) {
				if (afterName.type != Type.TEST) {
					break;
				}
				TestType tt = XPathQuery.getTestType(afterName);
				switch (tt) {
				/*
				 * test -> text() = "..."
				 * test -> contains(text(), "...")
				 * test -> @attname = "..."
				 */
				case ATTNAME:
				case CONTAINS:
				case TEXT:
					if (!matchNonStepTest(n, afterName.val, tt)) {
						return false;
					}
					break;
					
				// test -> step
				case STEP:
					// treat step within test as an XPath of its own
					Token[] testTokens = 
						XPathQuery.getAllTokens(afterName.val);
					
					TokenIterator testIt = new TokenIterator(testTokens);
					if (!matchStep(n, testIt)) {
						return false;
					}
					break;
				}
				// move pointer to next token
				it.step();
				afterName = it.curr();
			}
			
			// end of query
			if (afterName == null) {
				return true;
			// match path for lower levels in DOM tree
			} else {
				it.stepBack();
				return matchAxisStep(n, it);
			}
		case TEST:
			// test should be considered directly after nodename
			return false;
		default:
			return false;
		}
	}

	/**
	 * Performs test for tests that are a step
	 */
	private static boolean matchNonStepTest(Node n, String val, 
			TestType testType) {
		switch (testType) {
		case ATTNAME: {
			String[] eqSplit = val.split("=");
			String attName = eqSplit[0].replace("@", "");
			String expect = eqSplit[1];
			expect = expect.substring(1, expect.length() - 1);

			NamedNodeMap attribs = n.getAttributes();
			Node valNode = attribs.getNamedItem(attName);
			// attribute does not exist
			if (valNode == null) {
				return false;
			}
			String attVal = valNode.getNodeValue();
			return attVal.equals(expect);
		}
		case CONTAINS: {
			String test = val.replace("contains", "");
			test = test.substring(1, test.length() - 1);
			if (test.length() > 0) {
				String[] commaSplit = test.split(",", 2);
				String expected = commaSplit[1].trim();
				expected = expected.substring(1, expected.length() - 1);
				
				String nodeText = getTextVal(n);
				// text for current node does not exist
				if (nodeText == null) {
					return false;
				} else {
					return nodeText.contains(expected);
				}
			}
			return false;
		}
		case TEXT: {
			String[] equalSplit = val.split("=");
			String expectText = equalSplit[1];
			expectText = expectText.substring(1, expectText.length() - 1);
			String nodeText = getTextVal(n);
			return expectText.equals(nodeText);
		}
		default:
			return false;
		}
	}
	
	/**
	 * Retrieves text value for a DOM node
	 * @param n
	 * @return
	 * 		null if text does not exist
	 */
	private static String getTextVal(Node n) {
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.TEXT_NODE) {
				return child.getNodeValue();
			}
		}
		return null;
	}

	/**
	 * Invokes matching a step for children of a given node
	 */
	private static boolean matchStep(Node n, TokenIterator it) {
		int currPos = it.getPos();
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			it.setPos(currPos);
			boolean step = matchToken(child, it);
			if (step) {
				return step;
			}
		}
		// none of the children match the query
		return false;
	}

	/**
	 * Invokes matching an axis for the next token, then matching
	 * a step for the children of the given node
	 */
	private static boolean matchAxisStep(Node n, TokenIterator it) {
		int currPos = it.getPos();
		it.step();
		boolean axis = matchToken(n, it);
		// if axis does not match, there is no need to check further
		if (!axis) {
			return false;
		}
		NodeList children = n.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			it.setPos(currPos); //reset pointer
			it.step(2); //set to step token
			boolean step = matchToken(child, it);
			if (step) {
				return axis && step;
			}
		}
		// none of the children match the query
		return false;
	}
}





