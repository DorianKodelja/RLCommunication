package Basic;
import java.awt.*;

public class BasicWorld implements RLWorld{
	public int bx, by;

	public int mx, my; //agent position
	public int position; //information about scoring position 0:top/1:bottom

	
	public int agentScore = 0;
	public int succesReward=50;
	
	static final int NUM_OBJECTS=3, NUM_ACTIONS=8, WALL_TRIALS=100;
	static final double INIT_VALS=0;
		
	int[] stateArray;
	double waitingReward;
	public boolean[][] walls;

	public BasicWorld(int x, int y, int numWalls) {
		bx = x;
		by = y;
		makeWalls(x,y,numWalls);
		
		resetState();
	}
	
	public BasicWorld(int x, int y, boolean[][] newwalls) {
		bx = x;
		by = y;
		
		walls = newwalls;
		
		resetState();
	}

	/******* RLWorld interface functions ***********/
	public int[] getDimension() { 
		int[] retDim = new int[NUM_OBJECTS+1];
		int i;
		for (i=0; i<NUM_OBJECTS-1;) {
			retDim[i++] = bx;
			retDim[i++] = by;
		}
		retDim[i++]=2;
		retDim[i] = NUM_ACTIONS;
		
		return retDim;
	}
		
	// given action determine next state
	public int[] getNextState(int action) {
		// action is agent action:  0=u 1=ur 2=r 3=dr ... 7=ul
		Dimension d = getCoords(action);
		int ax=d.width, ay=d.height;
		if (legal(ax,ay)) {
			// move agent
			mx = ax; my = ay;
		} else {
			//System.err.println("Illegal action: "+action);
		}
		// update world
		waitingReward = calcReward();
		
		// if agent is in scoring area, relocate agent
		
		

		return getState();
	}
	
	private boolean agentInScoreArea() {
		if(position==0 && my<2 && mx>=(int)((bx-1)/2) && mx<=(int)(1+(bx-1)/2))return true;
		else if(position==1 && my>=by-2 && mx>=(int)((bx-1)/2) && mx<=(int)(1+(bx-1)/2))return true;
		else return false;
		}

	public double getReward(int i) { return getReward(); }
	public double getReward() {	return waitingReward; }
	
	public boolean validAction(int action) {
		Dimension d = getCoords(action);
		return legal(d.width, d.height);
	}
	
	Dimension getCoords(int action) {
		int ax=mx, ay=my;
		switch(action) {
			case 0: ay = my - 1; break;
			case 1: ay = my - 1; ax = mx + 1; break;
			case 2: ax = mx + 1; break;
			case 3: ay = my + 1; ax = mx + 1; break;
			case 4: ay = my + 1; break;
			case 5: ay = my + 1; ax = mx - 1; break;
			case 6: ax = mx - 1; break;
			case 7: ay = my - 1; ax = mx - 1; break;
			default: //System.err.println("Invalid action: "+action);
		}
		return new Dimension(ax, ay);
	}

	// find action value given x,y=0,+-1
	int getAction(int x, int y) {
		int[][] vals={{7,0,1},
		              {6,0,2},
					  {5,4,3}};
		if ((x<-1) || (x>1) || (y<-1) || (y>1) || ((y==0)&&(x==0))) return -1;
		int retVal = vals[y+1][x+1];
		return retVal;
	}

	public boolean endState() { return endGame(); }
	public int[] resetState() { 
		agentScore = 0;

		setInitPosition(); 
		return getState();
	}
		
	public double getInitValues() { return INIT_VALS; }
	/******* end RLWorld functions **********/
	
	public int[] getState() {
		// translates current state into int array
		stateArray = new int[NUM_OBJECTS];
		stateArray[0] = mx;
		stateArray[1] = my;
		stateArray[2] = position;
		return stateArray;
	}

	public double calcReward() {
		double newReward = 0;
		if (agentInScoreArea()) {
			agentScore++;
			newReward += succesReward;
		System.out.println("test:"+agentScore);	
		}

		return newReward;		
	}
	
	public void setInitPosition() {
		my = (int)by/2;
		mx = 0;
		position = (int)(Math.random() * 2);
		
	}

	boolean legal(int x, int y) {
		return ((x>=0) && (x<bx) && (y>=0) && (y<by)) && (!walls[x][y]);
	}

	boolean endGame() {
		//return (((mx==hx)&&(my==hy)&& gotCheese) || ((cx==mx) && (cy==my)));
		return (agentInScoreArea());
	}

	Dimension getRandomPos() {
		int nx, ny;
		nx = (int)(Math.random() * bx);
		ny = (int)(Math.random() * by);
		for(int trials=0; (!legal(nx,ny)) && (trials < WALL_TRIALS); trials++){
			nx = (int)(Math.random() * bx);
			ny = (int)(Math.random() * by);
		}
		return new Dimension(nx, ny);
	}

	

	/******** wall generating functions **********/
	void makeWalls(int xdim, int ydim, int numWalls) {
		walls = new boolean[xdim][ydim];
		
		// loop until a valid wall set is found
		for(int t=0; t<WALL_TRIALS; t++) {
			// clear walls
			for (int i=0; i<walls.length; i++) {
				for (int j=0; j<walls[0].length; j++) walls[i][j] = false;
			}
			
			float xmid = xdim/(float)2;
			float ymid = ydim/(float)2;
			
			// randomly assign walls.  
			for (int i=0; i<numWalls; i++) {
				Dimension d = getRandomPos();
				
				// encourage walls to be in center
				double dx2 = Math.pow(xmid - d.width,2);
				double dy2 = Math.pow(ymid - d.height,2);
				double dropperc = Math.sqrt((dx2+dy2) / (xmid*xmid + ymid*ymid));
				if (Math.random() < dropperc) {
					// reject this wall
					i--;
					continue;
				}
				
				
				walls[d.width][d.height] = true;
			}
			
			// check no trapped points
			if (validWallSet(walls)) break;
			
		}
		
	}
	
	boolean validWallSet(boolean[][] w) {
		// copy array
		boolean[][] c;
		c = new boolean[w.length][w[0].length];
		
		for (int i=0; i<w.length; i++) {
			for (int j=0; j<w[0].length; j++) c[i][j] = w[i][j];
		}
		
		// fill all 8-connected neighbours of the first empty
		// square.
		boolean found = false;
		search: for (int i=0; i<c.length; i++) {
			for (int j=0; j<c[0].length; j++) {
				if (!c[i][j]) {
					// found empty square, fill neighbours
					fillNeighbours(c, i, j);
					found = true;
					break search;
				}
			}
		}
		
		if (!found) return false;
		
		// check if any empty squares remain
		for (int i=0; i<c.length; i++) {
			for (int j=0; j<c[0].length; j++) if (!c[i][j]) return false;
		}
		return true;
	}
	
	void fillNeighbours(boolean[][] c, int x, int y) {
		c[x][y] = true;
		for (int i=x-1; i<=x+1; i++) {
			for (int j=y-1; j<=y+1; j++)
				if ((i>=0) && (i<c.length) && (j>=0) && (j<c[0].length) && (!c[i][j])) 
					fillNeighbours(c,i,j);
		}
	}
	/******** wall generating functions **********/

}
