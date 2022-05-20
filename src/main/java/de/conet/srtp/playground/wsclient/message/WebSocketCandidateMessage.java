package de.conet.srtp.playground.wsclient.message;

import lombok.Data;

@Data
public class WebSocketCandidateMessage implements WebSocketMessage {
    private String event = "candidate";
    private Object data;
}
