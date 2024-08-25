package org.muny.frameiouploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FileInputStream {
  private final int limit;
  private long bytesRead = 0;

  public LimitedInputStream(File file, long skipBytes, int limit) throws IOException {
	super(file);
    this.limit = limit;
    this.skip(skipBytes);
  }

  @Override
  public int read() throws IOException {
    if (bytesRead >= limit) {
      return -1; // Reached limit
    }
    int b = this.read();
    if (b != -1) {
      bytesRead++;
    }
    return b;
  }
}