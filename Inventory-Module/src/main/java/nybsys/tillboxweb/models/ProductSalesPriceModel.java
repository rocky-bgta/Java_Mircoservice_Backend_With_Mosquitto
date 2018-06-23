/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/14/2018
 * Time: 10:06 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.models;
import nybsys.tillboxweb.BaseModel;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;
import java.util.Objects;

public class ProductSalesPriceModel extends BaseModel {

    private Integer productSalesPriceID;
    private Integer businessID;
    private Integer productID;
    private Date    date;
    private Integer priceCategoryID;
    private Double  salesPrice;
    private Double exclusiveSalesPrice;

    public Integer getPriceCategoryID() {
        return priceCategoryID;
    }

    public void setPriceCategoryID(Integer priceCategoryID) {
        this.priceCategoryID = priceCategoryID;
    }

    public Double getExclusiveSalesPrice() {
        return exclusiveSalesPrice;
    }

    public void setExclusiveSalesPrice(Double exclusiveSalesPrice) {
        this.exclusiveSalesPrice = exclusiveSalesPrice;
    }

    public Integer getProductSalesPriceID() {
        return productSalesPriceID;
    }

    public void setProductSalesPriceID(Integer productSalesPriceID) {
        this.productSalesPriceID = productSalesPriceID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getProductID() {
        return productID;
    }

    public void setProductID(Integer productID) {
        this.productID = productID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getPriceListID() {
        return priceCategoryID;
    }

    public void setPriceListID(Integer priceListID) {
        this.priceCategoryID = priceListID;
    }

    public Double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(Double salesPrice) {
        this.salesPrice = salesPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductSalesPriceModel)) return false;
        if (!super.equals(o)) return false;
        ProductSalesPriceModel that = (ProductSalesPriceModel) o;
        return Objects.equals(getProductSalesPriceID(), that.getProductSalesPriceID()) &&
                Objects.equals(getBusinessID(), that.getBusinessID()) &&
                Objects.equals(getProductID(), that.getProductID()) &&
                Objects.equals(getDate(), that.getDate()) &&
                Objects.equals(getPriceCategoryID(), that.getPriceCategoryID()) &&
                Objects.equals(getSalesPrice(), that.getSalesPrice()) &&
                Objects.equals(getExclusiveSalesPrice(), that.getExclusiveSalesPrice());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getProductSalesPriceID(), getBusinessID(), getProductID(), getDate(), getPriceCategoryID(), getSalesPrice(), getExclusiveSalesPrice());
    }

    @Override
    public String toString() {
        return "ProductSalesPriceModel{" +
                "productSalesPriceID=" + productSalesPriceID +
                ", businessID=" + businessID +
                ", productID=" + productID +
                ", date=" + date +
                ", priceCategoryID=" + priceCategoryID +
                ", salesPrice=" + salesPrice +
                ", exclusiveSalesPrice=" + exclusiveSalesPrice +
                '}';
    }
}
