import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Duration;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class WattpadScraper {
	public static void main(String[] args) {
		// Start UI
		SwingUI userInterface = new SwingUI();
		System.out.print("Swing UI created: " + userInterface.toString());
	}

	// Converts an entire wattpad story into a word document from table of contents
	@SuppressWarnings("deprecation")
	public static void convertStory(String url, String filepath) throws Exception {
		if (!url.startsWith("https://www.wattpad.com/")) {
			throw new IllegalArgumentException("Unsupported URL: " + url);
		}
		WebDriver driver = new ChromeDriver();

		try {
			driver.get(url);

			// Wait for the TOC to load
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div/div[2]/div[1]/div[7]/div/div[2]/ul")));

			// Locate the TOC <ul> and extract all <a> elements inside
			WebElement tocList = driver.findElement(By.xpath("/html/body/div/div[2]/div[1]/div[7]/div/div[2]/ul"));
			List<WebElement> chapterLinks = tocList.findElements(By.tagName("a"));

			// Chapter counter
			int chapter = 1;

			for (WebElement link : chapterLinks) {
				String chapterUrl = link.getAttribute("href");

				// Build unique filepath for each chapter
				String chapterFile = filepath.replace(".docx", "_chapter" + chapter + ".docx");

				System.out.println("📘 Converting chapter " + chapter + ": " + chapterUrl);
				convertPage(chapterUrl, chapterFile, ("chapter" + chapter + ".docx"));

				chapter++;
			}

			System.out.println("✅ Full story conversion complete. " + (chapter - 1) + " chapters saved.");
		} catch (TimeoutException te) {
			throw new IllegalArgumentException("Timout: Unable to locate table of contents");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}

	// Converts a single chapter into a word document
	@SuppressWarnings("deprecation")
	public static void convertPage(String url, String filepath, String docTitle) throws Exception {
		if (!url.startsWith("https://www.wattpad.com/")) {
			throw new IllegalArgumentException("Unsupported URL: " + url);
		}
		WebDriver driver = new ChromeDriver();

		try {
			driver.get(url);

			JavascriptExecutor js = (JavascriptExecutor) driver;
			final long[] lastHeight = {(long) js.executeScript("return document.body.scrollHeight")};

			while (true) {
				js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

				try {
					WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
					shortWait.until(driver1 -> {
						long newHeight = (long) js.executeScript("return document.body.scrollHeight");
						if (newHeight != lastHeight[0]) {
							lastHeight[0] = newHeight;
							return true;
						}
						return false;
					});
				} catch (TimeoutException e) {
					// No more content loaded — exit scroll loop
					break;
				}
			}


			// Wait up to 10 seconds for paragraph content to appear
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
			wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p[data-p-id]")));

			// Initialize Word document
			XWPFDocument doc = new XWPFDocument();
			//String docTitle = "chapter" + chapter + ".docx";
			File file = new File(filepath, (docTitle + ".docx"));
			FileOutputStream out = new FileOutputStream(file);

			// Select all paragraphs that are part of the story
			List<WebElement> paragraphs = driver.findElements(By.cssSelector("p[data-p-id]"));

			try (BufferedWriter writer = new BufferedWriter(new FileWriter("wattpad_story.txt"))) {
				for (WebElement p : paragraphs) {
					String htmlContent = p.getAttribute("outerHTML");

					// Use Jsoup to parse the HTML content
					Document document = Jsoup.parse(htmlContent);
					Element element = document.body();
					
					// Remove unwanted elements (mostly add comment elements)
					element.select("sup, span, button, svg, .inlineComment, .add-comment-button").remove();

					// Add each paragraph to the Word document
					XWPFParagraph paragraph = doc.createParagraph();
					XWPFRun run = paragraph.createRun();

					// Check and apply bold, italic, underline, strikethrough styles
					if (element.select("i").size() > 0) {
						run.setItalic(true); // Italics
					}

					if (element.select("b").size() > 0) {
						run.setBold(true); // Bold
					}

					if (element.select("u").size() > 0) {
						run.setUnderline(UnderlinePatterns.SINGLE); // Underline
					}

					if (element.select("s").size() > 0 || element.select("strike").size() > 0) {
						run.setStrikeThrough(true); // Strikethrough
					}

					run.setText(element.text()); // Adds plain text
					run.addBreak(); // New line after each paragraph
				}

				// Write the Word document to a file
				doc.write(out);
				System.out.println("✅ Word document saved as " + docTitle + " to " + filepath);
			}

			// Close streams
			doc.close();
			out.close();

		} catch (TimeoutException te) {
			throw new IllegalArgumentException("Timout: Unable to locate paragraph in chapter");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}
}
