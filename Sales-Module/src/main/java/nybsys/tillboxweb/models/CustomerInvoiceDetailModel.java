/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 15/03/2018
 * Time: 02:15
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

import java.util.Objects;

public class CustomerInvoiceDetailModel extends BaseModel {
    private Integer customerInvoiceDetailID;
    private Integer customerInvoiceID;
    private Integer discountSettingID;
    private Integer productID;
    private Double quantity;
    private Integer uomid;
    private Double unitPrice;
    private Double vatPercentage;
    private Double vat;
    private Double discount;
    private Integer vatTypeID;
    private String description;
    private Double discountPercentage;


    public Integer getCustomerInvoiceDetailID() {
        return customerInvoiceDetailID;
    }

    public void setCustomerInvoiceDetailID(Integer customerInvoiceDetailID) {
        this.customerInvoiceDetailID = customerInvoiceDetailID;
    }

    public Integer getCustomerInvoiceID() {
        return customerInvoiceID;
    }

    public void setCustomerInvoiceID(Integer customerInvoiceID) {
        this.customerInvoiceID = customerInvoiceID;
    }

    public Integer getDiscountSettingID() {
        return discountSettingID;
    }

    public void setDiscountSettingID(Integer discountSettingID) {
        this.discountSettingID = discountSettingID;
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

    public Integer getUomid() {
        return uomid;
    }

    public void setUomid(Integer uomid) {
        this.uomid = uomid;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getVatPercentage() {
        return vatPercentage;
    }

    public void setVatPercentage(Double vatPercentage) {
        this.vatPercentage = vatPercentage;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Integer getVatTypeID() {
        return vatTypeID;
    }

    public void setVatTypeID(Integer vatTypeID) {
        this.vatTypeID = vatTypeID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
