package org.krakenapps.sqlengine.bdb;

import org.krakenapps.sqlengine.Status;

import com.sleepycat.je.OperationStatus;

public class StatusConverter {
	private StatusConverter() {
	}

	public static Status convert(OperationStatus status) {
		switch (status) {
		case SUCCESS:
			return Status.Success;
		case KEYEMPTY:
			return Status.KeyEmpty;
		case KEYEXIST:
			return Status.KeyExist;
		case NOTFOUND:
			return Status.NotFound;
		}

		throw new IllegalStateException();
	}

}
