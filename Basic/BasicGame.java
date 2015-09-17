package Basic;
import java.awt.*;
import javax.swing.*;

import old.SwingApplet;

public class BasicGame extends Thread {
	long delay;
	BasicApplet a;
	RLPolicy policy;
	BasicWorld world;
	
	public boolean gameOn = false, single=false, gameActive, newInfo = false;
	
	public BasicGame(BasicApplet basicApplet, long delay, BasicWorld w, RLPolicy policy) {
		world = w;
		
		a=basicApplet;
		this.delay = delay;
		this.policy = policy;
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
			int action=-1;
			action = policy.getBestAction(world.getState());
			world.getNextState(action);

			//a.updateBoard();
			SwingUtilities.invokeLater(a);
				
			try {
				sleep(delay);
			} catch (InterruptedException e) {
				System.out.println("interrupted.");
			}
		}
		a.agentScore += world.agentScore;
		
		// turn off gameOn flag if only single game
		if (single) gameOn = false;
	}
	
	public void interrupt() {
		super.interrupt();
		System.out.println("(interrupt)");
	}
	
	/* end Thread Functions */

	public void setPolicy(RLPolicy p) {	policy = p; }
	
	public Dimension getAgent() { return new Dimension(world.mx, world.my); }
	public int getScoringArea() { return world.position; }

	public boolean[][] getWalls() { return world.walls; }


	public void resetGame() {
		world.resetState();
	}
}

