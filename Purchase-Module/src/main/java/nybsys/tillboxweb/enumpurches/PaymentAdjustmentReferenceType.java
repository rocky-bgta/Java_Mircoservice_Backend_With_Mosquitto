/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 2018-06-07
 * Time: 10:50
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.enumpurches;

public enum PaymentAdjustmentReferenceType {
    Invoice(1),
    AdjustmentIncrease(2);

    private int paymentAdjustmentReferenceType;

    PaymentAdjustmentReferenceType(int paymentAdjustmentReferenceType) {
        this.paymentAdjustmentReferenceType = paymentAdjustmentReferenceType;
    }
    public int get() {
        return this.paymentAdjustmentReferenceType;
    }
}
