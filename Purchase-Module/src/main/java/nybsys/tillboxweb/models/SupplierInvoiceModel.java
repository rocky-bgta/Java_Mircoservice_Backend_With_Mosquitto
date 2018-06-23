/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/23/2018
 * Time: 12:12 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModelWithCurrency;

import java.util.Date;

public class SupplierInvoiceModel extends BaseModelWithCurrency {
    private Integer supplierInvoiceID;
    private Integer businessID;
    private Integer supplierID;
    private Integer purchaseOrderID;
    private String docNumber;
    private String supplierInvoiceNo;
    private String deliveryNoteNo;
    private Date date;
    private Double totalAmount;
    private Double totalVAT;
    private Double totalDiscount;
    private String receivedBy;
    private Integer receivedProductStatus;
    private String note;
    private Integer paymentStatus;
    private Date dueDate;
    private Double totalExclusive;
    private String description;
    private Integer additionalVATType;
    private Integer supplierAddressType;
    private Integer parentInvoiceID;

    private Double supplierDiscountPercentage;

    public Double getSupplierDiscountPercentage() {
        return supplierDiscountPercentage;
    }

    public void setSupplierDiscountPercentage(Double supplierDiscountPercentage) {
        this.supplierDiscountPercentage = supplierDiscountPercentage;
    }

    public Integer getParentInvoiceID() {
        return parentInvoiceID;
    }

    public void setParentInvoiceID(Integer parentInvoiceID) {
        this.parentInvoiceID = parentInvoiceID;
    }

    public Integer getSupplierAddressType() {
        return supplierAddressType;
    }

    public void setSupplierAddressType(Integer supplierAddressType) {
        this.supplierAddressType = supplierAddressType;
    }

    public Double getTotalExclusive() {
        return totalExclusive;
    }

    public void setTotalExclusive(Double totalExclusive) {
        this.totalExclusive = totalExclusive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAdditionalVATType() {
        return additionalVATType;
    }

    public void setAdditionalVATType(Integer additionalVATType) {
        this.additionalVATType = additionalVATType;
    }


    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getSupplierInvoiceID() {
        return supplierInvoiceID;
    }

    public void setSupplierInvoiceID(Integer supplierInvoiceID) {
        this.supplierInvoiceID = supplierInvoiceID;
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

    public Integer getPurchaseOrderID() {
        return purchaseOrderID;
    }

    public void setPurchaseOrderID(Integer purchaseOrderID) {
        this.purchaseOrderID = purchaseOrderID;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getSupplierInvoiceNo() {
        return supplierInvoiceNo;
    }

    public void setSupplierInvoiceNo(String supplierInvoiceNo) {
        this.supplierInvoiceNo = supplierInvoiceNo;
    }

    public String getDeliveryNoteNo() {
        return deliveryNoteNo;
    }

    public void setDeliveryNoteNo(String deliveryNoteNo) {
        this.deliveryNoteNo = deliveryNoteNo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getTotalVAT() {
        return totalVAT;
    }

    public void setTotalVAT(Double totalVAT) {
        this.totalVAT = totalVAT;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public Integer getReceivedProductStatus() {
        return receivedProductStatus;
    }

    public void setReceivedProductStatus(Integer receivedProductStatus) {
        this.receivedProductStatus = receivedProductStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

}
