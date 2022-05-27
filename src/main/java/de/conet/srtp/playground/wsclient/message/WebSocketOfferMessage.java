package de.conet.srtp.playground.wsclient.message;

import lombok.Data;

@Data
public class WebSocketOfferMessage implements WebSocketMessage {
    private OfferMessage data;

    @Data
    public static class OfferMessage {
        private String type;
        private String sdp;
    }
}
