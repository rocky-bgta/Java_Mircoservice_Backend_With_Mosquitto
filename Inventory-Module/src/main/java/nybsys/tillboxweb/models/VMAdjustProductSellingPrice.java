/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 15/05/2018
 * Time: 03:51
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import java.util.ArrayList;
import java.util.List;

public class VMAdjustProductSellingPrice {
    private Integer adjustmentType;
    private Integer effectType;
    private Integer adjustmentPercentage;
    private Integer roundingType;
    private Integer roundingNearestTo;
    private List<ProductModel> lstSelectedProduct = new ArrayList<>();

    public Integer getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(Integer adjustmentType) {
        this.adjustmentType = adjustmentType;
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

    public Integer getEffectType() {
        return effectType;
    }

    public void setEffectType(Integer effectType) {
        this.effectType = effectType;
    }

    public List<ProductModel> getLstSelectedProduct() {
        return lstSelectedProduct;
    }

    public void setLstSelectedProduct(List<ProductModel> lstSelectedProduct) {
        this.lstSelectedProduct = lstSelectedProduct;
    }
}
