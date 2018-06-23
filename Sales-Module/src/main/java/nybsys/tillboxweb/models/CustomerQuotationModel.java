/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 15/03/2018
 * Time: 01:04
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModelWithCurrency;

import java.util.Date;
import java.util.Objects;

public class CustomerQuotationModel extends BaseModelWithCurrency {
    private Integer customerQuotationID;
    private Integer businessID;
    private Date quotationDate;
    private Date expiryDate;
    private String docNumber;
    private String customerQuotationNo;
    private Integer customerID;
    private String customerRef;
    private String vatReference;
    private Double totalVAT;
    private Double totalDiscount;
    private Double totalAmount;
    private Integer quotationStatus;
    private Integer layoutID;
    private Integer customerRepresentativeID;
    private Double discountPercent;
    private String note;
    private Double totalExclusive;
    private Integer customerAddressType;

    public Integer getCustomerQuotationID() {
        return customerQuotationID;
    }

    public void setCustomerQuotationID(Integer customerQuotationID) {
        this.customerQuotationID = customerQuotationID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Date getQuotationDate() {
        return quotationDate;
    }

    public void setQuotationDate(Date quotationDate) {
        this.quotationDate = quotationDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getCustomerQuotationNo() {
        return customerQuotationNo;
    }

    public void setCustomerQuotationNo(String customerQuotationNo) {
        this.customerQuotationNo = customerQuotationNo;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public String getCustomerRef() {
        return customerRef;
    }

    public void setCustomerRef(String customerRef) {
        this.customerRef = customerRef;
    }

    public String getVatReference() {
        return vatReference;
    }

    public void setVatReference(String vatReference) {
        this.vatReference = vatReference;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(Integer quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public Integer getLayoutID() {
        return layoutID;
    }

    public void setLayoutID(Integer layoutID) {
        this.layoutID = layoutID;
    }

    public Integer getCustomerRepresentativeID() {
        return customerRepresentativeID;
    }

    public void setCustomerRepresentativeID(Integer customerRepresentativeID) {
        this.customerRepresentativeID = customerRepresentativeID;
    }

    public Double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getTotalExclusive() {
        return totalExclusive;
    }

    public void setTotalExclusive(Double totalExclusive) {
        this.totalExclusive = totalExclusive;
    }

    public Integer getCustomerAddressType() {
        return customerAddressType;
    }

    public void setCustomerAddressType(Integer customerAddressType) {
        this.customerAddressType = customerAddressType;
    }
}
