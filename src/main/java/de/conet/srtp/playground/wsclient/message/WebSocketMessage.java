package de.conet.srtp.playground.wsclient.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "event")
@JsonSubTypes({
    @JsonSubTypes.Type(value = WebSocketOfferMessage.class, name = "offer"),
    @JsonSubTypes.Type(value = WebSocketAnswerMessage.class, name = "answer"),
    @JsonSubTypes.Type(value = WebSocketCandidateMessage.class, name = "candidate")
})
public interface WebSocketMessage {
}
