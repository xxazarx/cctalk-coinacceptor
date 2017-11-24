package com.xazar.device.cctalk.coinacceptor;

import com.xazar.device.cctalk.CCTalkMessageHeader;
import com.xazar.device.cctalk.CCTalkMessage;
import com.xazar.device.cctalk.CCTalkDevice;
import com.xazar.device.cctalk.exception.CCTalkDeviceException;
import com.xazar.device.cctalk.exception.CCTalkIOException;

import java.util.Hashtable;
import java.util.Map;

public class CoinAcceptor extends CCTalkDevice {

    private static final String COIN_ACCEPTOR_EQUIPMENT_CATEGORY_ID = "Coin Acceptor";

    private final CoinAcceptorPoller poller = new CoinAcceptorPoller(this);

    private Map<Integer, CoinId> coinIdMap = new Hashtable<>();

    public CoinAcceptor() {
        super(COIN_ACCEPTOR_EQUIPMENT_CATEGORY_ID);
    }

    public CoinAcceptorPoller getPoller() {
        return poller;
    }

    @Override
    public void disconnect() throws CCTalkDeviceException {
        poller.stop();
        super.disconnect();
    }

    protected Map<Integer, CoinId> getCoinIdMap() {
        return coinIdMap;
    }

    public void enableAcceptance(CoinId... coinsForAcceptance) throws CCTalkIOException {
        coinIdMap.clear();
        for (int no = 1; no < 17; ++no) {
            CoinId coinId = getCoinId(no);
            if (coinId != null) {
                coinIdMap.put(no, coinId);
            }
        }

        byte[] inhibitStatus = new byte[]{0, 0};
        for (CoinId coinId : coinsForAcceptance) {
            for (Map.Entry<Integer, CoinId> entry : coinIdMap.entrySet()) {
                if (entry.getValue().equals(coinId)) {
                    Integer no = entry.getKey();
                    if (no != null) {
                        if (no < 9) {
                            inhibitStatus[0] |= 1 << (no - 1);
                        } else {
                            inhibitStatus[1] |= 1 << (no - 9);
                        }
                    }
                }
            }
        }
        setInhibitStatus(inhibitStatus);
    }

    public void disableAcceptance() throws CCTalkIOException {
        byte[] inhibitStatus = new byte[]{0, 0};
        setInhibitStatus(inhibitStatus);
    }

    private CoinId getCoinId(int no) throws CCTalkIOException {
        CCTalkMessage request = new CCTalkMessage(CCTalkMessageHeader.REQUEST_COIN_ID);
        byte[] requestData = new byte[1];
        requestData[0] = (byte) no;
        request.setData(requestData);
        CCTalkMessage response = sendMessage(request);
        String coinIdString = new String(response.getData()).trim();
        if (coinIdString.isEmpty()) {
            return null;
        } else {
            return CoinId.parse(coinIdString);
        }
    }
}
