package de.conet.srtp.playground.wsclient;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Vector;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
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
import org.ice4j.ice.RemoteCandidate;
import org.opentelecoms.javax.sdp.NistSdpFactory;
import org.springframework.util.StringUtils;
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

                    SdpFactory sdpFactory = new NistSdpFactory();

                    SessionDescription sessionDescription = sdpFactory.createSessionDescription(((WebSocketOfferMessage) socketMessage).getData().getSdp());
                    String sdpDescription = sessionDescription.toString();
                    SdpUtils.parseSDP(agent, sdpDescription);

                    Vector mediaDescriptions = sessionDescription.getMediaDescriptions(false);
                    //                    System.out.println("Media descriptions: \n" + mediaDescriptions);

                    //                    agent = AgentUtils.createAgent(20000, true, (MediaDescriptionImpl) mediaDescriptions.get(0));
                    agent.setControlling(false);
                    //                    String sdpDescription = SdpUtils.createSDPDescription(agent);


                    WebSocketAnswerMessage answer = new WebSocketAnswerMessage(new WebSocketAnswerMessage.AnswerMessage("answer", insertFingerprint(SdpUtils.createSDPDescription(agent))));
                    this.sendMessage(objectMapper.writeValueAsString(answer));

                } catch (final Throwable t) {
                    log.error(t.getMessage());
                }
            } else if (socketMessage instanceof WebSocketCandidateMessage) {
                log.info("Received candidate message: {} \\n\\n", socketMessage);
                if (StringUtils.hasText(((WebSocketCandidateMessage) socketMessage).getData().getCandidate())) {
                    agent.getStreams().forEach(stream -> {
                        final String midAttribute;
                        try {
                            midAttribute = stream.getMediaDescription().getAttribute("mid");
                        } catch (SdpParseException e) {
                            throw new RuntimeException(e);
                        }
                        if (((WebSocketCandidateMessage) socketMessage).getData().getSdpMid().equals(midAttribute)) {
                            RemoteCandidate remoteCandidate = SdpUtils.parseRemoteCandidate(((WebSocketCandidateMessage) socketMessage).getData(), stream);
//                            remoteCandidate.getParentComponent().addRemoteCandidate(remoteCandidate);
                        }
//                        component.addRemoteCandidate(remoteCandidate);
                    });
                } else {
                    agent.startConnectivityEstablishment();
                }
            } else if (socketMessage instanceof WebSocketAnswerMessage) {
                log.info("Received answer message: {} \\n\\n", socketMessage);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        DtlsUtils dtlsUtils = new DtlsUtils();
        dtlsUtils.connect(null);
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
