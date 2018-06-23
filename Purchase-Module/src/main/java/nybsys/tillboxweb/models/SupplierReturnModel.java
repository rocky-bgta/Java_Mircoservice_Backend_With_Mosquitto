/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/23/2018
 * Time: 12:28 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;
import nybsys.tillboxweb.BaseModelWithCurrency;

import java.util.Date;
import java.util.Objects;

public class SupplierReturnModel extends BaseModelWithCurrency {

    private Integer supplierReturnID;
    private Integer businessID;
    private Integer supplierInvoiceID;
    private String supplierReturnNumber;
    private String docNumber;
    private Integer supplierID;
    private Double totalAmount;
    private Double totalVAT;
    private Double totalDiscount;
    private Date returnDate;
    private Double totalExclusive;
    private Integer supplierAddressType;
    private Double supplierDiscountPercentage;
    private String note;

    public Integer getSupplierReturnID() {
        return supplierReturnID;
    }

    public void setSupplierReturnID(Integer supplierReturnID) {
        this.supplierReturnID = supplierReturnID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getSupplierInvoiceID() {
        return supplierInvoiceID;
    }

    public void setSupplierInvoiceID(Integer supplierInvoiceID) {
        this.supplierInvoiceID = supplierInvoiceID;
    }

    public String getSupplierReturnNumber() {
        return supplierReturnNumber;
    }

    public void setSupplierReturnNumber(String supplierReturnNumber) {
        this.supplierReturnNumber = supplierReturnNumber;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public Integer getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(Integer supplierID) {
        this.supplierID = supplierID;
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

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public Double getTotalExclusive() {
        return totalExclusive;
    }

    public void setTotalExclusive(Double totalExclusive) {
        this.totalExclusive = totalExclusive;
    }

    public Integer getSupplierAddressType() {
        return supplierAddressType;
    }

    public void setSupplierAddressType(Integer supplierAddressType) {
        this.supplierAddressType = supplierAddressType;
    }

    public Double getSupplierDiscountPercentage() {
        return supplierDiscountPercentage;
    }

    public void setSupplierDiscountPercentage(Double supplierDiscountPercentage) {
        this.supplierDiscountPercentage = supplierDiscountPercentage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
