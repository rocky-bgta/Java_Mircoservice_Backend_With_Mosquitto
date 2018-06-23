/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 15/05/2018
 * Time: 02:16
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.Enum;

import java.util.Map;
import java.util.TreeMap;

public enum ProductAdjustmentType {
    Selling_Prices_based_on_Selling_Price(1),
    Selling_Prices_based_on_Last_Cost(2),
    Selling_Prices_based_on_Average_Cost(3);

    private int productAdjustmentType;

    ProductAdjustmentType(int productAdjustmentType) {
        this.productAdjustmentType = productAdjustmentType;
    }

    public int get() {
        return this.productAdjustmentType;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (ProductAdjustmentType kv : ProductAdjustmentType.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }
    public static Map<String, Integer> getMAP() {
        return MAP;
    }

}
