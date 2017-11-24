package com.xazar.device.cctalk.coinacceptor;

public class CoinId {
    private String countryCode;
    private Integer value;
    private char mintIssue;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public char getMintIssue() {
        return mintIssue;
    }

    public void setMintIssue(char mintIssue) {
        this.mintIssue = mintIssue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CoinId) {
            CoinId o = (CoinId) obj;
            return countryCode.equals(o.countryCode) && value.equals(o.value) && mintIssue == o.mintIssue;
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public String toString() {
        return "CoinId [countryCode=" + countryCode + ", value=" + value + ", mintIssue=" + mintIssue + "]";
    }

    public String toCoinIdString() {
        String valueString = value.toString();
        for (int i = 0; i < 3 - valueString.length(); ++i) {
            valueString = "0" + valueString;
        }
        return countryCode + valueString + mintIssue;
    }

    public static CoinId parse(String coinIdString) {
        if (coinIdString.length() != 6) {
            throw new IllegalArgumentException("the length of the coinIdString must be 6");
        }
        CoinId coin = new CoinId();
        coin.countryCode = coinIdString.substring(0, 2);
        coin.value = Integer.parseInt(coinIdString.substring(2, 5));
        coin.mintIssue = coinIdString.charAt(5);
        return coin;
    }
}
