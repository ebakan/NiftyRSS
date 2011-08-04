/**
  * InvalidArticleException.java
  * A custom exception to be thrown
  * in the case of an error while
  * constructing an Article.
  * @author Eric Bakan
  */

/**
  * Thrown when an error occurs while
  * consructing an Article
  */
class InvalidArticleException extends Exception {
    /**
      * Constructor
      * Doesn't do much...
      * @param s Error Message String
      */
    public InvalidArticleException(String s) {
	super(s);
    }
}

