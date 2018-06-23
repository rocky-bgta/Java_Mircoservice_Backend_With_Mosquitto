/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 22/02/2018
 * Time: 11:24
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

public class SupplierAdjustmentDetailModel extends BaseModel {

    private Integer supplierAdjustmentDetailID;

    private Integer supplierAdjustmentID;

    private Integer referenceID;

    private Double adjustAmount;

    private Integer referenceType;

    public Integer getSupplierAdjustmentDetailID() {
        return supplierAdjustmentDetailID;
    }

    public void setSupplierAdjustmentDetailID(Integer supplierAdjustmentDetailID) {
        this.supplierAdjustmentDetailID = supplierAdjustmentDetailID;
    }

    public Integer getSupplierAdjustmentID() {
        return supplierAdjustmentID;
    }

    public void setSupplierAdjustmentID(Integer supplierAdjustmentID) {
        this.supplierAdjustmentID = supplierAdjustmentID;
    }

    public Integer getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
    }

    public Double getAdjustAmount() {
        return adjustAmount;
    }

    public void setAdjustAmount(Double adjustAmount) {
        this.adjustAmount = adjustAmount;
    }

    public Integer getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(Integer referenceType) {
        this.referenceType = referenceType;
    }
}
