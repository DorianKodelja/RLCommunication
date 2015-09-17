package commAsynchro;

import java.awt.*;

public class TwoAgentsWorld implements RLWorld {
	public int bx, by;

	public int mx, my; // agent position
	public int nx, ny; // agent2 position
	public int position; // information about scoring position 0:left/1:right/2:top/3:bottom

	public int agent1Score = 0;
	public int fail = 0;
	public int succesReward1 = 50;
	public int failPunishment= 100;

	static final int NUM_OBJECTS1 = 5, NUM_OBJECTS2 = 4, NUM_ACTIONS1 = 5,NUM_ACTIONS2=5, WALL_TRIALS = 100;
	static final double INIT_VALS = 0;

	int[] stateArray;
	double waitingReward1;
	double waitingReward2;
	public boolean[][] walls;

	public TwoAgentsWorld(int x, int y, int numWalls) {
		bx = x;
		by = y;
		makeWalls(x, y, numWalls);

		resetState();
	}

	public TwoAgentsWorld(int x, int y, boolean[][] newwalls) {
		bx = x;
		by = y;

		walls = newwalls;

		resetState();
	}

	/******* RLWorld interface functions ***********/
	public int[] getDimension1() {
		int[] retDim = new int[NUM_OBJECTS1 + 1];
		int i;
		for (i = 0; i < NUM_OBJECTS1 - 1;) {
			retDim[i++] = bx;
			retDim[i++] = by;
		}
		retDim[i++] = 4;
		retDim[i] = NUM_ACTIONS1;

		return retDim;
	}

	public int[] getDimension2() {
		int[] retDim2 = new int[NUM_OBJECTS2 + 1];
		int i;
		for (i = 0; i < NUM_OBJECTS2 - 1;) {
			retDim2[i++] = bx;
			retDim2[i++] = by;
		}
		retDim2[i] = NUM_ACTIONS2;

		return retDim2;
	}

	// given action determine next state
	public int[] getNextState(int action1, int action2) {
		// action is agent action: 0=left 1=right
		Dimension d1 = getCoords(action1, 1);
		Dimension d2 = getCoords(action2, 2);
		int ax1 = d1.width, ay1 = d1.height;
		int ax2 = d2.width, ay2 = d2.height;
		if (legal(ax1, ay1)) {
			// move agent
			mx = ax1;
			my = ay1;

		} else {
			// System.err.println("Illegal action: "+action);
		}
		if (legal(ax2, ay2)) {
			// move agent
			//System.out.println("agent2("+action2+"): "+nx+":"+ny+"-->"+ax2+":"+ay2);
			nx = ax2;
			ny = ay2;
		} else {
			// System.err.println("Illegal action: "+action);
		}
		// update world
		
		waitingReward1 = calcReward1();

		waitingReward2 = waitingReward1;
		
		return getState1();
	}
	public boolean catArea(){return agentInCatArea();};
	private boolean agentInCatArea() {
		
		boolean[] cases={nx == bx-3,nx == bx-1,ny==-1+(int)(by / 2),ny==1+(int)(by / 2)};
		if ((nx!=bx-2 || ny!=(int)(by / 2)) && cases[position]==false ){
			return true;}
		else
			return false;
	}
	public boolean scoreArea(){return agentInScoreArea();};
	private boolean agentInScoreArea() {
		if ((nx!=bx-2 || ny!=(int)(by / 2))&& agentInCatArea()==false){
			return true;}
		else 
			return false;
	}

	public double getReward1(int i) {
		return getReward1();
	}

	public double getReward1() {
		
		return waitingReward1;
	}

	public double getReward2(int i) {
		return getReward2();
	}

	public double getReward2() {
		return waitingReward2;
	}

	public boolean validAction(int action, int agent) {
		Dimension d = getCoords(action, agent);
		return legal(d.width, d.height);
	}

	Dimension getCoords(int action, int agent) {
		int ax, ay;
		if (agent == 1) {
			ax = mx;
			ay = my;
			switch (action) {
			case 0:
				ax -=1;
				break;
			case 1:
				ax +=1;
				break;
			case 2:
				ay -=1;
				break;
			case 3:
				ay +=1;
				break;
			case 4:
				break;
			default: // System.err.println("Invalid action: "+action);
			}
		} else {
			ax = nx;
			ay = ny;
			switch (action) {
			case 0:
				ax -=1;
				break;
			case 1:
				ax +=1;
				break;
			case 2:
				ay -=1;
				break;
			case 3:
				ay +=1;
				break;
			case 4:
				break;
			default: // System.err.println("Invalid action: "+action);
			}
		}
		return new Dimension(ax, ay);
	}

	// find action value given x,y=0,+-1
	int getAction(int x, int y) {
		int[][] vals = { { 7, 0, 1 }, { 6, 0, 2 }, { 5, 4, 3 } };
		if ((x < -1) || (x > 1) || (y < -1) || (y > 1) || ((y == 0) && (x == 0)))
			return -1;
		int retVal = vals[y + 1][x + 1];
		return retVal;
	}

	public boolean endState() {
		return endGame();
	}

	public int[] resetState() {
		agent1Score = 0;

		setInitPosition();
		return getState1();
	}

	public double getInitValues() {
		return INIT_VALS;
	}

	/******* end RLWorld functions **********/

	public int[] getState1() {
		// translates current state into int array
		stateArray = new int[NUM_OBJECTS1];
		stateArray[0] = mx;
		stateArray[1] = my;
		stateArray[2] = nx;
		stateArray[3] = ny;
		stateArray[4] = position;
		return stateArray;
	}

	public int[] getState2() {
		// translates current state into int array
		stateArray = new int[NUM_OBJECTS2];
		stateArray[0] = mx;
		stateArray[1] = my;
		stateArray[2] = nx;
		stateArray[3] = ny;
		

		return stateArray;
	}

	public double calcReward1() {
		double newReward = 0;
		if (agentInScoreArea()) {
			
			agent1Score++;
			newReward += succesReward1;
			System.out.println("3: score "+Boolean.toString(agentInScoreArea())+newReward);
		}
		else if (agentInCatArea()){
			fail++;
			newReward -=failPunishment;
			System.out.println("3: score "+Boolean.toString(agentInCatArea())+newReward);
		}
		return newReward;
	}

	

	public void setInitPosition() {
		my = (int) by / 2;
		mx = 1;
		ny = (int) by / 2;
		nx = bx - 2;

		position = (int) (Math.random() * 4);

	}

	boolean legal(int x, int y) {
		return ((x >= 0) && (x < bx) && (y >= 0) && (y < by)) && (!walls[x][y]);
	}

	boolean endGame() {
		return (agentInScoreArea() || agentInCatArea());
	}

	Dimension getRandomPos() {
		int rx, ry;
		rx = (int) (Math.random() * bx);
		ry = (int) (Math.random() * by);
		for (int trials = 0; (!legal(rx, ry)) && (trials < WALL_TRIALS); trials++) {
			rx = (int) (Math.random() * bx);
			ry = (int) (Math.random() * by);
		}
		return new Dimension(rx, ry);
	}

	/******** wall generating functions **********/
	void makeWalls(int xdim, int ydim, int numWalls) {
		walls = new boolean[xdim][ydim];

		// loop until a valid wall set is found
		for (int t = 0; t < WALL_TRIALS; t++) {
			// clear walls
			for (int i = 0; i < walls.length; i++) {
				for (int j = 0; j < walls[0].length; j++)
					walls[i][j] = false;
			}

			float xmid = xdim / (float) 2;
			float ymid = ydim / (float) 2;

			// randomly assign walls.
			for (int i = 0; i < numWalls; i++) {
				Dimension d = getRandomPos();

				// encourage walls to be in center
				double dx2 = Math.pow(xmid - d.width, 2);
				double dy2 = Math.pow(ymid - d.height, 2);
				double dropperc = Math.sqrt((dx2 + dy2) / (xmid * xmid + ymid * ymid));
				if (Math.random() < dropperc) {
					// reject this wall
					i--;
					continue;
				}

				walls[d.width][d.height] = true;
			}

			// check no trapped points
			if (validWallSet(walls))
				break;

		}

	}

	boolean validWallSet(boolean[][] w) {
		// copy array
		boolean[][] c;
		c = new boolean[w.length][w[0].length];

		for (int i = 0; i < w.length; i++) {
			for (int j = 0; j < w[0].length; j++)
				c[i][j] = w[i][j];
		}

		// fill all 8-connected neighbours of the first empty
		// square.
		boolean found = false;
		search: for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[0].length; j++) {
				if (!c[i][j]) {
					// found empty square, fill neighbours
					fillNeighbours(c, i, j);
					found = true;
					break search;
				}
			}
		}

		if (!found)
			return false;

		// check if any empty squares remain
		for (int i = 0; i < c.length; i++) {
			for (int j = 0; j < c[0].length; j++)
				if (!c[i][j])
					return false;
		}
		return true;
	}

	void fillNeighbours(boolean[][] c, int x, int y) {
		c[x][y] = true;
		for (int i = x - 1; i <= x + 1; i++) {
			for (int j = y - 1; j <= y + 1; j++)
				if ((i >= 0) && (i < c.length) && (j >= 0) && (j < c[0].length) && (!c[i][j]))
					fillNeighbours(c, i, j);
		}
	}
	/******** wall generating functions **********/

}
