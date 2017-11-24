package com.xazar.device.cctalk.coinacceptor;

public class CoinAcceptorEvent {
    private int creditCode;
    private CoinAcceptorError error;

    public CoinAcceptorEvent() {
    }

    public CoinAcceptorEvent(int creditCode, CoinAcceptorError error) {
        this.creditCode = creditCode;
        this.error = error;
    }

    public int getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(int creditCode) {
        this.creditCode = creditCode;
    }

    public CoinAcceptorError getError() {
        return error;
    }

    public void setError(CoinAcceptorError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "CoinAcceptorEvent [creditCode=" + creditCode + ", error=" + error + "]";
    }
}
