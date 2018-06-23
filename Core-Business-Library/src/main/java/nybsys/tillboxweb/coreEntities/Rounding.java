/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 19-Apr-18
 * Time: 12:31 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.coreEntities;

import nybsys.tillboxweb.BaseEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Rounding extends BaseEntity {

    @Id
    @GeneratedValue(generator = "IdGen")
    @GenericGenerator(name = "IdGen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters =
                    {
                            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "roundingID_seq"),
                            @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
                            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1"),
                    })

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