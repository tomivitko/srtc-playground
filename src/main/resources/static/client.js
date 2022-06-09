//connecting to our signaling server
var conn = new WebSocket('ws://localhost:8080/socket');

conn.onopen = function() {
    console.log("Connected to the signaling server");
    initialize();
};

conn.onmessage = function(msg) {
    console.log("Got message", msg.data);
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
    video: true,audio : true
};
navigator.mediaDevices.getUserMedia(constraints).
then(function(stream) { peerConnection.addStream(stream); })
    .catch(function(err) { /* handle the error */ });

function send(message) {
    conn.send(JSON.stringify(message));
}

var peerConnection;
var dataChannel;
const videoElement = document.querySelector('video');
var input = document.getElementById("messageInput");

function initialize() {
    var configuration = {
        'iceServers': [
            {
                'urls': 'stun:stun.l.google.com:19302'
            },
            {
                'urls': 'turn:10.158.29.39:3478?transport=udp',
                'credential': 'XXXXXXXXXXXXX',
                'username': 'XXXXXXXXXXXXXXX'
            },
            {
                'urls': 'turn:10.158.29.39:3478?transport=tcp',
                'credential': 'XXXXXXXXXXXXX',
                'username': 'XXXXXXXXXXXXXXX'
            }
        ]
    };

    peerConnection = new RTCPeerConnection(configuration);

    // Setup ice handling
/*    peerConnection.onicecandidate = function(event) {
        if (event.candidate) {
            send({
                event : "candidate",
                data : event.candidate
            });
        }
    };*/
    
    
    
//no trikle code start

peerConnection.onicecandidate = function(event)  {
    if (event.candidate === null) {
        return send_sdp_to_remote_peer();
    }
};

peerConnection.oniceconnectionstatuschange = function(event) {
    if (peer.iceGatheringState === 'complete') {
        send_sdp_to_remote_peer();
    }
};


var isSdpSent = false;

function send_sdp_to_remote_peer() {
    if (isSdpSent) return;
    isSdpSent = true;
  
    var sdp = peerConnection.localDescription;
    //socket.emit('remote-sdp', sdp);
                send({
                event : "offer",
                data : sdp
            });
}

//no trickle code end
    

    // creating data channel
    dataChannel = peerConnection.createDataChannel("dataChannel", {
        reliable : true
    });

    dataChannel.onerror = function(error) {
        console.log("Error occured on datachannel:", error);
    };

    // when we receive a message from the other peer, printing it on the console
    dataChannel.onmessage = function(event) {
        console.log("message:", event.data);
    };

    dataChannel.onclose = function() {
        console.log("data channel is closed");
    };
  
  	peerConnection.ondatachannel = function (event) {
        dataChannel = event.channel;
  	};

    peerConnection.onaddstream = function(event) {
        videoElement.srcObject = event.stream;
    };
    
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
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
    console.log("connection established successfully!!");
};

function sendMessage() {
    dataChannel.send(input.value);
    input.value = "";
}
