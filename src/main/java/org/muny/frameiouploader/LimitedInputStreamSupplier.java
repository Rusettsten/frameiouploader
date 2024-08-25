package org.muny.frameiouploader;

import java.io.IOException;
import java.io.InputStream;

public interface LimitedInputStreamSupplier {
	  InputStream getLimitedStream(String filePath, long startByte, int length) throws IOException;
}