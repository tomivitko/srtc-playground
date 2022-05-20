package de.conet.srtc.playground.wsclient.message;

import lombok.Data;

@Data
public class WebSocketOfferMessage implements WebSocketMessage {
    private String event = "offer";
    private OfferMessage data;

    @Data
    public static class OfferMessage{
        private String type;
        private String sdp; // todo convert to SDP object
    }
}
