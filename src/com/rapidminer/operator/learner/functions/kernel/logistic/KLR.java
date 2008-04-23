/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.functions.kernel.logistic;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.learner.functions.kernel.AbstractMySVMLearner;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.svm.SVMInterface;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * The main class for the Kernel Logistic Regression.
 * 
 * @author Stefan Rueping
 * @version $Id: KLR.java,v 1.3 2007/06/15 18:44:37 ingomierswa Exp $
 */
public class KLR implements SVMInterface {

	protected Kernel kernel;

	protected SVMExamples examples;

	int n; // #examples

	double[] target;

	int N1;

	int N2;

	// internal vars
	double[] alphas;

	double[] Hcache;

	boolean[] at_bound;

	int i_up;

	int i_low;

	double b;

	double b_up;

	double b_low;

	// user-defined params
	double tol = 1e-3; // Genauigkeit f?r b (convergence_epsilon)

	double C = 1.0d;

	double epsilon; // Genauigkeit f?r t (is_zero)

	double mu; // Genauigkeit f?r bound (is_zero)

	int max_iterations = 100000; // maximale Anzahl Iterationen im
									// Newton-Raphson-Schritt

	public KLR() {}

	public KLR(Operator paramOperator) throws UndefinedParameterError {
		C = paramOperator.getParameterAsDouble(AbstractMySVMLearner.PARAMETER_C); 
		tol = paramOperator.getParameterAsDouble(AbstractMySVMLearner.PARAMETER_CONVERGENCE_EPSILON); 
		max_iterations = paramOperator.getParameterAsInt(AbstractMySVMLearner.PARAMETER_MAX_ITERATIONS); 
	}

	public void init(Kernel new_kernel, SVMExamples new_examples) {
		kernel = new_kernel;
		examples = new_examples;

		// copy examples to local vars
		target = examples.get_ys();
		alphas = examples.get_alphas();
		// System.out.println(alphas);
		n = examples.count_examples();

		epsilon = C * 1e-10; // C*getParam("is_zero")

		mu = epsilon;
		N1 = examples.count_pos_examples();
		N2 = n - N1;
	};

	final double dG(double alpha) {
		// return dG(alpha/C)
		return Math.log(alpha / (C - alpha));
	};

	final double dPhi(double t, int i, int j, double ai, double aj, double Kii, double Kij, double Kjj) {
		// return Hi(alpha(t)) - Hj(alpha(t))
		double result = 0.0;
		double ydG = 0.0;
		if (target[i] > 0) {
			result = Kii - Kij;
			ydG = dG(ai + t) - dG(ai);
		} else {
			result = Kij - Kii;
			ydG = dG(ai) - dG(ai - t);
		};
		if (target[j] > 0) {
			result = Kjj - Kij;
			ydG -= dG(aj - t) - dG(aj);
		} else {
			result = Kij - Kjj;
			ydG -= dG(aj) - dG(aj + t);
		};
		// result *= t;
		result = t * (Kii - 2.0 * Kij + Kjj);
		result += ydG;
		result += Hcache[i] - Hcache[j]; // value for t=0

		return result;
	};

	final double d2Phi(double t, int i, int j, double ai, double aj, double Kii, double Kij, double Kjj) {
		double result;
		double atilde;
		if (target[i] > 0) {
			atilde = ai + t;
		} else {
			atilde = ai - t;
		};
		result = C / ((atilde) * (C - atilde));
		if (target[j] > 0) {
			atilde = aj - t;
		} else {
			atilde = aj + t;
		};
		result += C / ((atilde) * (C - atilde));

		result += Kii - 2.0 * Kij + Kjj; // eta

		return result;
	};

	protected boolean takeStep(int i, int j) {
		double[] kernel_row_i = kernel.get_row(i);
		double[] kernel_row_j = kernel.get_row(j);
		double aio = alphas[i];
		double ajo = alphas[j];
		double yi = target[i];
		double yj = target[j];
		double Hio = Hcache[i];
		double Hjo = Hcache[j];
		double Kii = kernel_row_i[i];
		double Kij = kernel_row_i[j];
		double Kjj = kernel_row_j[j];
		int takestepFlag = 1;

		// Compute t_min and t_max
		double t_max;
		double t_min;
		double t_tmp;
		if (yi > 0) {
			t_min = mu / 2.0 - aio;
			t_max = C - mu / 2.0 - aio;
		} else {
			t_max = aio - mu / 2.0;
			t_min = aio - (C - mu / 2.0);
		};
		if (yj > 0) {
			t_tmp = ajo - mu / 2.0;
			if (t_tmp < t_max) {
				t_max = t_tmp;
			};
			t_tmp = ajo - (C - mu / 2.0);
			if (t_tmp > t_min) {
				t_min = t_tmp;
			};
		} else {
			t_tmp = mu / 2.0 - ajo;
			if (t_tmp > t_min) {
				t_min = t_tmp;
			};
			t_tmp = C - mu / 2.0 - ajo;
			if (t_tmp < t_max) {
				t_max = t_tmp;
			};
		};

		if (t_max - t_min <= epsilon) {
			return false;
		};
		double t = 0.0;
		double the_dPhi = Hio - Hjo;
		double the_d2Phi = Kii - 2.0 * Kij + Kjj + C / (aio * (C - aio)) + C / (ajo * (C - ajo));
		double dPhi_left = 0.0;
		double d2Phi_left = 0.0;
		double dPhi_right = 0.0;
		double d2Phi_right = 0.0;
		double t_left = 0.0;
		double t_right = 0.0;

		if (the_dPhi > 0) {
			// Compute dPhi(t) and d2Phi(t) at t = t_min and denoted as
			// dPhi_left, d2Phi_left
			dPhi_left = dPhi(t_min, i, j, aio, ajo, Kii, Kij, Kjj);
			d2Phi_left = d2Phi(t_min, i, j, aio, ajo, Kii, Kij, Kjj);
			if (dPhi_left < 0) {
				t_left = t_min;
				t_right = t;
				dPhi_right = the_dPhi;
				d2Phi_right = the_d2Phi;
			} else {
				t = t_min;
				takestepFlag = 2;
			};
		} else if (the_dPhi < 0) {
			// Compute dPhi(t) and d2Phi(t) at t = t_max and denoted as
			// dPhi_right, d2Phi_right
			dPhi_right = dPhi(t_max, i, j, aio, ajo, Kii, Kij, Kjj);
			d2Phi_right = d2Phi(t_max, i, j, aio, ajo, Kii, Kij, Kjj);
			if (dPhi_right > 0) {
				t_left = t;
				t_right = t_max;
				dPhi_left = the_dPhi;
				d2Phi_left = the_d2Phi;
			} else {
				t = t_max;
				takestepFlag = 2;
			};
		} else {
			return false;
		};

		double t0;
		double dt;
		double ai = 0.0;
		double aj = 0.0;
		double Hi;
		double Hj;
		if (takestepFlag == 1) {
			// Choose a better start point
			if (Math.abs(dPhi_left) < Math.abs(dPhi_right)) {
				t0 = t_left;
				the_dPhi = dPhi_left;
				the_d2Phi = d2Phi_left;
			} else {
				t0 = t_right;
				the_dPhi = dPhi_right;
				the_d2Phi = d2Phi_right;
			}
			do {
				dt = -the_dPhi / the_d2Phi; // Newton-Raphson step
				t = t0 + dt;
				if ((t <= t_left) || (t >= t_right)) {
					t = (t_left + t_right) / 2.0; // Bisection step
				};

				ai = aio + t / yi;
				aj = ajo - t / yj;

				Hi = Hio + t * (Kii - Kij) + yi * (Math.log(ai / (C - ai)) - Math.log(aio / (C - aio)));
				Hj = Hjo + t * (Kij - Kjj) + yj * (Math.log(aj / (C - aj)) - Math.log(ajo / (C - ajo)));

				the_dPhi = Hi - Hj;
				the_d2Phi = Kii - 2 * Kij + Kjj + C / (ai * (C - ai)) + C / (aj * (C - aj));

				// Update t_left and t_right
				if (the_dPhi * dPhi_left > 0) {
					t_left = t;
					dPhi_left = the_dPhi;
				} else {
					t_right = t;
					dPhi_right = the_dPhi;
				};
				t0 = t;
			} while ((Math.abs(the_dPhi) > (0.1 * tol)) && (t_left + epsilon < t_right));
		} else if (takestepFlag == 2) {
			ai = aio + t / yi;
			aj = ajo - t / yj;
			Hi = Hio + t * (Kii - Kij) + yi * (Math.log(ai / (C - ai)) - Math.log(aio / (C - aio)));
			Hj = Hjo + t * (Kij - Kjj) + yj * (Math.log(aj / (C - aj)) - Math.log(ajo / (C - ajo)));
		};

		if (t == 0) {
			return false;
		};

		// Save ai, aj, Hi, Hj
		alphas[i] = ai;
		alphas[j] = aj;
		// Update the boundary property of indices i and j (evt. clip alpha)
		if (ai <= mu) {
			if (((target[i] > 0) && (target[j] > 0)) || ((target[i] < 0) && (target[j] < 0))) {
				alphas[j] -= (mu - alphas[i]);
			} else {
				alphas[j] += (mu - alphas[i]);
			};
			alphas[i] = mu;
			at_bound[i] = true;
		} else if (ai >= C - mu) {
			if (((target[i] > 0) && (target[j] > 0)) || ((target[i] < 0) && (target[j] < 0))) {
				alphas[j] -= (C - mu - alphas[i]);
			} else {
				alphas[j] += (C - mu - alphas[i]);
			};
			alphas[i] = C - mu;
			at_bound[i] = true;
		} else {
			at_bound[i] = false;
		};
		if (aj <= mu) {
			if (((target[i] > 0) && (target[j] > 0)) || ((target[i] < 0) && (target[j] < 0))) {
				alphas[i] -= (mu - alphas[j]);
			} else {
				alphas[i] += (mu - alphas[j]);
			};
			alphas[j] = mu;
			at_bound[j] = true;
		} else if (aj >= C - mu) {
			if (((target[i] > 0) && (target[j] > 0)) || ((target[i] < 0) && (target[j] < 0))) {
				alphas[i] -= (C - mu - alphas[j]);
			} else {
				alphas[i] += (C - mu - alphas[j]);
			};
			alphas[j] = C - mu;
			at_bound[j] = true;
		} else {
			at_bound[j] = false;
		};

		t = ((alphas[i] - aio) * yi + (ajo - alphas[j]) * yj) / 2.0;

		Hi = Hio + t * (Kii - Kij) + yi * (Math.log(alphas[i] / (C - alphas[i])) - Math.log(aio / (C - aio)));
		Hj = Hjo + t * (Kij - Kjj) + yj * (Math.log(alphas[j] / (C - alphas[j])) - Math.log(ajo / (C - ajo)));

		for (int k = 0; k < n; k++) {
			Hcache[k] += t * (kernel_row_i[k] - kernel_row_j[k]);
		};

		Hcache[i] = Hi;
		Hcache[j] = Hj;

		// Update i_low, i_up, b_low and b_up over indices of non-boundary group
		b_up = Double.NEGATIVE_INFINITY;
		b_low = Double.POSITIVE_INFINITY;
		i_up = 0;
		i_low = 0;
		for (int l = 0; l < n; l++) {
			if (!at_bound[l]) {
				if (Hcache[l] > b_up) {
					b_up = Hcache[l];
					i_up = l;
				};
				if (Hcache[l] < b_low) {
					b_low = Hcache[l];
					i_low = l;
				};
			};
		};

		return true;
	}; // end takeStep procedure

	public void klr() {
		// main routine:

		// precond: n, target, examples, kernel intialisiert
		// alphas = new double[n];
		examples.clearAlphas();
		Hcache = new double[n];
		at_bound = new boolean[n];

		int i;
		int j;
		double alpha_pos = C / N1; // !!! N1 = # pos examples
		double alpha_neg = C / N2; // !!! N2 = # neg examples
		for (i = 0; i < n; i++) {
			if (target[i] > 0) {
				alphas[i] = alpha_pos;
			} else {
				alphas[i] = alpha_neg;
			};
			at_bound[i] = false;
		};

		// initialize all Hcache[i]
		double sum_pos_K;
		double sum_neg_K;
		double[] kernel_row;
		b_up = Double.NEGATIVE_INFINITY;
		b_low = Double.POSITIVE_INFINITY;
		i_up = 0;
		i_low = 0;
		for (i = 0; i < n; i++) {
			sum_pos_K = 0.0;
			sum_neg_K = 0.0;
			kernel_row = kernel.get_row(i);
			for (j = 0; j < n; j++) {
				if (target[j] > 0) {
					sum_pos_K += kernel_row[j];
				} else {
					sum_neg_K += kernel_row[j];
				};
			};
			Hcache[i] = alpha_pos * sum_pos_K - alpha_neg * sum_neg_K + (target[i]) * dG(alphas[i]);
			if (Hcache[i] > b_up) {
				b_up = Hcache[i];
				i_up = i;
			};
			if (Hcache[i] < b_low) {
				b_low = Hcache[i];
				i_low = i;
			};
		};

		boolean Flag;
		double Hi;
		int numChange;

		int it = max_iterations;

		do {
			// takestep with i_low and i_up
			while (2.0 * tol < b_up - b_low) {
				it--;

				Flag = takeStep(i_low, i_up);
				if (!Flag) {
					break;
				};
			};
			// cout<<endl;

			// check optimality of boundary indices
			numChange = 0;
			for (i = 0; i < n; i++) {
				if (at_bound[i]) {
					Hi = Hcache[i];
					if (Math.abs(Hi - b_low) >= Math.abs(Hi - b_up)) {
						it--;
						Flag = takeStep(i, i_low);
						if (!Flag) {
							it--;
							Flag = takeStep(i, i_up);
						};
					} else {
						it--;
						Flag = takeStep(i, i_up);
						if (!Flag) {
							it--;
							Flag = takeStep(i, i_low);
						};
					};
					if (!at_bound[i]) {
						numChange++;
					};
				};
			};
		} while ((numChange != 0) && (it > 0));

		// System.out.println("...done, KKT error = "+((b_up - b_low)/2.0));

		b = (b_low + b_up) / 2.0;

		// double targetf = 0.0;
		// double tmp;
		// for(int i=0;i<n;i++){
		// double* kernel_row_i = kernel->get_row(i);
		// for(int j=0;j<n;j++){
		// targetf += 0.5*alphas[i]*alphas[j]
		// *target[i]*target[j]
		// *kernel_row_i[j];
		// };
		// tmp = alphas[i]/C;
		// targetf += C*(tmp*log(tmp)+(1-tmp)*log(1-tmp));
		// };
		// cout<<"TARGET = "<<targetf<<endl;

	};

	public double predict(SVMExample sVMExample) {
		int i;
		SVMExample sv;
		double the_sum = examples.get_b();
		double alpha;
		for (i = 0; i < n; i++) {
			alpha = alphas[i];
			if (alpha != 0) {
				sv = examples.get_example(i);
				the_sum += alphas[i] * kernel.calculate_K(sv, sVMExample);
			};
		};
		the_sum = 1.0 / (1.0 + Math.exp(-the_sum));
		return the_sum;
	};

	public void predict(SVMExamples to_predict) {
		int i;
		double prediction;
		SVMExample sVMExample;
		// int size = examples.count_examples(); // IM 04/02/12
		int size = to_predict.count_examples();
		for (i = 0; i < size; i++) {
			sVMExample = to_predict.get_example(i);
			prediction = predict(sVMExample);
			to_predict.set_y(i, prediction);
		};
	};

	public void train() {
		// train the klr
		klr();

		b = -b; // different b in SVM and KLR, set to SVM standard
		// copy local vars to training_set
		// save y_i*alpha_i instead of alpha_i !!! IM 04/02/12 passiert aber
		// nicht :-) !!!
		examples.set_b(b);
		//System.out.println("b = "+b);
		for (int i = 0; i < n; i++) {
			if (target[i] < 0) {
				alphas[i] = -alphas[i];
			};
			//System.out.println(alphas[i]);

		};
		//System.out.println("alphas_spaeter: " + alphas);
	};

	/** Return the weights of the features. */
	public double[] getWeights() {
		int dim = examples.get_dim();
		int examples_total = examples.count_examples();
		double[] w = new double[dim];
		for (int j = 0; j < dim; j++)
			w[j] = 0;
		for (int i = 0; i < examples_total; i++) {
			double[] x = examples.get_example(i).toDense(dim);
			double alpha = alphas[i];
			for (int j = 0; j < dim; j++) {
				w[j] += alpha * x[j];
			}
		}
		return w;
	}

	/** Returns the value of b. */
	public double getB() {
		return examples.get_b();
	}

};
