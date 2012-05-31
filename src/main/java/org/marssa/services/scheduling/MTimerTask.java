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
package org.marssa.services.scheduling;

import java.util.TimerTask;

import org.marssa.footprint.datatypes.MString;


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
