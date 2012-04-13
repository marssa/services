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
package mise.marssa.services.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Warren Zahra
 * 
 */
public abstract class LoggingTask<QuantityType> extends TimerTask {

	private ArrayList<QuantityType> quantities = new ArrayList<QuantityType>();
	private String taskName;

	protected LoggingTask(List<QuantityType> quantities) {
		StringBuilder sb = new StringBuilder(quantities.getClass().toString());
		sb.append("( ");
		for (QuantityType q : quantities) {
			sb.append(q);
			sb.append(' ');
		}
		sb.append(')');
		taskName = sb.toString();

		this.quantities.addAll(quantities);
	}

	public String getTaskName() {
		return taskName;
	}
	
	public ArrayList<QuantityType> getQuantities() {
		return new ArrayList<QuantityType>(quantities);
	}
}
