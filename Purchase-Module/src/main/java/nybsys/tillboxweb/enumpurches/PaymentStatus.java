package nybsys.tillboxweb.enumpurches;

import java.util.Map;
import java.util.TreeMap;

public enum PaymentStatus {
        Unpaid(1),
        PartiallyPaid(2),
        Paid(3),
        Overdue(4);

        private int paymentStatus;

    PaymentStatus(int paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public int get() {
            return this.paymentStatus;
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