package de.conet.srtp.playground.wsclient.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketAnswerMessage implements WebSocketMessage {
    private AnswerMessage data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerMessage {
        private String type;
        private String sdp;
    }
}
