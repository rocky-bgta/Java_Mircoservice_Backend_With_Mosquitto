package nybsys.tillboxweb.enumpurches;

import java.util.Map;
import java.util.TreeMap;

public enum VatType {

    NoVat(1.0),
    StandardRated(15.00d),
    ZeroRate(0.00),
    Exempt(0.00),
    ManualVat(2.0),
    ManualVatCapitalGoods(3.0);


    private Double vatType;

    VatType(Double paymentStatus) {
        this.vatType = vatType;
    }

    public Double get() {
        return this.vatType;
    }

    private static final Map<String, Double> MAP = new TreeMap<>();

    static {
        for (VatType kv : VatType.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Double> getMAP() {
        return MAP;
    }
}