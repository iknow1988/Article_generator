package utility;

import java.io.FileOutputStream;
import java.io.StringReader;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

@SuppressWarnings("deprecation")
public class HtmlToPDF {
	public static void generatePDF(String str, String path) {
		try {
			Document document = new Document(PageSize.LETTER);
			PdfWriter.getInstance(document, new FileOutputStream(path));
			document.open();
			document.addCreationDate();
			HTMLWorker htmlWorker = new HTMLWorker(document);
			htmlWorker.parse(new StringReader(str));
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}