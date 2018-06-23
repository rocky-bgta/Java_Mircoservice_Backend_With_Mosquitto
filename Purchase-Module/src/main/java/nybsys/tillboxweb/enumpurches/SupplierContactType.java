/**
 * Created By: Md. Abdul Hannan
 * Created Date: 5/24/2018
 * Time: 11:08 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.enumpurches;

import java.util.Map;
import java.util.TreeMap;

public enum SupplierContactType {
    Default(1),
    Mailing(2),
    Billing(3),
    Other(4);

    private int supplierContactType;

    SupplierContactType(int paymentStatus) {
        this.supplierContactType = paymentStatus;
    }

    public int get() {
        return this.supplierContactType;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (PaymentStatus kv : PaymentStatus.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Integer> getMAP() {
        return MAP;
    }
}


