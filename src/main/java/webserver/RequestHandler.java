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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);

            // HTTP 요청을 읽을 수 있게 InputStream 을 BufferedReader 로 바꾸기 2021-06-28
            // 주소가 없을시 접속이 안됨 해결  할 것 !!
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            
            String line = br.readLine();
            String [] tokens = line.split(" ");
            
            for(int i=0; i<tokens.length; i++) {
            	System.out.println("token : " + tokens[i] +" 번호 확인 : " + i);
            }
            
            String url = tokens[1];
            
            byte[] body;
            // 뒤 주소가 없을 시 Default로 Hello World 출력하게 만듦
            if( url.equals("/")) {
            	body = "Hello World".getBytes();
            	response200Header(dos, body.length);
            	responseBody(dos, body);
            
            // 뒤 주소가 있을 시 해당 url로 이동하게 만들어 줌 
            } else {
            	body = Files.readAllBytes(new File("./webapp" + url).toPath() );
            	 response200Header(dos, body.length);
                 responseBody(dos, body);			
            }
            
            
            
            
            
            
            
            
            // line while문 돌려 HTTP 마지막 Header 확인 2021-06-28
//            while( !"".equals(line) ) {
//            	
//            	System.out.println("BufferReader 주소 : " + line);
//            	
//            	 // 무한 루프를 방지하기 위하여 반복문을 빠져나오게 해준다 2021-06-28	
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
