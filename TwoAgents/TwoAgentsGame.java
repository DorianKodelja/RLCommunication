package TwoAgents;
import java.awt.*;
import javax.swing.*;

import old.SwingApplet;

public class TwoAgentsGame extends Thread {
	long delay;
	TwoAgentsApplet a;
	RLPolicy policy1,policy2;
	TwoAgentsWorld world;
	
	public boolean gameOn = false, single=false, gameActive, newInfo = false;
	
	public TwoAgentsGame(TwoAgentsApplet basicApplet, long delay, TwoAgentsWorld w, RLPolicy policy1, RLPolicy policy2) {
		world = w;
		
		a=basicApplet;
		this.delay = delay;
		this.policy1 = policy1;
		this.policy2 = policy2;
	}
	
	/* Thread Functions */
	public void run() {
		System.out.println("--Game thread started");
		// start game
		try {
			while(true) {
				while(gameOn) {
					gameActive = true;
					resetGame();
					SwingUtilities.invokeLater(a); // draw initial state
					runGame();
					gameActive = false;
					newInfo = true;
					SwingUtilities.invokeLater(a); // update state
					sleep(delay);
				}
				sleep(delay);
			}
		} catch (InterruptedException e) {
			System.out.println("interrupted.");
		}
		System.out.println("== Game finished.");
	}
	
	public void runGame() {
		while(!world.endGame()) {
			//System.out.println("Game playing. Making move.");
			int action1=-1;
			int action2=-1;
			action1 = policy1.getBestAction(world.getState1());
			action2 = policy2.getBestAction(world.getState2());
			world.getNextState(action1,action2);

			//a.updateBoard();
			SwingUtilities.invokeLater(a);
				
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				System.out.println("interrupted.");
			}
		}
		a.agent1score += world.agent1Score;
		a.agent2score += world.agent2Score;
		
		// turn off gameOn flag if only single game
		if (single) gameOn = false;
	}
	
	public void interrupt() {
		super.interrupt();
		System.out.println("(interrupt)");
	}
	
	/* end Thread Functions */

	public void setPolicy1(RLPolicy p) {	policy1 = p; }
	public void setPolicy2(RLPolicy p) {	policy2 = p; }
	
	public Dimension getAgent() { return new Dimension(world.mx, world.my); }
	public Dimension getAgent2() { return new Dimension(world.nx, world.ny); }
	public int getScoringArea() { return world.position; }

	public boolean[][] getWalls() { return world.walls; }


	public void resetGame() {
		world.resetState();
	}
}

