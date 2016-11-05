/*
 * Copyright (c) 2007, intarsys consulting GmbH
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of intarsys nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.intarsys.tools.randomaccess;

import java.io.IOException;

/**
 * A wrapper around a byte array to allow random access like API.
 * 
 */
public class RandomAccessByteArray extends AbstractRandomAccess {
	/**
	 * The minimum number of bytes we will resize the byte array buffer
	 */
	private static final int RESIZE_BYTES = 32;

	/**
	 * The byte array data
	 */
	protected byte[] data;

	/**
	 * The current offset into the byte array
	 */
	private int offset;

	/**
	 * The number of valid bytes in the byte array
	 */
	protected int length;

	public RandomAccessByteArray(byte[] buffer) {
		this.offset = 0;
		if (buffer == null) {
			this.data = new byte[RESIZE_BYTES];
			this.length = 0;
		} else {
			this.data = buffer;
			this.length = buffer.length;
		}
	}

	public RandomAccessByteArray(byte[] buffer, int length) {
		this.offset = 0;
		if (buffer == null) {
			this.data = new byte[RESIZE_BYTES];
			this.length = 0;
		} else {
			this.data = buffer;
			this.length = length;
		}
	}

	final protected void basicSetLengthResize(int newLength) {
		if (newLength > data.length) {
			byte[] newData;
			if ((newLength - data.length) < RESIZE_BYTES) {
				newData = new byte[data.length + RESIZE_BYTES];
			} else {
				newData = new byte[newLength];
			}
			System.arraycopy(data, 0, newData, 0, length);
			data = newData;
		}
		length = newLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccess#close()
	 */
	public void close() throws IOException {
		flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccess#flush()
	 */
	public void flush() throws IOException {
		// 
	}

	/**
	 * @return Returns the data.
	 */
	protected byte[] getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#getLength()
	 */
	public long getLength() throws IOException {
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#getOffset()
	 */
	public long getOffset() {
		return offset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#read()
	 */
	public int read() {
		if (offset < length) {
			return data[offset++] & 0xff;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#read(byte[])
	 */
	public int read(byte[] buffer) {
		return read(buffer, 0, buffer.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#read(byte[], int,
	 * int)
	 */
	public int read(byte[] buffer, int bufferOffset, int numBytes) {
		int remaining = (length - offset);
		if (numBytes > remaining) {
			numBytes = remaining;
			if (numBytes == 0) {
				return -1;
			}
		}
		System.arraycopy(data, offset, buffer, bufferOffset, numBytes);
		offset += numBytes;
		return numBytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#seek(long)
	 */
	public void seek(long pOffset) throws IOException {
		this.offset = (int) pOffset;
		if (offset < 0) {
			offset = 0;
			throw new IOException("offset less than 0");
		}
	}

	public void seekBy(long delta) throws IOException {
		this.offset = (int) (offset + delta);
		if (offset < 0) {
			offset = 0;
			throw new IOException("offset less than 0");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#setLength(int)
	 */
	public void setLength(long newLength) {
		int tempLength = (int) newLength;
		if (tempLength > length) {
			basicSetLengthResize(tempLength);
		} else {
			if (offset > tempLength) {
				offset = tempLength;
			}
			length = tempLength;
		}
	}

	public byte[] toByteArray() {
		byte[] newbuf = new byte[length];
		System.arraycopy(data, 0, newbuf, 0, length);
		return newbuf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#write(byte[])
	 */
	public void write(byte[] buffer) {
		write(buffer, 0, buffer.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#write(byte[])
	 */
	public void write(byte[] buffer, int start, int numBytes) {
		int newLength = offset + numBytes;
		if (newLength > length) {
			basicSetLengthResize(newLength);
		}
		System.arraycopy(buffer, start, data, offset, numBytes);
		offset += numBytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.intarsys.tools.randomaccess.IRandomAccessData#write(int)
	 */
	public void write(int b) {
		int newLength = offset + 1;
		if (newLength > length) {
			basicSetLengthResize(newLength);
		}
		data[offset++] = (byte) b;
	}
}
