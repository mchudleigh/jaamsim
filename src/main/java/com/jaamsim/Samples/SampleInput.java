/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
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
package com.jaamsim.Samples;

import java.util.ArrayList;
import java.util.Collections;

import com.jaamsim.basicsim.Entity;
import com.jaamsim.datatypes.DoubleVector;
import com.jaamsim.input.Input;
import com.jaamsim.input.InputErrorException;
import com.jaamsim.input.KeywordIndex;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

public class SampleInput extends Input<SampleProvider> {
	private Class<? extends Unit> unitType = DimensionlessUnit.class;

	public SampleInput(String key, String cat, SampleProvider def) {
		super(key, cat, def);
	}

	public void setUnitType(Class<? extends Unit> u) {
		if (u != unitType)
			this.reset();
		unitType = u;
		if (defValue instanceof SampleConstant)
			((SampleConstant)defValue).setUnitType(unitType);
	}

	@Override
	public void parse(KeywordIndex kw)
	throws InputErrorException {
		// Try to parse as a constant value
		try {
			DoubleVector tmp = Input.parseDoubles(kw, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, unitType);
			Input.assertCount(tmp, 1);
			value = new SampleConstant(unitType, tmp.get(0));
			return;
		}
		catch (InputErrorException e) {}

		// If not a constant, try parsing a SampleProvider
		Input.assertCount(kw, 1);
		Entity ent = Input.parseEntity(kw.getArg(0), Entity.class);
		SampleProvider s = Input.castImplements(ent, SampleProvider.class);
		if( s.getUnitType() != UserSpecifiedUnit.class )
			Input.assertUnitsMatch(unitType, s.getUnitType());
		value = s;
	}

	@Override
	public ArrayList<String> getValidOptions() {
		ArrayList<String> list = new ArrayList<>();
		for (Entity each : Entity.getClonesOfIterator(Entity.class, SampleProvider.class)) {
			SampleProvider sp = (SampleProvider)each;
			if (sp.getUnitType() == unitType)
				list.add(each.getName());
		}
		Collections.sort(list);
		return list;
	}

	@Override
	public void getValueTokens(ArrayList<String> toks) {
		if (value == null) return;

		if (value instanceof SampleConstant) {
			super.getValueTokens(toks);
			return;
		}
		else {
			toks.add(((Entity)value).getName());
		}
	}

	public void verifyUnit() {
		Input.assertUnitsMatch( unitType, value.getUnitType());
	}
}
