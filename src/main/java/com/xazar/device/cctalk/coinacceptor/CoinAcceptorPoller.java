package com.xazar.device.cctalk.coinacceptor;

import com.xazar.device.cctalk.CCTalkMessage;
import com.xazar.device.cctalk.CCTalkMessageHeader;
import com.xazar.device.cctalk.exception.InvalidMessageException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CoinAcceptorPoller {

    private static final int POLLING_INTERVAL = 400;

    private Thread pollerThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final CoinAcceptor coinAcceptor;
    private final List<CoinAcceptorEventListener> eventListeners
            = Collections.synchronizedList(new LinkedList<>());

    private int lastEventNum;
    private boolean isFirstEvent = true;

    public CoinAcceptorPoller(CoinAcceptor coinAcceptor) {
        this.coinAcceptor = coinAcceptor;
    }

    public void start() {
        if (isRunning.get()) {
            return;
        }
        isRunning.set(true);
        pollerThread = new Thread(() -> run());
        pollerThread.start();
    }

     private void run() {
        CCTalkMessage request = new CCTalkMessage(CCTalkMessageHeader.READ_BUFFERED_CREDIT_OR_ERROR_CODES);
        while (isRunning.get()) {
            try {
                CCTalkMessage response = coinAcceptor.sendMessage(request);
                onPollResponseReceived(response);
            } catch (Exception e) {
                e.printStackTrace(); //@TODO
            }
            try {
                Thread.sleep(POLLING_INTERVAL);
            } catch (InterruptedException e) {
            }
        }
    }

    private void onPollResponseReceived(CCTalkMessage response) throws InvalidMessageException {
        byte[] data = response.getData();
        if (data.length == 11) {
            int eventNum = data[0] & 0xFF;

            if (isFirstEvent) {
                lastEventNum = eventNum;
                isFirstEvent = false;
                return;
            }

            if (eventNum == lastEventNum) {
                return;
            }

            int eventCount = eventNum - lastEventNum;

            if (eventCount < 0) {
                eventCount += 256;
            }

            if (eventCount > 5) { // @TODO Warning
                eventCount = 5;
            }

            for (int i = eventCount - 1; i > -1; --i) {
                CoinAcceptorEvent event = new CoinAcceptorEvent();
                int creditIndex = 1 + (i * 2);
                int errorIndex = creditIndex + 1;
                CoinAcceptorError error = CoinAcceptorError.valueOf(data[errorIndex] & 0xFF);
                event.setCreditCode(data[creditIndex] & 0xFF);
                event.setError(error);
                onEventOccurred(event);
            }

            lastEventNum = eventNum;
        } else {
            throw new InvalidMessageException("Invalid poll response: data length: " + data.length);
        }
    }

    private void onEventOccurred(CoinAcceptorEvent event) {
        CoinId coinId = coinAcceptor.getCoinIdMap().get(event.getCreditCode());
        if (coinId != null) {
            eventListeners.forEach(listener -> listener.coinAccepted(coinId));
        }
        if (event.getError() != CoinAcceptorError.NO_ERROR) {
            eventListeners.forEach(listener -> listener.errorOccurred(event.getError()));
        }
    }

    public void stop() {
        isRunning.set(false);
    }

    public boolean addEventListener(CoinAcceptorEventListener listener) {
        return eventListeners.add(listener);
    }

    public boolean removeEventListener(CoinAcceptorEventListener listener) {
        return eventListeners.remove(listener);
    }
}
