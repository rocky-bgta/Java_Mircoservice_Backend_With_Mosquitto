/**
 * Created By: Md. Rashed Khan Menon
 * Created Date: 17/05/2018
 * Time: 04:20
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */

package nybsys.tillboxweb.Enum;

import java.util.Map;
import java.util.TreeMap;

public enum InventoryProductType {
    Physical(1),
    Service(2);

    private int inventoryProductType;

    InventoryProductType(int inventoryProductType) {
        this.inventoryProductType = inventoryProductType;
    }

    public int get() {
        return this.inventoryProductType;
    }

    private static final Map<String, Integer> MAP = new TreeMap<>();

    static {
        for (InventoryProductType kv : InventoryProductType.values()) {
            MAP.put(kv.name(), kv.get());
        }
    }

    public static Map<String, Integer> getMAP() {
        return MAP;
    }


}
