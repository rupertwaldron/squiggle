// ============= WebSocket Connection =============

// Create a WebSocket connection
const socket = new WebSocket('ws://localhost:8080/websocket');

// Event listener for when the connection is opened
socket.onopen = function() {
    console.log('WebSocket connection established');
};

// Event listener for incoming messages

socket.onmessage = function(event) {
    if (isArtist) {
        console.warn('You are the artist, not receiving messages');
        return;
    }
    const data = JSON.parse(event.data);
    console.log('Received data:', data[0]);
    if (data.action === 'mouseup') {
        // ctx.stroke();
        ctx.beginPath();
        console.log('Mouse up event received');
    } else if (data.action === 'mousemove') {
        receiveDraw(data);
    }
};

// Event listener for errors
socket.onerror = function(event){
    console.error('WebSocket error:', event);
};

// Event listener for connection close
socket.onclose = function() {
    console.log('WebSocket connection closed');
};

// Function to send a message when the button is clicked
function sendMessage(message) {
    if (!isArtist) {
        console.warn('You are not the artist');
        return;
    }
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(message);
        console.log('Sent:', message);
    } else {
        console.warn('WebSocket is not open');
    }
}

// HTML part
const button = document.createElement('button');
button.textContent = 'Send Message';
button.onclick = sendMessage;
document.body.appendChild(button);
