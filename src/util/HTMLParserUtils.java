package util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.FCMService;

/**
 * @author jaeeonjin
 * HTML Parsing utils.
 * Requirement : JSoup Library ( add Dependency "jsoup" in pom.xml )
 */
public class HTMLParserUtils {

	private static Logger logger = LoggerFactory.getLogger(HTMLParserUtils.class);
	
	final static int TIME_OUT = 30000; 

	/**
	 * uri에 접속해서 document 객체를 받아온다.
	 * @param uri
	 * @return
	 */
	private Document getDocument(String uri) {
		Document document = null;
		try {
			document = Jsoup.connect(uri).timeout(TIME_OUT).get();
			document.outputSettings().charset("UTF-8");
		} catch (IOException e) {
			logger.error("Document 객체 로딩에 실패했습니다.");
			e.printStackTrace();
		}
		return document;
	}
	
	/**
	 * 받은 도큐먼트에서 css 선택자가 가리키는 데이터를 파싱한다.
	 * @param uri
	 * @param cssSelector
	 * @return
	 */
	public Elements getElements(String uri, String cssSelector) {
		Elements elements = getDocument(uri).select(cssSelector);
		return elements;
	}

}
