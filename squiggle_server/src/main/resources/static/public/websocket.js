// ============= WebSocket Connection =============

// Create a WebSocket connection
const socket = new WebSocket('wss://1c24-86-17-172-4.ngrok-free.app/websocket');

// Event listener for when the connection is opened
socket.onopen = function() {
    console.log('WebSocket connection established');
};

// Event listener for incoming messages

socket.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('Received data:', data);
    if (data.action === 'mouseup') {
        // ctx.stroke();
        ctx.beginPath();
        console.log('Mouse up event received');
    } else if (data.action === 'mousemove') {
        receiveDraw(data);
    } else if (data.action === 'artist') {
        console.log('Artist is now:', data.playerId);
        isArtist = false;
        updateArtistBtn()
        guessBoxes(data.guessWord)
    } else if (data.action === 'reveal') {
        console.log('Reveal word:', data.guessWord);
        revealLetters(data.guessWord)
    }
    else if (data.action === 'not-artist') {
        console.log('Artist is now not:', data.playerId);
    } else if (data.action === 'winner') {
        if (playerId === data.playerId) {
            console.log('You are the winner:', data.playerId);
        } else if (isArtist) {
            isArtist = false;
            updateArtistBtn();
            console.log('This winner is:', data.playerId);
        } else {
            console.log('You are a looser, winner is:', data.playerId);
        }
        clearDrawing();
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
