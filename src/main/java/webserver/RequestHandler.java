package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler<RequestDispatcher> extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        
    }
    
	public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
        
        try(InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	// inputstream - 글자를 1byte 씩 밖에 못 읽음, 데이터 입력 시, outputstream - 데이터 출력 시 	
        	// Stream 은 단방향 통신, 하나의 Stream 으로 입출력을 동시에 할 수 없음 
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	// InputStream Reader 는 원래 1 Byte 씩 InputStream 은 읽어 들이지만 
        	// InputStreamReader 는 문자 단위로 읽어 준다 (InputStreamReader 은 InputStream 객체를 항상 가지고 있어야 한다.)
        	InputStreamReader reader = new InputStreamReader(in);
        	BufferedReader br = new BufferedReader(reader);
        	// InputStreamReader 덕분에 글자를 통쨰로 읽을 수 있지만
        	// 배열 크기를 일일이 지정해 주어야 해서 Bufferd Reader 는 Line 단위로 글을 읽음 
        	// BufferedReader 를 이용하여 	InputStreamReader 입력값을 객체로 사용
            String line = br.readLine();
            if(line == null) {
            	return ;
            }
        	String [] tokens = line != null ? line.split(" ") : null;
        	// 첫번째 Token 에서 url 을 얻는다
        	String url = tokens[1];
        	String header = "0"; 
        	int indexNum = 0;
            // TODO While 문 주는 이유 고민하여 보기 !
            // HTTP 요청 전체를 출력 --> 자꾸 무한루프에 빠짐 이유 생각하여보기 ...
            while(!"".equals(line)) {
            	log.info("Header Check : {} " , line);
            	line = br.readLine();
            	if(indexNum==2) {
            		String [] headerContent = line.split(": ");
            		header = headerContent[1];
            	}
            	indexNum++;
            }	
            System.out.println("URL 체크하기 !" + url);
        	byte[] body;
            DataOutputStream dos = new DataOutputStream(out);
            
            // 뒤 주소가 없을 시 Default로 Hello World 출력하게 만듦
            if(url.equals("/")) {
            	body = "Hello Linux Test Git World".getBytes();
            	response200Header(dos, body.length);
            	responseBody(dos, body);
            	
            	
            }
            
            if (url.contains("?"))  {
        		 // 쿼리 스트링이 있을 시 잘라버림 	
        		 int index = url.indexOf("?");
        		 String requestPath = url.substring(0, index);
        		 String params = url.substring(index+1);
        		 Map <String, String> paramCheck = HttpRequestUtils.parseQueryString(params);
        		 User user = new User(paramCheck.get("userId") , paramCheck.get("password") , paramCheck.get("name"), paramCheck.get("email"));
        		 DataBase.addUser(user);
        		 
        		 // 회원가입 성공 시 index.html 실패 시 /user/login_failed.html 로 이동
        		 if(DataBase.findUserById(paramCheck.get("userId")) != null ) {
        			 url = "/index.html";
        		 } else {
        			 url = "/user/login_failed.html";
        		 }
            } else if(url.equals("/user/create")) {
            	String postBody = IOUtils.readData(br, Integer.parseInt(header));
            	Map<String, Object> map = new HashMap<String, Object>();
            	String[] getParam = postBody.split("&");
            	User user = new User(getParam[0].split("=")[1] , getParam[1].split("=")[1] ,getParam[2].split("=")[1] , getParam[3].split("=")[1]);
            	DataBase.addUser(user);
            	// 302 로 Redirect 시켜 줌 
            	if(user.getUserId() != null) {
            		response302Header(dos, "http://localhost:8080/index.html");
            	} 
            } else if(url.equals("/user/login")) {
            	String postBody = IOUtils.readData(br, Integer.parseInt(header));
            	Map<String, Object> map = new HashMap<String, Object>();
            	String[] getParam = postBody.split("&");
            	String inputId = getParam[0].split("=")[1];
            	String inputPwd = getParam[1].split("=")[1];
            	User getUserId = DataBase.findUserById(inputId);
            	if(getUserId == null) {
            		System.out.println("잘못된 아이디 정보 입니다! ");
            	} else {
            		HttpCookie cookie;
            		if(getUserId.getPassword().equals(inputPwd)) {
            			System.out.println("로그인 되었습니다 !");
            			cookie = new HttpCookie("logined", "true");
            			url = "/index.html";
            		} else {
            			System.out.println("비밀번호가 다릅니다 !");
            			//cookie = new HttpCookie("logined", "false");
            			//url = "/user/login_failed.html";
            		}
            	}
             }
            
        	 // 성공시 index.html 로 가지만 회원가입 후 로그인 누를 시 오류 발생 이것 해결하기 
        	 body = Files.readAllBytes(new File("./webapp" + url).toPath());
        	 System.out.println("마지막 Body 확인 : " + body);
        	 response200Header(dos, body.length);
             responseBody(dos, body);	
             // get 방식으로 회원가입.. .
            
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + false + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
	
	private void response302Header(DataOutputStream dos, String location) {
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Location:" + location);
			dos.writeBytes("\r\n");
		} catch(IOException e) {
			log.error(e.getMessage());
		}
		
	}
    
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    // 302 Redirect 구현 하기 
    private URL redirectURL(URL url) {
    	try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	
    	
    	
    	return url;
    }
    
    
}
