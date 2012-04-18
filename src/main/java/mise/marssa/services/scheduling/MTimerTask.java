/**
 * 
 */
package mise.marssa.services.scheduling;

import java.util.TimerTask;

import mise.marssa.footprint.datatypes.MString;

/**
 * @author Clayton Tabone
 * 
 */
public abstract class MTimerTask extends TimerTask {

	private MString taskName;

	/**
	 * Default no-arg constructor. Task name is assigned to the class name of
	 * the class instance.
	 * 
	 * @see {@link Object#getClass()}
	 * @see {@link Class#getName()}
	 */
	public MTimerTask() {
		super();
		this.taskName = new MString(this.getClass().getName());
	}

	/**
	 * Constructor which takes a task name.
	 * 
	 * @param taskName
	 *            task name for this t
	 */
	public MTimerTask(MString taskName) {
		super();
		this.taskName = taskName;
	}

	/**
	 * Returns the name of this task
	 * 
	 * @return the task name
	 */
	public MString getTaskName() {
		return taskName;
	}
	
	public void setTaskName(MString taskName) {
		this.taskName = taskName;
	}
}
