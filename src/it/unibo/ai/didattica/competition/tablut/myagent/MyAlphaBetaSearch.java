package it.unibo.ai.didattica.competition.tablut.myagent;


import java.util.List;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class MyAlphaBetaSearch extends IterativeDeepeningAlphaBetaSearch<State, Action, Turn> {
	
	public static final int [][]fughe = {{0,1}, {0,2}, {0,6}, {0,7}, {1,0}, {1,8}, {2,0}, {2,8}, {6,0}, {6,8}, {7,0}, {7,8}, {8,1}, {8,2}, {8,6}, {8,7}};
	
	public MyAlphaBetaSearch(Game<State, Action, Turn> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
	}
	
	
	public int[] getKingPosition(State stato) {
		int[] re = new int[2];
		
		// TODO: forse posso mantenere la posizione corrente del re salvata da qualche parte invece di ricalcolarmela
		if(stato.getTurn().equalsTurn(Turn.WHITE.toString())) {
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
		}
		else {
			System.out.println("PANICO | ho chiamato getKingPosition() durante un turno diverso da quello del bianco");
			System.exit(-1);
		}
		
		return re;
	}
	
	public double getEscapeDistance(State stato, int[] re) {
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
	
	public double evalEscapeDistance(State stato, int[] re) {
		double distanza = getEscapeDistance(stato, re);
		// normalizzo per la distanza massima (quella iniziale -> (distanzaX=4; distanzaY=2))
		return 1.0 - distanza/Math.sqrt(20.0);
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
	
	@Override
	public double eval(State stato, Turn player) {
		double oldEval = super.eval(stato, player); // questo per side-effect
		if (game.isTerminal(stato)) {
			return oldEval;
		} 
		
		double stima = 0.0;
		
		if(stato.getTurn().equalsTurn(Turn.WHITE.toString())) {
//			// più il re è vicino alle case di fuga, meglio il nodo viene valutato
			int[] re = getKingPosition(stato);

			stima = evalNemici(stato)*0.3 + evalEscapeDistance(stato, re)*0.7;
		}
		else if(stato.getTurn().equalsTurn(Turn.BLACK.toString())) {
			stima = evalNemici(stato);
		} 
		else {
			System.out.println("PANICO | ho chiamato eval mentre sono in un nodo NON foglia, durante un turno diverso da bianco o da nero!");
			System.out.println(stato);
			System.exit(-1);
		}
		
		return stima;
	}
	
	
	// main di test
	public static void main(String args[]) {
		int repeated = 0;
		int cacheSize = -1;
		MyGame game = new MyGame(repeated, cacheSize);
		State stato = game.getInitialState();
		MyAlphaBetaSearch search = new MyAlphaBetaSearch(game, 0.0, 1.0, 60);
		
		// faccio fare una mossa al nero dato che per qualche motivo parte lui nel costruttore base dello stato
		List<Action> azioni =  game.getActions(stato);
		System.out.println("\n\n" + azioni.get(10));
		State newState = game.getResult(stato, azioni.get(10));
		System.out.println(newState + "\n\n");
		
		// controlliamo la valutazione iniziale della distanza
		int[] re = search.getKingPosition(newState);
		System.out.println("distanza: " + search.getEscapeDistance(newState, re) + "; normalizzata: " + search.evalEscapeDistance(newState, re));
		
		
	}
}
