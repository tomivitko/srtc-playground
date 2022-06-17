package de.conet.srtp.playground.wsclient;

import org.bouncycastle.tls.DTLSProtocol;
import org.jitsi.impl.neomedia.transform.dtls.DtlsControlImpl;
import org.jitsi.impl.neomedia.transform.dtls.DtlsPacketTransformer;
import org.jitsi.impl.neomedia.transform.dtls.DtlsTransformEngine;
import net.sf.fmj.media.Log;

public class DtlsUtils {

    public void connect(DTLSProtocol dtlsProtocol) {
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
        DtlsControlImpl dtlsControlImpl = new DtlsControlImpl();
        DtlsTransformEngine transformEngine = new DtlsTransformEngine(dtlsControlImpl);
        DtlsPacketTransformer dtlsPacketTransformer = new DtlsPacketTransformer(transformEngine, 0);
        byte[] buff = {1};
        Log.info("\n\n\n sending dtls packet\n\n\n\n");
        dtlsPacketTransformer.sendApplicationData(buff, 0, 1);
    }
}
