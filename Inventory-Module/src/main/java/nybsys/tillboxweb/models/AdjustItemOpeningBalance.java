/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 11/05/2018
 * Time: 10:28
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import java.util.Date;

public class AdjustItemOpeningBalance {
    private Integer itemID;
    private String reason;
    private Double currentOpeningCost;
    private Double newOpeningCost;
    private Double currentOpeningQuantity;
    private Double newOpeningQuantity;
    private Date openingBalanceAsAt;

    public Integer getItemID() {
        return itemID;
    }

    public void setItemID(Integer itemID) {
        this.itemID = itemID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getCurrentOpeningCost() {
        return currentOpeningCost;
    }

    public void setCurrentOpeningCost(Double currentOpeningCost) {
        this.currentOpeningCost = currentOpeningCost;
    }

    public Double getNewOpeningCost() {
        return newOpeningCost;
    }

    public void setNewOpeningCost(Double newOpeningCost) {
        this.newOpeningCost = newOpeningCost;
    }

    public Double getCurrentOpeningQuantity() {
        return currentOpeningQuantity;
    }

    public void setCurrentOpeningQuantity(Double currentOpeningQuantity) {
        this.currentOpeningQuantity = currentOpeningQuantity;
    }

    public Double getNewOpeningQuantity() {
        return newOpeningQuantity;
    }

    public void setNewOpeningQuantity(Double newOpeningQuantity) {
        this.newOpeningQuantity = newOpeningQuantity;
    }

    public Date getOpeningBalanceAsAt() {
        return openingBalanceAsAt;
    }

    public void setOpeningBalanceAsAt(Date openingBalanceAsAt) {
        this.openingBalanceAsAt = openingBalanceAsAt;
    }
}
