Student: Oleksandr Bieliakov s18885 Group 11c

All required functionalities have been implemented as follows. 

SERVER process is implemented in the classes: 

	1. Server (main( )) 
		- creates UDPServer(used for communication with viewers) and starts listening on it
		- creates ServerSocket, accepts client Sockets (players) in a loop and creates ServerThreads for each of the accepted clients

	2. ServerLogic
		- used to store, update and access information about the logged in players
		- used during matchmaking by creating and temporary storing a Match in the field "lobby"
		
	3. Match
		- contains information about the match between two players and methods of the TickTackToe game logic(board update, win condition check etc.)
		
	4. ServerThread
		- is created for each player and used for communication with this player using TCP Socket
		- this class contains all methods of the COMMUNICATION PROTOCOL (with players)
		- uses ServerLogic for matchmaking and accessing the list of logged in players
		- uses Match for utilizing the game flow logic
		
	5. UDPServer
		- creates DatagramSocket
		- method "listen()" receives datagrams from viewers and adds those viewers to the list of viewers(Viewer Entry)
		- method "broadcast(String message)" is called from the ServerThread during communication with players,
		  method "broadcast(String message)" broadcast messages to all viewers from the list of viewers by sending UDP datagrams to them
		  
	Auxiliary classes:
	
		5. PlayerEntry
			- contains information about the logged in players
		
		6. ViewerEntry
			- contains information about the active viewers
		
		7. Command
			- Enumerator of commands which can be received from the user (LIST, PLAY, LOGOUT)
		
		8. UDP
			- Contains Datagram configuration
		
		

CLIENT process is implemented in the classes:

	1. GameConsole (main( )) 
		- creates ClientSocket
		- scans in a loop commands from the user and executes them using the client logic methods contained in this class
		- client logic methods of this class call the COMMUNICATION PROTOCOL methods of the class ClientSocket
		- displays the messages of the communication with server
		- displays a board of a current match
		
	2. ClientSocket
		- creates TCP Socket
		- this class contains all methods of the COMMUNICATION PROTOCOL (with server)
	
	Auxiliary classes:
	
		3. Command
			- Enumerator of commands which can be received from the user (LIST, PLAY, LOGOUT)
			
	
	
VIEWER process is implemented in the class:

	1. Viewer (main( ))
		- creates UDP DatagramSocket
		- sends to the server a datagram which informs the server that this viewer is active and wants to receive messages from the server
		- in a loop receives messages from the server and prints them
		


--- COMMUNICATION PROTOCOL:

	ClientSocket:
		1. creates TCP Socket
		2. creates PrintWriter and BufferedReader from the Socket output and input streams
		3. right after creation of the Socket receives the playerId from the server in the method "receiveID()" by parsing an integer from the read message
		
		Method "listOfPlayers()":
			4.1 prints to the Socket output a message "LIST" (retrieved from the Command enum)
			4.2 in the loop reads lines from the Socket input and from each line creates entities of the class PlayerEntry
			4.3 adds all the created PlayerEntries to the list and returns the list
			4.4 leaves the loop when received the line containing "END"
			
		Method "requestOpponent()":
			5.1 prints to the Socket output a message "PLAY" (retrieved from the Command enum)
			5.2 reads a line from the Socket input
			5.3 if the line is not "NOT FOUND", splits the message and parses two ints: (1) opponent's playerId and (2) 1 or 2 if player has first or second turn respectively
			5.4 adds these two ints to the list and returns the list
			
		Method "makeTurn()":
			6.1 prints to the Socket output a message containing a player's next turn cell index
			
		Method "getGameStatus()":
			7.1 reads a line from the Socket input
			7.2 splits the message and parses: (1) opponent's last turn cell index and adds it to the list(for return)
			7.3 if after splitting there are more more than one part of the message, checks if the second part is "WIN", "LOSE", "DRAW" and adds to the list 1,2 or 3 respectively
			7.4 returns the list
			
		Method "logout()":
			8.1 prints to the Socket output a message "LOGOUT" (retrieved from the Command enum)
			8.2 closes the Socket by calling the method "close()"
			
			
	
	ServerThread:
		1. receives TCP Socket and instances of the classes ServerLogic and UDPServer as constructor parameters
		2. creates PrintWriter and BufferedReader from the Socket output and input streams
		
		Method "play()":
			3.1 executes logs in a player by calling the method "addPlayer()"
			3.2 in a loop calls method "receiveCommand()" which returns a command
			3.3 depending on the received command calls of the methods: "list()", "play()", "logout()"
			3.4 closes the Socket by calling the method "close()" after exiting the loop

		Method "addPlayer()":
			4.1 generates a playerID by calling the method "nextID()" of the class ServerLogic
			4.2 adds a new player with this playerID to the ServerLogic by calling its method "(addPlayer())"
			4.3 prints to the Socket output a message containing the added playerID
			4.4 calls the method "broadcast()" of the class UDPServer providing a message "Player <playerID> logged in"
		
		Method "receiveCommand()":
			5.1 reads a line from the Socket input
			5.2 returns the received command

		Method "list()":
			6.1 for each PlayerEntry stored in the ServerLogic prints to the Socket output a line with the information about a PlayerEntry (toString())
			6.2 prints to the Socket output a message "END"
			
		Method "play()"
			7.1 calls the method "requestMatchmaking()"
			7.2 if the player has first turn calls "receiveTurn()"
			7.3 in a loop checks if the game has finished by calling "sendGameStatus()" and makes a turn by calling "receiveTurn()"
			
		Method "requestMatchmaking()":
			8.1	creates a match by calling ServerLogic's methods "firstRequestMatchmaking()" and "reRequestMatchmaking()"
			8.2 prints to the Socket output a message containing a match opponent playerID and 1 or 2 if its players first or second turn respectively
			8.3 calls the method "broadcast()" of the class UDPServer providing a message "Match <matchID> started (<playerID>)"
			
		Method "receiveTurn()":
			9.1 reads line from the Socket input and parses int from it, which is player's turn cell index
			9.2 submits the turn to by calling the method "submitTurn()" of the class Match
			9.3 calls the method "broadcast()" of the class UDPServer providing a message "Match <matchID> player <playerID> (<X or 0>) - <cell>"

		Method "sendGameStatus()":
			10.1 waits until an opponent makes his or her turn
			10.2 prints to the Socket output a message containing the cell index of the last opponent's turn 
				 and, if the match ended, appended "WIN"/"LOSE"/"DRAW
			10.3 also, if the match ended, calls the method "broadcast()" of the class UDPServer providing a message "Match <matchID> player <playerID> <lost/won/draw>"
			
		Method "logout()":
			11.1 removes player's PlayerEntry from the ServerLogic
			11.2 calls the method "broadcast()" of the class UDPServer providing a message "Player <playerID> logged out"
		


--- HOW TO RUN:
	
	1. Run the class Server (main( )) with program arguments containing (1) port number for TCP, (2) port number for UDP
	
	2. Run any number of instances of the class Viewer (main( )) with program arguments containing (1) server address (2) server port number for UDP
	
	3. Run any number of of instances the class GameConsole (main( )) with program arguments containing (1) server address (2) server port number for TCP
	
		
		
--- HOW TO USE:

	GameConsole:
		1. In any of the opened instances of the class GameConsole enter one of the offered commands to the console and wait for the response
		
		2. Enter another command when offered
		
			3. If you entered the PLAY command, another player should also enter PLAY command in his or her GameConsole
			
			4. When it happens both players will receive the information that the match has started and the information about who has the first turn (plays as X)
			
			5. After that a player whose turn it currently is will be prompted to enter a number of a cell for his or her turn
				(the cells are enumerated from left to right, from top to bottom)
				
			6. When an opponent makes his or her turn, the updated board will be displayed in both players' consoles
			
			7. If a player's turn results in win/lose/draw, the corresponding information will be displayed in both players' consoles
		
	Viewer: 
		1. messages about players who logged in/logged out, start of a match, each turn of each match and results of matches will be displayed in console
