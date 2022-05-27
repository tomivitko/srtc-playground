package de.conet.srtp.playground.wsclient.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebSocketCandidateMessage implements WebSocketMessage {
    private CandidateObject data;

    public WebSocketCandidateMessage(CandidateObject data) {
        this.data = data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateObject {
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;
        private String usernameFragment;
    }
}
