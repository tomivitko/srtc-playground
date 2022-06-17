//connecting to our signaling server
var conn = new WebSocket('ws://localhost:8080/socket');

conn.onopen = function() {
    console.log("Connected to the signaling server");
    initialize();
};

conn.onmessage = function(msg) {
    var content = JSON.parse(msg.data);
    var data = content.data;
    switch (content.event) {
    // when somebody wants to call us
    case "offer":
        handleOffer(data);
        break;
    case "answer":
        handleAnswer(data);
        break;
    // when a remote peer sends an ice candidate to us
    case "candidate":
        handleCandidate(data);
        break;
    default:
        break;
    }
};

const constraints = {
    video: false,audio : true
};
navigator.mediaDevices.getUserMedia(constraints).
then(function(stream) { peerConnection.addStream(stream); })
    .catch(function(err) { /* handle the error */ });

function send(message) {
    conn.send(JSON.stringify(message));
}

var peerConnection;

function initialize() {
    var configuration = {
        'iceServers': [
            {
                'urls': 'stun:stun.l.google.com:19302'
            }
        ],
        "rtcpMuxPolicy": "negotiate"
    };

    peerConnection = new RTCPeerConnection(configuration);

    // Setup ice handling
    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
            send({
                event : "candidate",
                data : event.candidate
            });
        }
    };

    peerConnection.onconnectionstatechange = ev => {
        console.log(ev)
        console.log("PeerConnection state: " + peerConnection.connectionState);
    }

    peerConnection.onsignalingstatechange = ev => {
        console.log("Signaling state: " + peerConnection.signalingState)
    };

    peerConnection.oniceconnectionstatechange = ev => {
        console.log("ICE connection state: " + peerConnection.iceConnectionState);
    };

    peerConnection.onicegatheringstatechange = ev => {
        let connection = ev.target;

        switch(connection.iceGatheringState) {
            case "gathering":
                console.log("ICE gathering in progress")
                /* collection of candidates has begun */
                break;
            case "complete":
                console.log("ICE gathering completed")
                /* collection of candidates is finished */
                break;
        }
    }
}

function createOffer() {
    peerConnection.createOffer(function(offer) {
        send({
            event : "offer",
            data : offer
        });
        peerConnection.setLocalDescription(offer);
    }, function(error) {
        alert("Error creating an offer");
    });
}

function handleOffer(offer) {
    peerConnection.setRemoteDescription(new RTCSessionDescription(offer));

    // create and send an answer to an offer
    peerConnection.createAnswer(function(answer) {
        peerConnection.setLocalDescription(answer);
        send({
            event : "answer",
            data : answer
        });
    }, function(error) {
        alert("Error creating an answer");
    });

};

function handleCandidate(candidate) {
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
};

function handleAnswer(answer) {
    console.log("Received an answer from remote server");
    console.log(answer);
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
};
