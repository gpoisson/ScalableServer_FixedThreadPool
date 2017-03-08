# cs455_pa2
################################
PROGRAMMING ASSIGNMENT 2
CS455
AUTHOR: GREGORY POISSON
################################

DATA STRUCTURES
	For this assignment, I implemented a few data structures to simplify the design. They are as follows:
	
	HASH MESSAGE
		This is the 8KB message passed from the client to the server. Despite the name, it isn't actually hashed,
		it is just random data. When this data arrives at the server, it gets hashed into a code which is returned
		to the client.
		
	NODE
		This is an interface for the Server and Client. It contains only a boolean to turn debug mode on or off.
		If debug mode is turned on, the Server and Client run very slowly in order to allow debug information to be
		readable on the console. Debug mode is turned off by default.
		
	CLIENT COMMS
		This is just an object to contain the communication code relevant to the client. After the client main thread
		executes, it creates a ClientComms object which communicates with the server.
		
	THREAD POOL MANAGER
		This maintains a fixed size array of Worker Threads, as specified in the assignment
		
	WORKER THREAD
		The threads which perform the majority of the processing. These threads live for the duration of the program
		and do not get re-instantiated.
		
	ACCEPT INCOMING TRAFFIC TASK
		This is the task type that is queued when data is incoming from the client
		
	COMPUTE HASH TASK
		This is the task type that is assigned to the worker thread after it has completed an AcceptIncomingTrafficTask
		
	REPLY TO CLIENT TASK
		This is the task type that is queued when a hash code is ready to be sent back to the client
		
	TASK
		The abstract class extended by the other Task objects, as specified in the assignment
		
	HASH COMPUTER
		This is used by the server and client to compute hash codes
		
	STAT TRACKER
		References to this object are distributed to the worker threads to keep track of throughput, and it is also
		used by the clients to track their own throughput
		
		
From my tests, everything runs the way it ought to with 100 clients sending 4 messages per second. If I try to scale 
up to 200 clients, the server starts to choke on the amount of traffic.

If any issues are encountered while grading, feel free to contact me:  gpoisson@rams.colostate.edu

Thanks,

Greg Poisson