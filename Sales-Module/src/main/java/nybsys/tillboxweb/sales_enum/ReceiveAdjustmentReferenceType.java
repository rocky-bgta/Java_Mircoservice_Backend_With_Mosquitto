/**
 * Created By: Md. Abdul Hannan
 * Created Date: 6/20/2018
 * Time: 3:20 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.sales_enum;

public enum  ReceiveAdjustmentReferenceType {

    Invoice(1),
    AdjustmentIncrease(2);

    private int receiveAdjustmentReferenceType;

    ReceiveAdjustmentReferenceType(int receiveAdjustmentReferenceType) {
        this.receiveAdjustmentReferenceType = receiveAdjustmentReferenceType;
    }
    public int get() {
        return this.receiveAdjustmentReferenceType;
    }
}
