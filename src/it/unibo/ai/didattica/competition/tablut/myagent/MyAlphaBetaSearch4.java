package it.unibo.ai.didattica.competition.tablut.myagent;


import java.util.List;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;


/*
 * Strettamente peggiore della versione 3
 * Il costo computazionale aggiuntivo della funzione di valutazione più complicata
 * supera il guadagna che fa ottenere
 */
public class MyAlphaBetaSearch4 extends IterativeDeepeningAlphaBetaSearch<State, Action, Turn> {
	
	public static final int [][]fughe = {{0,1}, {0,2}, {0,6}, {0,7}, {1,0}, {1,8}, {2,0}, {2,8}, {6,0}, {6,8}, {7,0}, {7,8}, {8,1}, {8,2}, {8,6}, {8,7}};
	
	public MyAlphaBetaSearch4(Game<State, Action, Turn> game, double utilMin, double utilMax, int time) {
		super(game, utilMin, utilMax, time);
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
	
	public double evalLontananzaReMedia(State stato, int[] re) {
		double somma_distanze = 0.0F;
		int num_bianchi = 0;
		
		// scorro la scacchiera alla ricerca di pezzi bianchi
		for (int i = 0; i < stato.getBoard().length; i++) {
			for (int j = 0; j < stato.getBoard().length; j++) {
				if (stato.getPawn(i, j).equalsPawn(State.Pawn.WHITE.toString())) {
					num_bianchi++;
					int distanzaRiga = re[0]-i;
					int distanzaColonna = re[1]-j;
					somma_distanze += Math.sqrt(distanzaRiga*distanzaRiga + distanzaColonna*distanzaColonna);
				}
			}
		}
		
		double lontananza_media = somma_distanze / num_bianchi;
		
		// normalizzo:
		// - lontananza minimia è quella di una pedina appiccicata su un lato del re
		// 		distanzaRiga = 1 (o 1); distanzaColonna = 0 (o 1)
		//		lontananzaMin = sqrt(1) = 1
		// - lontananza massima si ottiene quando re e una pedina sono dalle parti opposte della scacchiera
		// 		distanzaRiga = 7; distanzaColonna = 7; 
		// 		lontananzaMax = sqrt(98) ~= 10
		// 	- lontananza media varia tra [1 - 10]
		return 1.0 - (lontananza_media-1)/9;
	}
	
	public double evalNemici(State stato) {
		double val = 0.0;
		
		if(stato.getTurn().equalsTurn(Turn.WHITE.toString())) {
			int nemici = stato.getNumberOf(Pawn.BLACK);
			val = 1.0 - nemici/16.0;
		}
		else if(stato.getTurn().equalsTurn(Turn.BLACK.toString())) {
			int nemici = stato.getNumberOf(Pawn.WHITE);
			val = 1.0 - nemici/8.0;
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
			return oldEval;
		} 
		
		double stima = 0.0;
		int[] re = getKingPosition(stato);
		
		if(stato.getTurn().equalsTurn(Turn.WHITE.toString())) {
//			// più il re è vicino alle case di fuga, meglio il nodo viene valutato
			stima = evalNemici(stato)*0.3 + evalLontananzaReMedia(stato, re)*0.05 + evalVieDiFuga(stato, re)*0.65;
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
	
	
	// main di test
	public static void main(String args[]) {
		int repeated = 0;
		int cacheSize = -1;
		MyGame game = new MyGame(repeated, cacheSize);
		State stato = game.getInitialState();
		MyAlphaBetaSearch4 search = new MyAlphaBetaSearch4(game, -0.1, 1.1, 60);
		
		// faccio fare una mossa al nero dato che per qualche motivo parte lui nel costruttore base dello stato
		List<Action> azioni =  game.getActions(stato);
		System.out.println("\n\n" + azioni.get(10));
		State newState = game.getResult(stato, azioni.get(10));
		System.out.println(newState + "\n\n");
		
		int[] re = search.getKingPosition(stato);
		System.out.println("LONTANANZA MEDIA: " + search.evalLontananzaReMedia(stato, re));
		
		
		
	
		
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
		
		re = search.getKingPosition(stato);
		System.out.println("vieDiFuga: " + search.getVieDiFuga(stato, re) + "; normalizzata: " + search.evalVieDiFuga(stato, re));
		
		System.out.println("LONTANANZA MEDIA: " + search.evalLontananzaReMedia(stato, re));
	}
}
