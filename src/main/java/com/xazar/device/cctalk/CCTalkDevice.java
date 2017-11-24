package com.xazar.device.cctalk;

import com.xazar.device.cctalk.exception.CCTalkDeviceException;
import com.xazar.device.cctalk.exception.CCTalkIOException;
import com.xazar.device.cctalk.exception.InvalidMessageException;
import com.fazecast.jSerialComm.SerialPort;

import java.util.stream.Stream;

public class CCTalkDevice {

    public static final int DEFAULT_BAUD_RATE = 9600;
    public static final int DEFAULT_DATA_BITS = 8;
    public static final int DEFAULT_STOP_BITS = SerialPort.ONE_STOP_BIT;
    public static final int DEFAULT_PARITY = SerialPort.NO_PARITY;

    private static final int READ_TIMEOUT = 200;
    private static final int WRITE_TIMEOUT = 200;

    private static final int READ_BUFFER_SIZE = 512;

    private SerialPort serialPort;
    private final String equipmentCategoryId;

    private final byte[] readBuffer = new byte[READ_BUFFER_SIZE];
    private final Object sendLock = new Object();

    public CCTalkDevice(String equipmentCategoryId) {
        this.equipmentCategoryId = equipmentCategoryId;
    }

    public void connectAuto() throws CCTalkDeviceException {
        for (SerialPort serialPort : SerialPort.getCommPorts()) {
            try {
                connect(serialPort);
                return;
            } catch (CCTalkDeviceException e) {
            }
        }
        throw new CCTalkDeviceException("Device not found");
    }

    public void connect(String serialPortName) throws CCTalkDeviceException {
        SerialPort serialPort = SerialPort.getCommPort(serialPortName);
        boolean portFound = Stream.of(SerialPort.getCommPorts())
                .map(sp -> sp.getSystemPortName())
                .anyMatch(serialPort.getSystemPortName()::equals);
        if (!portFound) {
            throw new CCTalkDeviceException("Port not found");
        }
        connect(serialPort);
    }

    private void connect(SerialPort serialPort) throws CCTalkDeviceException {
        if (this.serialPort != null && this.serialPort.isOpen()) {
            throw new CCTalkDeviceException("Is already connected");
        }
        this.serialPort = serialPort;
        serialPort.setComPortParameters(
                DEFAULT_BAUD_RATE, DEFAULT_DATA_BITS, DEFAULT_STOP_BITS, DEFAULT_PARITY
        );
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING,
                READ_TIMEOUT,
                WRITE_TIMEOUT
        );
        boolean connected = serialPort.openPort();
        if (!connected) {
            throw new CCTalkDeviceException("Port busy");
        }

        boolean validDevice = identify();
        if (!validDevice) {
            try {
                disconnect();
            } catch (CCTalkDeviceException e) {}
            throw new CCTalkDeviceException("Invalid device");
        }
    }

    public void disconnect() throws CCTalkDeviceException {
        try {
            if (serialPort == null) {
                throw new CCTalkDeviceException("Device is not connected");
            }
            serialPort.closePort();
        } catch (Exception e) {
            throw new CCTalkDeviceException("Could not be disconnected", e);
        } finally {
            serialPort = null;
        }
    }

    public CCTalkMessage sendMessage(CCTalkMessage message) throws CCTalkIOException {
        synchronized (sendLock) {
            writeMessage(message);
            return readMessage();
        }
    }

    protected void setInhibitStatus(byte[] status) throws CCTalkIOException {
        if (status.length != 2) {
            throw new IllegalArgumentException("The length of the status array should be 2");
        }
        CCTalkMessage request = new CCTalkMessage(CCTalkMessageHeader.MODIFY_INHIBIT_STATUS);
        request.setData(status);
        sendMessage(request);
    }

    private boolean identify() {
        try {
            return readEquipmentCategoryId().equals(equipmentCategoryId);
        } catch (Exception e) {
            return false;
        }
    }

    private String readEquipmentCategoryId() throws CCTalkIOException {
        CCTalkMessage request = new CCTalkMessage(CCTalkMessageHeader.REQUEST_EQUIPMENT_CATEGORY_ID);
        CCTalkMessage response = sendMessage(request);
        String equipmentCategoryId = new String(response.getData()).trim();
        return equipmentCategoryId;
    }

    private void writeMessage(CCTalkMessage message) throws CCTalkIOException {
        try {
            if (serialPort == null) {
                throw new CCTalkDeviceException("Device is not connected");
            }
            byte[] bytes = message.toByteArray();
            int len = serialPort.writeBytes(bytes, bytes.length);
            if (len != bytes.length) {
                throw new InvalidMessageException(
                        "CCTalkMessage was shorter than expected length of " + bytes.length + " bytes");
            }
        } catch (Exception e) {
            throw new CCTalkIOException("Message could not be written", e);
        }
    }

    private CCTalkMessage readMessage() throws CCTalkIOException {
        try {
            if (serialPort == null) {
                throw new CCTalkDeviceException("Device is not connected");
            }
            int len = serialPort.readBytes(readBuffer, 2);
            if (len != 2) {
                throw new InvalidMessageException(
                        "CCTalkMessage was less than two bytes long, therefore did not contain a data length byte");
            }
            //Convert unsigned byte to signed
            int dataLength = (int) (readBuffer[1] & 0xFF);
            int expectedMessageLength = dataLength + 5; // data + source, destination, checksum, numBytes, header
            int expectedBytesToRead = expectedMessageLength - 2;

            if (expectedMessageLength < 0 && expectedBytesToRead > READ_BUFFER_SIZE) {
                throw new InvalidMessageException("Invalid message length: " + expectedMessageLength);
            }

            byte[] messageBytes = new byte[expectedMessageLength];
            System.arraycopy(readBuffer, 0, messageBytes, 0, len);

            len = serialPort.readBytes(readBuffer, expectedBytesToRead);

            if(len != expectedBytesToRead) {
                throw new InvalidMessageException(
                        "CCTalkMessage was shorter than expected length of " + expectedMessageLength + " bytes");
            }

            System.arraycopy(readBuffer, 0, messageBytes, 2, len);

            return CCTalkMessage.parse(messageBytes);
        } catch (Exception e) {
            throw new CCTalkIOException("Message could not be read", e);
        }
    }
}
