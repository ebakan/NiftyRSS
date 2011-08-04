/**
  * Article.java
  * Represents one news article, including
  * its database of word count
  * @author Eric Bakan
  */

import java.util.HashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
  * Takes an RSS item and parses its information:
  * Title
  * Description
  * Link
  * Date
  * HashMap of word counts
  * NOTE: the word count HashMap includes ALL
  * words contained in the HTML, including non-article
  * words and markup tokens
  */
public class Article {
    /**
      * Constructor
      * Parses the inputted XML entry and
      * extracts the data and the word count
      * HashMap
      * @throws InvalidArticleException
      * @param xmlEntry The XML entry to parse
      */
    public Article(Element xmlEntry) throws InvalidArticleException {
	title=getFirstElementValue(xmlEntry,"title");
	description=getFirstElementValue(xmlEntry,"description");
	link=getFirstElementValue(xmlEntry,"link");
	date=getFirstElementValue(xmlEntry,"pubDate");
	try {
	    content=getContent(new URL(link));
	}
	catch (MalformedURLException e) {
	    System.out.printf("Bad URL: %s\n",link);
	    throw new InvalidArticleException(e.getMessage());
	}
	catch (IOException e) {
	    System.out.printf("Error while reading data: %s\n",link);
	    throw new InvalidArticleException(e.getMessage());
	}

	wordCountHash=getWordCount(content);

    }

    /**
      * Content Getter
      * @return Content
      */
    public String GetContent() {
	return content;
    }

    /**
      * Date Getter
      * @return Date
      */
    public String GetDate() {
	return date;
    }

    /**
      * Description Getter
      * @return Description
      */
    public String GetDescription() {
	return description;
    }

    /**
      * Word Count HashMap Getter
      * @return Word Count HashMap
      * @deprecated #GetNumOccurrences(String query) is preferred
      */
    public HashMap<String, Integer> GetHashMap() {
	return wordCountHash;
    }

    /**
      * Link Getter
      * @return Link
      */
    public String GetLink() {
	return link;
    }

    /**
      * Title Getter
      * @return Title
      */
    public String GetTitle() {
	return title;
    }

    /**
      * Get the number of occurrences
      * of a query in the Article
      * Preferred over using #GetHashMap() directly
      * @param query The String to check
      * @return Number of occurrences of query in the Article
      */
    public Integer GetNumOccurrences(String query) {
	Integer num=wordCountHash.get(query.toLowerCase());
	return num==null?0:num;
    }

    /**
      * Checks for equality between two Articles
      * @return false if o is not an Article, otherwise
      * checks if the title and link domain are the same
      */
    public boolean equals(Object o) {
	Article a;
	try {
	    a=(Article)o;
	}
	catch (ClassCastException e) {
	    return false;
	}

	boolean sameTitle=title.equals(a.title);
	boolean sameLink=false;
	try {
	    URL myLink = new URL(link);
	    URL yourLink = new URL(a.link);
	    sameLink=myLink.getHost().equals(yourLink.getHost());
	}
	//if there's a problem with the links,
	//sameLink will remain false
	catch (MalformedURLException e) {}
	return sameTitle && sameLink;

    }

    /**
      * Gets the value of an element
      * in the XML Element
      * Conforms to W3C's DOM specification
      * @param root DOM Element to check within
      * @param elementName Element name to search for
      * @return String value of the element, null if it does not exist
      */
    private String getFirstElementValue(Element root, String elementName) {
	try {
	    return root.getElementsByTagName(elementName).item(0).getFirstChild().getNodeValue();
	}
	//This case is met when there are 0 elements
	//of name elementName in root, thus the
	//.item(0) call returns null, and the
	//.getNodeValue() call throws an exception
	catch (NullPointerException e) {
	    return null;
	}
	catch (ArrayIndexOutOfBoundsException e) {
	    return null;
	}
    }

    /**
      * Gets the content of a given URL
      * Uses a buffered reader and reads the
      * URL's InputStream line-by-line into an
      * output variable, which it returns
      * @throws IOException if error occurs while reading URL
      * @return String of the URL's content
      */
    private String getContent(URL url) throws IOException {
	String out="";
	BufferedReader urlReader = new BufferedReader(new InputStreamReader(url.openStream()));
	String line=null;
	while((line=urlReader.readLine()) != null)
	    out+=line;
	urlReader.close();
	return out;
    }

    /**
      * Counts the number of times each word
      * in the article appears and stores the result
      * in a HashMap. NOTE that this current
      * implementation checks EVERY word in the HTML
      * of the article, including any markup, scripts,
      * or other non-article text.
      * @param data The data to parse
      * @return HashMap of all the words in data paired
      * with their respective number of occurrences
      */
    private HashMap<String, Integer> getWordCount(String data) {
	//This removes all punctuation, leaving only
	//alphanumeric characters
	final String invalidChars="[^a-zA-Z0-9\\s]";
	data=data.toLowerCase().replaceAll(invalidChars," ");
	String[] split=data.split("\\s");
	HashMap<String, Integer> map = new HashMap<String, Integer>();
	for(String s : split)
	    if(s.length()>0)
		incrementCount(map,s);
	return map;
    }

    /**
      * A helper method for getWordCount,
      * which increments the value of a given
      * entry in a HashMap.
      * @param map The HashMap to use
      * @param entry The entry to increment
      */
    private void incrementCount(HashMap<String, Integer> map, String entry) {
	Integer val = map.get(entry);
	if(val==null)
	    val=0;
	map.put(entry,val+1);
    }

    private String title;
    private String description;
    private String link;
    private String date;
    private String content;
    private HashMap<String, Integer> wordCountHash;
}

