package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import utility.Utility;

public class BingSearch {
	private ArrayList<Link> resultList = new ArrayList<Link>();
	private String queryString;

	public BingSearch(String queryString) {
		this.queryString = queryString;
	}

	public void createResult() {
		String accountKey = "43hZISppxtGLFaLc4uVUH5TZ24i3tJ+tL8KlwptMAdo";
		String bingUrlPattern = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%%27%s%%27&$format=JSON";
		URLConnection connection = null;
		try {
			String query = URLEncoder.encode("'" + queryString + "'", Charset
					.defaultCharset().name());
			String bingUrl = String.format(bingUrlPattern, query);

			String accountKeyEnc = Base64.getEncoder().encodeToString(
					(accountKey + ":" + accountKey).getBytes());

			URL url = new URL(bingUrl);
			connection = url.openConnection();
			connection.setRequestProperty("Authorization", "Basic "
					+ accountKeyEnc);
		} catch (UnsupportedEncodingException ex) {

		} catch (MalformedURLException ex) {

		} catch (IOException ex) {

		}
		try (BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()))) {
			String inputLine;
			final StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			final JSONObject json = new JSONObject(response.toString());
			final JSONObject d = json.getJSONObject("d");
			final JSONArray results = d.getJSONArray("results");
			int skip = 0;
			int rank = 1;
			final int resultsLength = results.length();
			for (int i = 0; i < resultsLength; i++) {
				final JSONObject aResult = results.getJSONObject(i);
				String title = aResult.get("Title").toString();
				String url = aResult.get("Url").toString().trim();
				String desc = aResult.get("Description").toString();
				JSONObject temp = (JSONObject) aResult.get("__metadata");
				String[] values = temp.get("uri").toString().split("\\&\\$");
				for (String value : values) {
					if (value.startsWith("http"))
						continue;
					String[] print = value.split("=");
					if (print.length > 0) {
						if (print[0].equals("skip")) {
							skip = Integer.parseInt(print[1]);
						}
					}
				}
				if (skip >= 10)
					break;
				if (title == null || desc == null)
					continue;
				if (Utility.validURL(url)) {
					resultList.add(new Link("bing", url, title, desc, rank));
					rank++;
				}
				// Matcher m = Utility.pattern.matcher(url);
				// if (m.matches()) {
				// resultList.add(new Link("bing", url, title, desc, rank));
				// rank++;
				// }else{
				// if (url.endsWith(".pdf") || url.endsWith(".PDF")) {
				// resultList.add(new Link("bing", url, title, desc, rank));
				// rank++;
				// }
				// }
			}
		} catch (IOException ex) {

		}
	}

	public ArrayList<Link> getResults() {
		return this.resultList;
	}
}