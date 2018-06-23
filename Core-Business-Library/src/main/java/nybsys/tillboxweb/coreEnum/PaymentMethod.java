/**
 * Created By: Md. Nazmus Salahin
 * Created Date: 02-Mar-18
 * Time: 10:45 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight Nybsys ltd.
 */

package nybsys.tillboxweb.coreEnum;

import java.util.Map;
import java.util.TreeMap;

public enum PaymentMethod {
    Cash(1),
    Cheque(2),
    CreditCard(3),
    EFT(4);

    private int paymentMethod;

    PaymentMethod(int paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int get() {
        return this.paymentMethod;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (PaymentMethod kv : PaymentMethod.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Integer> getMAP() {
        return MAP;
    }

}
