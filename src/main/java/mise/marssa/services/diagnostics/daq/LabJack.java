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
package mise.marssa.services.diagnostics.daq;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import mise.marssa.footprint.datatypes.MBoolean;
import mise.marssa.footprint.datatypes.MString;
import mise.marssa.footprint.datatypes.decimal.MDecimal;
import mise.marssa.footprint.datatypes.integer.MInteger;
import mise.marssa.footprint.exceptions.ConfigurationError;
import mise.marssa.footprint.exceptions.NoConnection;
import mise.marssa.footprint.exceptions.OutOfRange;
import mise.marssa.footprint.logger.MMarker;
import mise.marssa.services.diagnostics.daq.LabJackUE9.TimerUE9;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusIOException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
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
public abstract class LabJack {

	private static Logger logger = (Logger) LoggerFactory
			.getLogger(LabJack.class.getName());

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
	 * @see <a
	 *      href="http://labjack.com/support/u3/users-guide/2.9">http://labjack.com/support/u3/users-guide/2.9</a>
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
	 * The LabJack U3 has only two timers.
	 * 
	 * @see <a
	 *      href="http://labjack.com/support/u3/users-guide/2.9.1">http://labjack.com/support/u3/users-guide/2.9.1</a>
	 * @see mise.marssa.services.diagnostics.daq.LabJack.TimersEnabled
	 */
	public class Timer {
		ITimer timerNumber;
		// Timer mode addresses
		private final MInteger TIMER0_CONFIG_MODE_ADDR = new MInteger(7100);
		private final MInteger TIMER1_CONFIG_MODE_ADDR = new MInteger(7102);
		private final MInteger TIMER2_CONFIG_MODE_ADDR = new MInteger(7104);
		private final MInteger TIMER3_CONFIG_MODE_ADDR = new MInteger(7106);
		private final MInteger TIMER4_CONFIG_MODE_ADDR = new MInteger(7108);
		private final MInteger TIMER5_CONFIG_MODE_ADDR = new MInteger(7110);

		// Timer value addresses
		private final MInteger TIMER0_VALUE_ADDR = new MInteger(7200);
		private final MInteger TIMER1_VALUE_ADDR = new MInteger(7202);
		private final MInteger TIMER2_VALUE_ADDR = new MInteger(7204);
		private final MInteger TIMER3_VALUE_ADDR = new MInteger(7206);
		private final MInteger TIMER4_VALUE_ADDR = new MInteger(7208);
		private final MInteger TIMER5_VALUE_ADDR = new MInteger(7210);

		private MInteger timerConfigModeAddress;
		private MInteger timerValueAddress;

		private Timer(ITimer timerNumber) {
			this.timerNumber = timerNumber;
			// Constructor belongs to an enum class
			// Hence the only possible values for the timerNumber are 0 and 1
			logger.info("Setting timer to", timerNumber.getTimer().intValue());
			switch (timerNumber.getTimer().intValue()) {
			case 0:
				timerConfigModeAddress = TIMER0_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER0_VALUE_ADDR;
				break;
			case 1:
				timerConfigModeAddress = TIMER1_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER1_VALUE_ADDR;
				break;
			case 2:
				timerConfigModeAddress = TIMER2_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER2_VALUE_ADDR;
				break;
			case 3:
				timerConfigModeAddress = TIMER3_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER3_VALUE_ADDR;
				break;
			case 4:
				timerConfigModeAddress = TIMER4_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER4_VALUE_ADDR;
				break;
			case 5:
				timerConfigModeAddress = TIMER5_CONFIG_MODE_ADDR;
				timerValueAddress = TIMER5_VALUE_ADDR;
				break;
			}
		}

		protected int getOrdinal() {
			return timerNumber.getTimer().intValue();
		}
	}

	/**
	 * Contains a host/port connection and the instance of the LabJack class
	 * 
	 * @author Clayton Tabone
	 * 
	 */
	static final class LabJackConnection<LabJackModel> {
		static Logger labjackConnectionLogger = (Logger) LoggerFactory
				.getLogger(LabJackConnection.class);
		MString host;
		MInteger port;
		LabJackModel lj;

		public LabJackConnection(MString host, MInteger port, LabJackModel lj) {
			this.host = host;
			this.port = port;
			this.lj = lj;
			Object[] labjackConnection = { host, port, lj.getClass().getSimpleName() };
			labjackConnectionLogger
					.info("Connecting to a Labjack having host {} . port {} . and enabling {} . Labjack",
							labjackConnection);
		}

		public boolean inUse(MString host, MInteger port) {
			labjackConnectionLogger.debug(MMarker.GETTER,
					"Returning if labjack is already in use");
			return (this.host.getContents().equals(host) && this.port
					.equals(port));
		}
	}

	// The actual TCP connection to the LabJack used in this instance
	private TCPMasterConnection readConnection;
	private TCPMasterConnection writeConnection;

	// The number of timers
	private ITimersEnabled numTimers;

	// The list of timer instance
	private Timer[] timersList;

	// The base clock of the timers
	private ITimerBaseClock timerBaseClock;

	// This is the divisor for the timerBaseClock
	private MInteger timerClockDivisor;

	// host and port variables
	private MString host;
	private MInteger port;

	public LabJack(MString host, MInteger port, ITimersEnabled numTimers)
			throws UnknownHostException, NoConnection {
		Object[] labjackInformation = { host, port, numTimers };
		logger.info(
				"Connecting to a Labjack having host {}, port {}, and enabling {} timers",
				labjackInformation);
		try {
			InetAddress address = InetAddress.getByName(host.getContents()); // the
																				// slave's
																				// address
			logger.info(MMarker.SETTER, "Setting the readConnection");
			readConnection = new TCPMasterConnection(address);
			readConnection.setPort(port.intValue());
			readConnection.connect();
			logger.info(MMarker.SETTER, "Setting the writeConnection");
			writeConnection = new TCPMasterConnection(address);
			writeConnection.setPort(port.intValue());
			writeConnection.connect();
			this.host = host;
			this.port = port;
			this.numTimers = numTimers;

			if (numTimers.getTimersEnabled().intValue() > 0) {
				timersList = new Timer[numTimers.getTimersEnabled().intValue()];

				switch (numTimers.getTimersEnabled().intValue()) {
				case 6:
					timersList[5] = new Timer(TimerUE9.TIMER_5);
				case 5:
					timersList[4] = new Timer(TimerUE9.TIMER_4);
				case 4:
					timersList[3] = new Timer(TimerUE9.TIMER_3);
				case 3:
					timersList[2] = new Timer(TimerUE9.TIMER_2);
				case 2:
					timersList[1] = new Timer(TimerUE9.TIMER_1);
				case 1:
					timersList[0] = new Timer(TimerUE9.TIMER_0);
					break;
				}
			}
			this.write(NUM_TIMERS_ENABLED_ADDR, new MInteger(numTimers
					.getTimersEnabled().intValue()));
		} catch (UnknownHostException e) {
			NoConnection nc = new NoConnection("UnknownHostException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot find host{} . ", host, nc);
			throw nc;
		} catch (IOException e) {
			NoConnection nc = new NoConnection(
					"IOException\n" + e.getMessage(), e.getCause());
			logger.error("Cannot connect to Labjack on host {}", host, nc);
			throw nc;
		} catch (Exception e) {
			NoConnection nc = new NoConnection("Exception\n" + e.getMessage(),
					e.getCause());
			logger.error("Network failure (TCPMasterConnection): {}", host, nc);
			throw nc;
		}
	}

	protected void finalize() throws Throwable {
		logger.debug("Closing the readConnection");
		readConnection.close();
		logger.debug("Closing the writeConnection");
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
		CloneNotSupportedException e = new CloneNotSupportedException();
		logger.error("Clone is not supported\n" + e.getMessage(), e.getCause());
		throw e;
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

	// TODO add comment about clock divisor for first three modes
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
	 * @see <a
	 *      href="http://labjack.com/support/u3/users-guide/2.9">http://labjack.com/support/u3/users-guide/2.9</a>
	 */
	public void setTimerMode(ITimer timer, ITimerConfigMode timerConfigMode)
			throws ConfigurationError, NoConnection {
		logger.info(MMarker.SETTER, "Setting timerMode");
		if ((timer.getTimer().intValue() + 1) > numTimers.getTimersEnabled()
				.intValue()) {
			ConfigurationError e = new ConfigurationError("Timer "
					+ timer.getTimer() + " is not enabled");
			logger.error("ConfigurationError Exception\n" + e.getMessage(),
					e.getCause());
			throw e;
		}
		logger.info("Setting timer {} with a timerConfigMode {}.", timer,
				timerConfigMode);
		Timer t = timersList[timer.getTimer().intValue()];
		logger.debug("Calling the writeMultiple method");
		writeMultiple(t.timerConfigModeAddress,
				timerConfigMode.getTimerConfigMode());
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
	public void setTimerBaseClock(ITimerBaseClock timerBaseClock)
			throws NoConnection {
		logger.info(MMarker.SETTER, "Setting TimerBasedClock");
		this.timerBaseClock = timerBaseClock;
		logger.debug("Calling the writeMultiple method");
		writeMultiple(LabJack.TIMER_BASE_CLOCK_ADDR,
				timerBaseClock.getTimerBaseClock());
	}

	public ITimerBaseClock getTimerBaseClock() {
		logger.info(MMarker.GETTER, "Getting TimerBasedClock");
		return this.timerBaseClock;
	}

	/**
	 * Sets the value of the given timer. The number of timers enabled is set
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
	public void setTimerValue(ITimer timer, MInteger timerValue)
			throws NoConnection, OutOfRange, ConfigurationError {
		logger.info(MMarker.SETTER, "Setting timerValue");
		if ((timer.getTimer().intValue() + 1) > numTimers.getTimersEnabled()
				.intValue()) {
			ConfigurationError e = new ConfigurationError("Timer "
					+ timer.getTimer() + " is not enabled");
			logger.error("ConfigurationError", e);
			throw e;
		}
		if (timerValue.doubleValue() >= Math.pow(2, 32)) {
			OutOfRange e = new OutOfRange(
					"Timer Value must be a value between 0 and 4294967294 (2^32 - 1)");
			logger.error("Out of Range Exception", e);
			throw e;
		}
		logger.info("Setting timer {} . with a timerValue {} .", timer,
				timerValue);
		Timer t = timersList[timer.getTimer().intValue()];
		logger.debug("Calling the writeMultiple method");
		writeMultiple(t.timerValueAddress, timerValue);
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
	public void setTimerClockDivisor(MInteger timerClockDivisor)
			throws OutOfRange, NoConnection {
		logger.info(MMarker.SETTER,
				"Setting timerClockDivisor with value {} .", timerClockDivisor);
		if (timerClockDivisor.intValue() < 1
				|| timerClockDivisor.intValue() > 256) {
			OutOfRange e = new OutOfRange(
					"Timer Clock Divisor must be a value between 1 and 256");
			logger.error("OutOfRange Exception", e);
			throw e;
		}
		this.timerClockDivisor = timerClockDivisor;
		logger.debug("Calling the writeMultiple method");
		writeMultiple(LabJack.TIMER_BASE_CLOCK_ADDR, timerClockDivisor);
	}

	public MInteger getTimerClockDivisor() {
		logger.info(MMarker.GETTER,
				"Getting timerClockDivisor with value {} .", timerClockDivisor);
		return timerClockDivisor;
	}

	/**
	 * This write method is used to write to registers RegisterNumber is the
	 * register to write to while the registerValue is the value to write in the
	 * register(1 is used for logic high while 0 is used for logic low)
	 */
	public void write(MInteger registerNumber, MInteger registerValue)
			throws NoConnection {
		logger.info("Writing to register {} . with a registerValue {} .",
				registerNumber, registerValue);
		SimpleRegister register = new SimpleRegister(registerValue.intValue());
		WriteSingleRegisterRequest writeRequest = new WriteSingleRegisterRequest(
				registerNumber.intValue(), register);

		// Prepare the transaction
		logger.info("Preparing the Transaction");
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				writeConnection);
		logger.debug("Labjack setting request to write to a register");
		transaction.setRequest(writeRequest);

		try {
			logger.info("Executing the Transaction");
			transaction.execute();
		} catch (ModbusIOException e) {
			NoConnection nc = new NoConnection("ModbusIOException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot write to LabJack FIO port" + registerNumber,
					nc);
			throw nc;
		} catch (ModbusSlaveException e) {
			NoConnection nc = new NoConnection("ModBusSlaveException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot write to LabJack register number "
					+ registerNumber, nc);
			throw nc;
		} catch (ModbusException e) {
			NoConnection nc = new NoConnection("ModBusException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot write to labjack register number "
					+ registerNumber, nc);
			throw nc;
		}
	}

	/**
	 * A modification of the write method to accept boolean as a parameter.
	 * 
	 * @param registerNumber
	 *            The register number for the port which will take the new state
	 * @param state
	 *            The state of the output port (true = high, false = low)
	 * @throws NoConnection
	 */
	public void write(MInteger registerNumber, MBoolean state)
			throws NoConnection {
		logger.info("Setting register {} . to {} .", registerNumber, state);
		int highLow = (state.getValue() ? 1 : 0);
		logger.debug("Calling the write method");
		write(registerNumber, new MInteger(highLow));
	}

	public void writeMultiple(MInteger registerNumber, MInteger registerValue)
			throws NoConnection {

		logger.info("Writing to register {} . with a registerValue {} .",
				registerNumber, registerValue);
		logger.debug("Dividing the 16bit registeer to two 8bit registers");
		SimpleRegister registerLSB = new SimpleRegister(registerValue.and(
				new MInteger(0xFFFF)).intValue());
		SimpleRegister registerMSB = new SimpleRegister(
				(registerValue.and(new MInteger(0xFFFF0000))).shiftLeft(16)
						.intValue());
		SimpleRegister[] registerArray = { registerLSB, registerMSB };

		WriteMultipleRegistersRequest writeRequest = new WriteMultipleRegistersRequest(
				registerNumber.intValue(), registerArray);

		// Prepare the transaction
		logger.info("Preparing the Transaction");
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				writeConnection);

		logger.debug("Labjack setting request to write multiple registers");
		transaction.setRequest(writeRequest);

		// Execute the transaction repeat times
		try {
			logger.info("Executing the Transaction");
			transaction.execute();
		} catch (ModbusIOException e) {
			NoConnection nc = new NoConnection("ModbusIOException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot write to labjack register number "
					+ registerNumber, nc);
			throw nc;
		} catch (ModbusSlaveException e) {
			NoConnection nc = new NoConnection("ModBusSlaveException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot write to LabJack register number "
					+ registerNumber, nc);
			throw nc;
		} catch (ModbusException e) {
			NoConnection nc = new NoConnection("ModbusException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot write to LabJack register number "
					+ registerNumber, nc);
			throw new NoConnection("ModBus exception cannot write to register"
					+ (registerNumber.subtract(new MInteger(6000))) + "\n"
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

		logger.info("Preparing the Transaction");
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				readConnection);
		ReadMultipleRegistersRequest req = new ReadMultipleRegistersRequest(
				ref.intValue(), count.intValue());
		ReadMultipleRegistersResponse res = null;

		logger.debug("Labjack setting request to read multiple registers");
		transaction.setRequest(req);

		try {
			logger.info("Executing the Transaction");
			transaction.execute();
		} catch (ModbusIOException e) {
			NoConnection nc = new NoConnection("ModbusIOException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot read from LabJack AIN " + AIN, nc);
			throw nc;
		} catch (ModbusSlaveException e) {
			NoConnection nc = new NoConnection("ModBusSlaveException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot read from LabJack AIN " + AIN, nc);
			throw nc;
		} catch (ModbusException e) {
			NoConnection nc = new NoConnection("ModbusException\n"
					+ e.getMessage(), e.getCause());
			logger.error("Cannot read from LabJack AIN " + AIN, nc);
			throw nc;
		}

		res = (ReadMultipleRegistersResponse) transaction.getResponse();
		logger.debug("Setting registers value for the AIN");
		int reg1 = AIN.multiply(new MInteger(2)).intValue();
		int reg2 = (AIN.multiply(new MInteger(2))).add(new MInteger(1))
				.intValue();
		logger.debug("First 8bit register is set to LSB, Second 8bit register set to MSB");
		byte[] lsb = res.getRegister(reg1).toBytes();
		byte[] msb = res.getRegister(reg2).toBytes();
		logger.debug("Combinging the reg1 and reg2 as a 16bit register");
		byte[] both = { lsb[0], lsb[1], msb[0], msb[1] };

		ByteArrayInputStream bais = new ByteArrayInputStream(both);
		DataInputStream din = new DataInputStream(bais);
		double voltage = din.readFloat();
		logger.info("Returning {} .Volts read from AIN {} .", voltage, AIN);
		return new MDecimal(voltage);
	}

	public void readOne() {
		ModbusTCPTransaction transaction = new ModbusTCPTransaction(
				readConnection);
		ReadInputDiscretesRequest req = null; // the request
		ReadInputDiscretesResponse res = null; // the response
		req = new ReadInputDiscretesRequest(1, 3);
		transaction.setRequest(req);
		try {
			transaction.execute();
		} catch (ModbusIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModbusSlaveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModbusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		res = (ReadInputDiscretesResponse) transaction.getResponse();
		System.out.println("Digital Inputs Status="
				+ res.getDiscretes().toString());
	}
}
