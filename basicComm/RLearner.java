package basicComm;

import java.util.Vector;
import java.lang.*;
import java.lang.reflect.*;
import java.util.Scanner;

public class RLearner {

	TwoAgentsWorld thisWorld;
	RLPolicy policy1;
	RLPolicy policy2;
	Scanner keyboard = new Scanner(System.in);

	// Learning types
	public static final int Q_LEARNING = 1;
	public static final int SARSA = 2;
	// Good parms were lambda=0.05, gamma=0.1, alpha=0.01, epsilon=0.1

	// Action selection types
	public static final int E_GREEDY = 1;
	public static final int SOFTMAX = 2;

	int learningMethod;
	int actionSelection;

	double epsilon = 0.1;
	double temp;

	double alpha = 0.01;
	double gamma = 0;
	double lambda;

	int[] dimSize1;
	int[] dimSize2;
	int[] state1;
	int[] state2;
	int[] tempState2;
	int[] newstate1;
	int[] newstate2;
	int action1;
	int action2;
	double reward1;
	double reward2;

	int epochs;
	public int epochsdone;

	Thread thisThread;
	public boolean running;

	long timer;

	boolean random = false;
	Runnable a;

	public RLearner(TwoAgentsWorld world) {
		// Getting the world from the invoking method.
		thisWorld = world;

		// Get dimensions of the world.
		dimSize1 = thisWorld.getDimension1();
		dimSize2 = thisWorld.getDimension2();

		// Creating new policy with dimensions to suit the world.
		policy1 = new RLPolicy(dimSize1);
		policy2 = new RLPolicy(dimSize2);

		// Initializing the policy with the initial values defined by the world.
		policy1.initValues(thisWorld.getInitValues());
		policy2.initValues(thisWorld.getInitValues());

		learningMethod = Q_LEARNING; // Q_LAMBDA;//SARSA;
		actionSelection = E_GREEDY;

		// set default values
		epsilon = 0.1;
		temp = 1;

		alpha = 0.01; // For CliffWorld alpha = 1 is good
		gamma = 0;
		lambda = 0.1; // For CliffWorld gamma = 0.1, l = 0.5 (l*g=0.05)is a good
						// choice.

		System.out.println("RLearner initialised");

	}

	// execute one trial
	public void runTrial() {
		System.out.println("Learning! (" + epochs + " epochs)\n");
		for (int i = 0; i < epochs; i++) {
			if (!running)
				break;

			runEpoch();

			if (i % 1000 == 0) {
				// give text output
				timer = (System.currentTimeMillis() - timer);
				System.out.println("Epoch:" + i + " : " + timer);
				timer = System.currentTimeMillis();
			}
		}
	}

	// execute one epoch
	public void runEpoch() {

		// Reset state to start position defined by the world.
		state1 = thisWorld.resetState();
		state2 = thisWorld.getState2();
		switch (learningMethod) {

		case Q_LEARNING: {

			double this_Q1;
			double max_Q1;
			double new_Q1;

			double this_Q2;
			double max_Q2;
			double new_Q2;

			while (!thisWorld.endState()) {

				if (!running)
					break;
				 action1 = selectAction( state1,1 );
				// keyboard.nextLine();
				 thisWorld.getNextState( action1,4);
				tempState2 = thisWorld.getState2();
				action2 = selectAction(tempState2, 2);
				// keyboard.nextLine();
				newstate1 = thisWorld.getNextState(4, action2);
				newstate2 = thisWorld.getState2();
				reward1 = thisWorld.getReward1();
				reward2 = thisWorld.getReward2();
				
				 this_Q1 = policy1.getQValue( state1, action1 ); 
				 max_Q1 =policy1.getMaxQValue( newstate1 );
				 this_Q2 = policy2.getQValue(tempState2, action2);
				 max_Q2 = policy2.getMaxQValue(newstate2);
				
				 new_Q1 = this_Q1 + alpha * (reward1 + gamma * max_Q1 - this_Q1);
				  new_Q2 = this_Q2 + alpha * (reward2 + gamma * max_Q2 - this_Q2);
				policy1.setQValue(state1, action1, new_Q1);
				policy2.setQValue(tempState2, action2, new_Q2);


				// Set state to the new state.
				// state1 = newstate1;
				state2 = newstate2;
			}

		}

		} // switch
	} // runEpoch

	private int selectAction(int[] state, int agent) {
		double[] qValues;
		if (agent == 1)
			qValues = policy1.getQValuesAt(state);
		else
			qValues = policy2.getQValuesAt(state);

		int selectedAction = -1;

		switch (actionSelection) {

		case E_GREEDY: {

			random = false;
			double maxQ = -Double.MAX_VALUE;
			int[] doubleValues = new int[qValues.length];
			int maxDV = 0;

			// Explore
			if (Math.random() < epsilon) {
				selectedAction = -1;
				random = true;
			} else {

				for (int action = 0; action < qValues.length; action++) {

					if (qValues[action] > maxQ) {
						selectedAction = action;
						maxQ = qValues[action];
						maxDV = 0;
						doubleValues[maxDV] = selectedAction;
					} else if (qValues[action] == maxQ) {
						maxDV++;
						doubleValues[maxDV] = action;
					}
				}

				if (maxDV > 0) {
					int randomIndex = (int) (Math.random() * (maxDV + 1));
					selectedAction = doubleValues[randomIndex];
				}
			}

			// Select random action if all qValues == 0 or exploring.
			if (selectedAction == -1) {

				// System.out.println( "Exploring ..." );
				selectedAction = (int) (Math.random() * qValues.length);
			}

			// Choose new action if not valid.
			while (!thisWorld.validAction(selectedAction, agent)) {

				selectedAction = (int) (Math.random() * qValues.length);
				// System.out.println( "Invalid action, new one:" +
				// selectedAction);
			}

			break;
		}

		}
		return selectedAction;
	}

	/*
	 * private double getMaxQValue( int[] state, int action ) {
	 * 
	 * double maxQ = 0;
	 * 
	 * double[] qValues = policy.getQValuesAt( state );
	 * 
	 * for( action = 0 ; action < qValues.length ; action++ ) { if(
	 * qValues[action] > maxQ ) { maxQ = qValues[action]; } } return maxQ; }
	 */

	public RLPolicy getPolicy1() {

		return policy1;
	}

	public RLPolicy getPolicy2() {

		return policy2;
	}

	public void setAlpha(double a) {

		if (a >= 0 && a < 1)
			alpha = a;
	}

	public double getAlpha() {

		return alpha;
	}

	public void setGamma(double g) {

		if (g > 0 && g < 1)
			gamma = g;
	}

	public double getGamma() {

		return gamma;
	}

	public void setEpsilon(double e) {

		if (e > 0 && e < 1)
			epsilon = e;
	}

	public double getEpsilon() {

		return epsilon;
	}

	public void setEpisodes(int e) {

		if (e > 0)
			epochs = e;
	}

	public int getEpisodes() {

		return epochs;
	}

	public void setActionSelection(int as) {

		switch (as) {

		case SOFTMAX: {
			actionSelection = SOFTMAX;
			break;
		}
		case E_GREEDY:
		default: {
			actionSelection = E_GREEDY;
		}

		}
	}

	public int getActionSelection() {

		return actionSelection;
	}

	public void setLearningMethod(int lm) {

		switch (lm) {

		case SARSA: {
			learningMethod = SARSA;
			break;
		}

		case Q_LEARNING:
		default: {
			learningMethod = Q_LEARNING;
		}
		}
	}

	public int getLearningMethod() {

		return learningMethod;
	}

	// AK: let us clear the policy
	public RLPolicy newPolicy1() {
		policy1 = new RLPolicy(dimSize1);

		// Initializing the policy with the initial values defined by the world.
		policy1.initValues(thisWorld.getInitValues());

		return policy1;
	}

	public RLPolicy newPolicy2() {
		policy2 = new RLPolicy(dimSize2);

		// Initializing the policy with the initial values defined by the world.
		policy2.initValues(thisWorld.getInitValues());

		return policy2;
	}
}
