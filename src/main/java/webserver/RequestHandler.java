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

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try {
        	// inputstream - 데이터 입력 시, outputstream - 데이터 출력 시 	
        	// Stream 은 단방향 통신, 하나의 Stream 으로 입출력을 동시에 할 수 없음 
        	InputStream in = connection.getInputStream(); 
        	OutputStream out = connection.getOutputStream(); 
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	
            DataOutputStream dos = new DataOutputStream(out);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            
            String line = br.readLine();
            
            // line 의 맨 마지막을 가져와야 함 
            System.out.println("line " + line);
            String [] tokens = line.split(" ");
            String url = tokens[1];
            
//            while(line != null) {
//            	System.out.println("BR READLINE Check : " + br.readLine());
//            	
//            	String[] tokens = br.readLine().split(" ");
//            	
//            	
//            }

            byte[] body;
            
            // 뒤 주소가 없을 시 Default로 Hello World 출력하게 만듦
            if( url.equals("/")) {
            	body = "Hello Linux Test Git World".getBytes();
            	response200Header(dos, body.length);
            	responseBody(dos, body);
            
            // 뒤 주소가 있을 시 해당 url로 이동하게 만들어 줌 
            } else {
            	 // 쿼리스트링이 있을 시 
            	 if(url.contains("?")) {	
            		 // 쿼리 스트링이 있을 시 잘라버림 	
            		 int index = url.indexOf("?");
            		 
            		 String requestPath = url.substring(0, index);
            		 String params = url.substring(index+1);
            		 
            		 System.out.println("User 클래스에 담기 " + requestPath + "parmas : " + params);
            		 
            		 Map <String, String> paramCheck = HttpRequestUtils.parseQueryString(params);
            		 
            		 System.out.println("값 parsing Check 하기" + paramCheck);
            		 
            		 for(String keys : paramCheck.keySet()) {
            			 System.out.println("값 Key Check : " + keys + " 값 Value Check " + paramCheck.get(keys) );
            		 }
            		 
            		 User user = new User(paramCheck.get("userId") , paramCheck.get("password") , paramCheck.get("name"), paramCheck.get("email"));
            		 System.out.println("User Class 에 잘 담겼는지 Check : " + user);
            		 
            		 DataBase.addUser(user);
            		 
            		 System.out.println("DB 들어간 값 확인 " + DataBase.findUserById(paramCheck.get("userId")) );
            		 
            		 // 회원가입 성공 시 index.html 실패 시 /user/login_failed.html 로 이동
            		 if( DataBase.findUserById(paramCheck.get("userId")) != null ) {
            			 url = "/index.html";
            		 } else {
            			 url = "/user/login_failed.html";
            		 }
            	 } 
            	 
            	 // 성공시 index.html 로 가지만 회원가입 후 로그인 누를 시 오류 발생 이것 해결하기 
            	 body = Files.readAllBytes(new File("./webapp" + url).toPath());
            	 response200Header(dos, body.length);
                 responseBody(dos, body);	
            }
            
            // line while문 돌려 HTTP 마지막 Header 확인 2021-06-28
//            while( !"".equals(line) ) {	
//            	
//            	System.out.println("BufferReader 주소 : " + line);
//            	
//            	// 무한 루프를 방지하기 위하여 반복문을 빠져나오게 해준다 2021-06-28	
//            	if( line == null )  {
//            		return;
//            	}
//            	
//            }
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
