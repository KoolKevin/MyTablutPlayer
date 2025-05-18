package it.unibo.ai.didattica.competition.tablut.myagent;


import java.util.ArrayList;
import java.util.List;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class MyAlphaBetaSearch3 extends IterativeDeepeningAlphaBetaSearch<State, Action, Turn> {
	
	public static final int [][]fughe = {{0,1}, {0,2}, {0,6}, {0,7}, {1,0}, {1,8}, {2,0}, {2,8}, {6,0}, {6,8}, {7,0}, {7,8}, {8,1}, {8,2}, {8,6}, {8,7}};
	public Timer timer;
	private boolean heuristicEvaluationUsed; // indicates that non-terminal
	
	public MyAlphaBetaSearch3(Game<State, Action, Turn> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
		this.timer = new Timer(time);
	}
	
	
	public int[] getKingPosition(State stato) {
		int[] re = new int[2];
		
		// TODO: forse posso mantenere la posizione corrente del re salvata da qualche parte invece di ricalcolarmela
		outerloop:
		for (int i = 0; i < stato.getBoard().length; i++) {
			for (int j = 0; j < stato.getBoard().length; j++) {
				if (stato.getPawn(i, j).equalsPawn(State.Pawn.KING.toString())) {
					re[0] = i;
					re[1] = j;
					break outerloop;
				}
			}
		}
		
		return re;
	}
	
	public int getVieDiFuga(State stato, int[] re) {
		// controllo se il re si trova nel blocco 3x3 centrale in cui non ci sono vie di fuga
		if(re[0]!=1 && re[0]!=2 && re[0]!=6 && re[0]!=7 && re[1]!=1 && re[1]!=2 && re[1]!=6 && re[1]!=7)
			return 0;

		int vie = 0;
		int boardDim = stato.getBoard().length;
		boolean trovatoOstacolo = false;
		// controllo in ognuna delle quattro direzioni se ci sono degli ostacoli
		
		// SINISTRA
		// controllo accampamenti e2, e8 (== (1,4) ed (7,4))
		if( (re[0]==1 && re[1] > 4) || (re[0]==7 && re[1] > 4) ) {
			trovatoOstacolo=true;
		} 
		// controllo pedine
		else {
			for(int i=re[1]-1; i>=0; i--) {
				Pawn p = stato.getPawn(re[0], i);
				if(!p.equalsPawn(State.Pawn.EMPTY.toString())) {
					trovatoOstacolo=true;
					break;
				}
				
			}
		}
		if(!trovatoOstacolo) {
			vie++;
		}
		trovatoOstacolo=false;
		
	
	
		// DESTRA
		// controllo accampamenti e2, e8 (== (1,4) ed (7,4))
		if( (re[0]==1 && re[1] < 4) || (re[0]==7 && re[1] < 4) ) {
			trovatoOstacolo=true;
		} 
		// controllo pedine
		else {
			for(int i=re[1]+1; i<boardDim; i++) {
				Pawn p = stato.getPawn(re[0], i);
				if(!p.equalsPawn(State.Pawn.EMPTY.toString())) {
					trovatoOstacolo=true;
					break;
				}
				
			}
		}
		if(!trovatoOstacolo) {
			vie++;
		}
		trovatoOstacolo=false;
		
		
		
		// SOPRA
		// controllo accampamenti b5, h5 (== (4, 1) ed (4, 7))
		if( (re[1]==1 && re[0] > 4) || (re[1]==7 && re[0] > 4) ) {
			trovatoOstacolo=true;
		} 
		// controllo pedine
		else {
			for(int i=re[0]-1; i>=0; i--) {
				Pawn p = stato.getPawn(i, re[1]);
				if(!p.equalsPawn(State.Pawn.EMPTY.toString())) {
					trovatoOstacolo=true;
					break;
				}
				
			}
		}
		if(!trovatoOstacolo) {
			vie++;
		}
		trovatoOstacolo=false;
		
		
		
		
		// SOTTO
		// controllo accampamenti b5, h5 (== (4, 1) ed (4, 7))
		if( (re[1]==1 && re[0] < 4) || (re[1]==7 && re[0] < 4) ) {
			trovatoOstacolo=true;
		} 
		// controllo pedine
		else {
			for(int i=re[0]+1; i<boardDim; i++) {
				Pawn p = stato.getPawn(i, re[1]);
				if(!p.equalsPawn(State.Pawn.EMPTY.toString())) {
					trovatoOstacolo=true;
					break;
				}
				
			}
		}
		if(!trovatoOstacolo) {
			vie++;
		}
		trovatoOstacolo=false;		

		return vie;
	}
	
	public double evalVieDiFuga(State stato, int[] re) {
		int vie = getVieDiFuga(stato, re);
		
		// normalizzo considerando due vie di fuga come massimo (se ne ho due ho già vinto)
		return vie/2.0;
	}
	
	public double evalSbilanciamenti(State stato, int[] re) {
		double bestDistanza = 1000.0F;
		
		for(int[] fuga : fughe) {
			int distanzaX = re[0]-fuga[0];
			int distanzaY = re[1]-fuga[1];
			double distanza = Math.sqrt( Math.pow(distanzaX, 2.0) + Math.pow(distanzaY, 2.0));
			
			if (distanza < bestDistanza)
				bestDistanza = distanza;
		}
		
		return bestDistanza;
	}
	
	public double evalNemici(State stato) {
		double val = 0.0;
		
		if(stato.getTurn().equalsTurn(Turn.WHITE.toString())) {
			int nemici = stato.getNumberOf(Pawn.BLACK);
			val = 1.01 - nemici/16.0; // 1.01 per rimanere sopra a utilMin e quindi non interrompere prematuramente
		}
		else if(stato.getTurn().equalsTurn(Turn.BLACK.toString())) {
			int nemici = stato.getNumberOf(Pawn.WHITE);
			val = 1.01 - nemici/8.0;
		} 
		
		return val;
	}
	
	public int getKingAttackingPawns(State stato, int[] re) {
		int conta = 0;
		
		int giu = re[0]-1;
		int su = re[0]+1;
		int sx = re[1]-1;
		int dx = re[1]+1;
		
		int boardDim = stato.getBoard().length;
		
		if( giu < boardDim && stato.getPawn(giu, re[1]).equalsPawn(State.Pawn.BLACK.toString()) )
			conta++;
		if( su >= 0 && stato.getPawn(su, re[1]).equalsPawn(State.Pawn.BLACK.toString()))
			conta++;
		if( sx >= 0 && stato.getPawn(re[0], sx).equalsPawn(State.Pawn.BLACK.toString()))
			conta++;
		if( dx < boardDim && stato.getPawn(re[0], dx).equalsPawn(State.Pawn.BLACK.toString()))
			conta++; 
		
		return conta;	
	}
	
	public double evalKingAttackingPawns(State stato, int[] re) {
		// se il re 
		// - è nel castello ho bisogno di 4 attaccanti
		// - è adiacente al castello ho bisogno di 3 attaccanti
		int maxAttackers;
		
		if(re[0] == 4 && re[1]==4) {
			maxAttackers = 4;
		} else {
			maxAttackers = 3;
		}
		
		int attackers = getKingAttackingPawns(stato, re);
		
		return (1.0/maxAttackers) * attackers;
	}

	@Override
	public double eval(State stato, Turn player) {
		double oldEval = super.eval(stato, player); // questo per side-effect
		if (game.isTerminal(stato)) {
			System.out.println(stato);
			System.out.println("\t\tterminal");
			return oldEval;
		} 
		
		System.out.println("\t\teval chiamata "+ heuristicEvaluationUsed);
		heuristicEvaluationUsed = true;
		double stima = 0.0;
		int[] re = getKingPosition(stato);
		
		if(stato.getTurn().equalsTurn(Turn.WHITE.toString())) {
//			// più il re è vicino alle case di fuga, meglio il nodo viene valutato
			stima = evalNemici(stato)*0.3 + evalVieDiFuga(stato, re)*0.7;
		}
		else if(stato.getTurn().equalsTurn(Turn.BLACK.toString())) {
			stima = evalNemici(stato)*0.5 + evalKingAttackingPawns(stato, re)*0.5;
		} 
		else {
			System.out.println("PANICO | ho chiamato eval mentre sono in un nodo NON foglia, durante un turno diverso da bianco o da nero!");
			System.out.println(stato);
			System.exit(-1);
		}
		
		return stima;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Overrido roba per fare debug
	@Override
	public Action makeDecision(State state) {
		StringBuffer logText = null;
		Turn player = game.getPlayer(state);
		List<Action> results = orderActions(state, game.getActions(state), player, 0);
		
		
		timer.start();
		currDepthLimit = 0;
		do {
			incrementDepthLimit();
//			if (logEnabled)
				logText = new StringBuffer("DEPTH " + currDepthLimit + ": \n");
				
			heuristicEvaluationUsed = false;
			ActionStore<Action> newResults = new ActionStore<Action>();
			for (Action action : results) {
				double value = minValue(game.getResult(state, action), player, Double.NEGATIVE_INFINITY,
						Double.POSITIVE_INFINITY, 1);
				if (timer.timeOutOccured()) {
					System.out.println("\n\nTIMEOUT MENRE STO VALUTANDO LE MOSSE\n\n");
					break; // exit from action loop
				}
				newResults.add(action, value);
//				if (logEnabled)
					logText.append("\t"+action + "->" + value + "\n");
			}
//			if (logEnabled)
				System.out.println(logText);
			if (newResults.size() > 0) {
				results = newResults.actions;
				if (!timer.timeOutOccured()) {
					if (hasSafeWinner(newResults.utilValues.get(0))) {
						System.out.println("\n\nsafeWinner\n\n");
						break; // exit from iterative deepening loop
					}
					else if (newResults.size() > 1 
							&& isSignificantlyBetter(newResults.utilValues.get(0), newResults.utilValues.get(1))) {
						System.out.println("\n\nsignificantly better\n\n");
						break; // exit from iterative deepening loop
					}
				}
			}
		} while (!timer.timeOutOccured() /*&& heuristicEvaluationUsed*/);
		

		return results.get(0);
	}
	
	// returns an utility value
	@Override
	public double maxValue(State state, Turn player, double alpha, double beta, int depth) {
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccured()) {
			return eval(state, player);
		} else {
			double value = Double.NEGATIVE_INFINITY;
			for (Action action : orderActions(state, game.getActions(state), player, depth)) {
				value = Math.max(value, minValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value >= beta)
					return value;
				alpha = Math.max(alpha, value);
			}
			return value;
		}
	}
	
	// returns an utility value
	@Override
	public double minValue(State state, Turn player, double alpha, double beta, int depth) {
		if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccured()) {
			return eval(state, player);
		} else {
			double value = Double.POSITIVE_INFINITY;
			for (Action action : orderActions(state, game.getActions(state), player, depth)) {
				value = Math.min(value, maxValue(game.getResult(state, action), //
						player, alpha, beta, depth + 1));
				if (value <= alpha)
					return value;
				beta = Math.min(beta, value);
			}
			return value;
		}
	}

	
	@Override
	protected boolean hasSafeWinner(double resultUtility) {
		if(resultUtility <= utilMin || resultUtility >= utilMax) {
			System.out.println("\n\n" + resultUtility + " è un safe winner!!!\n\n");
			return true;
		}
		
		return false;
	}
	
	
	private static class Timer {
		private long duration;
		private long startTime;

		Timer(int maxSeconds) {
			this.duration = 1000l * maxSeconds;
		}

		void start() {
			startTime = System.currentTimeMillis();
		}

		boolean timeOutOccured() {
			return System.currentTimeMillis() > startTime + duration;
		}
	}

	/** Orders actions by utility. */
	private static class ActionStore<ACTION> {
		private List<ACTION> actions = new ArrayList<ACTION>();
		private List<Double> utilValues = new ArrayList<Double>();

		void add(ACTION action, double utilValue) {
			int idx;
			for (idx = 0; idx < actions.size() && utilValue <= utilValues.get(idx); idx++)
				;
			actions.add(idx, action);
			utilValues.add(idx, utilValue);
		}

		int size() {
			return actions.size();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	// main di test
	public static void main(String args[]) {
		int repeated = 0;
		int cacheSize = -1;
		MyGame game = new MyGame(repeated, cacheSize);
		State stato = game.getInitialState();
		MyAlphaBetaSearch3 search = new MyAlphaBetaSearch3(game, 0.0, 1.0, 60);
		
		// faccio fare una mossa al nero dato che per qualche motivo parte lui nel costruttore base dello stato
		List<Action> azioni =  game.getActions(stato);
		System.out.println("\n\n" + azioni.get(10));
		State newState = game.getResult(stato, azioni.get(10));
		System.out.println(newState + "\n\n");
		
		
		
		
		
	
		
		// controlliamo la valutazione delle vie di fuga
		Pawn[][] board = new Pawn[9][9];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				board[i][j] = Pawn.EMPTY;
			}
		}
		board[4][4] = Pawn.THRONE;
		board[3][3] = Pawn.KING;
		board[2][4] = Pawn.WHITE;
		board[3][4] = Pawn.WHITE;
		board[5][4] = Pawn.WHITE;
		board[6][4] = Pawn.WHITE;
		board[4][2] = Pawn.WHITE;
		board[4][3] = Pawn.WHITE;
		board[4][5] = Pawn.WHITE;
		board[4][6] = Pawn.WHITE;
		board[0][3] = Pawn.BLACK;
		board[0][4] = Pawn.BLACK;
		board[0][5] = Pawn.BLACK;
		board[1][4] = Pawn.BLACK;
		board[8][3] = Pawn.BLACK;
		board[8][4] = Pawn.BLACK;
		board[8][5] = Pawn.BLACK;
		board[7][4] = Pawn.BLACK;
		board[3][0] = Pawn.BLACK;
		board[4][0] = Pawn.BLACK;
		board[5][0] = Pawn.BLACK;
		board[4][1] = Pawn.BLACK;
		board[3][8] = Pawn.BLACK;
		board[4][8] = Pawn.BLACK;
		board[5][8] = Pawn.BLACK;
		board[4][7] = Pawn.BLACK;
		stato.setBoard(board);
		
		System.out.println(stato + "\n\n");
		
		int[] re = search.getKingPosition(stato);
		System.out.println("vieDiFuga: " + search.getVieDiFuga(stato, re) + "; normalizzata: " + search.evalVieDiFuga(stato, re));
		
		
	}
}
