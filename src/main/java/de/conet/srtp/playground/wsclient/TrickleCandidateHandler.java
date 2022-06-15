package de.conet.srtp.playground.wsclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.conet.srtp.playground.wsclient.message.WebSocketCandidateMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ice4j.ice.LocalCandidate;
import org.ice4j.ice.harvest.TrickleCallback;
import org.jetbrains.annotations.NotNull;

import javax.websocket.Session;
import java.util.Collection;

@Slf4j
@AllArgsConstructor
public class TrickleCandidateHandler implements TrickleCallback {
    private final Session session;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onIceCandidates(Collection<LocalCandidate> collection) {
        collection.forEach(candidate -> {
            log.info(candidate.toString());
            final WebSocketCandidateMessage message = createWebSocketCandidateMessage(candidate);
            try {
                final String json = objectMapper.writeValueAsString(message);
                log.info("New local candidate: {}", json);
                session.getAsyncRemote().sendText(json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull private WebSocketCandidateMessage createWebSocketCandidateMessage(LocalCandidate candidate) {
        final WebSocketCandidateMessage.CandidateObject candidateObject =
            new WebSocketCandidateMessage.CandidateObject(candidate.toString(), String.valueOf(candidate.getParentComponent().getComponentID()), 0, candidate.getUfrag());
        return new WebSocketCandidateMessage(candidateObject);
    }

}
