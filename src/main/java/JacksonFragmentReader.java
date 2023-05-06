import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JacksonFragmentReader extends Reader {

  private final ObjectMapper objectMapper;
  private final List<String> fragments;
  private final List<Object> parameters;
  
  private int currentFragmentIndex;
  private int currentCharacterIndex;
  private String currentFragment;
  private boolean closed;

  public JacksonFragmentReader(ObjectMapper objectMapper, List<String> fragments, List<Object> parameters) {
    this.objectMapper = objectMapper;
    this.fragments = fragments;
    this.parameters = parameters;
    this.currentFragmentIndex = 0;
    this.currentCharacterIndex = 0;
    this.currentFragment = fragments.get(0);
    this.closed = false;
  }
  
  private void closedCheck() throws IOException {
    if (this.closed) {
      throw new IOException("reader is closed");
    }
  }

  private void updateRead(int read) throws IOException {
    this.currentCharacterIndex += read;
    if (this.currentCharacterIndex == this.currentFragment.length()) {
      // TODO check for empty strings
      this.currentCharacterIndex = 0;
      this.currentFragmentIndex += 1;
      if (this.currentFragmentIndex < this.fragments.size() + this.parameters.size()) {
        if (this.currentFragmentIndex % 2 == 0) {
          this.currentFragment = this.fragments.get(this.currentFragmentIndex / 2);
        } else {
          this.currentFragment = this.objectMapper.writeValueAsString(this.parameters.get(this.currentFragmentIndex / 2));
        }
      }
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    this.closedCheck();
    if (this.currentFragmentIndex == this.fragments.size() + this.parameters.size()) {
      return -1;
    }
    // TODO parameter validation
    int toRead = Math.min(len, this.currentFragment.length() - this.currentCharacterIndex);
    this.currentFragment.getChars(this.currentCharacterIndex, this.currentCharacterIndex + toRead, cbuf, off);
    updateRead(toRead);
    return toRead;
  }
  
  @Override
  public int read() throws IOException {
    this.closedCheck();
    return super.read();
  }
  
  @Override
  public int read(CharBuffer target) throws IOException {
    this.closedCheck();
    if (target.hasArray()) {
      return super.read(target);
    } else {
      if (this.currentFragmentIndex == this.fragments.size() + this.parameters.size()) {
        return -1;
      }
      // TODO parameter validation
      int remaining = target.remaining();
      if (remaining == 0) {
        throw new BufferOverflowException();
      }
      int toRead = Math.min(remaining, this.currentFragment.length() - this.currentCharacterIndex);
      target.put(this.currentFragment, this.currentCharacterIndex, this.currentCharacterIndex + toRead);
      updateRead(toRead);
      return toRead;
    }
  }
  
  @Override
  public long transferTo(Writer out) throws IOException {
    this.closedCheck();
    return super.transferTo(out);
  }

  @Override
  public void close() {
    this.closed = true;
  }

}
