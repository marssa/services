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
package org.marssa.services.scheduling;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;

/**
 * @author Warren Zahra
 * 
 */
public class MTimerService implements ITimerService {
	private Timer marssaTimer;
	private static MTimerService mTimer;
	private MTimerTask task;
	private final ArrayList<MTimerTask> timerTasklist = new ArrayList<MTimerTask>();;

	private MTimerService() {
		marssaTimer = new Timer();
	}

	public static MTimerService getInstance() {
		if (mTimer == null) {
			mTimer = new MTimerService();
		}
		return mTimer;
	}

	@Override
	public void addSchedule(MTimerTask task, Date date) {
		marssaTimer.schedule(task, date);
		timerTasklist.add(task);

	}

	@Override
	public void addSchedule(MTimerTask task, Date date, long period) {
		marssaTimer.schedule(task, date, period);
		timerTasklist.add(task);

	}

	@Override
	public void addSchedule(MTimerTask task, long delay) {
		marssaTimer.schedule(task, delay);
		timerTasklist.add(task);

	}

	@Override
	public void addSchedule(MTimerTask task, long delay, long period) {
		marssaTimer.schedule(task, delay, period);
		timerTasklist.add(task);

	}

	public void addScheduleAtFixedRate(MTimerTask task, Date firstTime,
			long period) {
		marssaTimer.scheduleAtFixedRate(task, firstTime, period);
		timerTasklist.add(task);
	}

	public void addScheduleAtFixedRate(MTimerTask task, long delay, long period) {
		marssaTimer.scheduleAtFixedRate(task, delay, period);
		timerTasklist.add(task);
	}

	public void cancel() {
		marssaTimer.cancel();
		marssaTimer = null;
		timerTasklist.clear();
	}

	public void cancelTask(String timerTaskName) {
		timerTasklist.remove(new String(timerTaskName.toString()));
	}
}
