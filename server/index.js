var app = require("express")();
var server = require("http").Server(app);
var io = require("socket.io")(server);
var lobbies = {};
var validLobbyCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
var LOBBY_ID_LENGTH = 5;

server.listen(9999, function () {
  console.log("Server is now running on port 9999...");
});

io.on("connection", function (socket) {
  console.log("Player Connected!" + socket.id);

  // send to the particular client
  socket.emit("socketID", { id: socket.id });

  // send to all clients except the particular connected client
  socket.broadcast.emit("newPlayer", { id: socket.id });

  socket.on("disconnect", function () {
    console.log("Player Disconnected" + socket.id);
    socket.broadcast.emit("playerDisconnected", { id: socket.id });

    // TODO error handling
  });

  socket.on("create lobby", function (data) {
    console.log("data: " + data);
    const lobbyID = createNewLobby();
    console.log("creating lobby..." + lobbyID);
    socket.join(lobbyID);
    console.log(socket.id + " has joined lobby " + lobbyID);
    var player = {
      socketID: socket.id,
      characterType: data,
    };
    lobbies[lobbyID] = [player];
    socket.emit("lobby created", lobbyID);
  });

  socket.on("join lobby", function (data) {
    console.log("join lobby");
    // verify lobbyid
    console.log(data.lobbyID);
    const lobbyID = data.lobbyID;
    if (lobbies[lobbyID] == null) {
      console.log("lobby doesn't exist");
      socket.emit("invalid lobby id");
      return;
    }

    const characterType = data.chosenCharacter;
    if (characterAlreadyExists(lobbyID, characterType)) {
      console.log("character already chosen");
      socket.emit("character taken");
      return;
    }

    var player = {
      socketID: socket.id,
      characterType: data.chosenCharacter,
    };
    lobbies[lobbyID] = [...lobbies[lobbyID], player];
    //const playersJSON = createPlayersJSON(lobbyID);
    console.log("updating player : " + lobbies[lobbyID]);

    socket.emit("join lobby successful");
    socket.join(lobbyID);
    console.log(socket.id + " has joined lobby " + lobbyID);
    socket.to(lobbyID).emit("update players", lobbies[lobbyID]);
  });

  socket.on("get lobby players", function (data) {
    console.log("getting lobby players for " + socket.id);
    socket.emit("update players", lobbies[data]);
  });

  socket.on("player move", function (data) {
    const lobbyID = data.lobbyID;
    const players = lobbies[lobbyID];

    for (let player of players) {
      if (player.socketID === socket.id) {
        var moveInfo = {
          moveSocketId: socket.id,
          x: data.x,
          y: data.y,
        };
        socket.to(lobbyID).emit("update players move", moveInfo);
      }
    }
  });

  socket.on("start game", function (data) {
    socket.to(data).emit("start game");
  });

  function characterAlreadyExists(lobbyID, characterType) {
    console.log("checking character...");
    const players = lobbies[lobbyID];
    console.log("players: " + players);
    for (let player of players) {
      if (player.characterType == characterType) {
        return true;
      }
    }

    return false;
  }

  // function createPlayersJSON(lobbyID) {
  //     const players = lobbies[lobbyID];
  //     const playersJSON =
  //     for (var i = 0; i < players.length; i++) {
  //         const player = players[i];
  //         const playerString = {
  //             "socketID": player.socketID,
  //             "characterType": player.characterType
  //         }

  //     }
  // }

  function createNewLobby() {
    var id = "";
    for (var i = 0; i < LOBBY_ID_LENGTH; i++) {
      id += validLobbyCharacters.charAt(
        Math.random() * validLobbyCharacters.length
      );
    }

    lobbies[id] = {};
    return id;
  }
});
