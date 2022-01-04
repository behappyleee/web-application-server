package webserver;

import java.io.BufferedReader;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        
    }
    
	public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
        
        try(InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	// inputstream - 데이터 입력 시, outputstream - 데이터 출력 시 	
        	// Stream 은 단방향 통신, 하나의 Stream 으로 입출력을 동시에 할 수 없음 
        	
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
           
            String line = br.readLine();
            
            // line 의 맨 마지막을 가져와야 함 
            System.out.println("line " + line);
            
        	String [] tokens = line != null ? line.split(" ") : null;
        	// 첫번째 Token 에서 url 을 얻는다
        	String url = tokens[1];
        	System.out.println("URL 체크하기 : " + url);
            
            // TODO While 문 주는 이유 고민하여 보기 !
            // HTTP 요청 전체를 출력 --> 자꾸 무한루프에 빠짐 이유 생각하여보기 ...
        	
//            while(!"".equals(line)) {
//            	line = br.readLine();
//            	System.out.println("BR READLINE Check : " + br.readLine());
//            	System.out.println("URL CHECK !!! :: " + url);
//            	if(line == null) {
//            		break;
//            	}
//            }	
            
        	byte[] body;
            DataOutputStream dos = new DataOutputStream(out);
            // 뒤 주소가 없을 시 Default로 Hello World 출력하게 만듦s
            if( url.equals("/")) {
            	body = "Hello Linux Test Git World".getBytes();
            	response200Header(dos, body.length);
            	responseBody(dos, body);
            	
            // 뒤 주소가 있을 시 해당 url로 이동하게 만들어 줌 
            } else {
            	
            	// 쿼리스트링이 있을 시 
            	// get / post 방식으로 회원가입.. .
            	 if(url.contains("?")) {	
            		 // 쿼리 스트링이 있을 시 잘라버림 	
            		 int index = url.indexOf("?");
            		 String requestPath = url.substring(0, index);
            		 String params = url.substring(index+1);
            		 
            		 Map <String, String> paramCheck = HttpRequestUtils.parseQueryString(params);
            		 	
            		 for(String keys : paramCheck.keySet()) {
            			 System.out.println("값 Key Check : " + keys + " 값 Value Check " + paramCheck.get(keys) );
            		 }
            		 
            		 User user = new User(paramCheck.get("userId") , paramCheck.get("password") , paramCheck.get("name"), paramCheck.get("email"));
            		 DataBase.addUser(user);
            		 
            		 // 회원가입 성공 시 index.html 실패 시 /user/login_failed.html 로 이동
            		 if(DataBase.findUserById(paramCheck.get("userId")) != null ) {
            			 url = "/index.html";
            		 } else {
            			 url = "/user/login_failed.html";
            		 }
            	 } else if( url.equals("/user/create")) { 
               
            		 IOUtils a = new IOUtils();
            		 
            		// 뒤 br 뒤에 숫자를 고정적이 아닌 Data 추출 Length 만큼 주기
            		// br 에 Content-Length 를 길이 를 주고, 데이터를 다 읽지 않고 마지막 데이터만 읽기 (Post 데이터가 담겨져 있음)
            		 String lineData = a.readData(br, 1000);
            		 
            		 System.out.println("While 문 Line Data Check : " + lineData);
            		 
            		 String[] testSplit = lineData.split(" ");
            		 
            		 
            		 for(int i=0; i<testSplit.length; i++) {
            			 System.out.println("각 데이터 : " + testSplit[i] );
            		 }
            		 
            		 
            		 
            		 // 뒤 br 뒤에 숫자를 고정적이 아닌 Data 추출 Length 만큼 주기
//            		 while(true) {
//            			 System.out.println("Line Data 확인 : " + lineData );
//            			 if(lineData == null) { 
//            				 System.out.println("Line Data Is Null !");
//            				 break;
//            			 }
//            		 }
            		 
//	            	while(!"".equals(line)) {
//	            		
//	                 	System.out.println("User Register Form Post 요청 ");
//	            		
//	                 	line = br.readLine();
//	            		System.out.println("BR READLINE Check : " + br.readLine());
//	                 	System.out.println("URL CHECK !!! :: " + url);
//	                 	if(line == null) {
//	                 		return;
//	                 	}
//	                 }	
            		 
            		 System.out.println("Hello Create Post !");
            	 }
            	
            	 // 성공시 index.html 로 가지만 회원가입 후 로그인 누를 시 오류 발생 이것 해결하기 
            	 body = Files.readAllBytes(new File("./webapp" + url).toPath());
            	 System.out.println("마지막 Body 확인 : " + body);
            	 response200Header(dos, body.length);
                 responseBody(dos, body);	
                 // get 방식으로 회원가입.. .
            }
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
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
}
