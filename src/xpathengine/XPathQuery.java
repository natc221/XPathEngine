/**
 * @author Nathaniel Chan
 */
package xpathengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import xpathengine.Token.Type;
import xpathengine.Token.TestType;

public class XPathQuery {
	
	public static final String TEXT = "text()";
	public static final String CONTAINS = "contains";
	
	private String originalQuery;
	private Token[] tokens;
	public XPathQuery(String query) {
		this.originalQuery = query;
		this.tokens = getCheckedTokens(query);
		if (this.tokens == null) {
			throw new IllegalArgumentException("invalid xpath");
		}
	}
	
	public Token[] getTokens() {
		return this.tokens;
	}
	
	public String getOriginalQuery() {
		return this.originalQuery;
	}
	
	/**
	 * Convenience method for printing all tokens
	 * @param tokens
	 */
	public static void printTokens(Token[] tokens) {
		for (Token t : tokens) {
			System.out.println(t);
		}
	}
	
	/**
	 * Checks whether a given XPath is valid
	 * @param query
	 * @return
	 */
	public static boolean isValid(String query) {
		try {
			Token[] tokens = getCheckedTokens(query);
			return tokens != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	/**
	 * Parses given XPath by tokenization, and checks whether the XPath
	 * is valid.
	 * @param query
	 * 		XPath query
	 * @return
	 * 		null if query is invalid
	 */
	public static Token[] getCheckedTokens(String query) {
		if (query == null) {
			return null;
		}
		try {
			Token[] tokens = getAllTokens(query);
			if (isValidSequence(tokens)) {
				// append XPATH token before all tokens
				Token[] xpath = new Token[tokens.length + 1];
				xpath[0] = new Token(Type.XPATH, "");
				for (int i = 0; i < tokens.length; i++) {
					xpath[i+1] = tokens[i];
				}
				return xpath;
			} else {
				return null;
			}
		// syntax error reached during parsing
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Checks whether a given sequence of XPath tokens are valid.
	 * Does not include check for XPATH token check
	 * @param tokens
	 * @return
	 */
	private static boolean isValidSequence(Token[] tokens) {
		// need at least: axis step
		if (tokens.length < 2) {
			return false;
		}
		// iterate through tokens and check validity of each
		for (int i = 0; i < tokens.length; i++) {
			Token curr = tokens[i];
			// first token must be an axis
			if (i == 0) {
				if (curr.type != Type.AXIS) {
					return false;
				// second token must be nodename
				} else if (tokens[i+1].type != Type.NODENAME) {
					return false;
				}
			}
			switch (curr.type) {
			// nodename must follow axis
			case AXIS:
				if (i + 1 >= tokens.length || tokens[i+1].type != Type.NODENAME) {
					return false;
				}
				break;
			/*
			 * there are no restrictions on the nodename. 
			 * Identifier naming check already performed during parsing
			 */
			case NODENAME:
				break;
				
			// test must come after nodename or test, and be a valid test
			case TEST:
				Token prev = tokens[i-1];
				if (prev.type != Type.NODENAME && prev.type != Type.TEST) {
					return false;
				}
				if (!isValidTest(curr)) {
					return false;
				}
				break;
				
			// there should be no other type ot token
			default:
				return false;
			}
		}
		// all tokens have passed validity checks
		return true;
	}
	
	/**
	 * Checks whether a token is a test token, and is a valid test token
	 * @param token
	 * @return
	 */
	private static boolean isValidTest(Token token) {
		TestType tt = getTestType(token);
		return tt != null;
	}
	
	/**
	 * Gets the type of test used by the test token, if a valid test token
	 * @param token
	 * @return
	 * 		null if invalid test token
	 */
	public static TestType getTestType(Token token) {
		if (token.type != Type.TEST) {
			return null;
		}
		String val = token.val;
		
		// text() = "..."
		String[] equalSplit = val.split("=", 2);
		if (equalSplit.length == 2) {
			if (equalSplit[0].equals(TEXT) 
					&& isStartEndChar(equalSplit[1], '"')) {
				return TestType.TEXT;
			}
		}
		
		// contains(text(), "...")
		if (val.contains(CONTAINS)) {
			String test = val.replace(CONTAINS, "");
			if (isStartEndParens(test)) {
				// remove parenthesis
				test = test.substring(1, test.length() - 1);
				if (test.length() > 0) {
					String[] commaSplit = test.split(",", 2);
					if (commaSplit.length == 2) {
						// first component is "text()" and second is quoted
						if (commaSplit[0].equals(TEXT) 
								&& isStartEndChar(commaSplit[1].trim(), '"')) {
							return TestType.CONTAINS;
						}
					}
				}
			}
		}
		
		// @attname = "..."
		if (val.charAt(0) == '@') {
			equalSplit = val.split("=", 2);
			if (equalSplit.length == 2) {
				// attribute value must be quoted
				if (isStartEndChar(equalSplit[1], '"')) {
					return TestType.ATTNAME;
				}
			}
		}

		// check if is step
		try {
			// treat step like an XPath, and check whether it is a valid XPath
			String test = "/" + token.val;
			if (isValid(test)) {
				return TestType.STEP;
			}
		// syntax error reached when treating step like an XPath
		} catch (IllegalArgumentException e) {
			return null;
		}
					
		// does not match grammar
		return null;
	}
	
	private static boolean isStartEndChar(String s, char c) {
		if (s == null || s.length() < 2) {
			return false;
		}
		return s.charAt(0) == c && s.charAt(s.length() - 1) == c;
	}
	
	private static boolean isStartEndParens(String s) {
		if (s == null || s.length() < 2) {
			return false;
		}
		return s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')';
	}
	
	/**
	 * Checks whether a string is a valid XML identifier.
	 * Rules according to: http://www.w3schools.com/xml/xml_elements.asp
	 * @param s
	 * @return
	 */
	public static boolean isValidIdentifier(String s) {
		if (s == null) {
			return false;
		}
		// cannot start with "xml"
		String xml = "xml";
		if (s.length() >= 3 && s.substring(0, 3).equalsIgnoreCase(xml)) {
			return false;
		}
		String regex = "[a-zA-Z|_][a-zA-Z|0-9|-|_|.]*";
		return s.matches(regex);
	}
	
	/**
	 * Tokenizes XPath query
	 * @param query
	 * @return
	 */
	static Token[] getAllTokens(String query) {
		StringBuffer buffer = new StringBuffer(query);
		List<Token> tokens = new ArrayList<>();
		Token next = null;
		while ((next = getNextToken(buffer)) != null) {
			tokens.add(next);						
		}
		Token[] tokenArr = new Token[tokens.size()];
		return tokens.toArray(tokenArr);
	}
	
	/**
	 * Gets next token in XPath query, and removes tokenized component
	 * from the string buffer
	 * @param query
	 * 		buffer representing the remaining XPath to be tokenized
	 * @return
	 * 		next valid Token parsed, null if no next Token
	 */
	private static Token getNextToken(StringBuffer query) {
		if (query.length() == 0) {
			return null;
		}
		// axis
		if (query.charAt(0) == '/') {
			// remove from buffersssss
			query.deleteCharAt(0);
			return new Token(Type.AXIS, "/");
		}
		
		// test
		if (query.charAt(0) == '[') {
			int closeIdx = getFirstCloseIdx(query.toString());
			if (closeIdx == -1) {
				throw new IllegalArgumentException("no square close bracket found");
			}
			// get string within square brackets
			String testString = query.substring(1, closeIdx).trim();
			// trim test string of whitespaces
			int firstQuoteIdx = testString.indexOf('"');
			int equalsIdx = testString.indexOf('=');
			if (equalsIdx != -1 && equalsIdx < firstQuoteIdx) {
				String[] split = testString.split("=", 2);
				testString = split[0].trim() + "=" + split[1].trim();
			}
			// remove from buffer
			query.delete(0, closeIdx + 1);
			// if test string is non-empty
			if (testString.length() > 0) {
				Token token = new Token(Type.TEST, testString);
				return token;
			}
		}
		
		// nodename
		// get nodename between current position and first square bracket or axis
		int squareIdx = query.indexOf("[");
		int axisIdx = query.indexOf("/");
		int idx = -1;
		if (squareIdx > 0 && axisIdx > 0) {
			idx = Math.min(squareIdx, axisIdx);
		} else {
			idx = Math.max(squareIdx, axisIdx);
		}
		String nodename = "";
		if (idx == -1) {
			nodename = query.toString();
			query.delete(0, query.length());
		} else {
			nodename = query.substring(0, idx);
			query.delete(0, idx);
		}
		nodename = nodename.trim();
		if (isValidIdentifier(nodename)) {
			return new Token(Type.NODENAME, nodename);			
		} else {
			throw new IllegalArgumentException("invalid identifier: " + nodename);
		}
	}
	
	private static int getFirstCloseIdx(String s) {
		Stack<Character> brackets = new Stack<>();
		boolean inQuotes = false;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '[' && !inQuotes) {
				brackets.push(c);
			} else if (c == ']' && !inQuotes) {
				if (brackets.peek() == '[') {
					brackets.pop();
					if (brackets.size() == 0) {
						return i;
					}
				} else {
					throw new IllegalArgumentException("close bracket has no start");
				}
			} else if (c == '"') {
				inQuotes = !inQuotes;
			}
		}
		return -1;
	}

}
