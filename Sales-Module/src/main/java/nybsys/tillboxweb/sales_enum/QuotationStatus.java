/**
 * Created By: Md. Abdul Hannan
 * Created Date: 6/20/2018
 * Time: 11:28 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.sales_enum;

import java.util.Map;
import java.util.TreeMap;

public enum QuotationStatus {

    Created(1),
    Invoiced(2),
    Closed(3),
    Cancel(4);

    private int quotationStatus;

    QuotationStatus(int quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public int get() {
        return this.quotationStatus;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (QuotationStatus kv : QuotationStatus.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }
    public static Map<String, Integer> getMAP() {
        return MAP;
    }
}
