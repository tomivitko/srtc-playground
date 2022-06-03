package de.conet.srtp.playground.wsclient;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.conet.srtp.playground.wsclient.message.WebSocketAnswerMessage;
import de.conet.srtp.playground.wsclient.message.WebSocketCandidateMessage;
import lombok.extern.slf4j.Slf4j;
import de.conet.srtp.playground.wsclient.message.WebSocketMessage;
import de.conet.srtp.playground.wsclient.message.WebSocketOfferMessage;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.harvest.CandidateHarvester;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.ice.harvest.UPNPHarvester;
import org.ice4j.ice.sdp.IceSdpUtils;
import org.ice4j.security.LongTermCredential;
import org.opentelecoms.javax.sdp.NistSdpFactory;

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
                    String sdpDescription = SdpUtils.createSDPDescription(agent);
//                    sdpDescription =
//                        sdpDescription + "a=fingerprint:sha-256 F3:04:DD:D5:DC:E6:14:5F:6E:E9:0D:55:74:84:DD:7D:B2:01:1B:BA:5B:67:DA:6E:9D:52:CD:EE:28:8A:73:1F";
                    WebSocketAnswerMessage answer = new WebSocketAnswerMessage(new WebSocketAnswerMessage.AnswerMessage("answer", sdpDescription));
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

    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
    }

    public void sendMessage(String message) {
        this.session.getAsyncRemote().sendText(message);
    }

}
