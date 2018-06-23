/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/9/2018
 * Time: 11:05 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.coreModels;

import nybsys.tillboxweb.BaseModel;

import java.util.Date;
import java.util.Objects;

public class CurrencyExchangeRateModel extends BaseModel {

    private Integer currencyExchangeRateID;
    private Integer businessID;
    private Date date;
    private Integer currencyID;
    private Double rate;

    public Integer getCurrencyExchangeRateID() {
        return currencyExchangeRateID;
    }

    public void setCurrencyExchangeRateID(Integer currencyExchangeRateID) {
        this.currencyExchangeRateID = currencyExchangeRateID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getCurrencyID() {
        return currencyID;
    }

    public void setCurrencyID(Integer currencyID) {
        this.currencyID = currencyID;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }
}
