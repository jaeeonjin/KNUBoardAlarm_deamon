package service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.FCMUtils;
import util.HttpConnectionUtils;
import util.PropertiesUtils;

/**
 * FCMUtils를 앱의 목적에 맞게 수정한 클래스
 * @author JaeeonJin
 */
public class FCMService {

	private static Logger logger = LoggerFactory.getLogger(FCMService.class);
	
	private FCMUtils fcmUtils;
	private String serverKey;
	private HttpConnectionUtils httpUtils;
	private FileService fileService;
	
	public FCMService() {
		this.serverKey = PropertiesUtils.readProperty("fcm.serverKey");
		httpUtils = new HttpConnectionUtils(PropertiesUtils.readProperty("api.baseUrl"));
		fcmUtils = new FCMUtils(serverKey);
		fileService = FileService.getInstance();
	}

	/**
	 * 특정 게시판을 추가한 사용자들을 조회한 후, 그 사용자들에게 FCM을 보낸다.
	 * @param boardUri : 게시판 uri
	 * @param boardTitle
	 */
	@SuppressWarnings("unchecked")
	public void sendMessage(String boardURI, String boardDataURL, String boardDataTitle) {	

		if( serverKey!= null ) {

			// 해당 게시판을 추가한 멤버토큰 / 멤버게시판이름 조회
			JSONArray tokenList = httpUtils.callAPIForGET("/members/tokens/"+boardURI);

			if( tokenList != null && tokenList.size() != 0 ) {
				for(int i=0; i<tokenList.size(); i++) {
					JSONObject jsonObj = (JSONObject) tokenList.get(i);
					jsonObj.put("boardDataUrl", boardDataURL);
					jsonObj.put("boardDataTitle", boardDataTitle);

					System.err.println("TOKEN : " + jsonObj.get("token"));
					fcmUtils.sendNotification(jsonObj, (String) jsonObj.get("token"));

					// 1초 안 쉬면 다중 전송이 안된다.
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				logger.warn("{} 게시판을 추가한 사용자 토큰이 없습니다."
						+ "더 이상 해당 게시판을 검사하지 않기 위해 파일을 삭제합니다.", boardURI);
				fileService.removeJSONFile(boardURI);
			}
		} else {
			logger.error("서버키가 존재하지 않습니다. property를 확인해주세요.");
		}
	}

	//	public static void main(String args[]) {
	//		new FCMService().sendMessage("asdf", "http://www.naver.com", "네이버");
	//	}

}
