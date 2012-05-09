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
package mise.marssa.services.diagnostics.daq;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import mise.marssa.footprint.datatypes.MString;
import mise.marssa.footprint.datatypes.integer.MInteger;
import mise.marssa.footprint.exceptions.NoConnection;
import mise.marssa.footprint.logger.MMarker;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Warren Zahra
 * 
 */
public class LabJackUE9 extends LabJack {

	private static Logger logger = (Logger) LoggerFactory
			.getLogger(LabJackUE9.class.getName());

	static private LabJackConnections connectionPairs = new LabJackConnections();

	private LabJackUE9(MString host, MInteger port, TimersEnabledUE9 numTimers)
			throws UnknownHostException, NoConnection {
		super(host, port, numTimers);
		this.write(NUM_TIMERS_ENABLED_ADDR, new MInteger(numTimers.ordinal()));
	}

	public LabJackUE9(MString host, MInteger port) throws UnknownHostException,
			NoConnection {
		super(host, port);
	}

	public enum TimersEnabledUE9 implements ITimersEnabled {
		NONE(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(0), SIX(6);

		private TimersEnabledUE9(int timersEnabled) {
		}

		@Override
		public MInteger getTimersEnabled() {
			return new MInteger(this.ordinal());
		}
	}

	public enum TimerUE9 implements ITimer {
		TIMER_0(0), TIMER_1(1), TIMER_2(2), TIMER_3(3), TIMER_4(4), TIMER_5(5);
		private TimerUE9(int timers) {
		}

		@Override
		public MInteger getTimer() {
			return new MInteger(this.ordinal());
		}
	};

	public enum TimerConfigModeUE9 implements ITimerConfigMode {
		PWM_OUTPUT_16BIT(0), // Documented in section
		PWM_OUTPUT_8BIT(1), // Documented in section
		PERIOD_MEASURMENT_RISING_32BIT(2), // Documented in section
		PERIOD_MEASURMENT_FALLING_32BIT(3), // Documented in section
		DUTY_CYCLE_MEASURMENT(4), // Documented in section
		FIRMWARE_COUNTER_INPUT(5), // Documented in section
		FIRMWARE_COUNTER_INPUT_DEBOUNCE(6), // Documented in section
		FREQUENCY_OUTPUT(7), // Documented in section
		QUADRATURE_INPUT(8), // Documented in section
		TIME_STOP_INPUT(9), // Documented in section
		SYTEM_TIMER_LOWER_32BITS(10), // Documented in section
		SYTEM_TIMER_UPPER_32BITS(11), // Documented in section
		PERIOD_MEASURMENT_RISING_16BIT(12), // Documented in section
		PERIOD_MEASURMENT_FALLING_16BIT(13); // Documented in section
		// LINE_TO_LINE(14); // Documented in section

		private TimerConfigModeUE9(int mode) {
		}

		@Override
		public MInteger getTimerConfigMode() {
			return new MInteger(this.ordinal());
		}
	};

	public enum TimerBaseClockUE9 implements ITimerBaseClock {
		CLOCK_750_KHZ_DIVISOR(0), CLOCK_48_MHZ_DIVISOR(1);

		private TimerBaseClockUE9(int clock) {
		}

		@Override
		public MInteger getTimerBaseClock() {
			// TODO Auto-generated method stub
			return new MInteger(this.ordinal());
		}
	}

	/**
	 * This method return an instance to the singleton class LabJack<br />
	 * Note: This method is thread-safe Note: Default Timers Enabled: two This
	 * can be changed using setNumEnabledTimers
	 * 
	 * @param host
	 *            The host IP to which LabJack is connected
	 * @param port
	 *            The host port to which LabJack is connected
	 * @return singleton instance to the LabJack
	 * @throws UnknownHostException
	 * @throws NoConnection
	 * @see mise.marssa.control.LabJack.TimersEnabled
	 * @see mise.marssa.control.LabJack.setNumEnabledTimers
	 */
	public static synchronized LabJackUE9 getInstance(MString host,
			MInteger port) throws UnknownHostException, NoConnection {

		logger.info("Getting a Labjack Instance");
		LabJackConnection<LabJackUE9> connection = connectionPairs
				.getConnection(host, port);
		logger.debug(MMarker.GETTER, "Returning connection.lj");
		return connection.lj;
	}

	private static final class LabJackConnections implements
			Iterator<LabJackConnection<LabJackUE9>> {
		// static private Set<LabJackConnection> activeConnections;
		static private ArrayList<LabJackConnection<LabJackUE9>> activeConnections = new ArrayList<LabJack.LabJackConnection<LabJackUE9>>();

		public boolean hasNext() {
			return activeConnections.iterator().hasNext();
		}

		public LabJackConnection<LabJackUE9> next() {
			return activeConnections.iterator().next();
		}

		public void remove() {
			activeConnections.iterator().remove();
		}

		public LabJackConnection<LabJackUE9> getConnection(MString host,
				MInteger port) throws UnknownHostException, NoConnection {
			if (activeConnections != null) {
				for (LabJackConnection<LabJackUE9> conn : activeConnections) {
					if (conn.inUse(host, port))
						return conn;
				}
			}
			LabJackUE9 lj = new LabJackUE9(host, port);
			LabJackConnection<LabJackUE9> newConnectionPair = new LabJackConnection<LabJackUE9>(
					host, port, lj);
			activeConnections.add(newConnectionPair);
			return newConnectionPair;
		}
	}

}
