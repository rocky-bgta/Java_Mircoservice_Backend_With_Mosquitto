/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 19-Feb-18
 * Time: 4:21 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

import java.util.Date;

public class ProductAdjustmentModel extends BaseModel {


    private Integer productAdjustmentID;
    private Integer businessID;
    private Date date;
    private Boolean isApproved;
    private Boolean approvedBy;
    private String note;

    public Integer getProductAdjustmentID() {
        return productAdjustmentID;
    }

    public void setProductAdjustmentID(Integer productAdjustmentID) {
        this.productAdjustmentID = productAdjustmentID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public Boolean getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Boolean approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
