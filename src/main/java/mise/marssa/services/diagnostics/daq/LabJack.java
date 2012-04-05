/**
 * Copyright 2012 MARSEC-XL Foundation
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
package mise.marssa.services.diagnostics.daq;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import mise.marssa.footprint.datatypes.MBoolean;
import mise.marssa.footprint.datatypes.MString;
import mise.marssa.footprint.datatypes.decimal.MDecimal;
import mise.marssa.footprint.datatypes.integer.MInteger;
import mise.marssa.footprint.datatypes.integer.MLong;
import mise.marssa.footprint.exceptions.ConfigurationError;
import mise.marssa.footprint.exceptions.NoConnection;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.logger.MMarker;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteMultipleRegistersRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * @author Warren Zahra
 * 
 */
public class LabJack {

	private static Logger labjackLogger = (Logger) LoggerFactory
			.getLogger("LabJack");

	// Register addresses
	static public final MInteger FIO4_ADDR = new MInteger(6004);
	static public final MInteger FIO5_ADDR = new MInteger(6005);
	static public final MInteger FIO6_ADDR = new MInteger(6006);
	static public final MInteger FIO7_ADDR = new MInteger(6007);
	static public final MInteger FIO8_ADDR = new MInteger(6008);
	static public final MInteger FIO9_ADDR = new MInteger(6009);
	static public final MInteger FIO10_ADDR = new MInteger(6010);
	static public final MInteger FIO11_ADDR = new MInteger(6011);
	static public final MInteger FIO12_ADDR = new MInteger(6012);
	static public final MInteger FIO13_ADDR = new MInteger(6013);
	static public final MInteger FIO14_ADDR = new MInteger(6014);
	static public final MInteger FIO15_ADDR = new MInteger(6015);
	static public final MInteger FIO16_ADDR = new MInteger(6016);
	static public final MInteger FIO17_ADDR = new MInteger(6017);
	static public final MInteger FIO18_ADDR = new MInteger(6018);
	static public final MInteger FIO19_ADDR = new MInteger(6019);

	// AIN addresses
	static public final MInteger AIN0_ADDR = new MInteger(0);
	static public final MInteger AIN1_ADDR = new MInteger(1);
	static public final MInteger AIN2_ADDR = new MInteger(2);
	static public final MInteger AIN3_ADDR = new MInteger(3);

	// Direction addresses
	static public final MInteger FIO4_DIR_ADDR = new MInteger(6104);
	static public final MInteger FIO5_DIR_ADDR = new MInteger(6105);
	static public final MInteger FIO6_DIR_ADDR = new MInteger(6106);
	static public final MInteger FIO7_DIR_ADDR = new MInteger(6107);
	static public final MInteger FIO8_DIR_ADDR = new MInteger(6108);
	static public final MInteger FIO9_DIR_ADDR = new MInteger(6109);
	static public final MInteger FIO10_DIR_ADDR = new MInteger(6110);
	static public final MInteger FIO11_DIR_ADDR = new MInteger(6111);
	static public final MInteger FIO12_DIR_ADDR = new MInteger(6112);
	static public final MInteger FIO13_DIR_ADDR = new MInteger(6113);
	static public final MInteger FIO14_DIR_ADDR = new MInteger(6114);
	static public final MInteger FIO15_DIR_ADDR = new MInteger(6115);
	static public final MInteger FIO16_DIR_ADDR = new MInteger(6116);
	static public final MInteger FIO17_DIR_ADDR = new MInteger(6117);
	static public final MInteger FIO18_DIR_ADDR = new MInteger(6118);
	static public final MInteger FIO19_DIR_ADDR = new MInteger(6119);

	/**
	 * The register containing the number of timers enabled. For the LabJack U3
	 * this can be a value between 0 and 2
	 * 
	 * @see http://labjack.com/support/u3/users-guide/2.9
	 */
	static public final MInteger NUM_TIMERS_ENABLED_ADDR = new MInteger(50501);

	/**
	 * The register containing the timer base clock.
	 * 
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimerBaseClock
	 */
	static public final MInteger TIMER_BASE_CLOCK_ADDR = new MInteger(7000);

	/**
	 * The register containing the timer clock divisor.
	 * 
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimerBaseClock
	 */
	static public final MInteger TIMER_CLOCK_DIVISOR_ADDR = new MInteger(7002);

	// TODO There must be something wrong here. FIO4 is operating in output
	// mode, regardless of FIO4-dir
	static public final MBoolean FIO_OUT_DIRECTION = new MBoolean(true);
	static public final MBoolean FIO_IN_DIRECTION = new MBoolean(false);

	/**
	 * The LabJack U3 has only two timers. The number of timers enabled can be
	 * either none, one timer or two timers.<br />
	 * 
	 * @see http://labjack.com/support/u3/users-guide/2.9
	 */
	public enum TimersEnabled {
		NONE(0), ONE(1), TWO(2);

		private TimersEnabled(int timersEnabled) {
		}
	};

	/**
	 * The LabJack U3 has only two timers.<br />
	 * 
	 * @see http://labjack.com/support/u3/users-guide/2.9.1
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimersEnabled
	 */
	public enum Timers {
		TIMER_0(0), // Documented in section 2.9.1.1
		TIMER_1(1); // Documented in section 2.9.1.2

		// Timer mode addresses
		private final MInteger TIMER0_CONFIG_MODE_ADDR = new MInteger(7100);
		private final MInteger TIMER1_CONFIG_MODE_ADDR = new MInteger(7102);

		// Timer mode addresses
		private final MInteger TIMER0_VALUE_ADDR = new MInteger(7200);
		private final MInteger TIMER1_VALUE_ADDR = new MInteger(7202);

		private TimerConfigMode timerConfigMode;
		private MInteger timerConfigModeAddress;
		private MLong timerValue;
		private MInteger timerValueAddress;

		private Timers(int timerNumber) {
			// Constructor belongs to an enum class
			// Hence the only possible values for the timerNumber are 0 and 1
			labjackLogger.info("Setting timer to", timerNumber);
			switch (timerNumber) {
			case 0:
				timerConfigModeAddress = TIMER0_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER0_VALUE_ADDR;
				break;
			case 1:
				timerConfigModeAddress = TIMER1_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER1_VALUE_ADDR;
				break;
			}
		}

		private void setTimerConfigMode(TimerConfigMode timerConfigMode) {
			labjackLogger.debug(MMarker.SETTER,
					"Setting TimerConfigMode to {} .", timerConfigMode.name());
			this.timerConfigMode = timerConfigMode;
		}

		private TimerConfigMode getTimerConfigMode() {
			labjackLogger.debug(MMarker.GETTER,
					"Returning TimerConfigMode {} .", timerConfigMode.name());
			return timerConfigMode;
		}

		private MInteger getTimerConfigModeAddress() {
			labjackLogger.debug(MMarker.GETTER,
					"Returning TimerConfigModeAddress {} .",
					timerConfigModeAddress.getValue());
			return this.timerConfigModeAddress;
		}

		private MLong getTimerValue() {
			labjackLogger.debug(MMarker.GETTER, "Returning TimerValue {} .",
					timerValue.getValue());
			return this.timerValue;
		}

		private void setTimerValue(MLong timerValue) {
			labjackLogger.debug(MMarker.SETTER, "Setting TimerValue {} .",
					timerValue.getValue());
			this.timerValue = timerValue;
		}

		private MInteger getTimerValueAddress() {
			labjackLogger.debug(MMarker.GETTER,
					"Returning TimerValueAddress {} .",
					timerValueAddress.getValue());
			return this.timerValueAddress;
		}
	};

	/**
	 * Timer base clock can have a mode between 0 and 6.<br />
	 * <b>Note: Both timers use the same timer clock!</b>
	 * 
	 * @see http://labjack.com/support/u3/users-guide/2.9.1
	 */
	public enum TimerConfigMode {
		PWM_OUTPUT_16BIT(0), // Documented in section 2.9.1.1
		PWM_OUTPUT_8BIT(1), // Documented in section 2.9.1.2
		PERIOD_MEASURMENT_RISING_32BIT(2), // Documented in section 2.9.1.3
		PERIOD_MEASURMENT_FALLING_32BIT(3), // Documented in section 2.9.1.3
		DUTY_CYCLE_MEASURMENT(4), // Documented in section 2.9.1.4
		FIRMWARE_COUNTER_INPUT(5), // Documented in section 2.9.1.5
		FIRMWARE_COUNTER_INPUT_DEBOUNCE(6), // Documented in section 2.9.1.6
		FREQUENCY_OUTPUT(7), // Documented in section 2.9.1.7
		QUADRATURE_INPUT(8), // Documented in section 2.9.1.8
		TIME_STOP_INPUT(9), // Documented in section 2.9.1.9
		SYTEM_TIMER_LOWER_32BITS(10), // Documented in section 2.9.1.10
		SYTEM_TIMER_UPPER_32BITS(11), // Documented in section 2.9.1.10
		PERIOD_MEASURMENT_RISING_16BIT(12), // Documented in section 2.9.1.11
		PERIOD_MEASURMENT_FALLING_16BIT(13), // Documented in section 2.9.1.11
		LINE_TO_LINE(14); // Documented in section 2.9.1.12

		private TimerConfigMode(int mode) {
		}
	};

	/**
	 * Timer base clock can have a mode between 0 and 6.<br />
	 * <b>Note: Both timers use the same timer clock!</b><br />
	 * Section 2.9.1.1 of the LabJack documentation has a good description of
	 * these modes
	 * 
	 * @see http://labjack.com/support/u3/users-guide/2.9
	 * @see mise.marssa.control.LabJack.TIMER_CLOCK_ADDR
	 */
	public enum TimerBaseClock {
		CLOCK_4_MHZ(0), CLOCK_12_MHZ(1), CLOCK_48_MHZ(2), CLOCK_1_MHZ_DIVISOR(3), CLOCK_4_MHZ_DIVISOR(
				4), CLOCK_12_MHZ_DIVISOR(5), CLOCK_48_MHZ_DIVISOR(6);

		private TimerBaseClock(int clock) {
		}
	};

	/**
	 * Contains a host/port connection and the instance of the LabJack class
	 * 
	 * @author Clayton Tabone
	 * 
	 */
	private static final class LabJackConnection {
		static Logger labjackConnectionLogger = (Logger) LoggerFactory
				.getLogger(LabJackConnection.class);
		MString host;
		MInteger port;
		LabJack lj;

		public LabJackConnection(MString host, MInteger port, LabJack lj) {
			this.host = host;
			this.port = port;
			this.lj = lj;
			Object[] labjackConnection = { host.toString(), port.getValue(), lj };
			labjackConnectionLogger
					.info("Connecting to a Labjack having host {} . port {} . and enabling {} . Labjack",
							labjackConnection);
		}

		public boolean inUse(MString host, MInteger port) {
			labjackConnectionLogger.debug(MMarker.GETTER,
					"Returning if labjack is already in use");
			return (this.host.getContents().equals(host) && this.port
					.getValue() == port.getValue());
		}
	}

	/**
	 * Contains the unique Set of ConnectionPair objects Multiple connections
	 * can be handled by the LabJack class. However, only one LabJack connection
	 * per instance is allowed. In order to conserve resources, for every new
	 * connection request a check is made. If a connection to the given address
	 * (ConnectionPair) is already active, it is used instead of opening a new
	 * connection.
	 * 
	 * @author Clayton Tabone
	 * 
	 */
	// TODO logger
	private static final class LabJackConnections implements
			Iterator<LabJackConnection> {
		// static private Set<LabJackConnection> activeConnections;
		static private ArrayList<LabJackConnection> activeConnections = new ArrayList<LabJack.LabJackConnection>();

		public boolean hasNext() {
			return activeConnections.iterator().hasNext();
		}

		public LabJackConnection next() {
			return activeConnections.iterator().next();
		}

		public void remove() {
			activeConnections.iterator().remove();
		}

		public LabJackConnection getConnection(MString host, MInteger port)
				throws UnknownHostException, NoConnection {
			if (activeConnections != null) {
				for (LabJackConnection conn : activeConnections) {
					if (conn.inUse(host, port))
						return conn;
				}
			}
			LabJack lj = new LabJack(host, port);
			LabJackConnection newConnectionPair = new LabJackConnection(host,
					port, lj);
			activeConnections.add(newConnectionPair);
			return newConnectionPair;
		}
	}

	// The static list of connection pairs, each containing an instance of the
	// LabJack class
	static private LabJackConnections connectionPairs = new LabJackConnections();

	// The actual TCP connection to the LabJack used in this instance
	private TCPMasterConnection readConnection;
	private TCPMasterConnection writeConnection;

	// The number of timers
	private TimersEnabled numTimers = TimersEnabled.NONE;

	// The base clock of the timers
	private TimerBaseClock timerBaseClock;

	private MLong timerClockDivisor;

	// host and port variables
	private MString host;
	private MInteger port;

	private LabJack(MString host, MInteger port) throws UnknownHostException,
			NoConnection {
		Object[] labjackInformation = { host.toString(), port.getValue(),
				numTimers };
		labjackLogger
				.info("Connecting to a Labjack having host {} . port {} . and enabling {} . timers",
						labjackInformation);
		try {
			InetAddress address = InetAddress.getByName(host.getContents()); // the
																				// slave's
																				// address
			labjackLogger.info(MMarker.SETTER, "Setting the readConnection");
			readConnection = new TCPMasterConnection(address);
			readConnection.setPort(port.getValue());
			readConnection.connect();
			labjackLogger.info(MMarker.SETTER, "Setting the writeConnection");
			writeConnection = new TCPMasterConnection(address);
			writeConnection.setPort(port.getValue());
			writeConnection.connect();
			this.host = host;
			this.port = port;
			this.numTimers = TimersEnabled.TWO;
			this.write(NUM_TIMERS_ENABLED_ADDR,
					new MInteger(numTimers.ordinal()));
		} catch (UnknownHostException e) {
			labjackLogger.error("UnknownHostException- Cannot find host{} . ",
					host.toString(), new NoConnection());
			// throw new NoConnection("Cannot find host: " + host +
			// "Exception details\n" + e.getMessage(), e.getCause());
		} catch (IOException e) {
			labjackLogger.error(
					"IOException- Cannot connect to Labjack on host{} . ",
					host.toString(), new NoConnection());
			// throw new NoConnection("Cannot connect to LabJack on host: " +
			// host.getContents() + "\nException details:\n" + e.getMessage(),
			// e.getCause());
		} catch (Exception e) {
			labjackLogger.error("Network failure (TCPMasterConnection):{} . ",
					host.toString(), new NoConnection());
			// throw new NoConnection("Network failure (TCPMasterConnection): "
			// + host + "Exception details\n" + e.getMessage(), e.getCause());
		}
	}

	protected void finalize() throws Throwable {
		labjackLogger.debug("Closing the readConnection");
		readConnection.close();
		labjackLogger.debug("Closing the writeConnection");
		writeConnection.close();
		// TODO Implement Mutex lock on the connection pair. The connectionPair
		// should only be removed if no other instance is using this connection
		// TODO Make sure that there are no leaked references to the singleton
		// instance for the connection pair which is being removed
		// connectionPairs.remove();
		super.finalize(); // not necessary if extending Object.
	}

	/**
	 * Override method to prevent Object cloning
	 * 
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		labjackLogger.error("Clone is not supported",
				new CloneNotSupportedException());
		throw new CloneNotSupportedException();
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
	public static synchronized LabJack getInstance(MString host, MInteger port)
			throws UnknownHostException, NoConnection {

		labjackLogger.info("Getting a Labjack Instance");
		LabJackConnection connection = connectionPairs
				.getConnection(host, port);
		labjackLogger.debug(MMarker.GETTER, "Returning connection.lj");
		return connection.lj;
	}

	/**
	 * Return host address
	 * 
	 * @return host
	 */
	public MString getHost() {
		return host;
	}

	/**
	 * return port number
	 * 
	 * @return port
	 */
	public MInteger getPort() {
		return port;
	}

	/**
	 * Set the number of Enabled Timers
	 * 
	 * @param numTimers
	 * @throws NoConnection
	 * @see mise.marssa.control.LabJack.TimersEnabled
	 */
	public void setNumEnabledTimers(TimersEnabled numTimers)
			throws NoConnection {
		this.numTimers = numTimers;
		this.write(NUM_TIMERS_ENABLED_ADDR, new MInteger(numTimers.ordinal()));
	}

	/**
	 * Sets the timer mode The number of timers enabled is set from the
	 * constructor. Since this is variable, the timer selected here may not be
	 * available. In this case the constructor will fail and raise an exception.
	 * 
	 * @param timer
	 *            the timer which will be configured
	 * @param timerConfigMode
	 *            the mode for the specified timer
	 * @throws ConfigurationError
	 * @throws NoConnection
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimersEnabled
	 */
	public void setTimerMode(Timers timer, TimerConfigMode timerConfigMode)
			throws ConfigurationError, NoConnection {
		labjackLogger.info(MMarker.SETTER, "Setting timerMode");
		if ((timer.ordinal() + 1) > numTimers.ordinal()) {
			labjackLogger.error("Timer {} . is not enabled", timer.ordinal(),
					new ConfigurationError());
			// throw new ConfigurationError("Timer " + timer.ordinal() +
			// " is not enabled");
		}
		labjackLogger.info("Setting timer {} . with a timerConfiMode {} .",
				timer, timerConfigMode);
		timer.setTimerConfigMode(timerConfigMode);
		labjackLogger.debug("Calling the writeMultiple method");
		writeMultiple(timer.timerConfigModeAddress,
				new MLong(timerConfigMode.ordinal()));
	}

	/**
	 * Sets the base clock for the LabJack timers
	 * 
	 * @param timerBaseClock
	 *            the base clock for the LabJack timers
	 * @throws NoConnection
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimerBaseClock
	 * @see mise.marssa.control.LabJack.TIMER_BASE_CLOCK_ADDR
	 */

	// TODO
	public void setTimerBaseClock(TimerBaseClock timerBaseClock)
			throws NoConnection {
		labjackLogger.info(MMarker.SETTER, "Setting TimerBasedClock");
		this.timerBaseClock = timerBaseClock;
		labjackLogger.debug("Calling the writeMultiple method");
		writeMultiple(LabJack.TIMER_BASE_CLOCK_ADDR,
				new MLong(timerBaseClock.ordinal()));
	}

	/**
	 * Sets the value of the given timer The number of timers enabled is set
	 * from the constructor. Since this is variable, the timer selected here may
	 * not be available.<br />
	 * Note: 0 means duty cycle = 100% and 65535 means duty cycle = 0%
	 * 
	 * @param timer
	 *            the timer which will be configured
	 * @param timerValue
	 *            the value for the given timer
	 * @throws ConfigurationError
	 * @throws OutOfRange
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimersEnabled
	 */
	public void setTimerValue(Timers timer, MLong timerValue)
			throws NoConnection, OutOfRange, ConfigurationError {
		labjackLogger.info(MMarker.SETTER, "Setting timerValue");
		if ((timer.ordinal() + 1) > numTimers.ordinal()) {
			labjackLogger.error("Timer {} . is not enabled", timer.ordinal(),
					new ConfigurationError());
			// throw new ConfigurationError("Timer " + timer.ordinal() +
			// " is not enabled");
		}
		if (timerValue.getValue() >= Math.pow(2, 32)) {
			labjackLogger
					.error("TimerValue must be a value between 0 and 4294967294 (2^32 - 1)",
							new OutOfRange());
			throw new OutOfRange(
					"Timer Value must be a value between 0 and 4294967294 (2^32 - 1)");
		}
		labjackLogger.info("Setting timer {} . with a timerValue {} .", timer,
				timerValue);
		timer.setTimerValue(timerValue);
		labjackLogger.debug("Calling the writeMultiple method");
		writeMultiple(timer.timerValueAddress, timerValue);
	}

	/**
	 * Sets the base clock for the LabJack timers<br />
	 * <b>Note: The timer clock divisor value must be between 1 and 256,
	 * otherwise an OutOfRange will be thrown!</b>
	 * 
	 * @param timerBaseClock
	 *            the base clock for the LabJack timers
	 * @throws OutOfRange
	 * @throws ConfigurationError
	 * @throws NoConnection
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimerBaseClock
	 * @see mise.marssa.control.LabJack.TIMER_CLOCK_DIVISOR_ADDR
	 */
	public void setTimerClockDivisor(MLong timerClockDivisor)
			throws OutOfRange, NoConnection {
		labjackLogger.info(MMarker.SETTER,
				"Setting timerClockDivisor with value {} .",
				timerClockDivisor.getValue());
		if (timerClockDivisor.getValue() < 1
				|| timerClockDivisor.getValue() > 256) {
			labjackLogger.error(
					"TimerClockDivisor must be a value between 1 and 256",
					new OutOfRange());
			// throw new
			// OutOfRange("Timer Clock Divisor must be a value between 1 and 256");
		}
		this.timerClockDivisor = timerClockDivisor;
		labjackLogger.debug("Calling the writeMultiple method");
		writeMultiple(LabJack.TIMER_BASE_CLOCK_ADDR, new MLong(
				timerClockDivisor.getValue()));
	}

	/**
	 * This write method is used to write to registers RegisterNumber is the
	 * register to write to while the registerValue is the value to write in the
	 * register(1 is used for logic high while 0 is used for logic low)
	 */
	public void write(MInteger registerNumber, MInteger registerValue)
			throws NoConnection {
		labjackLogger.info(
				"Writing to register {} . with a registerValue {} .",
				registerNumber, registerValue);
		SimpleRegister register = new SimpleRegister(registerValue.getValue());
		WriteSingleRegisterRequest writeRequest = new WriteSingleRegisterRequest(
				registerNumber.getValue(), register);

		// Prepare the transaction
		labjackLogger.info("Preparing the Transaction");
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				writeConnection);
		labjackLogger.debug("Labjack setting request to write to a register");
		transaction.setRequest(writeRequest);
		// WriteSingleRegisterResponse writeResponse =
		// (WriteSingleRegisterResponse) transaction.getResponse();

		try {
			labjackLogger.info("Executing the Transaction");
			transaction.execute();
		} catch (ModbusIOException e) {
			// TODO is it feasable to deduce 6000 ????? better as it was
			labjackLogger
					.error("ModbusIOException- Cannot write to labjack register number {} .",
							registerNumber);
			throw new NoConnection("Cannot write to LabJack FIO port"
					+ (registerNumber.getValue() - 6000) + "\n"
					+ e.getMessage(), e.getCause());
		} catch (ModbusSlaveException e) {
			labjackLogger
					.error("ModbusSlaveExceptio- Cannot write to labjack register number {} .",
							registerNumber);
			throw new NoConnection(
					"ModBus Slave exception cannot write to register"
							+ (registerNumber.getValue() - 6000) + "\n"
							+ e.getMessage(), e.getCause());
		} catch (ModbusException e) {
			labjackLogger
					.error("ModbusException Cannot write to labjack register number {} .",
							registerNumber);
			throw new NoConnection("ModBus exception cannot write to register"
					+ (registerNumber.getValue() - 6000) + "\n"
					+ e.getMessage(), e.getCause());
		}
	}

	/**
	 * A modification of the write method to accept boolean as a parameter true
	 * to set logic to hgigh flase to low
	 */
	public void write(MInteger registerNumber, MBoolean state)
			throws NoConnection {
		labjackLogger.info("Setting register {} . to {} .", registerNumber,
				state);
		int highLow = (state.getValue() ? 1 : 0);
		labjackLogger.debug("Calling the write method");
		write(registerNumber, new MInteger(highLow));
	}

	public void writeMultiple(MInteger registerNumber, MLong registerValue)
			throws NoConnection {

		labjackLogger.info(
				"Writing to register {} . with a registerValue {} .",
				registerNumber, registerValue);
		labjackLogger
				.debug("Dividing the 16bit registeer to two 8bit registers");
		SimpleRegister registerLSB = new SimpleRegister(
				(int) (registerValue.getValue() & 0xFFFF));
		SimpleRegister registerMSB = new SimpleRegister(
				(int) ((registerValue.getValue() & 0xFFFF0000) >> 16));
		SimpleRegister[] registerArray = { registerLSB, registerMSB };

		WriteMultipleRegistersRequest writeRequest = new WriteMultipleRegistersRequest(
				registerNumber.getValue(), registerArray);

		// Prepare the transaction
		labjackLogger.info("Preparing the Transaction");
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				writeConnection);

		labjackLogger
				.debug("Labjack setting request to write multiple registers");
		transaction.setRequest(writeRequest);
		// WriteSingleRegisterResponse writeResponse =
		// (WriteSingleRegisterResponse) transaction.getResponse();

		// Execute the transaction repeat times
		try {
			labjackLogger.info("Executing the Transaction");
			transaction.execute();
		} catch (ModbusIOException e) {
			labjackLogger
					.error("ModbusIOException- Cannot write to labjack register number {} .",
							registerNumber);
			throw new NoConnection("Cannot write to LabJack FIO port"
					+ (registerNumber.getValue() - 6000) + "\n"
					+ e.getMessage(), e.getCause());
		} catch (ModbusSlaveException e) {
			labjackLogger
					.error("ModbusSlaveExceptio- Cannot write to labjack register number {} .",
							registerNumber);
			throw new NoConnection(
					"ModBus Slave exception cannot write to register"
							+ (registerNumber.getValue() - 6000) + "\n"
							+ e.getMessage(), e.getCause());
		} catch (ModbusException e) {
			labjackLogger
					.error("ModbusException Cannot write to labjack register number {} .",
							registerNumber);
			throw new NoConnection("ModBus exception cannot write to register"
					+ (registerNumber.getValue() - 6000) + "\n"
					+ e.getMessage(), e.getCause());
		}
	}

	/**
	 * this method is used to read from a register/port. The ref and count
	 * MIntegers are to select the registers that are going to be read. The AIN
	 * is the value which is going to be read
	 */
	public MDecimal read(MInteger ref, MInteger count, MInteger AIN)
			throws NoConnection, IOException {

		labjackLogger.info("Preparing the Transaction");
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				readConnection);
		ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(
				ref.getValue(), count.getValue());
		ReadMultipleRegistersResponse res = null;
		transaction = new ModbusTCPTransaction(readConnection);

		labjackLogger
				.debug("Labjack setting request to read multiple registers");
		transaction.setRequest(req);

		try {
			labjackLogger.info("Executing the Transaction");
			transaction.execute();
		} catch (ModbusIOException e) {
			labjackLogger
					.error("ModbusIOException- Cannot read from labjack AIN {} .",
							AIN);
			throw new NoConnection("Cannot read from LabJack port AIN" + AIN
					+ "\n" + e.getMessage(), e.getCause());
		} catch (ModbusSlaveException e) {
			labjackLogger.error(
					"ModbusSlaveExceptio- Cannot read from labjack AIN {} .",
					AIN);
			throw new NoConnection(
					"ModBus Slave exception cannot write to register\n"
							+ e.getMessage(), e.getCause());
		} catch (ModbusException e) {
			labjackLogger.error(
					"ModbusException Cannot read from labjack AIN {} .", AIN);
			throw new NoConnection(
					"ModBus exception cannot write to register\n"
							+ e.getMessage(), e.getCause());
		}

		res = (ReadMultipleRegistersResponse) transaction.getResponse();
		labjackLogger.debug("Setting registers value for the AIN");
		int reg1 = AIN.getValue() * 2;
		int reg2 = (AIN.getValue() * 2) + 1;
		labjackLogger
				.debug("First 8bit register is set to LSB, Second 8bit register set to MSB");
		byte[] lsb = res.getRegister(reg1).toBytes();
		byte[] msb = res.getRegister(reg2).toBytes();
		labjackLogger.debug("Combinging the reg1 and reg2 as a 16bit register");
		byte[] both = { lsb[0], lsb[1], msb[0], msb[1] };

		ByteArrayInputStream bais = new ByteArrayInputStream(both);
		DataInputStream din = new DataInputStream(bais);
		double voltage = din.readDouble();
		labjackLogger.info("Returning {} .Volts read from AIN {} .", voltage,
				AIN);
		return new MDecimal(voltage);
	}
}
