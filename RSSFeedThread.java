/**
  * RSS Feed Thread
  * Takes the URL of an RSS feed and
  * spawns new RSSArticleThreads to create
  * articles for each feed entry
  */

import java.io.IOException;

import java.lang.Runnable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;

import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class RSSFeedThread implements Runnable {
    public RSSFeedThread(
	    ExecutorService executor,
	    URL feedURL,
	    LinkedList<Article> outList,
	    ReentrantLock listLock,
	    CountDownLatch feedLatch,
	    AtomicInteger articleCount,
	    ReentrantLock printLock) {
	this.executor=executor;
	this.feedURL=feedURL;
	this.outList=outList;
	this.listLock=listLock;
	this.feedLatch=feedLatch;
	this.articleCount=articleCount;
	this.printLock=printLock;
    }

    public void run() {
	Element[] elements = getElements(feedURL);
	//an error occurred while parsing the url
	if(elements==null) {
	    feedLatch.countDown();
	    return;
	}
	CountDownLatch articleLatch = new CountDownLatch(elements.length);
	Runnable[] threads = new Runnable[elements.length];
	for(int i=0;i<elements.length;i++) {
	    threads[i]=new RSSArticleThread(
			elements[i],
			outList,
			listLock,
			articleLatch,
			articleCount,
			printLock);
	    executor.execute(threads[i]);
	}
	try {
	    articleLatch.await();
	}
	catch (InterruptedException e) {}
	//if we're exiting, make sure to
	//count down the latch
	finally {
	    feedLatch.countDown();
	}
    }

    private Element[] getElements(URL url) {
	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder = null;
	Document doc = null;
	try {
	    docBuilder = docBuilderFactory.newDocumentBuilder();
	}
	//if the parser is improperly configured for some reason
	//just skip the url and return null
	catch (ParserConfigurationException e) {
	    System.out.printf("Parser not properly configured. Skipping url %s\n",url.toString());
	    return null;
	}

	try {
	    doc = docBuilder.parse(url.toString());
	}
	//if the url cannot be parsed, again just 
	//skip the url and return null
	catch (SAXException e) {
	    System.out.printf("URL %s cannot be parsed. Skipping URL.\n",url.toString());
	    return null;
	}
	catch (IOException e) {
	    System.out.printf("URL %s cannot be read. Skipping URL.\n",url.toString());
	    return null;
	}
	Element rss=doc.getDocumentElement();
	Element channel=(Element)rss.getElementsByTagName("channel").item(0);
	NodeList items=channel.getElementsByTagName("item");
	Element[] elements = new Element[items.getLength()];
	for(int i=0;i<items.getLength();i++) {
	    elements[i]=(Element)items.item(i);
	}
	return elements;
    }

    private ExecutorService executor;
    private URL feedURL;
    private LinkedList<Article> outList;
    private ReentrantLock listLock;
    private CountDownLatch feedLatch;
    private AtomicInteger articleCount;
    private ReentrantLock printLock;

}

