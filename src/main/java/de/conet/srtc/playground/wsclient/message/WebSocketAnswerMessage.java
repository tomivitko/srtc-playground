package de.conet.srtc.playground.wsclient.message;

import lombok.Data;

@Data
public class WebSocketAnswerMessage implements WebSocketMessage {
    private String event = "answer";
    private Object data;
}
