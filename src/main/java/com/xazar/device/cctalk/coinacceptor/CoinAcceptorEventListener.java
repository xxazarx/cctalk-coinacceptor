package com.xazar.device.cctalk.coinacceptor;

public interface CoinAcceptorEventListener {
    void coinAccepted(CoinId coinId);
    void errorOccurred(CoinAcceptorError error);
}
