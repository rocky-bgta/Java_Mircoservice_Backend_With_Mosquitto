/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 13/03/2018
 * Time: 02:19
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

import java.util.Date;
import java.util.Objects;

public class CustomerModel extends BaseModel {
    private Integer customerID;
    private Integer businessID;
    private Integer customerCategoryID;
    private Integer customerTypeID;
    private String customerName;
    private String address;
    private String phone;
    private String email;
    private Double openingBalanceAmount;
    private Date openingBalanceDate;
    private Integer preferredCurrencyID;
    private Boolean autoAllocateReceipt;
    private Boolean acceptElectronicInvoice;
    private Boolean viewInvoiceOnline;
    private String customerVATNNumber;
    private Integer salesRepresentativeID;
    private Integer status;
    private Boolean cashCustomer;
    private Integer dueDateValue;
    private Double creditLimit;
    private String vatReference;

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getCustomerCategoryID() {
        return customerCategoryID;
    }

    public void setCustomerCategoryID(Integer customerCategoryID) {
        this.customerCategoryID = customerCategoryID;
    }

    public Integer getCustomerTypeID() {
        return customerTypeID;
    }

    public void setCustomerTypeID(Integer customerTypeID) {
        this.customerTypeID = customerTypeID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getOpeningBalanceAmount() {
        return openingBalanceAmount;
    }

    public void setOpeningBalanceAmount(Double openingBalanceAmount) {
        this.openingBalanceAmount = openingBalanceAmount;
    }

    public Date getOpeningBalanceDate() {
        return openingBalanceDate;
    }

    public void setOpeningBalanceDate(Date openingBalanceDate) {
        this.openingBalanceDate = openingBalanceDate;
    }

    public Integer getPreferredCurrencyID() {
        return preferredCurrencyID;
    }

    public void setPreferredCurrencyID(Integer preferredCurrencyID) {
        this.preferredCurrencyID = preferredCurrencyID;
    }

    public Boolean getAutoAllocateReceipt() {
        return autoAllocateReceipt;
    }

    public void setAutoAllocateReceipt(Boolean autoAllocateReceipt) {
        this.autoAllocateReceipt = autoAllocateReceipt;
    }

    public Boolean getAcceptElectronicInvoice() {
        return acceptElectronicInvoice;
    }

    public void setAcceptElectronicInvoice(Boolean acceptElectronicInvoice) {
        this.acceptElectronicInvoice = acceptElectronicInvoice;
    }

    public Boolean getViewInvoiceOnline() {
        return viewInvoiceOnline;
    }

    public void setViewInvoiceOnline(Boolean viewInvoiceOnline) {
        this.viewInvoiceOnline = viewInvoiceOnline;
    }

    public String getCustomerVATNNumber() {
        return customerVATNNumber;
    }

    public void setCustomerVATNNumber(String customerVATNNumber) {
        this.customerVATNNumber = customerVATNNumber;
    }

    public Integer getSalesRepresentativeID() {
        return salesRepresentativeID;
    }

    public void setSalesRepresentativeID(Integer salesRepresentativeID) {
        this.salesRepresentativeID = salesRepresentativeID;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getCashCustomer() {
        return cashCustomer;
    }

    public void setCashCustomer(Boolean cashCustomer) {
        this.cashCustomer = cashCustomer;
    }

    public Integer getDueDateValue() {
        return dueDateValue;
    }

    public void setDueDateValue(Integer dueDateValue) {
        this.dueDateValue = dueDateValue;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getVatReference() {
        return vatReference;
    }

    public void setVatReference(String vatReference) {
        this.vatReference = vatReference;
    }
}
