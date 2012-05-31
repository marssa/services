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
package org.marssa.services.logging;

import java.util.ArrayList;
import java.util.List;

import org.marssa.footprint.datatypes.MString;
import org.marssa.services.scheduling.MTimerTask;


/**
 * @author Warren Zahra
 * 
 */
public abstract class LoggingTask<QuantityType> extends MTimerTask {

	private ArrayList<QuantityType> quantities = new ArrayList<QuantityType>();

	protected LoggingTask(List<QuantityType> quantities) {
		super();
		StringBuilder sb = new StringBuilder(quantities.getClass().toString());
		sb.append("( ");
		for (QuantityType q : quantities) {
			sb.append(q);
			sb.append(' ');
		}
		sb.append(')');
		setTaskName(new MString(sb.toString()));

		this.quantities.addAll(quantities);
	}

	public ArrayList<QuantityType> getQuantities() {
		return new ArrayList<QuantityType>(quantities);
	}
}
