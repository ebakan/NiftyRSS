/**
  * RSS Article Thread
  * Allows for the creation of
  * multiple articles in parallel
  */

import java.lang.Runnable;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.locks.ReentrantLock;

import java.util.LinkedList;

import org.w3c.dom.Element;

public class RSSArticleThread implements Runnable {
    public RSSArticleThread(Element xmlEntry,
	    LinkedList<Article> outList,
	    ReentrantLock listLock,
	    CountDownLatch articleLatch,
	    AtomicInteger articleCount,
	    ReentrantLock printLock) {
	this.xmlEntry=xmlEntry;
	this.outList=outList;
	this.listLock=listLock;
	this.articleLatch=articleLatch;
	this.articleCount=articleCount;
	this.printLock=printLock;
    }

    public void run() {
	try {
	    String link=xmlEntry.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
	    //make sure all the articles print in proper order
	    //by ensuring the article count increment and
	    //printf happen simultaneously
	    //NOTE: this WILL cause some slowdown at the start
	    //of each thread (not deadlock though) but is worth
	    //the cost to have correct output
	    try {
		printLock.lock();
		int numArticles=articleCount.incrementAndGet();
		if(numArticles==1)
		    System.out.printf("New Article: %s\n%d Article Left\n\n",link,numArticles);
		else
		    System.out.printf("New Article: %s\n%d Articles Left\n\n",link,numArticles);
	    }
	    finally {
		printLock.unlock();
	    }

	    Article a = new Article(xmlEntry);
	    listLock.lock();
	    if(!outList.contains(a)) {
		outList.add(a);
		System.out.printf("New Article Added!\n%s\n%s\n\n",a.GetTitle(),a.GetDescription());
	    }
	    else
		System.out.printf("Duplicate Article: %s\n\n",a.GetTitle());
	}
	catch (InvalidArticleException e) {}
	finally {
	    if(listLock.isHeldByCurrentThread())
		listLock.unlock();
	    articleLatch.countDown();
	    try {
		printLock.lock();
		int numArticles=articleCount.decrementAndGet();
		if(numArticles==1)
		    System.out.printf("%d Article left\n\n", numArticles);
		else
		    System.out.printf("%d Articles left\n\n", numArticles);
	    }
	    finally {
		printLock.unlock();
	    }
	}

    }


    private Element xmlEntry;
    private LinkedList<Article> outList;
    private ReentrantLock listLock;
    private CountDownLatch articleLatch;
    private AtomicInteger articleCount;
    private ReentrantLock printLock;

}

