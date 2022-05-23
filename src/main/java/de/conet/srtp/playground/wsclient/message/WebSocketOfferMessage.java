package de.conet.srtp.playground.wsclient.message;

import lombok.Data;

@Data
public class WebSocketOfferMessage implements WebSocketMessage {
    private String event = "offer";
    private OfferMessage data;

    @Data
    public static class OfferMessage{
        private String type;
        private String sdp;
        //        private SessionDescription sdp;
        //
        //        OfferMessage(String sdpString) {
        //            SdpFactory sdpFactory = SdpFactory.getInstance();
        //            try {
        //                this.sdp = sdpFactory.createSessionDescription(sdpString);
        //            } catch (Exception e) {
        //                System.out.println("Error parsing sdp object");
        //            }
        //        }
        
    }
}
