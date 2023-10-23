package edu.ucsb.ece150.gauchopay;

import java.io.Serializable;


public class Card implements Serializable{
    private String cardNumber;
    private String cardExpirationMonth;
    private String cardExpirationYear;
    private String cardCvv;
    private String cardPostalCode;

    public Card(String cardNumber, String cardExpirationMonth, String cardExpirationYear, String cardCvv, String cardPostalCode) {
        this.cardNumber = cardNumber;
        this.cardExpirationMonth = cardExpirationMonth;
        this.cardExpirationYear = cardExpirationYear;
        this.cardCvv = cardCvv;
        this.cardPostalCode = cardPostalCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardExpirationMonth() {
        return cardExpirationMonth;
    }

    public String getCardExpirationYear() {
        return cardExpirationYear;
    }

    public String getCardCvv() {
        return cardCvv;
    }

    public String getCardPostalCode() {
        return cardPostalCode;
    }
}
