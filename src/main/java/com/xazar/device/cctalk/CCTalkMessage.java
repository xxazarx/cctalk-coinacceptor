package com.xazar.device.cctalk;

import com.xazar.device.cctalk.exception.InvalidMessageException;

import java.util.Arrays;

public class CCTalkMessage {

	private static final int DEFAULT_DESTINATION_ADDRESS	= 0x02; // destination address (slave)
	private static final int DEFAULT_SOURCE_ADDRESS			= 0x01; // source address (host)

	private CCTalkMessageHeader header;
	private byte dest = DEFAULT_DESTINATION_ADDRESS;
	private byte source = DEFAULT_SOURCE_ADDRESS;
	private byte[] data = new byte[0];

	public CCTalkMessage() {}

	public CCTalkMessage(CCTalkMessageHeader header) {
		this.header = header;
	}

	public byte getDest() {
    	return dest;
    }

	public byte[] getData() {
    	return data;
    }

	public CCTalkMessageHeader getHeader() {
    	return header;
    }

	public byte getSource() {
    	return source;
    }

	public void setDest(byte dest) {
		this.dest = dest;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setHeader(CCTalkMessageHeader header) {
		this.header = header;
	}

	public void setSource(byte source) {
		this.source = source;
	}

	@Override
    public String toString() {
	    return "CCTalkMessage [data=" + Arrays.toString(data) + ", dest="
	            + dest + ", header=" + header + ", source=" + source + "]";
    }

    public byte[] toByteArray() {
		byte[] bytes = new byte[data.length + 5];
		bytes[0] = dest;
		bytes[1] = (byte) data.length;
		bytes[2] = source;
		bytes[3] = header.getValue();
		System.arraycopy(data, 0, bytes, 4, data.length);
		bytes[bytes.length - 1] = calculateChecksum(bytes);
		return bytes;
	}

	public static CCTalkMessage parse(byte[] bytes) throws InvalidMessageException {
		if (calculateChecksum(bytes) != bytes[bytes.length - 1]) {
			throw new InvalidMessageException("Invalid checksum");
		}
		CCTalkMessage message = new CCTalkMessage();
		message.setDest(bytes[0]);
		message.setSource(bytes[2]);
		message.setHeader(CCTalkMessageHeader.valueOf(bytes[3]));
		message.setData(Arrays.copyOfRange(bytes, 4, bytes.length - 1));
		return message;
	}

	private static byte calculateChecksum(byte[] message) {
		int sum = 0;
		for (int i = 0; i < message.length - 1; ++i) {
			sum += message[i];
		}
		sum %= 256;
		return (byte) (256 - sum);
	}
}
