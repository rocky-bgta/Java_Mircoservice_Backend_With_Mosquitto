/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 13/02/2018
 * Time: 10:35
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

import java.util.Date;
import java.util.Objects;

public class ProductModel extends BaseModel {

    private Integer productID;
    private Integer businessID;
    private Integer docNumber;
    private String code;
    private String name;
    private Integer productCategoryID;
    private Integer ProductTypeID;
    private Integer uOMID;
    private String alternativeSupplierName;
    private Double minimumReorder;
    private Double maximumReorder;
    private Double economicOrderQty;
    private Double openingQuantity;
    private Double openingCost;
    private Date quantityOnHandAt;
    private Integer purchaseVATID;
    private Integer salesVATID;
    private Integer salesAccountID;
    private Integer purchaseAccountID;
    private Double salesVatRate;

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(Integer docNumber) {
        this.docNumber = docNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(Integer productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

    public Integer getProductTypeID() {
        return ProductTypeID;
    }

    public void setProductTypeID(Integer productTypeID) {
        ProductTypeID = productTypeID;
    }

    public Integer getuOMID() {
        return uOMID;
    }

    public void setuOMID(Integer uOMID) {
        this.uOMID = uOMID;
    }

    public String getAlternativeSupplierName() {
        return alternativeSupplierName;
    }

    public void setAlternativeSupplierName(String alternativeSupplierName) {
        this.alternativeSupplierName = alternativeSupplierName;
    }

    public Double getMinimumReorder() {
        return minimumReorder;
    }

    public void setMinimumReorder(Double minimumReorder) {
        this.minimumReorder = minimumReorder;
    }

    public Double getMaximumReorder() {
        return maximumReorder;
    }

    public void setMaximumReorder(Double maximumReorder) {
        this.maximumReorder = maximumReorder;
    }

    public Double getEconomicOrderQty() {
        return economicOrderQty;
    }

    public void setEconomicOrderQty(Double economicOrderQty) {
        this.economicOrderQty = economicOrderQty;
    }

    public Double getOpeningQuantity() {
        return openingQuantity;
    }

    public void setOpeningQuantity(Double openingQuantity) {
        this.openingQuantity = openingQuantity;
    }

    public Double getOpeningCost() {
        return openingCost;
    }

    public void setOpeningCost(Double openingCost) {
        this.openingCost = openingCost;
    }

    public Date getQuantityOnHandAt() {
        return quantityOnHandAt;
    }

    public void setQuantityOnHandAt(Date quantityOnHandAt) {
        this.quantityOnHandAt = quantityOnHandAt;
    }

    public Integer getPurchaseVATID() {
        return purchaseVATID;
    }

    public void setPurchaseVATID(Integer purchaseVATID) {
        this.purchaseVATID = purchaseVATID;
    }

    public Integer getSalesVATID() {
        return salesVATID;
    }

    public void setSalesVATID(Integer salesVATID) {
        this.salesVATID = salesVATID;
    }

    public Integer getSalesAccountID() {
        return salesAccountID;
    }

    public void setSalesAccountID(Integer salesAccountID) {
        this.salesAccountID = salesAccountID;
    }

    public Integer getPurchaseAccountID() {
        return purchaseAccountID;
    }

    public void setPurchaseAccountID(Integer purchaseAccountID) {
        this.purchaseAccountID = purchaseAccountID;
    }

    public Double getSalesVatRate() {
        return salesVatRate;
    }

    public void setSalesVatRate(Double salesVatRate) {
        this.salesVatRate = salesVatRate;
    }
}
