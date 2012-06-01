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
/**
 * 
 */
package org.marssa.services.diagnostics.daq;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.marssa.footprint.datatypes.MString;
import org.marssa.footprint.datatypes.integer.MInteger;
import org.marssa.footprint.exceptions.NoConnection;
import org.marssa.footprint.logger.MMarker;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Warren Zahra
 * 
 */
public class LabJackU3 extends LabJack {

	private static Logger logger = (Logger) LoggerFactory
			.getLogger(LabJackU3.class.getName());

	static private LabJackConnections connectionPairs = new LabJackConnections();

	private LabJackU3(MString host, MInteger port, TimersEnabledU3 numTimers)
			throws UnknownHostException, NoConnection {
		super(host, port, numTimers);
		this.write(NUM_TIMERS_ENABLED_ADDR, new MInteger(numTimers.ordinal()));
	}

	/**
	 * The number of timers which have been enable for this instance of the
	 * LabJack class
	 * 
	 * @see <a
	 *      href="http://labjack.com/support/u3/users-guide/2.9">http://labjack.com/support/u3/users-guide/2.9</a>
	 */
	public enum TimersEnabledU3 implements ITimersEnabled {
		NONE(0), ONE(1), TWO(2);

		private TimersEnabledU3(int timersEnabled) {
		}

		@Override
		public MInteger getTimersEnabled() {
			return new MInteger(this.ordinal());
		}
	}

	public enum TimerU3 implements ITimer {
		TIMER_0(0), // Documented in section 2.9.1.1
		TIMER_1(1); // Documented in section 2.9.1.2
		private TimerU3(int timers) {
		}

		@Override
		public MInteger getTimer() {
			return new MInteger(this.ordinal());
		}
	};

	/**
	 * The available Timer modes for LabJackU3
	 * 
	 * @see <a
	 *      href="http://labjack.com/support/u3/users-guide/2.9.1">http://labjack.com/support/u3/users-guide/2.9.1</a>
	 */
	public enum TimerConfigModeU3 implements ITimerConfigMode {
		/**
		 * PWM Output (16-Bit, Mode 0)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.1">http://labjack.com/support/u3/users-guide/2.9.1.1</a>
		 */
		PWM_OUTPUT_16BIT(0),
		/**
		 * PWM Output (8-Bit, Mode 1)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.2">http://labjack.com/support/u3/users-guide/2.9.1.2</a>
		 */
		PWM_OUTPUT_8BIT(1),

		/**
		 * Period Measurement (32-Bit, Modes 2 & 3)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.3">http://labjack.com/support/u3/users-guide/2.9.1.3</a>
		 */
		PERIOD_MEASURMENT_RISING_32BIT(2),

		/**
		 * Period Measurement (32-Bit, Modes 2 & 3)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.3">http://labjack.com/support/u3/users-guide/2.9.1.3</a>
		 */
		PERIOD_MEASURMENT_FALLING_32BIT(3),

		/**
		 * Duty Cycle Measurement (Mode 4)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.4">http://labjack.com/support/u3/users-guide/2.9.1.4</a>
		 */
		DUTY_CYCLE_MEASURMENT(4),

		/**
		 * Firmware Counter Input (Mode 5)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.5">http://labjack.com/support/u3/users-guide/2.9.1.5</a>
		 */
		FIRMWARE_COUNTER_INPUT(5),

		/**
		 * Firmware Counter Input With Debounce (Mode 6)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.6">http://labjack.com/support/u3/users-guide/2.9.1.6</a>
		 */
		FIRMWARE_COUNTER_INPUT_DEBOUNCE(6),

		/**
		 * Frequency Output (Mode 7)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.7">http://labjack.com/support/u3/users-guide/2.9.1.7</a>
		 */
		FREQUENCY_OUTPUT(7),

		/**
		 * Quadrature Input (Mode 8)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.8">http://labjack.com/support/u3/users-guide/2.9.1.8</a>
		 */
		QUADRATURE_INPUT(8),

		/**
		 * Timer Stop Input (Mode 9)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.9">http://labjack.com/support/u3/users-guide/2.9.1.9</a>
		 */
		TIME_STOP_INPUT(9),

		/**
		 * System Timer Low/High Read (Modes 10 & 11)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.10">http://labjack.com/support/u3/users-guide/2.9.1.10</a>
		 */
		SYTEM_TIMER_LOWER_32BITS(10),

		/**
		 * System Timer Low/High Read (Modes 10 & 11)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.10">http://labjack.com/support/u3/users-guide/2.9.1.10</a>
		 */
		SYTEM_TIMER_UPPER_32BITS(11),

		/**
		 * Period Measurement (16-Bit, Modes 12 & 13)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.11">http://labjack.com/support/u3/users-guide/2.9.1.11</a>
		 */
		PERIOD_MEASURMENT_RISING_16BIT(12),

		/**
		 * Period Measurement (16-Bit, Modes 12 & 13)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.11">http://labjack.com/support/u3/users-guide/2.9.1.11</a>
		 */
		PERIOD_MEASURMENT_FALLING_16BIT(13),

		/**
		 * Line-to-Line (Mode 14)
		 * 
		 * @see <a
		 *      href="http://labjack.com/support/u3/users-guide/2.9.1.12">http://labjack.com/support/u3/users-guide/2.9.1.12</a>
		 */
		LINE_TO_LINE(14);

		private TimerConfigModeU3(int mode) {
		}

		@Override
		public MInteger getTimerConfigMode() {
			return new MInteger(this.ordinal());
		}
	};

	public enum TimerBaseClockU3 implements ITimerBaseClock {
		CLOCK_4_MHZ(0), CLOCK_12_MHZ(1), CLOCK_48_MHZ(2), CLOCK_1_MHZ_DIVISOR(3), CLOCK_4_MHZ_DIVISOR(
				4), CLOCK_12_MHZ_DIVISOR(5), CLOCK_48_MHZ_DIVISOR(6);
		private TimerBaseClockU3(int clock) {
		}

		@Override
		public MInteger getTimerBaseClock() {
			// TODO Auto-generated method stub
			return new MInteger(this.ordinal());
		}
	};

	/**
	 * This method return an instance to the singleton class LabJack. This
	 * method is thread-safe<br />
	 * <b>Note that this constructor defaults the LabJack configuration with no
	 * timers enabled</b>
	 * 
	 * @param host
	 *            The host IP to which LabJack is connected
	 * @param port
	 *            The host port to which LabJack is connected
	 * @return singleton instance to the LabJack
	 * @throws UnknownHostException
	 * @throws NoConnection
	 * @see TimersEnabledU3
	 */
	public static synchronized LabJackU3 getInstance(MString host, MInteger port)
			throws UnknownHostException, NoConnection {

		return getInstance(host, port, TimersEnabledU3.NONE);
	}

	/**
	 * This method return an instance to the singleton class LabJack<br />
	 * Note: This method is thread-safe
	 * 
	 * @param host
	 *            The host IP to which LabJack is connected
	 * @param port
	 *            The host port to which LabJack is connected
	 * @param numTimers
	 * @return singleton instance to the LabJack
	 * @throws UnknownHostException
	 * @throws NoConnection
	 * @see TimersEnabledU3
	 */
	public static synchronized LabJackU3 getInstance(MString host,
			MInteger port, TimersEnabledU3 numTimers)
			throws UnknownHostException, NoConnection {

		logger.info("Getting a Labjack Instance");
		LabJackConnection<LabJackU3> connection = connectionPairs
				.getConnection(host, port, numTimers);
		logger.debug(MMarker.GETTER, "Returning connection.lj");
		return connection.lj;
	}

	private static final class LabJackConnections implements
			Iterator<LabJackConnection<LabJackU3>> {
		// static private Set<LabJackConnection> activeConnections;
		static private ArrayList<LabJackConnection<LabJackU3>> activeConnections = new ArrayList<LabJack.LabJackConnection<LabJackU3>>();

		public boolean hasNext() {
			return activeConnections.iterator().hasNext();
		}

		public LabJackConnection<LabJackU3> next() {
			return activeConnections.iterator().next();
		}

		public void remove() {
			activeConnections.iterator().remove();
		}

		public LabJackConnection<LabJackU3> getConnection(MString host,
				MInteger port, TimersEnabledU3 numTimers)
				throws UnknownHostException, NoConnection {
			if (activeConnections != null) {
				for (LabJackConnection<LabJackU3> conn : activeConnections) {
					if (conn.inUse(host, port))
						return conn;
				}
			}
			LabJackU3 lj = new LabJackU3(host, port, numTimers);
			LabJackConnection<LabJackU3> newConnectionPair = new LabJackConnection<LabJackU3>(
					host, port, lj);
			activeConnections.add(newConnectionPair);
			return newConnectionPair;
		}
	}
}
