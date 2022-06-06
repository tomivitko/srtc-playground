package de.conet.srtp.playground.wsclient;

import java.net.URI;
import java.nio.ByteBuffer;
import javax.sdp.SessionDescription;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.ice4j.ice.Agent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import de.conet.srtp.playground.wsclient.message.WebSocketAnswerMessage;
import de.conet.srtp.playground.wsclient.message.WebSocketCandidateMessage;
import de.conet.srtp.playground.wsclient.message.WebSocketMessage;
import de.conet.srtp.playground.wsclient.message.WebSocketOfferMessage;

@Slf4j
@ClientEndpoint
public class WebSocketClient {
    Session session = null;
    final ObjectMapper objectMapper = new ObjectMapper();
    Agent agent = new Agent();

    public WebSocketClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("opening websocket");
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("closing websocket");
        this.session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            WebSocketMessage socketMessage = objectMapper.readValue(message, WebSocketMessage.class);
            if (socketMessage instanceof WebSocketOfferMessage) {
                log.info("Received offer message: {} \\n\\n", socketMessage);
                try {
                    agent = AgentUtils.createAgent(20000, true);
                    log.info("stream count: {}", agent.getStreamCount());
                    String sdpDescription = SdpUtils.createSDPDescription(agent);
//                    sdpDescription =
//                        sdpDescription + "a=fingerprint:sha-256 F3:04:DD:D5:DC:E6:14:5F:6E:E9:0D:55:74:84:DD:7D:B2:01:1B:BA:5B:67:DA:6E:9D:52:CD:EE:28:8A:73:1F";

                    String answerSdp =  insertFingerprint(sdpDescription);
                    WebSocketAnswerMessage answer = new WebSocketAnswerMessage(new WebSocketAnswerMessage.AnswerMessage("answer",answerSdp));
                    log.info("answer SDP: {}",answerSdp);
                    this.sendMessage(objectMapper.writeValueAsString(answer));

//                    CompletableFuture.runAsync(() -> agent.startCandidateTrickle(new TrickleCandidateHandler(session)));
                } catch (final Throwable t) {
                    log.error(t.getMessage());
                }
            } else if (socketMessage instanceof WebSocketCandidateMessage) {
                log.info("Received candidate message: {} \\n\\n", socketMessage);
            } else if (socketMessage instanceof WebSocketAnswerMessage) {
                log.info("Received answer message: {} \\n\\n", socketMessage);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String insertFingerprint(final String sdp) {
        String[] sdpParts = sdp.split("t=0 0");
        return sdpParts[0] + "t=0 0\r\na=fingerprint:sha-256 F3:04:DD:D5:DC:E6:14:5F:6E:E9:0D:55:74:84:DD:7D:B2:01:1B:BA:5B:67:DA:6E:9D:52:CD:EE:28:8A:73:1F"
            + sdpParts[1];
    }

    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
    }

    public void sendMessage(String message) {
        this.session.getAsyncRemote().sendText(message);
    }

}
