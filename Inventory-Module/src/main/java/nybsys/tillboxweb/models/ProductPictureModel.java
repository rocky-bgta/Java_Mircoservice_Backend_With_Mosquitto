/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/14/2018
 * Time: 9:32 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;

public class ProductPictureModel extends BaseModel {
    private Integer productPictureID;
    private Integer businessID;
    private Integer productID;
    private Byte[] picture;

    public Integer getProductPictureID() {
        return productPictureID;
    }

    public void setProductPictureID(Integer productPictureID) {
        this.productPictureID = productPictureID;
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

    public Byte[] getPicture() {
        return picture;
    }

    public void setPicture(Byte[] picture) {
        this.picture = picture;
    }
}
