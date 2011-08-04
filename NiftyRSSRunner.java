/**
  * NiftyRSSRunner.java
  * Demonstrates the capabilities
  * of the NiftyRss Class
  * @author Eric Bakan
  */

import java.util.Scanner;
import java.util.LinkedList;

/**
  * Demonstrates the capabilities of the
  * NiftyRSS class. First indexes a
  * list of RSS feeds, then allows for
  * the searching of single-word queries
  * within the articles, returning the
  * appropriate sorted results.
  */
public class NiftyRSSRunner {
    /**
      * Main method
      * Accepts the list of newline-separated
      * feed URLs as a command-line parameter
      * and then constructs a database of articles.
      * The user is then allowed to query this
      * database with single-word queries
      */
    public static void main(String[] args) {
	//The file name should be the first
	//command-line parameter
	String rssFileName=null;
	try {
	    rssFileName = args[0];
	}
	catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("No input file. Please give the name of a file containing a list of RSS feed URLs.\n");
	    System.exit(1);
	}
	int numThreads=0;
	try {
	    numThreads = Integer.parseInt(args[1]);
	    if(numThreads<=0)
		System.out.println("Max number of threads is not positive. Using default of infinite");
	    else
		System.out.printf("Using %d max number of threads\n",numThreads);
	}
	catch (NumberFormatException e) {
	    System.out.println("Invalid number of max threads entered. Using default of infinite.");
	}
	catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("No max number of threads entered. Using default of infinite.");
	}

	if(rssFileName==null)
	    System.exit(1);

	Scanner sc = new Scanner(System.in);

	System.out.println("Welcome to the Nifty RSS Searcher!");
	System.out.println("Indexing Article Database...");

	NiftyRSS nifty = new NiftyRSS(rssFileName, numThreads);

	int numArticles=nifty.GetNumArticles();
	if(numArticles==1)
	    System.out.printf("Article Database Complete. Currently indexing %d article.\n",nifty.GetNumArticles());
	else
	    System.out.printf("Article Database Complete. Currently indexing %d articles.\n",nifty.GetNumArticles());

	String query="";
	while(true) {
	    //get user query
	    System.out.print("Please enter single search term (or blank to exit): ");
	    query=sc.nextLine();

	    //if the entry is blank, exit
	    if(query.length()<1)
		break;

	    //sanitize the query
	    query=sanitizeQuery(query);
	    System.out.printf("Actual query: %s\n",query);

	    //get the articles for the query
	    LinkedList<Article> articles = nifty.GetArticles(query);

	    int numResults=articles.size();
	    if(numResults==1)
		System.out.printf("Search returned %d result\n",articles.size());
	    else
		System.out.printf("Search returned %d results\n",articles.size());

	    //limit the number of displayed results
	    if(articles.size()>10)
		System.out.printf("Only the first 10 results will be displayed\n");

	    //display results
	    int rank=1;
	    for(Article a : articles) {
		System.out.printf("%d %s, %d hits\n%s\n%s\n\n",
			rank++,a.GetTitle(),a.GetNumOccurrences(query),
			a.GetDescription(),
			a.GetLink());
		if(rank>10)
		    break;
	    }
	}
	System.out.println("Thank you for using the Nifty RSS Searcher!");

    }
    /**
      * Trims whitespace and eliminates case,
      * then attempts to find the first "word"
      * in the query. A loop is used here in case
      * there are invalid characters at the beginning
      * of the query, which would result in one or more
      * empty strings at the beginning of the array
      */
    public static String sanitizeQuery(String query) {
	final String invalidChars="[^a-zA-Z0-9\\s]";
	String[] dividedQuery=query.trim().toLowerCase().split(invalidChars);
	for(int i=0;i<dividedQuery.length;i++) {
	    if(dividedQuery[i].length()>0) {
		query=dividedQuery[i];
		break;
	    }
	}
	return query;
    }

}

