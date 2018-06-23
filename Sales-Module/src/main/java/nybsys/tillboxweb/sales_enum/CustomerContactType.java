/**
 * Created By: Md. Abdul Hannan
 * Created Date: 6/13/2018
 * Time: 3:10 PM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.sales_enum;

import java.util.Map;
import java.util.TreeMap;

public enum CustomerContactType {
    Default(1),
    Mailing(2),
    Billing(3),
    Other(4);

    private int customerContactType;

    CustomerContactType(int paymentStatus) {
        this.customerContactType = paymentStatus;
    }

    public int get() {
        return this.customerContactType;
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
