/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 28-Feb-18
 * Time: 10:25 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

import java.util.Objects;

public class SupplierPaymentDetailModel extends BaseModel {

    private Integer supplierPaymentDetailID;
    private Integer supplierPaymentID;
    private Integer referenceID;
    private Double paidAmount;
    private Double discount;
    private Integer paymentStatus;
    private Integer referenceType;

    public Integer getSupplierPaymentDetailID() {
        return supplierPaymentDetailID;
    }

    public void setSupplierPaymentDetailID(Integer supplierPaymentDetailID) {
        this.supplierPaymentDetailID = supplierPaymentDetailID;
    }

    public Integer getSupplierPaymentID() {
        return supplierPaymentID;
    }

    public void setSupplierPaymentID(Integer supplierPaymentID) {
        this.supplierPaymentID = supplierPaymentID;
    }

    public Integer getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(Integer referenceType) {
        this.referenceType = referenceType;
    }
}
