package it.unibo.ai.didattica.competition.tablut.myagent;


import java.io.IOException;
import java.util.List;

import aima.core.search.adversarial.Game;
import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class MyAlphaBetaSearch3 extends IterativeDeepeningAlphaBetaSearch<State, Action, Turn> {
	
	public static final int [][]fughe = {{0,1}, {0,2}, {0,6}, {0,7}, {1,0}, {1,8}, {2,0}, {2,8}, {6,0}, {6,8}, {7,0}, {7,8}, {8,1}, {8,2}, {8,6}, {8,7}};
	
	public MyAlphaBetaSearch3(Game<State, Action, Turn> game, double utilMin, double utilMax, int time) {
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

		if(stato.getTurn().equalsTurn(player.toString()) )
			return stima;
		else
			return -stima;
	}
	
	
	// main di test
	public static void main(String args[]) {
		int repeated = 0;
		int cacheSize = -1;
		MyGame game = new MyGame(repeated, cacheSize);
		State stato = game.getInitialState();
		MyAlphaBetaSearch3 search = new MyAlphaBetaSearch3(game, -1.0, 1.0, 4);
		search.setLogEnabled(true);
		
		Pawn[][] board = new Pawn[9][9];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				board[i][j] = Pawn.EMPTY;
			}
		}
		board[0][3] = Pawn.BLACK;
		board[0][4] = Pawn.BLACK;
		board[0][5] = Pawn.BLACK;
		board[1][2] = Pawn.BLACK;
		board[1][6] = Pawn.WHITE;
		board[3][4] = Pawn.BLACK;
		board[3][8] = Pawn.BLACK;
		board[4][0] = Pawn.BLACK;
		board[4][3] = Pawn.BLACK;
		board[4][4] = Pawn.KING;
		board[4][5] = Pawn.WHITE;
		board[4][7] = Pawn.BLACK;
		board[4][8] = Pawn.BLACK;
		board[5][4] = Pawn.BLACK;
		board[6][5] = Pawn.BLACK;
		board[7][4] = Pawn.BLACK;
		board[8][4] = Pawn.BLACK;
		stato.setBoard(board);
		
		System.out.println(stato + "\n\n");
		System.out.println("eval: " + search.eval(stato, Turn.BLACK));
		
		
		Action mossa = search.makeDecision(stato);
		System.out.println(mossa);
		
		try {
			mossa = new Action("h5", "g5", Turn.BLACK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			stato = game.applyValidMove(stato, mossa);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(stato + "\n\n");
		System.out.println("eval: " + search.eval(stato, Turn.BLACK));
	}
}
