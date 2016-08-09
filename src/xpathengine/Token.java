/**
 * @author Nathaniel Chan
 */
package xpathengine;

/**
 * Represents a token in the XPath query
 */
public class Token {
	
	public enum Type {
		XPATH, AXIS, TEST, NODENAME
	}
	
	enum TestType {
		STEP, TEXT, CONTAINS, ATTNAME
	}

	Type type;
	String val;
	
	public Token(Type type, String val) {
		this.type = type;
		this.val = val;
	}
	
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return type + ": " + val;
	}
	
	/**
	 * Tokens are equal if the type and the embedded value are the same
	 */
	@Override
	public boolean equals(Object o) {
		if (!o.getClass().equals(this.getClass())) {
			return false;
		}
		Token other = (Token) o;
		return this.type == other.type && this.val.equals(other.val);
	}
	
}
