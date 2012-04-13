/**
 * Copyright 2012 MARSEC-XL International Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mise.marssa.services.control;

import mise.marssa.footprint.datatypes.decimal.MDecimal;
import mise.marssa.footprint.datatypes.integer.MInteger;
import mise.marssa.footprint.exceptions.ConfigurationError;
import mise.marssa.footprint.exceptions.NoConnection;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.interfaces.control.IController;
import mise.marssa.footprint.interfaces.control.IRamping;

/**
 * @author Clayton Tabone
 * 
 */
public class Ramping implements IRamping {
	long stepDelay;
	double currentValue, stepSize;
	private IController controller;
	private RampingType rampType;
	// true means positive ramping
	boolean direction = false;
	private IController.Polarity polarity;
	private Thread rampingThread;
	private RampingTask rampingTask = null;

	private class RampingTask implements Runnable {
		MDecimal desiredValue;

		public RampingTask(MDecimal desiredValue) {
			this.desiredValue = desiredValue;
		}

		// TODO These exceptions have to be properly handled
		public void run() {
			try {
				double difference = desiredValue.doubleValue() - currentValue;

				direction = (difference > 0);
				while (true) {

					if (difference == 0) {
						// Do nothing. The desired value is the same as the
						// current value.
					} else if (direction) {
						if (currentValue == stepSize) {
							polarity = IController.Polarity.POSITIVE;
							controller.setPolaritySignal(polarity);
						}
						currentValue += stepSize;
					} else {
						if (currentValue == -stepSize) {
							polarity = IController.Polarity.NEGATIVE;
							controller.setPolaritySignal(polarity);
						}
						currentValue -= stepSize;
					}
					/**
					 * @author the ramping type Accelerated accelerates the
					 *         ramping when decreasing the speed in both
					 *         directions
					 * 
					 */
					if (rampType == RampingType.ACCELERATED) {

						if (polarity == IController.Polarity.POSITIVE
								&& !direction) {
							if (desiredValue.doubleValue() > 0)
								currentValue = desiredValue.doubleValue();
							else if (desiredValue.doubleValue() < 0)
								currentValue = -1;
							else
								currentValue = 0;
						} else if (polarity == IController.Polarity.NEGATIVE
								&& direction) {
							if (desiredValue.doubleValue() > 0)
								currentValue = 1;
							else if (desiredValue.doubleValue() < 0)
								currentValue = desiredValue.doubleValue();
							else
								currentValue = 0;
						}
					}
					controller.outputValue(new MDecimal(currentValue));
					if ((currentValue == desiredValue.doubleValue())) {
						break;
					}
					Thread.sleep(stepDelay);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConfigurationError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OutOfRange e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoConnection e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * The rampingType Enum is used to select the type of ramping
	 * 
	 */
	public enum RampingType {
		DEFAULT(0), ACCELERATED(1);

		private RampingType(int rampingType) {
		}
	};

	public Ramping(MInteger stepDelay, MDecimal stepSize,
			IController controller, RampingType rampType)
			throws ConfigurationError, OutOfRange, NoConnection {
		this.stepDelay = stepDelay.longValue();
		this.stepSize = stepSize.doubleValue();
		this.controller = controller;
		this.currentValue = 0;
		this.rampType = rampType;
		controller.outputValue(new MDecimal(this.currentValue));
	}

	public Ramping(MInteger stepDelay, MDecimal stepSize,
			IController controller, MDecimal initialValue, RampingType rampType)
			throws ConfigurationError, OutOfRange, NoConnection {
		this.stepDelay = stepDelay.longValue();
		this.stepSize = stepSize.doubleValue();
		this.controller = controller;
		this.currentValue = initialValue.doubleValue();
		this.rampType = rampType;
		controller.outputValue(new MDecimal(this.currentValue));
	}

	public void rampTo(MDecimal desiredValue) throws InterruptedException {
		// Check if the ramping task exists
		if (this.rampingTask != null) {
			// Check if the ramping thread is running
			if (this.rampingThread.isAlive()) {
				// If the ramping thread is running, interrupt it
				this.rampingThread.interrupt();
				// After interrupting the thread, wait for it to terminate
				this.rampingThread.join();
			}
		}
		synchronized (this) {
			this.rampingTask = new RampingTask(desiredValue);
			this.rampingThread = new Thread(rampingTask);
			rampingThread.start();
		}
	}

	public void increase(MDecimal incrementValue) throws InterruptedException,
			ConfigurationError, OutOfRange, NoConnection {
		rampTo((MDecimal)incrementValue.add(new MDecimal(currentValue)));
	}

	public void decrease(MDecimal decrementValue) throws InterruptedException,
			ConfigurationError, OutOfRange, NoConnection {
		rampTo((MDecimal)decrementValue.subtract(new MDecimal(currentValue)));
	}

	public MDecimal getCurrentValue() {
		return new MDecimal(currentValue);
	}
}