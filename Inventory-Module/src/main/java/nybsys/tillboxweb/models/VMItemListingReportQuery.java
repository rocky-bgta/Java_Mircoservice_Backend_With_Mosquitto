/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 17/05/2018
 * Time: 10:42
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

public class VMItemListingReportQuery {
    private Integer status;
    private Integer itemType;
    private Integer priceListID;
    private Integer adjustmentType;
    private Integer effectType;
    private Integer adjustmentPercentage;
    private Integer roundingType;
    private Integer roundingNearestTo;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getItemType() {
        return itemType;
    }

    public void setItemType(Integer itemType) {
        this.itemType = itemType;
    }

    public Integer getPriceListID() {
        return priceListID;
    }

    public void setPriceListID(Integer priceListID) {
        this.priceListID = priceListID;
    }

    public Integer getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(Integer adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public Integer getEffectType() {
        return effectType;
    }

    public void setEffectType(Integer effectType) {
        this.effectType = effectType;
    }

    public Integer getAdjustmentPercentage() {
        return adjustmentPercentage;
    }

    public void setAdjustmentPercentage(Integer adjustmentPercentage) {
        this.adjustmentPercentage = adjustmentPercentage;
    }

    public Integer getRoundingType() {
        return roundingType;
    }

    public void setRoundingType(Integer roundingType) {
        this.roundingType = roundingType;
    }

    public Integer getRoundingNearestTo() {
        return roundingNearestTo;
    }

    public void setRoundingNearestTo(Integer roundingNearestTo) {
        this.roundingNearestTo = roundingNearestTo;
    }
}
