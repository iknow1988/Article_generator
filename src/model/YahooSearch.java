package model;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.regex.Matcher;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utility.Utility;

public class YahooSearch {
	private ArrayList<Link> resultList = new ArrayList<Link>();
	private String queryString;

	public YahooSearch(String queryString) {
		this.queryString = queryString;
	}

	public void createResult() {
		String request = "http://search.yahoo.com/search?p=" + queryString;
		try {
			Document doc = Jsoup
					.connect(request)
					.userAgent(
							"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
					.timeout(5000).get();
			Elements resultDiv = doc.select("div#results");
			Elements lis = resultDiv.select("li");
			int rank = 1;
			for (Element li : lis) {
				if (li.hasClass("first")) {
				} else {
					String title = li.select("div.compTitle")
							.select("h3.title").text();
					String url = li.select("a[href]").attr("abs:href")
							.toString().trim();
					String desc = li.select("div.compText").text();
					if (title.length() == 0 || desc.length() == 0)
						continue;
					if (Utility.validURL(url)) {
						resultList.add(new Link("yahoo", url, title, desc,
								rank++));
					}
					// Matcher m = Utility.pattern.matcher(url);
					// if (m.matches()) {
					// resultList.add(new Link("yahoo", url, title, desc,
					// rank++));
					// } else {
					// if (url.endsWith(".pdf") || url.endsWith(".PDF")) {
					// resultList.add(new Link("yahoo", url, title, desc,
					// rank++));
					// }
					// }
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Link> getResults() {
		return this.resultList;
	}
}
