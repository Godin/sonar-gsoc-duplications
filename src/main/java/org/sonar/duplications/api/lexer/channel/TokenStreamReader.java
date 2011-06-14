package org.sonar.duplications.api.lexer.channel;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.sonar.duplications.api.codeunit.Token;

/**
 * A token stream (as like character stream) whose source is list of token.
 * This is a modification of {@link StringReader} class
 *
 * @author	Sharif
 */

public class TokenStreamReader extends Reader {

    private Token[] tokens;
    private int length;
    private int next = 0;
    private int mark = 0;

    /**
     * Create a new string reader.
     *
     * @param tokenList the token list providing the token stream.
     */
    public TokenStreamReader(List<Token> tokenList) {
    	tokens = new Token [tokenList.size()];
    	tokenList.toArray(tokens);
    	this.length = tokenList.size();
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
    	if (tokens == null)
    		throw new IOException("Stream closed");
    }

    /**
     * this method cannot be used, use readToken() instead 
     */
    @Deprecated
    public int read() throws IOException {
    	throw new IOException("use method readToken() instead");
    }

    /**
     * Read a single token.
     *
     * @return     The token read, or {@link Token}.EMPTY_TOKEN if the end of the stream has been
     *             reached
     *
     * @exception  IOException  If an I/O error occurs
     */    
	public Token readToken() throws IOException {
		synchronized (lock) {
			ensureOpen();
			if (next >= length)
				return Token.EMPTY_TOKEN;
			return tokens[next++];
		}
	}
    
	/**
     * this method cannot be used, use readToken(Token cbuf[], int off, int len) instead 
     */
	@Deprecated
    public int read(char cbuf[], int off, int len) throws IOException {
    	throw new IOException("use method readToken(Token cbuf[], int off, int len) instead");
    }

    public int readToken(Token cbuf[], int off, int len) throws IOException {
    	synchronized (lock) {
    	    ensureOpen();
                if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return 0;
                }
    	    if (next >= length)
    		return -1;
    	    int n = Math.min(length - next, len);
    	    copyTokens(next, next + n, cbuf, off);
    	    next += n;
    	    return n;
    	}
        }
    
    private void copyTokens(int srcBegin, int srcEnd, Token dst[], int dstBegin) {
        if (srcBegin < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (srcEnd > length) {
            throw new IndexOutOfBoundsException();
        }
        if (srcBegin > srcEnd) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(tokens, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }
    
    /**
     * Skips the specified number of tokens in the stream. Returns
     * the number of tokens that were skipped.
     *
     * <p>The <code>ns</code> parameter may be negative, even though the
     * <code>skip</code> method of the {@link Reader} superclass throws
     * an exception in this case. Negative values of <code>ns</code> cause the
     * stream to skip backwards. Negative return values indicate a skip
     * backwards. It is not possible to skip backwards past the beginning of
     * the string.
     *
     * <p>If the entire string has been read or skipped, then this method has
     * no effect and always returns 0.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public long skip(long ns) throws IOException {
	synchronized (lock) {
            ensureOpen();
            if (next >= length)
                return 0;
            // Bound skip by beginning and end of the source
            long n = Math.min(length - next, ns);
            n = Math.max(-next, n);
            next += n;
            return n;
        }
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input
     *
     * @exception  IOException  If the stream is closed
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
        ensureOpen();
        return true;
        }
    }

    /**
     * Tell whether this stream supports the mark() operation, which it does.
     */
    public boolean markSupported() {
	return true;
    }

    /**
     * Mark the present position in the stream.  Subsequent calls to reset()
     * will reposition the stream to this point.
     *
     * @param  readAheadLimit  Limit on the number of tokens that may be
     *                         read while still preserving the mark.  Because
     *                         the stream's input comes from a string, there
     *                         is no actual limit, so this argument must not
     *                         be negative, but is otherwise ignored.
     *
     * @exception  IllegalArgumentException  If readAheadLimit is < 0
     * @exception  IOException  If an I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
	if (readAheadLimit < 0){
	    throw new IllegalArgumentException("Read-ahead limit < 0");
	}
	synchronized (lock) {
	    ensureOpen();
	    mark = next;
	}
    }

    /**
     * Reset the stream to the most recent mark, or to the beginning of the
     * string if it has never been marked.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void reset() throws IOException {
	synchronized (lock) {
	    ensureOpen();
	    next = mark;
	}
    }

    /**
     * Close the stream.
     */
    public void close() {
    	tokens = null;
    }

}

