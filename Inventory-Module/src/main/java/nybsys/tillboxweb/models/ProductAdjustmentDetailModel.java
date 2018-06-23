/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 19-Feb-18
 * Time: 4:22 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

public class ProductAdjustmentDetailModel extends BaseModel {

    private Integer productAdjustmentDetailID;
    private Integer productAdjustmentID;
    private Integer productID;
    private Double quantity;
    private Double unitPrice;
    private Integer adjustmentMethodID;
    private Integer reasonID;
    private Integer inOut;

    public Integer getProductAdjustmentDetailID() {
        return productAdjustmentDetailID;
    }

    public void setProductAdjustmentDetailID(Integer productAdjustmentDetailID) {
        this.productAdjustmentDetailID = productAdjustmentDetailID;
    }

    public Integer getProductAdjustmentID() {
        return productAdjustmentID;
    }

    public void setProductAdjustmentID(Integer productAdjustmentID) {
        this.productAdjustmentID = productAdjustmentID;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getAdjustmentMethodID() {
        return adjustmentMethodID;
    }

    public void setAdjustmentMethodID(Integer adjustmentMethodID) {
        this.adjustmentMethodID = adjustmentMethodID;
    }

    public Integer getReasonID() {
        return reasonID;
    }

    public void setReasonID(Integer reasonID) {
        this.reasonID = reasonID;
    }

    public Integer getInOut() {
        return inOut;
    }

    public void setInOut(Integer inOut) {
        this.inOut = inOut;
    }
}
