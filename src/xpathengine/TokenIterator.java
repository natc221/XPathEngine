/**
 * @author Nathaniel Chan
 */
package xpathengine;

/**
 * Wrapper for iterating over an array of Tokens.
 * Public for JUnit testing
 */
public class TokenIterator {
	
	private Token[] tokens;
	private int curr = -1;

	public TokenIterator(Token[] tokens) {
		this.tokens = tokens;
		this.curr = 0;
	}
	
	public boolean hasNext() {
		return hasNext(1);
	}
	
	public boolean hasNext(int offset) {
		if (tokens == null) {
			return false;
		}
		return this.curr + offset < tokens.length
				&& this.curr + offset >= 0;
	}
	
	public boolean hasPrev() {
		return hasPrev(1);
	}
	
	public boolean hasPrev(int offset) {
		if (tokens == null) {
			return false;
		}
		return curr - offset >= 0 
				&& curr - offset < tokens.length;
	}
	
	public boolean hasCurr() {
		return hasNext(0);
	}
	
	public Token curr() {
		if (!hasNext(0)) {
			return null;
		}
		return tokens[curr];
	}
	
	public Token next() {
		return next(1);
	}
	
	public Token next(int offset) {
		if (!hasNext(offset)) {
			return null;
		}
		return tokens[curr + offset];
	}
	
	public Token prev() {
		return prev(1);
	}
	
	public Token prev(int offset) {
		if (!hasPrev(offset)) {
			return null;
		}
		return tokens[curr - offset];
	}
	
	public int getPos() {
		return curr;
	}
	
	public void setPos(int position) {
		curr = position;
	}
	
	public void step() {
		step(1);
	}
	
	public void step(int steps) {
		curr += steps;
	}
	
	public void stepBack()  {
		stepBack(1);
	}
	
	public void stepBack(int steps) {
		curr -= steps;
	}
}
