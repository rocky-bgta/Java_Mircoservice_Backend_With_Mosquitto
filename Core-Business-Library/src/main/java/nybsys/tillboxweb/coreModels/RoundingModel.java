/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 19-Apr-18
 * Time: 12:37 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.coreModels;

import nybsys.tillboxweb.BaseModel;

public class RoundingModel extends BaseModel {

    private Integer roundingID;
    private Integer businessID;
    private Integer roundingType;
    private Integer roundingNearestTo;
    private Integer numberOfDigitAfterDecimalPoint;

    public Integer getRoundingID() {
        return roundingID;
    }

    public void setRoundingID(Integer roundingID) {
        this.roundingID = roundingID;
    }

    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public Integer getRoundingType() {
        return roundingType;
    }

    public void setRoundingType(Integer roundingType) {
        this.roundingType = roundingType;
    }

    public Integer getRoundingNearestTo() {
        return roundingNearestTo;
    }

    public void setRoundingNearestTo(Integer roundingNearestTo) {
        this.roundingNearestTo = roundingNearestTo;
    }

    public Integer getNumberOfDigitAfterDecimalPoint() {
        return numberOfDigitAfterDecimalPoint;
    }

    public void setNumberOfDigitAfterDecimalPoint(Integer numberOfDigitAfterDecimalPoint) {
        this.numberOfDigitAfterDecimalPoint = numberOfDigitAfterDecimalPoint;
    }
}
