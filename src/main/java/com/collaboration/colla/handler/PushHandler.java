package com.collaboration.colla.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.collaboration.colla.service.CardService;
import com.collaboration.colla.service.MemberService;
import com.collaboration.colla.vo.CMemberVO;
import com.collaboration.colla.vo.CardVO;

@RequestMapping("/push")
public class PushHandler extends TextWebSocketHandler {

	@Autowired
	CardService cservice;
	@Autowired
	MemberService mservice;

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("push alarm websocket closed");
		super.afterConnectionClosed(session, status);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("push alarm websocket opened");
		System.out.println("session : " + session.getPrincipal().getName());

	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println("handleTextMessage in");
		
		super.handleTextMessage(session, message);
		
		String email = session.getPrincipal().getName();

		List<CMemberVO> list = mservice.getCMember(email);

		int[] cFkArr = new int[list.size()];

		for (int i = 0; i < list.size(); i++) {
			cFkArr[i] = list.get(i).getcFk();
		}

		
		List<CardVO> clist = cservice.forPush(cFkArr);

		Date[] deadline = new Date[clist.size()];

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String to = df.format(cal.getTime());
		Date today = df.parse(to);
		System.out.println("current: " + today);

		String sendMessage = "";
		
		long[] term = new long[clist.size()];
		
		for (int i = 0; i < clist.size(); i++) {
			
			deadline[i] = df.parse(clist.get(i).getcDeadline());
			
			if(deadline[i].getMonth() == today.getMonth()) {
			
			if ((deadline[i].getDay() - today.getDay() >= 0) && (deadline[i].getDay() - today.getDay() <=3)) {
				term[i] = deadline[i].getDay() - today.getDay();
				sendMessage += clist.get(i).getcTitle() + "=" + term[i] + "/";
			}
			
			}else if(deadline[i].getMonth() > today.getMonth()) {
				
				int lastDay = 32 - new Date(today.getYear(), today.getMonth(), 32).getDate();
				if(lastDay - today.getDay() == 0) {
					if(deadline[i].getDay() <=3) {
						term[i] = deadline[i].getDay() - today.getDay();
						sendMessage += clist.get(i).getcTitle() + "=" + term[i] + "/";
					}
				}else if(lastDay - today.getDay() == 1) {
					if(deadline[i].getDay() <=2) {
						term[i] = deadline[i].getDay() - today.getDay();
						sendMessage += clist.get(i).getcTitle() + "=" + term[i] + "/";
					}
				}else if(lastDay - today.getDay() == 2) {
					if(deadline[i].getDay() <=1) {
						term[i] = deadline[i].getDay() - today.getDay();
						sendMessage += clist.get(i).getcTitle() + "=" + term[i] + "/";
					}
				}
				
			}
		
		}
		System.out.println("sendMessage : " + sendMessage);
		
		session.sendMessage(new TextMessage(sendMessage));
		
	
	}

}
