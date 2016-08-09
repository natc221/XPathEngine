package xpathengine;

import org.w3c.dom.Document;

public interface XPathEngine {

	/**
	 * Set XPath expressions to be evaluated
	 * @param expressions
	 */
	void setXPaths(String[] expressions);

	/**
	 * Checks whether specified XPath that is set in setXPaths() is valid
	 * @param i
	 * 		index of XPath specified in setXPaths()
	 * @return
	 * 		whether specified XPath is valid
	 */
	boolean isValid(int i);

	/**
	 * Evaluates XPaths set in setXPaths() against the document. In the returning array,
	 * the i'th element is true if the document matches the i'th XPath expression.
	 * @param document
	 * 		DOM root node
	 * @return
	 * 		i'th element is true if document matches the i'th XPath expression, and false otherwise.
	 * 		null if setXPaths() has not been called
	 */
	boolean[] evaluate(Document document);

}
