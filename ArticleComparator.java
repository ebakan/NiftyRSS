/**
  * ArticleComparator.java
  * Enables easy comparison and sorting of
  * Collections of articles by allowing
  * For comparison of the frequency
  * Of a specific word between articles
  * @author Eric Bakan
  */

import java.util.Comparator;

/**
  * Enables comparison between Articles'
  * word counts for a given query.
  */
public class ArticleComparator implements Comparator<Article> {
    /**
      * Constructor
      * @param word The word the articles
      * will be compared by
      */
    public ArticleComparator(String word) {
	this.word=word;
    }

    /**
      * Compares two articles by a given word
      * @return -1 if query appears in a1 more than
      * a2, 0 if query appears in each the same, and
      * 1 if query appears in a2 more times than
      * in a1
      */
    public int compare(Article a1, Article a2) {
	Integer a1count = a1.GetNumOccurrences(word);
	Integer a2count = a2.GetNumOccurrences(word);
	return -a1count.compareTo(a2count);
    }

    private String word;
}
	    
