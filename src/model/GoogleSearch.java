package model;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utility.Utility;

public class GoogleSearch {
	private ArrayList<Link> resultList = new ArrayList<Link>();
	private String queryString;

	public GoogleSearch(String queryString) {
		this.queryString = queryString;
	}

	public void createResult() {
		String request = "https://www.google.com/search?q=" + queryString
				+ "&num=10";
		try {
			Document doc = Jsoup
					.connect(request)
					.userAgent(
							"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
					.timeout(5000).get();

			Elements divs = doc.select("li.g");
			int rank = 1;
			for (Element div : divs) {
				String title = null;
				String url = null;
				String desc = null;
				if (!div.select("a[href]").isEmpty()) {
					url = div.select("a[href]").attr("abs:href").toString()
							.split("=")[1].replace("&sa", "").trim();
					url = url.replace("%3Fv%3D", "?v=");
					title = div.select("h3.r").text();
					desc = div.select("span.st").text();
					if (title.length() == 0 || desc.length() == 0)
						continue;
					if (Utility.validURL(url)) {
						resultList.add(new Link("google", url, title, desc,
								rank++));
					}
					// System.out.println("Google url : " + url);
					// Matcher m = Utility.pattern.matcher(url);
					// if (m.matches()) {
					// resultList.add(new Link("google", url, title, desc,
					// rank++));
					// } else {
					// if (url.endsWith(".pdf") || url.endsWith(".PDF")) {
					// resultList.add(new Link("google", url, title, desc,
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