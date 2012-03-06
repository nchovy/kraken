package org.krakenapps.instrumentation;

import java.lang.instrument.Instrumentation;

import org.krakenapps.api.InstrumentationService;
import org.krakenapps.main.Kraken;

public class InstrumentationServiceImpl implements InstrumentationService {
	@Override
	public Instrumentation getInstrumentation() {
		return Kraken.instrumentation;
	}
}
