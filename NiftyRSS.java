/**
  * Nifty Rss
  * Parses a list of rss files into a database
  * and returns relative articles for single
  * search term queries
  * @author Eric Bakan
  */

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.locks.ReentrantLock;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

/**
  * Reads a list of RSS feeds from a
  * file and constructs a LinkedList of
  * Articles from them. Then allows for
  * access to these articles, in particular
  * advanced searching for single-word
  * queries. The class uses multithreading
  * to allow the data for each feed URL
  * and each article URL to be pulle down
  * in parallel, mitigating the slow speed
  * of trying to access numerous URLs
  * in series.
  */
public class NiftyRSS {
    /**
      * Constructor
      * @param rssFile The name of the file
      * containing the list of RSS feed URLs
      */
    public NiftyRSS(String rssFile, int numThreads) {
	articleList = getArticles(rssFile, numThreads);
    }

    /**
      * Gets the number of articles in
      * the NiftyRss
      * @return Number of Articles
      */
    public int GetNumArticles()  {
	return articleList==null?0:articleList.size();
    }

    /**
      * Gets all the articles which contain
      * a given query, sorted in descending order
      * by the number of occurrences in each article
      * @param query The sinele-word query to search for
      * @return A sorted LinkedList of the Articles which
      * contain the query
      */
    public LinkedList<Article> GetArticles(String query) {
	if(articleList==null)
	    return null;
	LinkedList<Article> articles = new LinkedList<Article>();
	for(Article a : articleList)
	    if(a.GetNumOccurrences(query)>0)
		articles.add(a);
	SortArticleList(articles, query);
	return articles;
    }

    /**
      * A helper method for #GetArticles(String query)
      * Sorts a given LinkedList of Articles in descending order
      * by the number of times query appears in each of them
      * @param query The single-word query to sort by
      */
    private void SortArticleList(LinkedList<Article> articles, String query) {
	ArticleComparator comparator = new ArticleComparator(query);
	Collections.sort(articles,comparator);
	
    }

    /**
      * Helper method which returns the BufferedReader for
      * a given file
      * @param fileName the name of the file to access
      * @return the BufferedReader for the file, otherwise
      * null if an error occurs while opening the file
      */
    private BufferedReader getBufferedReader(String fileName) {
	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(fileName));
	}
	catch (FileNotFoundException e) {
	    System.out.printf("File %s not found\n",fileName);
	    return null;
	}

	//check to ensure the file is read
	try {
	    if(!reader.ready()) {
		System.out.printf("Invalid file. Please check that file %s exists and is not empty\n",fileName);
		return null;
	    }
	}
	//Error while checking the status of the file
	catch (IOException e) {
	    System.out.printf("File %s is corrupted. Program exiting.\n",fileName);
	    return null;
	}

	return reader;
    }

    /**
      * The heart of the class
      * Given the name of a file containing a list of
      * RSS feed URLs, the method spawns a new
      * RSSFeedThread for each URL, which spawns
      * another thread for each article URL, allowing
      * article data to be downloaded much more quickly
      * than were the articles to be downloaded
      * sequentially. Currently there is no limit on
      * the number of threads this approach will spawn,
      * which could lead to system slowdown. However,
      * it is extremely easy to use a ThreadPool to
      * limit the number of active threads. However,
      * the author did not see any reason to do such
      * as the program ran quickly enough on his system.
      * Were this program to be extended to catalog
      * more than a couple hundred articles, the author
      * STRONGLY reccomends using a ThreadPool, otherwise
      * the JVM is likely to crash or experience
      * significant slowdown.
      * @param fileName The location of the list of
      * RSS feeds to catalog
      * @return a LinkedList of all the parsed Articles
      * contained within the RSS feeds, without
      * duplicates
      */ 
    private LinkedList<Article> getArticles(String fileName, int numThreads) {
	LinkedList<Article> articles = new LinkedList<Article>();
	ReentrantLock listLock = new ReentrantLock();
	ReentrantLock printLock = new ReentrantLock();
	BufferedReader reader = getBufferedReader(fileName);
	LinkedList<URL> urls = new LinkedList<URL>();
	AtomicInteger articleCount = new AtomicInteger();

	if(reader==null)
	    return null;

	//Get all the RSS feed urls
	String feed=null;
	try {
	    while((feed=reader.readLine()) != null) {
		System.out.printf("Feed: %s\n",feed);
		try {
		    urls.add(new URL(feed));
		}
		catch (MalformedURLException e) {
		    System.out.printf("Bad URL: %s\n",feed);
		}

	    }
	}
	//Error while reading from the file
	catch (IOException e) {
	    System.out.printf("File %s is corrupted.\n",fileName);
	    return null;
	}

	//close the BufferedReader for good measure
	try {
	    reader.close();
	}
	catch (IOException e) {
	    System.out.printf("Cannot close file %s.\n",fileName);
	    return null;
	}

	//begin spawning the threads.

	//use a CountDownLatch to tell when all the threads
	//are completed

	//One RSSFeedThread is spawned per feed. Then each
	//RSSFeedThread will spawn a new RSSArticleThread
	//for each article within its feed

	//This current implementation uses a ThreadPool to
	//limit the number of threads used if requested by
	//the user, otherwise allows for infinite number
	ExecutorService threadPool;
	if(numThreads<=0)
	    threadPool = Executors.newCachedThreadPool();
	else
	    threadPool = Executors.newFixedThreadPool(numThreads);

	//compute this once because this operation is O(n)
	int numURLs=urls.size();
	CountDownLatch feedLatch = new CountDownLatch(numURLs);
	Runnable[] feedThreads = new Runnable[numURLs];

	//uses an integer to access the array elements
	//and an Iterator for the LinkedList to reduce
	//the time complexity
	Iterator<URL> it = urls.iterator();
	int i=0;
	while(it.hasNext() && i<numURLs) {
	    feedThreads[i]=new RSSFeedThread(
			threadPool,
			it.next(),
			articles,
			listLock,
			feedLatch,
			articleCount,
			printLock);

	    threadPool.execute(feedThreads[i]);
	    i++;
	}
	try {
	    feedLatch.await();
	}
	//if we're interrupted, kill everything
	//because we don't care about bad data
	catch (InterruptedException e) {
	    threadPool.shutdownNow();
	    return null;
	}
	return articles;
    }

    private LinkedList<Article> articleList;

}

