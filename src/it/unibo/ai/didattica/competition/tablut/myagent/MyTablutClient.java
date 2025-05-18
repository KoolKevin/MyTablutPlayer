package it.unibo.ai.didattica.competition.tablut.myagent;

import java.io.IOException;
import java.net.UnknownHostException;

import it.unibo.ai.didattica.competition.tablut.domain.*;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;


/**
 * 
 * @author A. Piretti, Andrea Galassi
 *
 */
public class MyTablutClient extends TablutClient {

	public MyTablutClient(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
	}
	
	public MyTablutClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, "random", timeout, ipAddress);
	}

	public MyTablutClient(String player) throws UnknownHostException, IOException {
		this(player, "random", 60, "localhost");
	}
	
	@Override
	public void run() {
		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int repeated = 0;
		int cacheSize = -1;
		double utilMin = 0.0;
		double utilMax = 1.0;
		int timeout = this.getTimeout()-1;	// -1 per stare larghi e considerare il tempo di risposta
		MyGame game = new MyGame(repeated, cacheSize);
		MyAlphaBetaSearch3 search = new MyAlphaBetaSearch3(game, utilMin, utilMax, timeout);
		search.setLogEnabled(true);
		
		System.out.println("Ashton Tablut game");
		System.out.println("You are player " + this.getPlayer().toString() + "!");
		
		while (true) {
			// leggo lo stato dal server
			try {
				this.read();
			} catch (ClassNotFoundException | IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			System.out.println("Current state:");
			State state = this.getCurrentState();
			System.out.println(state.toString());

			if (this.getPlayer().equals(Turn.WHITE)) {
				// mio turno
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) {
					// penso alla mia mossa
					Action nextMove = search.makeDecision(state);
					System.out.println("Mossa scelta: " + nextMove.toString());
					State nextState = null;
					try {
						nextState = game.applyValidMove(state, nextMove);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// se qualcosa è stato catturato libero la cache dei pareggi
					int numPedinePrima = this.getCurrentState().getNumberOf(Pawn.WHITE) + this.getCurrentState().getNumberOf(Pawn.BLACK);
					int numPedineDopo = nextState.getNumberOf(Pawn.WHITE) + nextState.getNumberOf(Pawn.BLACK);
					if (numPedineDopo < numPedinePrima)
						game.getDrawConditions().clear();
					// salvo lo stato relativo alla mossa scelta nella cache dei pareggi
					game.getDrawConditions().add(nextState);
					
					// mando la mossa al server
					try {
						this.write(nextMove);
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// Turno dell'avversario
				else if (state.getTurn().equals(StateTablut.Turn.BLACK)) {
					System.out.println("Waiting for your opponent move... ");
				}
				// ho vinto
				else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				}
				// ho perso
				else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				}
				// pareggio
				else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			} 
			// sono il nero
			else {
				// Mio turno
				if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {
					// penso alla mia mossa
					Action nextMove = search.makeDecision(state);
					
					System.out.println("Mossa scelta: " + nextMove.toString());
					try {
						this.write(nextMove);
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (state.getTurn().equals(StateTablut.Turn.WHITE)) {
					System.out.println("Waiting for your opponent move... ");
				} else if (state.getTurn().equals(StateTablut.Turn.WHITEWIN)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.BLACKWIN)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				} else if (state.getTurn().equals(StateTablut.Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			}
		}
	}
	
	
	
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {
		String role = "WHITE";
		String name = "random";
		String ipAddress = "localhost";
		int timeout = 5;

		if (args.length < 1) {
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.exit(-1);
		} else {
			System.out.println(args[0]);
			role = args[0].toUpperCase();
		}
		if (args.length == 2) {
			System.out.println(args[1]);
			timeout = Integer.parseInt(args[1]);
		}
		if (args.length == 3) {
			ipAddress = args[2];
		}
		System.out.println("Selected client: " + args[0]);

		MyTablutClient client = new MyTablutClient(role, name, timeout, ipAddress);
		client.run();
	}
}
