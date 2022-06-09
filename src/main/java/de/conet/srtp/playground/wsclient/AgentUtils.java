package de.conet.srtp.playground.wsclient;

import java.util.Vector;
import javax.sdp.MediaDescription;
import javax.sdp.SessionDescription;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.Agent;
import org.ice4j.ice.IceMediaStream;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.ice.harvest.UPNPHarvester;
import org.ice4j.security.LongTermCredential;

public class AgentUtils {


    protected static Agent createAgent(int rtpPort, boolean isTrickling, SessionDescription sdp) throws Throwable {
        Agent agent = new Agent();
        agent.setTrickling(isTrickling);

        // STUN
        StunCandidateHarvester stunHarv = new StunCandidateHarvester(
            new TransportAddress("stun.l.google.com", 19302, Transport.UDP));
        //            StunCandidateHarvester stun6Harv = new StunCandidateHarvester(
        //                new TransportAddress("stun6.jitsi.net", 3478, Transport.UDP));

        agent.addCandidateHarvester(stunHarv);
        //            agent.addCandidateHarvester(stun6Harv);

        // TURN
        String[] hostnames = new String[]
            {
                "turn.bistri.com"
            };
        int port = 80;
        LongTermCredential longTermCredential
            = new LongTermCredential("homeo", "homeo!!");

        for (String hostname : hostnames) {
            agent.addCandidateHarvester(
                new TurnCandidateHarvester(
                    new TransportAddress(
                        hostname, port, Transport.UDP),
                    longTermCredential));
        }

        //UPnP: adding an UPnP harvester because they are generally slow
        //which makes it more convenient to test things like trickle.
        agent.addCandidateHarvester(new UPNPHarvester());

        //STREAMS

        Vector<MediaDescription> mediaDescriptions = sdp.getMediaDescriptions(true);
        mediaDescriptions.forEach(m -> {
            try {
                createStream(rtpPort, ((MediaDescription) m).getMedia().getMediaType(), agent);
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                throw new RuntimeException("Unchecked exception occurs: " + e.toString(), e);
            }
        });
        /*        createStream(rtpPort, "audio", agent);
        createStream(rtpPort, "video", agent);*/

        return agent;
    }

    private static IceMediaStream createStream(int rtpPort, String streamName, Agent agent)
        throws Throwable {
        IceMediaStream stream = agent.createMediaStream(streamName);

        //TODO: component creation should probably be part of the library. it
        //should also be started after we've defined all components to be
        //created so that we could run the harvesting for everyone of them
        //simultaneously with the others.

        //rtp
        agent.createComponent(stream, rtpPort, rtpPort, rtpPort + 100);

        //rtcpComp
        agent.createComponent(stream, rtpPort + 1, rtpPort + 1, rtpPort + 101);

        return stream;
    }
}
