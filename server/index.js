var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var playerSockets = [];

server.listen(9999, function(){
    console.log("Server is now running on port 9999...");
})

io.on('connection', function(socket) {
    console.log("Player Connected!");


    // send to the particular client
    socket.emit('socketID', { id: socket.id });
    socket.emit('currentPlayers', playerSockets);


    // send to all clients except the particular connected client
    socket.broadcast.emit('newPlayer', { id: socket.id })

    playerSockets.push(socket)
    console.log(playerSockets.length)

    socket.on('disconnect', function() {
        console.log("Player Disconnected")
        socket.broadcast.emit('playerDisconnected', { id: socket.id })

        playerSockets = playerSockets.filter(function(value, index, arr) {
            value.id != socket.id
        })

        console.log(playerSockets.length)
    })
})