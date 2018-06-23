/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 18-May-18
 * Time: 11:54 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.models;

import nybsys.tillboxweb.BaseModel;

public class ReasonModel extends BaseModel {

    private Integer reasonID;
    private String reason;

    public Integer getReasonID() {
        return reasonID;
    }

    public void setReasonID(Integer reasonID) {
        this.reasonID = reasonID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
