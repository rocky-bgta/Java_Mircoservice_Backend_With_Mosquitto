/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 22/02/2018
 * Time: 11:15
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModelWithCurrency;

import java.util.Date;
import java.util.Objects;

public class SupplierAdjustmentModel extends BaseModelWithCurrency{
    private Integer supplierAdjustmentID;
    private Integer businessID;
    private Integer supplierID;
    private Integer effectType;
    private Integer vatType;
    private Integer accountID;
    private String docNumber;
    private String supplierAdjustmentNo;
    private String reference;
    private Double exclusive;
    private Double vat;
    private Double total;
    private String description;
    private String comment;
    private Date date;
    private Double unAllocatedAmount;
    private Integer adjustmentStatus;

    public Boolean isAdjustmentEditable;

    public Integer getSupplierAdjustmentID() {
        return supplierAdjustmentID;
    }

    public void setSupplierAdjustmentID(Integer supplierAdjustmentID) {
        this.supplierAdjustmentID = supplierAdjustmentID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
    }

    public Integer getEffectType() {
        return effectType;
    }

    public void setEffectType(Integer effectType) {
        this.effectType = effectType;
    }

    public Integer getVatType() {
        return vatType;
    }

    public void setVatType(Integer vatType) {
        this.vatType = vatType;
    }

    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getSupplierAdjustmentNo() {
        return supplierAdjustmentNo;
    }

    public void setSupplierAdjustmentNo(String supplierAdjustmentNo) {
        this.supplierAdjustmentNo = supplierAdjustmentNo;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Double getExclusive() {
        return exclusive;
    }

    public void setExclusive(Double exclusive) {
        this.exclusive = exclusive;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getUnAllocatedAmount() {
        return unAllocatedAmount;
    }

    public void setUnAllocatedAmount(Double unAllocatedAmount) {
        this.unAllocatedAmount = unAllocatedAmount;
    }

    public Integer getAdjustmentStatus() {
        return adjustmentStatus;
    }

    public void setAdjustmentStatus(Integer adjustmentStatus) {
        this.adjustmentStatus = adjustmentStatus;
    }
}
