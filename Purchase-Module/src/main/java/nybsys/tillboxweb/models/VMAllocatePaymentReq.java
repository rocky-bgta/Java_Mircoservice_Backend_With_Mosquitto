/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 2018-06-05
 * Time: 10:41
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.models;

import java.util.Date;

public class VMAllocatePaymentReq {
    private Integer  selectedSupplierID;
    private Date fromDate;
    private Date toDate;
    private Boolean showOutStandingOnly;
    private String docNumber;
    private Integer invoiceType;

    public Integer getSelectedSupplierID() {
        return selectedSupplierID;
    }

    public void setSelectedSupplierID(Integer selectedSupplierID) {
        this.selectedSupplierID = selectedSupplierID;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Boolean getShowOutStandingOnly() {
        return showOutStandingOnly;
    }

    public void setShowOutStandingOnly(Boolean showOutStandingOnly) {
        this.showOutStandingOnly = showOutStandingOnly;
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }
}
