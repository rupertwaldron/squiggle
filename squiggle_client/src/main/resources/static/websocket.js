// ============= WebSocket Connection =============

// Create a WebSocket connection
const socket = new WebSocket('ws://localhost:8080/websocket');

// Event listener for when the connection is opened
socket.onopen = function() {
    console.log('WebSocket connection established');
};

// Event listener for incoming messages

socket.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('Received data:', data[0]);
    receiveDraw(data);
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
