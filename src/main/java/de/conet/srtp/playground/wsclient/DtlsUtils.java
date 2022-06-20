package de.conet.srtp.playground.wsclient;

import java.io.IOException;
import java.net.InetAddress;
import javax.media.rtp.SessionAddress;
import org.jitsi.impl.neomedia.NeomediaServiceUtils;
import org.jitsi.impl.neomedia.RTPConnectorUDPImpl;
import org.jitsi.impl.neomedia.transform.dtls.DtlsControlImpl;
import org.jitsi.impl.neomedia.transform.dtls.DtlsPacketTransformer;
import org.jitsi.impl.neomedia.transform.dtls.DtlsTransformEngine;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.DefaultStreamConnector;
import org.jitsi.service.neomedia.SrtpControl;
import org.jitsi.service.neomedia.SrtpControlType;
import org.jitsi.service.neomedia.StreamConnector;
import net.sf.fmj.media.Log;

public class DtlsUtils {

    public void connect(Integer port) {
        /*        try {
            if (dtlsProtocol instanceof DTLSClientProtocol) {
                ((DTLSClientProtocol) dtlsProtocol).connect(null, null);
            } else if (dtlsProtocol instanceof DTLSServerProtocol) {
                ((DTLSServerProtocol) dtlsProtocol).accept(null, null);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("Unchecked exception occurs: " + e.toString(), e);
        }*/

        LibJitsi.start();
        SrtpControl srtpControl = NeomediaServiceUtils.getMediaServiceImpl().createSrtpControl(SrtpControlType.DTLS_SRTP);
        //DtlsControlImpl dtlsControlImpl = new DtlsControlImpl();
        DtlsTransformEngine transformEngine = new DtlsTransformEngine((DtlsControlImpl) srtpControl);
        DtlsPacketTransformer dtlsPacketTransformer = new DtlsPacketTransformer(transformEngine, 0);
        byte[] buff = {1};
        Log.info("\n\n\n sending dtls packet\n\n\n\n");
        dtlsPacketTransformer.sendApplicationData(buff, 0, 1);

        StreamConnector streamConnector = new DefaultStreamConnector();

        RTPConnectorUDPImpl rtpConnectorUDPImpl = new RTPConnectorUDPImpl(streamConnector);
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName("localhost");
            SessionAddress address = new SessionAddress(inetAddress, port);
            rtpConnectorUDPImpl.addTarget(address);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("Unchecked exception occurs: " + e.toString(), e);
        }

    }
}
